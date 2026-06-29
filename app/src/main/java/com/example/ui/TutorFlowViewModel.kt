package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TutorFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TutorFlowRepository
    private val db: TutorFlowDatabase

    init {
        db = TutorFlowDatabase.getDatabase(application)
        repository = TutorFlowRepository(db.dao())
    }

    // --- State Flows ---
    val currentUser = repository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentStudentProfile = currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getStudentProfileFlow(user.userId)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Admin states
    val pendingStudents = repository.pendingStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val approvedStudents = repository.approvedStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allStudents = repository.allStudents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Videos
    val allVideos = repository.allVideos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val liveVideosCount = repository.liveVideosCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Filtered videos for the current student's class
    val studentVideos = combine(currentStudentProfile, repository.allVideos) { profile, videos ->
        if (profile != null) {
            videos.filter { it.targetClass == profile.currentClass || it.targetClass == "All" }
        } else {
            videos
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat
    val chatMessages = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    // Notifications
    val notifications = currentUser.flatMapLatest { user ->
        if (user != null) {
            if (user.role == "admin") {
                repository.getNotifications("admin")
            } else {
                repository.getNotifications(user.userId)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Suggested subjects from Gemini (Section 8.1)
    private val _suggestedSubjects = MutableStateFlow<List<String>>(emptyList())
    val suggestedSubjects: StateFlow<List<String>> = _suggestedSubjects

    private val _isSuggestingSubjects = MutableStateFlow(false)
    val isSuggestingSubjects: StateFlow<Boolean> = _isSuggestingSubjects

    // Generated description from Gemini (Section 8.2)
    private val _generatedDescription = MutableStateFlow("")
    val generatedDescription: StateFlow<String> = _generatedDescription

    private val _isGeneratingDescription = MutableStateFlow(false)
    val isGeneratingDescription: StateFlow<Boolean> = _isGeneratingDescription

    // --- Authentication Actions (Section 1) ---
    fun login(email: String, role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (email.isEmpty()) {
                    onError("Email cannot be empty")
                    return@launch
                }
                repository.login(email, role)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Login failed")
            }
        }
    }

    fun loginWithGoogle(email: String, name: String, role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.loginAsGoogle(email, name, role)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // --- Student Registration Actions (Section 2) ---
    fun submitRegistration(
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
        profilePhotoURL: String,
        onComplete: (StudentProfileEntity) -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val student = repository.registerStudent(
                userId = user.userId,
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
                profilePhotoURL = profilePhotoURL
            )
            onComplete(student)
        }
    }

    // Smart subject trigger (Section 8.1)
    fun fetchSubjectSuggestions(currentClass: String, board: String) {
        viewModelScope.launch {
            _isSuggestingSubjects.value = true
            try {
                val suggestions = GeminiService.suggestSubjects(currentClass, board)
                _suggestedSubjects.value = suggestions
            } catch (e: Exception) {
                Log.e("TutorFlowViewModel", "Failed to suggest subjects", e)
            } finally {
                _isSuggestingSubjects.value = false
            }
        }
    }

    // --- Admin Actions (Section 3) ---
    fun approveStudent(studentId: String) {
        viewModelScope.launch {
            repository.approveStudent(studentId)
        }
    }

    fun rejectStudent(studentId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectStudent(studentId, reason)
        }
    }

    // AI Video Description Generator (Section 8.2)
    fun generateDescriptionForVideo(title: String) {
        viewModelScope.launch {
            _isGeneratingDescription.value = true
            try {
                val desc = GeminiService.generateVideoDescription(title)
                _generatedDescription.value = desc
            } catch (e: Exception) {
                Log.e("TutorFlowViewModel", "Failed to generate video description", e)
            } finally {
                _isGeneratingDescription.value = false
            }
        }
    }

    // Create Video (Section 3D / 3F)
    fun createAndScheduleVideo(
        title: String,
        subject: String,
        targetClass: String,
        description: String,
        thumbnailURL: String,
        videoURL: String,
        scheduledAt: Long,
        streamPassword: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.uploadVideo(
                title = title,
                subject = subject,
                targetClass = targetClass,
                description = description,
                thumbnailURL = thumbnailURL,
                videoURL = videoURL,
                scheduledAt = scheduledAt,
                streamPassword = streamPassword
            )
            onComplete()
        }
    }

    fun sendStreamNotification(video: VideoEntity) {
        viewModelScope.launch {
            repository.sendLiveClassNotification(video)
        }
    }

    fun setVideoLive(videoId: String) {
        viewModelScope.launch {
            repository.setVideoLive(videoId)
        }
    }

    fun setVideoCompleted(videoId: String) {
        viewModelScope.launch {
            repository.setVideoCompleted(videoId)
        }
    }

    // --- Student Preferences Setup (Section 4B) ---
    fun saveStudentPreferences(subjects: List<String>, preferredTimes: String, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.savePreferences(user.userId, subjects, preferredTimes)
            onComplete()
        }
    }

    // --- Doubt Assistant Actions (Section 8.3) ---
    fun askDoubt(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            // Save student message in DB
            repository.sendChatMessage("student", message)

            _isChatLoading.value = true

            // Gather full chat history for model context
            val currentChat = chatMessages.value
            val history = currentChat.map {
                GeminiContent(
                    role = if (it.sender == "student") "user" else "model",
                    parts = listOf(GeminiPart(text = it.message))
                )
            } + GeminiContent(role = "user", parts = listOf(GeminiPart(text = message)))

            try {
                val aiResponse = GeminiService.askDoubt(history)
                // Save tutor response in DB
                repository.sendChatMessage("tutor", aiResponse)
            } catch (e: Exception) {
                repository.sendChatMessage("tutor", "I am having difficulty connecting. Please check details or retry.")
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // Mark notifications read
    fun markAllNotificationsAsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val recipientId = if (user.role == "admin") "admin" else user.userId
            repository.markNotificationsRead(recipientId)
        }
    }
}
