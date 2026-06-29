package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class TutorFlowRepository(private val dao: TutorFlowDao) {

    companion object {
        private const val TAG = "TutorFlowRepository"
    }

    // Auth & Session
    val currentUser: Flow<UserEntity?> = dao.getCurrentUser()

    suspend fun login(email: String, role: String): UserEntity {
        dao.logoutAllUsers()
        val cleanEmail = email.trim().lowercase()
        var existingUser = dao.getUserByEmail(cleanEmail)
        if (existingUser == null) {
            val userId = "user_" + UUID.randomUUID().toString().take(8)
            val displayName = email.substringBefore("@")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            existingUser = UserEntity(
                userId = userId,
                email = cleanEmail,
                displayName = displayName,
                photoURL = "",
                role = role,
                isLoggedIn = true
            )
            dao.insertUser(existingUser)

            // Seed initial data if first time
            if (role == "admin") {
                seedInitialVideos()
            }
        } else {
            // Update role if changed
            existingUser = existingUser.copy(role = role, isLoggedIn = true)
            dao.insertUser(existingUser)
        }
        return existingUser
    }

    suspend fun loginAsGoogle(email: String, displayName: String, role: String): UserEntity {
        dao.logoutAllUsers()
        val cleanEmail = email.trim().lowercase()
        var existingUser = dao.getUserByEmail(cleanEmail)
        if (existingUser == null) {
            val userId = "user_g_" + UUID.randomUUID().toString().take(8)
            existingUser = UserEntity(
                userId = userId,
                email = cleanEmail,
                displayName = displayName,
                photoURL = "https://lh3.googleusercontent.com/a/default-user=s96-c",
                role = role,
                isLoggedIn = true
            )
            dao.insertUser(existingUser)
        } else {
            existingUser = existingUser.copy(role = role, isLoggedIn = true)
            dao.insertUser(existingUser)
        }
        return existingUser
    }

    suspend fun logout() {
        dao.logoutAllUsers()
    }

    // Students
    fun getStudentProfileFlow(userId: String): Flow<StudentProfileEntity?> = dao.getStudentProfileFlow(userId)

    suspend fun getStudentProfile(userId: String): StudentProfileEntity? = dao.getStudentProfile(userId)

    val allStudents: Flow<List<StudentProfileEntity>> = dao.getAllStudentsFlow()
    val pendingStudents: Flow<List<StudentProfileEntity>> = dao.getStudentsByStatusFlow("pending")
    val approvedStudents: Flow<List<StudentProfileEntity>> = dao.getStudentsByStatusFlow("approved")

    /**
     * Section 2 - Student Registration Form submit
     */
    suspend fun registerStudent(
        userId: String,
        fullName: String,
        dob: String,
        gender: String,
        phone: String,
        whatsapp: String,
        address: String,
        city: String,
        pinCode: String,
        school: String,
        currentClass: String,
        board: String,
        academicYear: String,
        previousMarks: String,
        guardianName: String,
        guardianPhone: String,
        guardianRelation: String,
        profilePhotoURL: String = ""
    ): StudentProfileEntity {
        // Auto-generate Token number: TF-{YEAR}-{4-digit-random}
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val randomDigit = Random.nextInt(1000, 9999)
        val token = "TF-$currentYear-$randomDigit"

        val student = StudentProfileEntity(
            userId = userId,
            fullName = fullName,
            dob = dob,
            gender = gender,
            phone = phone,
            whatsapp = whatsapp,
            address = address,
            city = city,
            pinCode = pinCode,
            school = school,
            currentClass = currentClass,
            board = board,
            academicYear = academicYear,
            previousMarks = previousMarks,
            guardianName = guardianName,
            guardianPhone = guardianPhone,
            guardianRelation = guardianRelation,
            profilePhotoURL = profilePhotoURL,
            tokenNumber = token,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )

        dao.insertStudent(student)

        // Trigger Notification to Admin device (simulated FCM)
        val adminNotification = NotificationEntity(
            notificationId = "notif_" + UUID.randomUUID().toString().take(6),
            recipientId = "admin",
            type = "registration_pending",
            title = "New Student Registered! 📝",
            body = "$fullName has submitted their registration. Token: $token. Status is pending approval.",
            sentAt = System.currentTimeMillis()
        )
        dao.insertNotification(adminNotification)

        // Trigger email simulation
        Log.d(TAG, "Sent Confirmation Email to $fullName: Registration received with token $token")

        return student
    }

    /**
     * Section 3B - Approve student request
     */
    suspend fun approveStudent(userId: String) {
        val student = dao.getStudentProfile(userId) ?: return
        dao.updateStudentStatus(userId, "approved", "", System.currentTimeMillis())

        // Send simulated FCM to student device
        val notification = NotificationEntity(
            notificationId = "notif_" + UUID.randomUUID().toString().take(6),
            recipientId = userId,
            type = "approval",
            title = "Registration Approved! 🎉",
            body = "Your TutorFlow account is approved. Login now using your token: ${student.tokenNumber}",
            sentAt = System.currentTimeMillis()
        )
        dao.insertNotification(notification)

        Log.d(TAG, "Simulated Email Sent: Approved student ${student.fullName} with token ${student.tokenNumber}")
    }

    /**
     * Section 3B - Reject student request
     */
    suspend fun rejectStudent(userId: String, reason: String) {
        val student = dao.getStudentProfile(userId) ?: return
        dao.updateStudentStatus(userId, "rejected", reason, System.currentTimeMillis())

        // Send simulated FCM to student device
        val notification = NotificationEntity(
            notificationId = "notif_" + UUID.randomUUID().toString().take(6),
            recipientId = userId,
            type = "rejection",
            title = "Registration Status Update ⚠️",
            body = "Your TutorFlow registration was not approved. Reason: $reason",
            sentAt = System.currentTimeMillis()
        )
        dao.insertNotification(notification)

        Log.d(TAG, "Simulated Email Sent: Rejected student ${student.fullName}. Reason: $reason")
    }

    /**
     * Section 4A - Token Verification
     */
    suspend fun verifyToken(userId: String, token: String): Boolean {
        val student = dao.getStudentProfile(userId) ?: return false
        if (student.tokenNumber.trim().uppercase() == token.trim().uppercase()) {
            return true
        }
        return false
    }

    /**
     * Section 4B - Subject & Schedule Setup
     */
    suspend fun savePreferences(userId: String, subjects: List<String>, preferredTimes: String) {
        val subjectsCsv = subjects.joinToString(",")
        dao.updateStudentPreferences(userId, subjectsCsv, preferredTimes)
    }

    // Videos
    val allVideos: Flow<List<VideoEntity>> = dao.getAllVideosFlow()
    val liveVideosCount: Flow<Int> = dao.getLiveVideosCountFlow()

    fun getVideosForClass(targetClass: String): Flow<List<VideoEntity>> = dao.getVideosForClassFlow(targetClass)

    /**
     * Section 3D & 3F - Video Management / Bulk Scheduling
     */
    suspend fun uploadVideo(
        title: String,
        subject: String,
        targetClass: String,
        description: String,
        thumbnailURL: String,
        videoURL: String,
        scheduledAt: Long,
        streamPassword: String = ""
    ): VideoEntity {
        // Auto-generate password if empty
        val finalPassword = if (streamPassword.isNotEmpty()) {
            streamPassword
        } else {
            // Generate 6-char alphanumeric password
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            (1..6).map { chars.random() }.joinToString("")
        }

        // Determine initial status based on scheduled time
        val isNow = scheduledAt <= System.currentTimeMillis()
        val status = if (isNow) "live" else "scheduled"

        val video = VideoEntity(
            videoId = "vid_" + UUID.randomUUID().toString().take(8),
            title = title,
            subject = subject,
            targetClass = targetClass,
            description = description,
            thumbnailURL = thumbnailURL,
            videoURL = videoURL,
            scheduledAt = scheduledAt,
            streamPassword = finalPassword,
            status = status
        )

        dao.insertVideo(video)

        // Send FCM if already live
        if (status == "live") {
            sendLiveClassNotification(video)
        }

        return video
    }

    /**
     * Section 3E - Send Notification to selected class / all students
     */
    suspend fun sendLiveClassNotification(video: VideoEntity) {
        // Simulated FCM push notifications to target students
        val notif = NotificationEntity(
            notificationId = "notif_" + UUID.randomUUID().toString().take(6),
            recipientId = "all", // Or target class students
            type = "live_stream",
            title = "Class Starting Soon! 📺",
            body = "Your ${video.subject} class is live. Password: ${video.streamPassword}",
            sentAt = System.currentTimeMillis()
        )
        dao.insertNotification(notif)
        Log.d(TAG, "Sent Live Stream notification for ${video.subject} - Pass: ${video.streamPassword}")
    }

    suspend fun setVideoLive(videoId: String) {
        val video = dao.getVideoById(videoId) ?: return
        val updatedVideo = video.copy(status = "live")
        dao.insertVideo(updatedVideo)
        sendLiveClassNotification(updatedVideo)
    }

    suspend fun setVideoCompleted(videoId: String) {
        dao.updateVideoStatus(videoId, "completed")
    }

    // Notifications
    fun getNotifications(recipientId: String): Flow<List<NotificationEntity>> = dao.getNotificationsFlow(recipientId)

    suspend fun markNotificationsRead(recipientId: String) {
        dao.markAllNotificationsAsRead(recipientId, System.currentTimeMillis())
    }

    // Chat / Doubt Assistant (Section 8.3)
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getChatMessagesFlow()

    suspend fun sendChatMessage(sender: String, message: String) {
        val msg = ChatMessageEntity(sender = sender, message = message)
        dao.insertChatMessage(msg)
    }

    suspend fun clearChat() {
        dao.clearChat()
    }

    private suspend fun seedInitialVideos() {
        // Seed some demo videos
        if (dao.getAllVideosFlow().firstOrNull().isNullOrEmpty()) {
            uploadVideo(
                title = "Introduction to Quadratic Equations",
                subject = "Mathematics",
                targetClass = "Class 10",
                description = "Learn how to solve quadratic equations using factorization and quadratic formula with simple explanations.",
                thumbnailURL = "math_thumb",
                videoURL = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                scheduledAt = System.currentTimeMillis() - 3600000, // 1 hour ago
                streamPassword = "MATH10"
            )
            uploadVideo(
                title = "Reflection of Light & Laws of Reflection",
                subject = "Physics",
                targetClass = "Class 12",
                description = "Master the concept of light reflection, spherical mirrors, and standard lens formulas in this structured session.",
                thumbnailURL = "physics_thumb",
                videoURL = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                scheduledAt = System.currentTimeMillis() + 18000000, // 5 hours later (scheduled)
                streamPassword = "PHYS12"
            )
        }
    }
}
