package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.NotificationEntity
import com.example.data.StudentProfileEntity
import com.example.data.VideoEntity
import java.text.SimpleDateFormat
import java.util.*

// --- COLOR CONTEXTS (Section 7) ---
val NavyBg = Color(0xFF0A0F1E)
val PurpleGradientEnd = Color(0xFF1A0533)
val CardColor = Color(0xFF111827)
val EmeraldGreen = Color(0xFF00C896)
val RoyalPurple = Color(0xFF7C3AED)
val LiveRed = Color(0xFFEF4444)
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)

// --- Shared Glassmorphism Background ---
@Composable
fun TutorFlowBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyBg, PurpleGradientEnd)
                )
            )
    ) {
        // Subtle ambient blur circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(RoyalPurple.copy(alpha = 0.15f), CircleShape)
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .background(EmeraldGreen.copy(alpha = 0.12f), CircleShape)
                .blur(80.dp)
        )
        content()
    }
}

// --- Glass Card Composable ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

// --- Section 1: LOGIN & SIGN UP ---
@Composable
fun LoginScreen(
    viewModel: TutorFlowViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("student") } // "student" or "admin"
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    TutorFlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // App Logo Centered
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "TutorFlow Logo",
                tint = EmeraldGreen,
                modifier = Modifier
                    .size(80.dp)
                    .background(EmeraldGreen.copy(alpha = 0.1f), CircleShape)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "TutorFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "Smart Tuition Management System",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphism Card for login form
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card")
            ) {
                Text(
                    text = if (isSignUp) "Create Account" else "Welcome Back",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (isSignUp) "Sign up to begin your learning journey" else "Sign in to access your classes",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Role Selector Tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedRole == "student") EmeraldGreen else Color.Transparent)
                            .clickable { selectedRole = "student" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Student",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRole == "student") NavyBg else TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedRole == "admin") RoyalPurple else Color.Transparent)
                            .clickable { selectedRole = "admin" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Admin",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRole == "admin") TextPrimary else TextPrimary.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = if (selectedRole == "student") EmeraldGreen else RoyalPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = if (selectedRole == "student") EmeraldGreen else RoyalPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = if (selectedRole == "student") EmeraldGreen else RoyalPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = if (selectedRole == "student") EmeraldGreen else RoyalPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input")
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button with animated gradient or color
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        viewModel.login(
                            email = email,
                            role = selectedRole,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRole == "student") EmeraldGreen else RoyalPurple
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = NavyBg, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isSignUp) "Sign Up" else "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRole == "student") NavyBg else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OR Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                    Text(
                        text = " OR ",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        val name = if (email.contains("@")) email.substringBefore("@") else "User"
                        viewModel.loginWithGoogle(
                            email = if (email.isNotEmpty()) email else "student@tutorflow.com",
                            name = name,
                            role = selectedRole,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_login_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Google Icon",
                            tint = NavyBg,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            color = NavyBg,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Account Creation
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Create One",
                    fontSize = 12.sp,
                    color = if (selectedRole == "student") EmeraldGreen else RoyalPurple,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { isSignUp = !isSignUp }
                )
            }
        }
    }
}

// --- Section 2: STUDENT REGISTRATION FORM ---
@Composable
fun StudentRegistrationForm(
    viewModel: TutorFlowViewModel,
    onRegistrationSubmitted: (String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Step 1 Details
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2010-05-15") }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var whatsappSame by remember { mutableStateOf(true) }
    var whatsappPhone by remember { mutableStateOf("") }
    var homeAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    // Step 2 Details
    var schoolName by remember { mutableStateOf("") }
    var currentClass by remember { mutableStateOf("Class 10") }
    var board by remember { mutableStateOf("CBSE") }
    var academicYear by remember { mutableStateOf("2026-27") }
    var prevMarks by remember { mutableStateOf("") }
    var guardianName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var relationToGuardian by remember { mutableStateOf("Father") }

    // Suggestions derived from Gemini (Section 8.1)
    val suggestedSubjects by viewModel.suggestedSubjects.collectAsState()
    val isSuggesting by viewModel.isSuggestingSubjects.collectAsState()

    TutorFlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Step Indicator / Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (step > 1) step-- },
                    enabled = step > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (step > 1) TextPrimary else TextSecondary.copy(alpha = 0.3f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Student Registration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Step $step of 3: ${
                            when (step) {
                                1 -> "Personal Details"
                                2 -> "Academic Details"
                                else -> "Review & Submit"
                            }
                        }",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (step >= 1) EmeraldGreen else Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (step >= 2) EmeraldGreen else Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (step >= 3) EmeraldGreen else Color.White.copy(alpha = 0.1f))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (step) {
                    1 -> {
                        // STEP 1 - Personal Details
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldGreen,
                                        focusedBorderColor = EmeraldGreen
                                    )
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = dob,
                                        onValueChange = { dob = it },
                                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                    OutlinedTextField(
                                        value = gender,
                                        onValueChange = { gender = it },
                                        label = { Text("Gender") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldGreen,
                                        focusedBorderColor = EmeraldGreen
                                    )
                                )
                            }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { whatsappSame = !whatsappSame }
                                ) {
                                    Checkbox(
                                        checked = whatsappSame,
                                        onCheckedChange = { whatsappSame = it },
                                        colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
                                    )
                                    Text("WhatsApp is same as phone", color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                            if (!whatsappSame) {
                                item {
                                    OutlinedTextField(
                                        value = whatsappPhone,
                                        onValueChange = { whatsappPhone = it },
                                        label = { Text("WhatsApp Number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = homeAddress,
                                    onValueChange = { homeAddress = it },
                                    label = { Text("Home Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldGreen,
                                        focusedBorderColor = EmeraldGreen
                                    )
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = { city = it },
                                        label = { Text("City") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                    OutlinedTextField(
                                        value = pinCode,
                                        onValueChange = { pinCode = it },
                                        label = { Text("PIN Code") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // STEP 2 - Academic Details
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                OutlinedTextField(
                                    value = schoolName,
                                    onValueChange = { schoolName = it },
                                    label = { Text("School/College Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldGreen,
                                        focusedBorderColor = EmeraldGreen
                                    )
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = currentClass,
                                        onValueChange = { currentClass = it },
                                        label = { Text("Class / Grade") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                    OutlinedTextField(
                                        value = board,
                                        onValueChange = { board = it },
                                        label = { Text("Board (e.g. CBSE)") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                            item {
                                Button(
                                    onClick = {
                                        viewModel.fetchSubjectSuggestions(currentClass, board)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, EmeraldGreen)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Suggestions", tint = EmeraldGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fetch AI Subject Suggestions", color = EmeraldGreen, fontSize = 12.sp)
                                }
                            }

                            if (isSuggesting) {
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = EmeraldGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Consulting Gemini API...", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            } else if (suggestedSubjects.isNotEmpty()) {
                                item {
                                    Text("Suggested Subjects for $currentClass:", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(suggestedSubjects.joinToString(", "), color = TextPrimary, fontSize = 12.sp)
                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = academicYear,
                                        onValueChange = { academicYear = it },
                                        label = { Text("Academic Year") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                    OutlinedTextField(
                                        value = prevMarks,
                                        onValueChange = { prevMarks = it },
                                        label = { Text("Prev CGPA / %") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = guardianName,
                                    onValueChange = { guardianName = it },
                                    label = { Text("Guardian Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = EmeraldGreen,
                                        focusedBorderColor = EmeraldGreen
                                    )
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = guardianPhone,
                                        onValueChange = { guardianPhone = it },
                                        label = { Text("Guardian Phone") },
                                        modifier = Modifier.weight(1.2f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                    OutlinedTextField(
                                        value = relationToGuardian,
                                        onValueChange = { relationToGuardian = it },
                                        label = { Text("Relation") },
                                        modifier = Modifier.weight(0.8f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedLabelColor = EmeraldGreen,
                                            focusedBorderColor = EmeraldGreen
                                        )
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // STEP 3 - Review & Submit
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                GlassCard {
                                    Text("Summary Profile", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Name: $fullName", color = TextPrimary)
                                    Text("DOB: $dob | Gender: $gender", color = TextSecondary)
                                    Text("Phone: $phone", color = TextSecondary)
                                    Text("Address: $homeAddress, $city, $pinCode", color = TextSecondary)
                                }
                            }
                            item {
                                GlassCard {
                                    Text("Academic Credentials", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("School: $schoolName", color = TextPrimary)
                                    Text("Grade: $currentClass | Board: $board", color = TextSecondary)
                                    Text("Academic Year: $academicYear", color = TextSecondary)
                                    Text("Guardian: $guardianName ($relationToGuardian) - $guardianPhone", color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    Button(
                        onClick = { step-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Text("Edit", color = TextPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            viewModel.submitRegistration(
                                fullName = fullName,
                                dob = dob,
                                gender = gender,
                                phone = phone,
                                whatsapp = if (whatsappSame) phone else whatsappPhone,
                                address = homeAddress,
                                city = city,
                                pinCode = pinCode,
                                school = schoolName,
                                currentClass = currentClass,
                                board = board,
                                academicYear = academicYear,
                                previousMarks = prevMarks,
                                guardianName = guardianName,
                                guardianPhone = guardianPhone,
                                guardianRelation = relationToGuardian,
                                profilePhotoURL = "",
                                onComplete = { profile ->
                                    onRegistrationSubmitted(profile.tokenNumber)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("next_step_button")
                ) {
                    Text(
                        text = if (step == 3) "Submit Registration" else "Continue",
                        color = NavyBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- TOKEN HIGHLIGHT SCREEN (After Registration submission) ---
@Composable
fun TokenHighlightScreen(
    token: String,
    onContinue: () -> Unit
) {
    TutorFlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = EmeraldGreen,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registration Submitted!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Your application is currently pending admin approval.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmeraldGreen.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "YOUR TUTORFLOW TOKEN",
                    fontSize = 12.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = token,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("token_display_box")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Save this token. You will need it to access the app after approval.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("registration_complete_ok")
            ) {
                Text("Got It, Continue", color = NavyBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Section 4: AWAITING APPROVAL / STATUS SCREEN ---
@Composable
fun AwaitingApprovalScreen(
    profile: StudentProfileEntity,
    onLogout: () -> Unit,
    onProceedToTokenVerification: () -> Unit
) {
    TutorFlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profile.status == "pending") {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = EmeraldGreen,
                    strokeWidth = 5.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Awaiting Approval...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hello, ${profile.fullName}. Your registration is being reviewed by our Admin team.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Details Registered:", fontWeight = FontWeight.Bold, color = EmeraldGreen, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Token Number: ${profile.tokenNumber}", fontFamily = FontFamily.Monospace, color = TextPrimary)
                    Text("Class: ${profile.currentClass} (${profile.board})", color = TextSecondary)
                    Text("School: ${profile.school}", color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action simulation triggers
                Text(
                    text = "Tip: You can use the Admin role button at the Login screen to approve this profile instantly and see the full student app in action!",
                    color = EmeraldGreen,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

            } else if (profile.status == "rejected") {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Rejected",
                    tint = LiveRed,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Registration Rejected",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = LiveRed.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "REJECTION REASON",
                        fontSize = 11.sp,
                        color = LiveRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (profile.rejectionReason.isNotEmpty()) profile.rejectionReason else "Documents or details submitted were unclear.",
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Log Out", color = TextPrimary)
                }

                if (profile.status == "approved") {
                    Button(
                        onClick = onProceedToTokenVerification,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Verify Token Now", color = NavyBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Section 4A: TOKEN VERIFICATION SCREEN ---
@Composable
fun TokenVerificationScreen(
    profile: StudentProfileEntity,
    viewModel: TutorFlowViewModel,
    onTokenVerified: () -> Unit
) {
    var tokenInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }

    TutorFlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security Token Gate",
                tint = EmeraldGreen,
                modifier = Modifier
                    .size(82.dp)
                    .background(EmeraldGreen.copy(alpha = 0.08f), CircleShape)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter Your Token",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "TutorFlow requires one-time validation to unlock your schedule dashboard.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        showError = false
                    },
                    label = { Text("Registration Token") },
                    placeholder = { Text("TF-YYYY-XXXX") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = EmeraldGreen,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("token_gate_input")
                )

                if (showError) {
                    Text(
                        text = "Invalid token. Check your email or try again.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isValidating = true
                        showError = false
                        if (tokenInput.trim().uppercase() == profile.tokenNumber.trim().uppercase()) {
                            isValidating = false
                            onTokenVerified()
                        } else {
                            isValidating = false
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("verify_token_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NavyBg)
                    } else {
                        Text("Unlock Dashboard", color = NavyBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Section 4B: SUBJECT & SCHEDULE SETUP ---
@Composable
fun SubjectScheduleSetupScreen(
    viewModel: TutorFlowViewModel,
    onSetupCompleted: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // State
    val subjectsList = listOf("Mathematics", "Physics", "Chemistry", "Biology", "English", "Social Science", "Hindi", "Computer Science")
    val selectedSubjects = remember { mutableStateListOf<String>() }
    var customSubject by remember { mutableStateOf("") }

    // Slots Setup
    val slots = listOf("Morning (6AM–12PM)", "Afternoon (12PM–5PM)", "Evening (5PM–9PM)", "Night (9PM–11PM)")
    var selectedSlot by remember { mutableStateOf(slots[2]) } // default Evening

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = remember { mutableStateListOf<String>("Mon", "Wed", "Fri") }

    TutorFlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Preferences Configuration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Step $step of 3: ${
                    when (step) {
                        1 -> "Select Subjects"
                        2 -> "Schedule Preferences"
                        else -> "Confirm Choices"
                    }
                }",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step Indicator Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (step >= 1) EmeraldGreen else Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (step >= 2) EmeraldGreen else Color.White.copy(alpha = 0.1f))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (step >= 3) EmeraldGreen else Color.White.copy(alpha = 0.1f))
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (step) {
                    1 -> {
                        // Step 1: Subject Selection Grid
                        Column {
                            Text("Select subjects you want tuition classes for:", color = TextPrimary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(subjectsList) { subject ->
                                    val isSelected = selectedSubjects.contains(subject)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) EmeraldGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, if (isSelected) EmeraldGreen else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isSelected) selectedSubjects.remove(subject) else selectedSubjects.add(subject)
                                            }
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = subject, color = if (isSelected) EmeraldGreen else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customSubject,
                                onValueChange = { customSubject = it },
                                label = { Text("Custom Subject (Optional)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedLabelColor = EmeraldGreen,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                trailingIcon = {
                                    if (customSubject.isNotBlank()) {
                                        IconButton(onClick = {
                                            selectedSubjects.add(customSubject.trim())
                                            customSubject = ""
                                        }) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom", tint = EmeraldGreen)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    2 -> {
                        // Step 2: Preferred Study Time
                        Column {
                            Text("When do you prefer to study?", color = TextPrimary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Time Slot Dropdown / Column
                            Text("Select Preferred Time Slot:", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            slots.forEach { slot ->
                                val isSelected = selectedSlot == slot
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, if (isSelected) EmeraldGreen else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { selectedSlot = slot }
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                            contentDescription = "Selected",
                                            tint = if (isSelected) EmeraldGreen else TextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = slot, color = if (isSelected) EmeraldGreen else TextPrimary, fontSize = 14.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text("Preferred Days of Week:", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(daysOfWeek) { day ->
                                    val isSelected = selectedDays.contains(day)
                                    Box(
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) EmeraldGreen else Color.White.copy(alpha = 0.05f))
                                            .clickable {
                                                if (isSelected) selectedDays.remove(day) else selectedDays.add(day)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day,
                                            color = if (isSelected) NavyBg else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Step 3: Confirm & Submit
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Please verify your subject preferences:", color = TextPrimary, fontSize = 14.sp)

                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text("SELECTED SUBJECTS", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (selectedSubjects.isEmpty()) {
                                    Text("No subjects selected", color = Color.Red)
                                } else {
                                    Text(selectedSubjects.joinToString(", "), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text("STUDY SCHEDULE", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Time: $selectedSlot", color = TextPrimary, fontSize = 14.sp)
                                Text("Days: ${selectedDays.joinToString(", ")}", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    Button(
                        onClick = { step-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
                    ) {
                        Text("Back", color = TextPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            viewModel.saveStudentPreferences(
                                subjects = selectedSubjects.toList(),
                                preferredTimes = "Time: $selectedSlot, Days: ${selectedDays.joinToString(",")}",
                                onComplete = onSetupCompleted
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("preferences_submit_button")
                ) {
                    Text(
                        text = if (step == 3) "Save & Unlock App" else "Next Step",
                        color = NavyBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
