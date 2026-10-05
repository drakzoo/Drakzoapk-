package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.AdminReportEntity
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.viewmodel.DrakzoViewModel

@Composable
fun SettingsScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val adminReports by viewModel.adminReports.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLastSeenDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    var showBugReportDialog by remember { mutableStateOf(false) }
    var showAdminLogDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Text(
                text = "DRAKZO PREFERENCES",
                color = DrakzoTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            // --- Section: Privacy ---
            SettingsSectionCard(title = "Privacy & Visibility", icon = Icons.Default.PrivacyTip) {
                SettingsClickableRow(
                    title = "Profile & Status Visibility",
                    subtitle = settings.profileVisibility,
                    onClick = { showVisibilityDialog = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsClickableRow(
                    title = "Last Seen Visibility",
                    subtitle = settings.lastSeenVisibility,
                    onClick = { showLastSeenDialog = true }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Read Receipts",
                    subtitle = "Show double blue checkmarks when messages are read",
                    checked = settings.readReceiptsEnabled,
                    onCheckedChange = { viewModel.updateSettings(readReceipts = it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DrakzoCardBg)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DrakzoEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("End-to-End Encryption", color = DrakzoTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Active 256-bit AES protocol on all socket channels", color = DrakzoEmerald, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Section: Display & Refresh Rate ---
            SettingsSectionCard(title = "Display & Performance", icon = Icons.Default.Lock) {
                SettingsClickableRow(
                    title = "Screen Refresh Rate",
                    subtitle = settings.refreshRateMode,
                    onClick = { showRefreshRateDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DrakzoCardBg)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DrakzoSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "120Hz/60Hz hardware accelerated pacing unlocked",
                        color = DrakzoSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Section: Notifications ---
            SettingsSectionCard(title = "Notifications & Alerts", icon = Icons.Default.Notifications) {
                SettingsToggleRow(
                    title = "In-App Notification Banners",
                    subtitle = "Display drop-down message alerts while using the app",
                    checked = settings.inAppNotificationsEnabled,
                    onCheckedChange = { viewModel.updateSettings(inAppNotifs = it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Sound & Vibration",
                    subtitle = "Haptic pulse and alert chime on incoming transmission",
                    checked = settings.soundVibrateEnabled,
                    onCheckedChange = { viewModel.updateSettings(soundVibrate = it) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Message Previews",
                    subtitle = "Include sender and text snippet in notification banner",
                    checked = settings.messagePreviewEnabled,
                    onCheckedChange = { viewModel.updateSettings(preview = it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Section: Language ---
            SettingsSectionCard(title = "Language & Region", icon = Icons.Default.Language) {
                SettingsClickableRow(
                    title = "Interface Language",
                    subtitle = settings.selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Section: Reporting Admin & Bug Center ---
            SettingsSectionCard(title = "Reporting & Admin Support", icon = Icons.Default.Email) {
                // Admin Email Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C1F2E))
                        .border(1.dp, DrakzoSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DrakzoSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = DrakzoSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reporting Admin Email",
                            color = DrakzoTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "drakzooapk@gmail.com",
                            color = DrakzoSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Admin Email", "drakzooapk@gmail.com")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied drakzooapk@gmail.com to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Admin Email",
                                tint = DrakzoSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:drakzooapk@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Drakzo App Bug Report / Support")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Admin: drakzooapk@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Email",
                                tint = DrakzoSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Report Bug Button
                Button(
                    onClick = { showBugReportDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_report_bug"),
                    colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoPrimary.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = DrakzoPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Report a Bug / Problem to Admin",
                        color = DrakzoTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingsClickableRow(
                    title = "Admin Activity & Dispatch Log",
                    subtitle = "${adminReports.size} items recorded to drakzooapk@gmail.com",
                    onClick = { showAdminLogDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Section: Account Management ---
            SettingsSectionCard(title = "Account Management", icon = Icons.Default.Security) {
                Button(
                    onClick = { viewModel.switchToNewLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_switch_account"),
                    colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoSecondary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Login With Another Email / Phone",
                        color = DrakzoSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.resetToFirstTimeLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_reset_first_time"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoBorder)
                ) {
                    Text(
                        text = "Reset to First-Time Login Page",
                        color = DrakzoTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Section: Logout / Clear Active Session ---
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_logout_session"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFF87171)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Logout & Clear Active Session",
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout of Drakzo?", color = DrakzoTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Logging out will terminate and purge your active session token from the local Room database. You will return to the Login screen.",
                        color = DrakzoTextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("btn_confirm_logout")
                    ) {
                        Text("Clear Session & Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }

        // Language Selector Dialog
        if (showLanguageDialog) {
            val languages = listOf("English (US)", "Español", "Français", "Deutsch", "日本語")
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Select Language", color = DrakzoTextPrimary) },
                text = {
                    Column {
                        languages.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(language = lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.selectedLanguage == lang,
                                    onClick = {
                                        viewModel.updateSettings(language = lang)
                                        showLanguageDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = DrakzoSecondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = lang, color = DrakzoTextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("Cancel", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }

        // Last Seen Dialog
        if (showLastSeenDialog) {
            val options = listOf("Everyone", "Contacts Only", "Nobody")
            AlertDialog(
                onDismissRequest = { showLastSeenDialog = false },
                title = { Text("Last Seen Visibility", color = DrakzoTextPrimary) },
                text = {
                    Column {
                        options.forEach { opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(lastSeen = opt)
                                        showLastSeenDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.lastSeenVisibility == opt,
                                    onClick = {
                                        viewModel.updateSettings(lastSeen = opt)
                                        showLastSeenDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = DrakzoPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = opt, color = DrakzoTextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showLastSeenDialog = false }) {
                        Text("Cancel", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }

        // Profile & Status Visibility Dialog
        if (showVisibilityDialog) {
            val options = listOf("Public (All Network Nodes)", "Friends Only (Mutual Contacts)", "Ghost Mode (Private)")
            AlertDialog(
                onDismissRequest = { showVisibilityDialog = false },
                title = { Text("Profile & Status Audience", color = DrakzoTextPrimary) },
                text = {
                    Column {
                        options.forEach { opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(profileVisibility = opt.split(" ").first())
                                        showVisibilityDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.profileVisibility == opt.split(" ").first(),
                                    onClick = {
                                        viewModel.updateSettings(profileVisibility = opt.split(" ").first())
                                        showVisibilityDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = DrakzoSecondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = opt, color = DrakzoTextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showVisibilityDialog = false }) {
                        Text("Cancel", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }

        // Refresh Rate Dialog
        if (showRefreshRateDialog) {
            val options = listOf("120Hz Max (Ultra Smooth)", "Dynamic 60Hz - 120Hz", "Power Saver 60Hz")
            AlertDialog(
                onDismissRequest = { showRefreshRateDialog = false },
                title = { Text("Display Refresh Rate", color = DrakzoTextPrimary) },
                text = {
                    Column {
                        options.forEach { opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSettings(refreshRate = opt)
                                        showRefreshRateDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.refreshRateMode == opt,
                                    onClick = {
                                        viewModel.updateSettings(refreshRate = opt)
                                        showRefreshRateDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = DrakzoEmerald)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = opt, color = DrakzoTextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showRefreshRateDialog = false }) {
                        Text("Cancel", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }

        // Bug & Problem Report Dialog
        if (showBugReportDialog) {
            var selectedCategory by remember { mutableStateOf("Login / OTP Issue") }
            var bugTitle by remember { mutableStateOf("") }
            var bugDescription by remember { mutableStateOf("") }
            var feedbackSent by remember { mutableStateOf(false) }

            val categories = listOf("Login / OTP Issue", "Chat & Messaging", "Photos / Videos", "Network & Speed", "Other")

            AlertDialog(
                onDismissRequest = { showBugReportDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = DrakzoPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report Problem to Admin", color = DrakzoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Admin Email: drakzooapk@gmail.com",
                            color = DrakzoSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Issue Category:", color = DrakzoTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Chips
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.take(3).forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DrakzoPrimary else DrakzoCardBg)
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(text = cat, color = if (isSelected) Color.White else DrakzoTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bugTitle,
                            onValueChange = { bugTitle = it },
                            label = { Text("Issue Summary") },
                            placeholder = { Text("e.g. Login OTP problem") },
                            modifier = Modifier.fillMaxWidth().testTag("input_bug_title"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DrakzoTextPrimary,
                                unfocusedTextColor = DrakzoTextPrimary,
                                focusedBorderColor = DrakzoPrimary,
                                unfocusedBorderColor = DrakzoBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bugDescription,
                            onValueChange = { bugDescription = it },
                            label = { Text("Describe the problem") },
                            placeholder = { Text("Details of what happened...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_bug_desc"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = DrakzoTextPrimary,
                                unfocusedTextColor = DrakzoTextPrimary,
                                focusedBorderColor = DrakzoPrimary,
                                unfocusedBorderColor = DrakzoBorder
                            )
                        )

                        if (feedbackSent) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✅ Dispatched to drakzooapk@gmail.com successfully!",
                                color = DrakzoEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (bugTitle.isNotBlank()) {
                                viewModel.submitBugReport(selectedCategory, bugTitle, bugDescription)
                                feedbackSent = true
                                Toast.makeText(context, "Problem reported to drakzooapk@gmail.com", Toast.LENGTH_SHORT).show()
                                showBugReportDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                        modifier = Modifier.testTag("btn_submit_bug_report")
                    ) {
                        Text("Submit to Admin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBugReportDialog = false }) {
                        Text("Close", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }

        // Admin Activity & Dispatch Log Dialog
        if (showAdminLogDialog) {
            AlertDialog(
                onDismissRequest = { showAdminLogDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Dispatch Log", color = DrakzoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Recipient: drakzooapk@gmail.com",
                            color = DrakzoSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Record of all account creations, OTP codes, and bug reports sent to the administrator.",
                            color = DrakzoTextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (adminReports.isEmpty()) {
                            Text(
                                text = "No dispatches recorded yet. Creating an account or requesting OTP will record events here.",
                                color = DrakzoTextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (report in adminReports) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = DrakzoCardBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = report.title,
                                                    color = DrakzoTextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = report.status,
                                                    color = DrakzoEmerald,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = report.description,
                                                color = DrakzoTextSecondary,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "To: ${report.adminRecipient}",
                                                color = DrakzoSecondary,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAdminLogDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoSecondary)
                    ) {
                        Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DrakzoSurface
            )
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DrakzoBorder, RoundedCornerShape(16.dp)),
        color = DrakzoSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = DrakzoPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = DrakzoTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = DrakzoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = DrakzoTextMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DrakzoPrimary,
                uncheckedThumbColor = DrakzoTextMuted,
                uncheckedTrackColor = DrakzoCardBg
            )
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, color = DrakzoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = DrakzoSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
