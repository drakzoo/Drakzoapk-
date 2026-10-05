package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DrakzoAvatar
import com.example.ui.components.DrakzoMyQrDialog
import com.example.ui.components.DrakzoQrCodeCanvas
import com.example.ui.components.DrakzoQrScannerDialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.activeSession.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val friends by viewModel.friends.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val user = session
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("profile_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Avatar & Name Card
            Box(contentAlignment = Alignment.BottomEnd) {
                DrakzoAvatar(
                    name = user?.displayName ?: "Drakzo User",
                    colorHex = user?.avatarColorHex ?: "#8B5CF6",
                    size = 90.dp,
                    isOnline = true,
                    showOnlineBadge = true
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DrakzoPrimary)
                        .border(1.5.dp, DrakzoBg, CircleShape)
                        .clickable { showEditDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = user?.displayName ?: "Alex Drakzo",
                color = DrakzoTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "@${user?.username ?: "alex_drakzo"}",
                color = DrakzoSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )

            if (user != null) {
                val registeredChannel = if (user.phoneNumber.isNotBlank()) "📱 " + user.phoneNumber else "✉️ " + user.email
                Text(
                    text = registeredChannel,
                    color = DrakzoTextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = user?.statusBio ?: "Online on Drakzo encrypted node",
                color = DrakzoTextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DrakzoSurface)
                    .border(1.dp, DrakzoBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(title = "Chats", value = "${chats.size}")
                ProfileStatItem(title = "Friends", value = "${friends.size}")
                ProfileStatItem(title = "Display", value = "120Hz Max")
                ProfileStatItem(title = "E2EE", value = "AES-256")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Drakzo Identity QR Passkey Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, DrakzoSecondary.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                color = DrakzoSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrakzoQrCodeCanvas(
                        data = "drakzo://user/@${user?.username ?: "alex_drakzo"}",
                        modifier = Modifier
                            .size(72.dp)
                            .clickable { viewModel.openMyQrCode() }
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Identity Passkey QR",
                            color = DrakzoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Share with peers for direct profile lookup & 1-tap encrypted chat.",
                            color = DrakzoTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.openMyQrCode() },
                                colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoSecondary.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("My QR", fontSize = 11.sp, color = DrakzoTextPrimary)
                            }

                            Button(
                                onClick = { viewModel.openQrScanner() },
                                colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Secure Session Information (CRITICAL ALGORITHM STEP)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, DrakzoPrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                color = DrakzoSurface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = DrakzoSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURE SESSION METRICS",
                            color = DrakzoTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SessionDetailRow(
                        icon = Icons.Default.VpnKey,
                        label = "Session Token",
                        value = user?.sessionToken ?: "DKZ-SEC-XXXX"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SessionDetailRow(
                        icon = Icons.Default.PermDeviceInformation,
                        label = "Device Node",
                        value = user?.deviceId ?: "NODE-DEFAULT"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SessionDetailRow(
                        icon = Icons.Default.Fingerprint,
                        label = "Key Fingerprint",
                        value = user?.encryptionFingerprint ?: "SHA256:E2EE:VERIFIED"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SessionDetailRow(
                        icon = Icons.Default.CheckCircle,
                        label = "Session Created",
                        value = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                            Date(user?.createdAt ?: System.currentTimeMillis())
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_edit_profile"),
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoBorder)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = DrakzoSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile Details", color = DrakzoTextPrimary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.switchToNewLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_profile_switch_account"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoSecondary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = DrakzoSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Account / New Login", color = DrakzoSecondary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Edit Profile Modal
        if (showEditDialog && user != null) {
            EditProfileDialog(
                currentName = user.displayName,
                currentBio = user.statusBio,
                currentColor = user.avatarColorHex,
                onDismiss = { showEditDialog = false },
                onSave = { name, bio, color ->
                    viewModel.updateProfile(name, bio, color)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = DrakzoTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            color = DrakzoTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SessionDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DrakzoCardBg)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = DrakzoPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, color = DrakzoTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = value,
                color = DrakzoTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentColor: String,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var selectedColor by remember { mutableStateOf(currentColor) }

    val colorOptions = listOf("#8B5CF6", "#06B6D4", "#EC4899", "#10B981", "#F59E0B", "#3B82F6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Drakzo Profile", color = DrakzoTextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Status Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Avatar Color:", color = DrakzoTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, selectedColor) },
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DrakzoTextMuted)
            }
        },
        containerColor = DrakzoSurface
    )
}
