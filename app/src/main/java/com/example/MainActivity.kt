package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TutorFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyBg
                ) {
                    TutorFlowApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TutorFlowApp(viewModel: TutorFlowViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentProfile by viewModel.currentStudentProfile.collectAsState()

    var recentlySubmittedTokenText by remember { mutableStateOf("") }
    var tokenVerifiedThisSession by remember { mutableStateOf(false) }

    // Navigation gate based on State Machine
    if (recentlySubmittedTokenText.isNotEmpty()) {
        TokenHighlightScreen(
            token = recentlySubmittedTokenText,
            onContinue = { recentlySubmittedTokenText = "" }
        )
    } else {
        val user = currentUser
        if (user == null) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    // Reset session flags on new login
                    tokenVerifiedThisSession = false
                }
            )
        } else {
            if (user.role == "admin") {
                AdminPanelScreen(
                    viewModel = viewModel,
                    onLogout = { viewModel.logout() }
                )
            } else {
                // Student flow
                val profile = currentProfile
                if (profile == null) {
                    StudentRegistrationForm(
                        viewModel = viewModel,
                        onRegistrationSubmitted = { generatedToken ->
                            recentlySubmittedTokenText = generatedToken
                        }
                    )
                } else {
                    when (profile.status) {
                        "pending", "rejected" -> {
                            AwaitingApprovalScreen(
                                profile = profile,
                                onLogout = { viewModel.logout() },
                                onProceedToTokenVerification = {
                                    // Normally handled by status refresh, but allowed here
                                }
                            )
                        }
                        "approved" -> {
                            val isPreferencesConfigured = profile.subjectsCsv.isNotEmpty()
                            if (!isPreferencesConfigured) {
                                if (!tokenVerifiedThisSession) {
                                    TokenVerificationScreen(
                                        profile = profile,
                                        viewModel = viewModel,
                                        onTokenVerified = {
                                            tokenVerifiedThisSession = true
                                        }
                                    )
                                } else {
                                    SubjectScheduleSetupScreen(
                                        viewModel = viewModel,
                                        onSetupCompleted = {
                                            // The Flow will automatically re-render because profile.subjectsCsv updates
                                        }
                                    )
                                }
                            } else {
                                // Full unlocked app
                                StudentDashboardScreen(
                                    viewModel = viewModel,
                                    profile = profile,
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
