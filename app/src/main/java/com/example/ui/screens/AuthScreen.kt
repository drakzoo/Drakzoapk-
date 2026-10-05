package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.DrakzoBg
import com.example.ui.theme.DrakzoBorder
import com.example.ui.theme.DrakzoCardBg
import com.example.ui.theme.DrakzoEmerald
import com.example.ui.theme.DrakzoPrimary
import com.example.ui.theme.DrakzoSecondary
import com.example.ui.theme.DrakzoSurface
import com.example.ui.theme.DrakzoTextMuted
import com.example.ui.theme.DrakzoTextPrimary
import com.example.ui.theme.DrakzoTextSecondary
import com.example.ui.viewmodel.AuthMethod
import com.example.ui.viewmodel.AuthStep
import com.example.ui.viewmodel.DrakzoViewModel

@Composable
fun AuthScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val authStep by viewModel.authStep.collectAsState()
    val authMethod by viewModel.authMethod.collectAsState()
    val username by viewModel.usernameInput.collectAsState()
    val displayName by viewModel.displayNameInput.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val phone by viewModel.phoneInput.collectAsState()
    val verificationCode by viewModel.verificationCodeInput.collectAsState()
    val activeOtp by viewModel.activeOtpCode.collectAsState()
    val destination by viewModel.otpSentDestination.collectAsState()
    val countdown by viewModel.otpCountdownSec.collectAsState()
    val otpBanner by viewModel.otpSentBanner.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Simulated OTP System Dispatch Toast/Banner
            AnimatedVisibility(visible = otpBanner != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, DrakzoSecondary, RoundedCornerShape(14.dp)),
                    color = Color(0xFF0F2432)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = DrakzoSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = otpBanner ?: "",
                                color = DrakzoSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissOtpBanner() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = DrakzoTextMuted)
                        }
                    }
                }
            }

            // Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(DrakzoPrimary, DrakzoSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "DRAKZO",
                        color = DrakzoTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "ALGORITHM AUTHENTICATION",
                        color = DrakzoSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Step Content
            AnimatedContent(
                targetState = authStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "auth_step_transition"
            ) { step ->
                when (step) {
                    AuthStep.LOGIN, AuthStep.REGISTER -> {
                        AuthCredentialsContent(
                            isRegister = step == AuthStep.REGISTER,
                            authMethod = authMethod,
                            username = username,
                            displayName = displayName,
                            email = email,
                            phone = phone,
                            authError = authError,
                            onMethodChange = { viewModel.setAuthMethod(it) },
                            onUsernameChange = { viewModel.usernameInput.value = it },
                            onDisplayNameChange = { viewModel.displayNameInput.value = it },
                            onEmailChange = { viewModel.emailInput.value = it },
                            onPhoneChange = { viewModel.phoneInput.value = it },
                            onToggleMode = {
                                viewModel.selectAuthStep(
                                    if (step == AuthStep.LOGIN) AuthStep.REGISTER else AuthStep.LOGIN
                                )
                            },
                            onSubmit = { viewModel.proceedToVerify() },
                            onQuickDemo = { u, d, c -> viewModel.quickDemoLogin(u, d, c) }
                        )
                    }

                    AuthStep.VERIFY_ACCOUNT -> {
                        VerifyAccountContent(
                            verificationCode = verificationCode,
                            activeOtp = activeOtp,
                            destination = destination,
                            countdown = countdown,
                            authError = authError,
                            onCodeChange = { viewModel.verificationCodeInput.value = it },
                            onResend = { viewModel.resendOtp() },
                            onVerify = { viewModel.verifyAndCreateSession() },
                            onBack = { viewModel.selectAuthStep(AuthStep.LOGIN) }
                        )
                    }

                    AuthStep.CREATING_SESSION -> {
                        CreatingSecureSessionContent()
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun AuthCredentialsContent(
    isRegister: Boolean,
    authMethod: AuthMethod,
    username: String,
    displayName: String,
    email: String,
    phone: String,
    authError: String?,
    onMethodChange: (AuthMethod) -> Unit,
    onUsernameChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onQuickDemo: (username: String, displayName: String, colorHex: String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DrakzoBorder, RoundedCornerShape(20.dp)),
        color = DrakzoSurface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Tabs (Login / Register)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DrakzoCardBg)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isRegister) DrakzoPrimary else Color.Transparent)
                        .clickable { if (isRegister) onToggleMode() }
                        .padding(vertical = 10.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Login",
                        color = if (!isRegister) Color.White else DrakzoTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isRegister) DrakzoPrimary else Color.Transparent)
                        .clickable { if (!isRegister) onToggleMode() }
                        .padding(vertical = 10.dp)
                        .testTag("tab_register"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        color = if (isRegister) Color.White else DrakzoTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation: Only one technology needed
            Text(
                text = "Only ONE technology needed: Email OR Phone Number.",
                color = DrakzoSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Selector: Phone SMS vs Email OTP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DrakzoCardBg)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMethod == AuthMethod.SMS_PHONE) DrakzoSecondary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (authMethod == AuthMethod.SMS_PHONE) DrakzoSecondary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onMethodChange(AuthMethod.SMS_PHONE) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mobile Phone",
                            color = DrakzoTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (authMethod == AuthMethod.EMAIL) DrakzoSecondary.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (authMethod == AuthMethod.EMAIL) DrakzoSecondary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onMethodChange(AuthMethod.EMAIL) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Email Address",
                            color = DrakzoTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary single input (Phone or Email)
            if (authMethod == AuthMethod.SMS_PHONE) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Mobile Phone Number") },
                    placeholder = { Text("+1 (555) 019-2834") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = DrakzoSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_phone"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DrakzoTextPrimary,
                        unfocusedTextColor = DrakzoTextPrimary,
                        focusedBorderColor = DrakzoSecondary,
                        unfocusedBorderColor = DrakzoBorder,
                        focusedLabelColor = DrakzoSecondary,
                        unfocusedLabelColor = DrakzoTextSecondary
                    ),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    placeholder = { Text("neymar@example.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = DrakzoSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_email"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DrakzoTextPrimary,
                        unfocusedTextColor = DrakzoTextPrimary,
                        focusedBorderColor = DrakzoSecondary,
                        unfocusedBorderColor = DrakzoBorder,
                        focusedLabelColor = DrakzoSecondary,
                        unfocusedLabelColor = DrakzoTextSecondary
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Optional user profile name (e.g. Neymar)
            OutlinedTextField(
                value = displayName,
                onValueChange = {
                    onDisplayNameChange(it)
                    onUsernameChange(it.lowercase().replace(" ", "_"))
                },
                label = { Text("Your Name (e.g. Neymar)") },
                placeholder = { Text("Neymar") },
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = DrakzoPrimary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_display_name"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DrakzoTextPrimary,
                    unfocusedTextColor = DrakzoTextPrimary,
                    focusedBorderColor = DrakzoPrimary,
                    unfocusedBorderColor = DrakzoBorder,
                    focusedLabelColor = DrakzoPrimary,
                    unfocusedLabelColor = DrakzoTextSecondary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Dispatch Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1E28))
                    .border(1.dp, DrakzoSecondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = DrakzoSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Account creation & OTP logged to admin: drakzooapk@gmail.com. Photos, videos & chats saved separately.",
                    color = DrakzoTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = authError,
                    color = Color(0xFFF43F5E),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_proceed_verify"),
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isRegister) "Register & Send 4-Digit OTP" else "Login & Send 4-Digit OTP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Demo Profiles for instant testing
            Text(
                text = "— QUICK 1-TAP DEMO TEST —",
                color = DrakzoTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onQuickDemo("neymar", "Neymar Jr", "#10B981") },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("demo_login_neymar"),
                    colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoEmerald.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "⚽ Neymar",
                        color = DrakzoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { onQuickDemo("alex_drakzo", "Alex Drakzo", "#8B5CF6") },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("demo_login_alex"),
                    colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "⚡ Alex",
                        color = DrakzoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { onQuickDemo("elena_r", "Elena Rostova", "#EC4899") },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("demo_login_elena"),
                    colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoSecondary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "🌸 Elena",
                        color = DrakzoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifyAccountContent(
    verificationCode: String,
    activeOtp: String,
    destination: String,
    countdown: Int,
    authError: String?,
    onCodeChange: (String) -> Unit,
    onResend: () -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DrakzoSecondary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        color = DrakzoSurface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(DrakzoSecondary.copy(alpha = 0.15f))
                    .border(1.5.dp, DrakzoSecondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = DrakzoSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verify Account",
                color = DrakzoTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Security passkey delivered to:\n$destination\nAdmin audit copy dispatched to drakzooapk@gmail.com",
                color = DrakzoTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighted Generated Passkey Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DrakzoEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onCodeChange(activeOtp) },
                color = DrakzoCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Security Passkey:", color = DrakzoTextMuted, fontSize = 11.sp)
                        Text(
                            text = activeOtp,
                            color = DrakzoEmerald,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DrakzoEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Auto-Fill",
                            color = DrakzoEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = verificationCode,
                onValueChange = { if (it.length <= 6) onCodeChange(it) },
                label = { Text("Enter Passkey") },
                leadingIcon = {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = DrakzoSecondary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_verification_code"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DrakzoTextPrimary,
                    unfocusedTextColor = DrakzoTextPrimary,
                    focusedBorderColor = DrakzoSecondary,
                    unfocusedBorderColor = DrakzoBorder,
                    focusedLabelColor = DrakzoSecondary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Resend Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (countdown > 0) {
                    Text(
                        text = "Resend code in ${countdown}s",
                        color = DrakzoTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clickable { onResend() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Resend Passkey",
                            color = DrakzoSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Master: 8821",
                    color = DrakzoTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { onCodeChange("8821") }
                )
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authError,
                    color = Color(0xFFF43F5E),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_verify_and_create_session"),
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoSecondary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Verify & Create Secure Session",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Back to Login",
                color = DrakzoTextMuted,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun CreatingSecureSessionContent() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DrakzoEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        color = DrakzoSurface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(46.dp),
                color = DrakzoEmerald,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Creating Secure Session...",
                color = DrakzoTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Generating local device ID & SHA256 end-to-end cryptographic keys.",
                color = DrakzoTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
