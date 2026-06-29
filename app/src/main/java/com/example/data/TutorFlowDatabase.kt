package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val displayName: String,
    val photoURL: String,
    val role: String, // "student" or "admin"
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "students")
data class StudentProfileEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val dob: String,
    val gender: String,
    val phone: String,
    val whatsapp: String,
    val address: String,
    val city: String,
    val pinCode: String,
    val school: String,
    val currentClass: String, // Grade
    val board: String,
    val academicYear: String,
    val previousMarks: String = "",
    val guardianName: String,
    val guardianPhone: String,
    val guardianRelation: String,
    val profilePhotoURL: String = "",
    val tokenNumber: String,
    val status: String, // "pending" | "approved" | "rejected"
    val rejectionReason: String = "",
    val approvedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val subjectsCsv: String = "", // Comma-separated list
    val preferredTimesJson: String = "", // Comma-separated or custom format
    val fcmToken: String = ""
)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val subject: String,
    val targetClass: String,
    val description: String,
    val thumbnailURL: String,
    val videoURL: String,
    val scheduledAt: Long,
    val streamPassword: String, // 6-char alphanumeric
    val status: String, // "scheduled" | "live" | "completed"
    val uploadedAt: Long = System.currentTimeMillis(),
    val uploadedBy: String = "admin"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val recipientId: String,
    val type: String,
    val title: String,
    val body: String,
    val sentAt: Long = System.currentTimeMillis(),
    val readAt: Long = 0L,
    val isRead: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "student" or "tutor" (or "system")
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TutorFlowDao {
    // Users
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE users SET isLoggedIn = 1 WHERE userId = :userId")
    suspend fun loginUser(userId: String)

    // Students
    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    fun getStudentProfileFlow(userId: String): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    suspend fun getStudentProfile(userId: String): StudentProfileEntity?

    @Query("SELECT * FROM students WHERE tokenNumber = :token LIMIT 1")
    suspend fun getStudentByToken(token: String): StudentProfileEntity?

    @Query("SELECT * FROM students ORDER BY createdAt DESC")
    fun getAllStudentsFlow(): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM students WHERE status = :status ORDER BY createdAt DESC")
    fun getStudentsByStatusFlow(status: String): Flow<List<StudentProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfileEntity)

    @Query("UPDATE students SET status = :status, rejectionReason = :reason, approvedAt = :approvedAt WHERE userId = :userId")
    suspend fun updateStudentStatus(userId: String, status: String, reason: String, approvedAt: Long)

    @Query("UPDATE students SET subjectsCsv = :subjectsCsv, preferredTimesJson = :timesJson WHERE userId = :userId")
    suspend fun updateStudentPreferences(userId: String, subjectsCsv: String, timesJson: String)

    // Videos
    @Query("SELECT * FROM videos ORDER BY scheduledAt DESC")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE targetClass = :targetClass OR targetClass = 'All' ORDER BY scheduledAt DESC")
    fun getVideosForClassFlow(targetClass: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE videoId = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("UPDATE videos SET status = :status WHERE videoId = :videoId")
    suspend fun updateVideoStatus(videoId: String, status: String)

    @Query("SELECT COUNT(*) FROM videos WHERE status = 'live'")
    fun getLiveVideosCountFlow(): Flow<Int>

    // Notifications
    @Query("SELECT * FROM notifications WHERE recipientId = :recipientId OR recipientId = 'all' ORDER BY sentAt DESC")
    fun getNotificationsFlow(recipientId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE recipientId = :recipientId")
    suspend fun markAllNotificationsAsRead(recipientId: String, readAt: Long)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Database(
    entities = [
        UserEntity::class,
        StudentProfileEntity::class,
        VideoEntity::class,
        NotificationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TutorFlowDatabase : RoomDatabase() {
    abstract fun dao(): TutorFlowDao

    companion object {
        @Volatile
        private var INSTANCE: TutorFlowDatabase? = null

        fun getDatabase(context: Context): TutorFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TutorFlowDatabase::class.java,
                    "tutorflow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
