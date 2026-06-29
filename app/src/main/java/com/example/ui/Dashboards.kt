package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatMessageEntity
import com.example.data.NotificationEntity
import com.example.data.StudentProfileEntity
import com.example.data.VideoEntity
import java.text.SimpleDateFormat
import java.util.*

// --- Section 4C: STUDENT DASHBOARD MAIN SCREEN ---
@Composable
fun StudentDashboardScreen(
    viewModel: TutorFlowViewModel,
    profile: StudentProfileEntity,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("home") } // "home", "schedule", "doubt", "notifications"
    val videos by viewModel.studentVideos.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val context = LocalContext.current

    // Video Player State
    var activePlayingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var showPasswordGateVideo by remember { mutableStateOf<VideoEntity?>(null) }

    Scaffold(
        containerColor = NavyBg,
        bottomBar = {
            NavigationBar(
                containerColor = CardColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "schedule",
                    onClick = { activeTab = "schedule" },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Schedule") },
                    label = { Text("Schedule", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "doubt",
                    onClick = { activeTab = "doubt" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Doubt Assistant") },
                    label = { Text("AI Doubt", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "notifications",
                    onClick = { activeTab = "notifications" },
                    icon = {
                        BadgedBox(badge = {
                            if (notifications.any { !it.isRead }) {
                                Badge(containerColor = LiveRed)
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    },
                    label = { Text("Inbox", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldGreen,
                        selectedTextColor = EmeraldGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldGreen.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "home" -> {
                    StudentHomeTab(
                        profile = profile,
                        videos = videos,
                        onPlayClick = { video ->
                            if (video.status == "live") {
                                showPasswordGateVideo = video
                            } else {
                                activePlayingVideo = video
                            }
                        },
                        onLogout = onLogout
                    )
                }
                "schedule" -> {
                    StudentScheduleTab(profile = profile, videos = videos)
                }
                "doubt" -> {
                    DoubtAssistantTab(viewModel = viewModel)
                }
                "notifications" -> {
                    StudentNotificationsTab(viewModel = viewModel, notifications = notifications)
                }
            }

            // Password Gate Dialog (Section 4D)
            showPasswordGateVideo?.let { video ->
                PasswordGateDialog(
                    video = video,
                    onDismiss = { showPasswordGateVideo = null },
                    onCorrectPassword = {
                        showPasswordGateVideo = null
                        activePlayingVideo = video
                    }
                )
            }

            // Video Player Screen (Section 4F)
            activePlayingVideo?.let { video ->
                VideoPlayerDialog(
                    video = video,
                    onDismiss = { activePlayingVideo = null },
                    onCompleted = {
                        viewModel.setVideoCompleted(video.videoId)
                        Toast.makeText(context, "Completed! Marked as Watched.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// --- Home Tab ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentHomeTab(
    profile: StudentProfileEntity,
    videos: List<VideoEntity>,
    onPlayClick: (VideoEntity) -> Unit,
    onLogout: () -> Unit
) {
    val liveVideos = videos.filter { it.status == "live" }
    val upcomingVideos = videos.filter { it.status == "scheduled" }
    val pastVideos = videos.filter { it.status == "completed" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TutorFlow",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Exit",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .clickable { onLogout() }
                            .padding(8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.fullName.take(1).uppercase(),
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Greeting Card (Section 4C)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Day, ${profile.fullName} 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile.currentClass} | ${profile.board} board",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Token: ${profile.tokenNumber}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }

        // 🔴 LIVE BANNER (Pulsing badge)
        if (liveVideos.isNotEmpty()) {
            item {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LiveRed.copy(alpha = 0.15f))
                        .border(1.dp, LiveRed.copy(alpha = pulseAlpha), RoundedCornerShape(12.dp))
                        .clickable { onPlayClick(liveVideos.first()) }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(LiveRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIVE CLASS: ${liveVideos.first().subject}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "ENTER PASSWORD GATE 🔐",
                            color = LiveRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Upcoming Classes Horizontal Row
        item {
            Text(
                text = "Upcoming Classes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (upcomingVideos.isEmpty()) {
            item {
                Text(
                    text = "No scheduled upcoming classes.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(upcomingVideos) { video ->
                        val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(video.scheduledAt))
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { onPlayClick(video) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RoyalPurple.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(video.subject, color = RoyalPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(video.title, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(video.description, color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Date", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(date, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // My Subjects Grid
        item {
            Text(
                text = "My Subjects",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        val subjectList = profile.subjectsCsv.split(",").filter { it.isNotBlank() }
        if (subjectList.isEmpty()) {
            item {
                Text("No subjects configured. Complete study time step to register.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectList.forEach { subject ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(EmeraldGreen.copy(alpha = 0.08f))
                                .border(1.dp, EmeraldGreen.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(subject, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recent / Replayable Classes (Completed Streams)
        item {
            Text(
                text = "Recent Videos (Replayable)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (pastVideos.isEmpty()) {
            item {
                Text(
                    text = "No recorded classes yet.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            items(pastVideos) { video ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardColor)
                        .clickable { onPlayClick(video) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(video.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(video.description, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(video.subject, color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Replay",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Schedule Tab ---
@Composable
fun StudentScheduleTab(profile: StudentProfileEntity, videos: List<VideoEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Weekly Study Schedule", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Your preferred preferences and assigned weekly slots:", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = "Schedule", tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Configured Study Preferences", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (profile.preferredTimesJson.isNotEmpty()) profile.preferredTimesJson else "Preferences not configured yet.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Registered Class Sessions:", fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(10.dp))

        if (videos.isEmpty()) {
            Text("No scheduled classes mapped to your class grade yet.", color = TextSecondary, fontSize = 13.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(videos) { video ->
                    val date = SimpleDateFormat("EEE, dd MMM, hh:mm a", Locale.getDefault()).format(Date(video.scheduledAt))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardColor)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(video.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(video.subject, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(date, color = TextSecondary, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (video.status) {
                                            "live" -> LiveRed.copy(alpha = 0.2f)
                                            "scheduled" -> RoyalPurple.copy(alpha = 0.2f)
                                            else -> Color.Gray.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = video.status.uppercase(),
                                    color = when (video.status) {
                                        "live" -> LiveRed
                                        "scheduled" -> RoyalPurple
                                        else -> Color.LightGray
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Doubt Assistant AI Chat Tab (Section 8.3) ---
@Composable
fun DoubtAssistantTab(viewModel: TutorFlowViewModel) {
    val chatHistory by viewModel.chatMessages.collectAsState()
    val isTyping by viewModel.isChatLoading.collectAsState()
    var messageInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AI Doubt Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Ask questions about any subject from Class 1-12", fontSize = 11.sp, color = TextSecondary)
            }
            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat List
        Box(modifier = Modifier.weight(1f)) {
            if (chatHistory.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hello! Main aapka AI tutor hoon.", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Physics, Math ya Chemistry ka koi bhi sawal poochiye!", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatHistory) { msg ->
                        ChatBubble(message = msg)
                    }

                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EmeraldGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tutor is thinking...", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = { Text("Ask a question...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = EmeraldGreen,
                    focusedContainerColor = CardColor,
                    unfocusedContainerColor = CardColor
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("doubt_chat_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        viewModel.askDoubt(messageInput.trim())
                        messageInput = ""
                    }
                },
                modifier = Modifier
                    .background(EmeraldGreen, CircleShape)
                    .size(48.dp)
                    .testTag("doubt_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = NavyBg)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isStudent = message.sender == "student"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isStudent) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isStudent) 16.dp else 0.dp,
                        bottomEnd = if (isStudent) 0.dp else 16.dp
                    )
                )
                .background(if (isStudent) EmeraldGreen else CardColor)
                .border(1.dp, if (isStudent) EmeraldGreen else Color.White.copy(alpha = 0.08f))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                color = if (isStudent) NavyBg else TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

// --- Notifications Tab ---
@Composable
fun StudentNotificationsTab(viewModel: TutorFlowViewModel, notifications: List<NotificationEntity>) {
    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Notification Inbox", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No recent alerts received.", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { notif ->
                    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notif.sentAt))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardColor)
                            .padding(14.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = "Bell", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(notif.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(notif.body, color = TextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(date, color = TextSecondary.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Section 4D: PASSWORD GATE DIALOG ---
@Composable
fun PasswordGateDialog(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onCorrectPassword: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Gate",
                    tint = LiveRed,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter Class Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "This livestream of ${video.subject} is password protected.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        showError = false
                    },
                    label = { Text("6-Digit Password") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = LiveRed,
                        focusedLabelColor = LiveRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stream_password_input")
                )

                if (showError) {
                    Text(
                        text = "Wrong password. Shake!",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (passwordInput.trim().lowercase() == video.streamPassword.trim().lowercase()) {
                                onCorrectPassword()
                            } else {
                                showError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        modifier = Modifier.testTag("watch_now_button")
                    ) {
                        Text("Watch Now", color = TextPrimary)
                    }
                }
            }
        }
    }
}

// --- Section 4F: VIDEO PLAYER DIALOG ---
@Composable
fun VideoPlayerDialog(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onCompleted: () -> Unit
) {
    var progress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(Unit) {
        // Auto progress simulator!
        while (progress < 1.0f) {
            kotlinx.coroutines.delay(1000)
            progress += 0.15f
            if (progress >= 0.8f && progress - 0.15f < 0.8f) {
                // Completed watch criteria (Section 4F)
                onCompleted()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Now Playing: ${video.title}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Simulated video screen block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Streaming pre-recorded video...",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = progress.coerceAtMost(1f),
                    color = EmeraldGreen,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Subject: ${video.subject}", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = if (progress >= 0.8f) "Watched ✅" else "Watching: ${(progress * 100).toInt()}%",
                        color = if (progress >= 0.8f) EmeraldGreen else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
