package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.StudentProfileEntity
import com.example.data.VideoEntity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminPanelScreen(
    viewModel: TutorFlowViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("home") } // "home", "requests", "students", "videos"

    val pendingStudents by viewModel.pendingStudents.collectAsState()
    val approvedStudents by viewModel.approvedStudents.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()

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
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Overview", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalPurple,
                        selectedTextColor = RoyalPurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = RoyalPurple.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "requests",
                    onClick = { activeTab = "requests" },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingStudents.isNotEmpty()) {
                                Badge(containerColor = LiveRed) { Text(pendingStudents.size.toString()) }
                            }
                        }) {
                            Icon(Icons.Default.Approval, contentDescription = "Requests")
                        }
                    },
                    label = { Text("Pending", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalPurple,
                        selectedTextColor = RoyalPurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = RoyalPurple.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "students",
                    onClick = { activeTab = "students" },
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Directory", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalPurple,
                        selectedTextColor = RoyalPurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = RoyalPurple.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "videos",
                    onClick = { activeTab = "videos" },
                    icon = { Icon(Icons.Default.VideoCall, contentDescription = "Video Manager") },
                    label = { Text("Classes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RoyalPurple,
                        selectedTextColor = RoyalPurple,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = RoyalPurple.copy(alpha = 0.1f)
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
                "home" -> AdminOverviewTab(
                    pendingCount = pendingStudents.size,
                    approvedCount = approvedStudents.size,
                    rejectedCount = allStudents.count { it.status == "rejected" },
                    videos = allVideos,
                    onLogout = onLogout
                )
                "requests" -> AdminRequestsTab(
                    viewModel = viewModel,
                    pendingList = pendingStudents
                )
                "students" -> AdminStudentsDirectoryTab(
                    approvedList = approvedStudents
                )
                "videos" -> AdminVideosTab(
                    viewModel = viewModel,
                    videos = allVideos
                )
            }
        }
    }
}

// --- Overview Dashboard Tab (Section 3A) ---
@Composable
fun AdminOverviewTab(
    pendingCount: Int,
    approvedCount: Int,
    rejectedCount: Int,
    videos: List<VideoEntity>,
    onLogout: () -> Unit
) {
    val liveCount = videos.count { it.status == "live" }
    val scheduledCount = videos.count { it.status == "scheduled" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = "App Logo", tint = RoyalPurple, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TutorFlow Web Admin", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sign Out",
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
                            .background(RoyalPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = RoyalPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats Cards Row
        item {
            Text("Registration Status", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pending", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pendingCount.toString(), color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Approved card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Approved", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(approvedCount.toString(), color = EmeraldGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Rejected card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Rejected", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rejectedCount.toString(), color = LiveRed, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Class Streaming Stats
        item {
            Text("Streaming Dashboard", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LiveRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active Streams", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(liveCount.toString(), color = LiveRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = "Future", tint = RoyalPurple, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scheduled Today", color = TextSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(scheduledCount.toString(), color = RoyalPurple, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent activity
        item {
            Text("Recent Stream Actions", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (videos.isEmpty()) {
            item {
                Text("No streams configured yet.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            items(videos.take(5)) { video ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardColor)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(video.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Password: ${video.streamPassword}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (video.status == "live") LiveRed.copy(alpha = 0.2f) else RoyalPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(video.status.uppercase(), color = if (video.status == "live") LiveRed else RoyalPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- Pending Requests Tab (Section 3B) ---
@Composable
fun AdminRequestsTab(
    viewModel: TutorFlowViewModel,
    pendingList: List<StudentProfileEntity>
) {
    var showRejectionDialogForUserId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pending Student Approvals", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (pendingList.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Zero pending requests! Awesome.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingList) { student ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = CardColor),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(student.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Class: ${student.currentClass} | Token: ${student.tokenNumber}", color = TextSecondary, fontSize = 12.sp)
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = TextSecondary
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("School: ${student.school}", color = TextSecondary, fontSize = 13.sp)
                                Text("Board: ${student.board} | Year: ${student.academicYear}", color = TextSecondary, fontSize = 13.sp)
                                Text("Phone: ${student.phone} | WhatsApp: ${student.whatsapp}", color = TextSecondary, fontSize = 13.sp)
                                Text("Address: ${student.address}, ${student.city}", color = TextSecondary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { showRejectionDialogForUserId = student.userId },
                                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                        modifier = Modifier.testTag("reject_button_${student.userId}")
                                    ) {
                                        Text("REJECT ❌", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = { viewModel.approveStudent(student.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        modifier = Modifier.testTag("approve_button_${student.userId}")
                                    ) {
                                        Text("APPROVE ✅", color = NavyBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rejection Reason Input Modal
        showRejectionDialogForUserId?.let { userId ->
            var rejectionReasonInput by remember { mutableStateOf("") }
            Dialog(onDismissRequest = { showRejectionDialogForUserId = null }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Rejection Details", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            label = { Text("Reason for Rejection") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = LiveRed,
                                focusedLabelColor = LiveRed,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showRejectionDialogForUserId = null }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = {
                                    viewModel.rejectStudent(userId, rejectionReasonInput.trim())
                                    showRejectionDialogForUserId = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                            ) {
                                Text("Submit Rejection", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Student Directory Tab (Section 3C) ---
@Composable
fun AdminStudentsDirectoryTab(approvedList: List<StudentProfileEntity>) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Approved Students Directory", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, class, token...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = RoyalPurple,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedContainerColor = CardColor,
                unfocusedContainerColor = CardColor
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filtered = approvedList.filter {
            it.fullName.contains(searchQuery, true) ||
                    it.currentClass.contains(searchQuery, true) ||
                    it.tokenNumber.contains(searchQuery, true)
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No approved students match the query.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { student ->
                    var showDetailDialog by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardColor)
                            .clickable { showDetailDialog = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(student.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Class: ${student.currentClass} | Token: ${student.tokenNumber}", color = TextSecondary, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.Info, contentDescription = "View Profile", tint = RoyalPurple)
                        }
                    }

                    if (showDetailDialog) {
                        Dialog(onDismissRequest = { showDetailDialog = false }) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Full Profile Details", color = RoyalPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Name: ${student.fullName}", color = TextPrimary)
                                    Text("Token: ${student.tokenNumber}", color = EmeraldGreen, fontFamily = FontFamily.Monospace)
                                    Text("School: ${student.school}", color = TextSecondary)
                                    Text("Class: ${student.currentClass} (${student.board})", color = TextSecondary)
                                    Text("Preferred Subjects: ${student.subjectsCsv}", color = TextPrimary)
                                    Text("Weekly Timing Slot: ${student.preferredTimesJson}", color = TextSecondary)
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { showDetailDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Close")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Class Scheduling & Videos manager Tab (Section 3D / 3E / 3F) ---
@Composable
fun AdminVideosTab(
    viewModel: TutorFlowViewModel,
    videos: List<VideoEntity>
) {
    var showAddForm by remember { mutableStateOf(false) }

    // Form Details
    var videoTitle by remember { mutableStateOf("") }
    var videoSubject by remember { mutableStateOf("Mathematics") }
    var targetClass by remember { mutableStateOf("Class 10") }
    val generatedDesc by viewModel.generatedDescription.collectAsState()
    var manualDescription by remember { mutableStateOf("") }
    val isGeneratingDesc by viewModel.isGeneratingDescription.collectAsState()
    var videoURL by remember { mutableStateOf("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4") }
    var scheduleDelayMinutes by remember { mutableStateOf("0") } // 0 means live immediately, other schedules in future
    var manualPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

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
            Text("Recorded Videos & Streams", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = { showAddForm = !showAddForm },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
            ) {
                Text(if (showAddForm) "Show List" else "Add Video ➕", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showAddForm) {
            // Form to upload / schedule new classes (Section 3D)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = videoTitle,
                        onValueChange = { videoTitle = it },
                        label = { Text("Video Title") },
                        placeholder = { Text("e.g., Optics Introduction") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = RoyalPurple,
                            focusedLabelColor = RoyalPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Gemini AI description generator button (Section 8.2)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                if (videoTitle.isNotBlank()) {
                                    viewModel.generateDescriptionForVideo(videoTitle)
                                } else {
                                    Toast.makeText(context, "Enter a title first!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, RoyalPurple)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI description", tint = RoyalPurple)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Description", color = RoyalPurple, fontSize = 12.sp)
                        }

                        if (isGeneratingDesc) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = RoyalPurple)
                        }
                    }
                }

                // AI Generated Description text or Manual Text Area
                item {
                    val finalDesc = if (generatedDesc.isNotEmpty()) generatedDesc else manualDescription
                    OutlinedTextField(
                        value = finalDesc,
                        onValueChange = {
                            if (generatedDesc.isNotEmpty()) {
                                // Once custom edited, save back
                                manualDescription = it
                            } else {
                                manualDescription = it
                            }
                        },
                        label = { Text("Class Description") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = RoyalPurple,
                            focusedLabelColor = RoyalPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = videoSubject,
                            onValueChange = { videoSubject = it },
                            label = { Text("Subject") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = RoyalPurple,
                                focusedLabelColor = RoyalPurple
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetClass,
                            onValueChange = { targetClass = it },
                            label = { Text("Grade (e.g. Class 10)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = RoyalPurple,
                                focusedLabelColor = RoyalPurple
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = videoURL,
                        onValueChange = { videoURL = it },
                        label = { Text("Video URL Link (Storage/Drive/YT)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = RoyalPurple,
                            focusedLabelColor = RoyalPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = scheduleDelayMinutes,
                            onValueChange = { scheduleDelayMinutes = it },
                            label = { Text("Schedule delay (mins)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = RoyalPurple,
                                focusedLabelColor = RoyalPurple
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = manualPassword,
                            onValueChange = { manualPassword = it },
                            label = { Text("Passcode (leave empty for random)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = RoyalPurple,
                                focusedLabelColor = RoyalPurple
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            val delayMins = scheduleDelayMinutes.toLongOrNull() ?: 0L
                            val scheduledTime = System.currentTimeMillis() + (delayMins * 60 * 1000)
                            val finalDescriptionText = if (generatedDesc.isNotEmpty()) generatedDesc else manualDescription

                            viewModel.createAndScheduleVideo(
                                title = videoTitle,
                                subject = videoSubject,
                                targetClass = targetClass,
                                description = finalDescriptionText,
                                thumbnailURL = "thumb",
                                videoURL = videoURL,
                                scheduledAt = scheduledTime,
                                streamPassword = manualPassword,
                                onComplete = {
                                    Toast.makeText(context, "Class Scheduled Successfully!", Toast.LENGTH_SHORT).show()
                                    // Reset Form
                                    videoTitle = ""
                                    manualDescription = ""
                                    manualPassword = ""
                                    showAddForm = false
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_video_form_button")
                    ) {
                        Text("Add Stream to Schedule & Notify 📺", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        } else {
            // Video Class list
            if (videos.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No video streams defined. Click Add Video above to schedule.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(videos) { video ->
                        val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(video.scheduledAt))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardColor)
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(video.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${video.subject} | Grade: ${video.targetClass}", color = TextSecondary, fontSize = 12.sp)
                                        Text("Date: $date", color = TextSecondary, fontSize = 11.sp)
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
                                        Text(video.status.uppercase(), color = if (video.status == "live") LiveRed else if (video.status == "scheduled") RoyalPurple else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(video.description, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Password Gate Code: ${video.streamPassword}", color = EmeraldGreen, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                if (video.status == "scheduled" || video.status == "live") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row {
                                        if (video.status == "scheduled") {
                                            Button(
                                                onClick = { viewModel.setVideoLive(video.videoId) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                modifier = Modifier.testTag("make_live_button_${video.videoId}")
                                            ) {
                                                Text("Make Live 🔴", color = NavyBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { viewModel.sendStreamNotification(video) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                                        ) {
                                            Text("Send Stream Notification 📺", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
