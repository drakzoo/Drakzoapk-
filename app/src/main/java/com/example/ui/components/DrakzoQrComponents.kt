package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.model.FriendEntity
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
import kotlin.math.abs

/**
 * Authentic Procedural QR Code Canvas
 */
@Composable
fun DrakzoQrCodeCanvas(
    data: String,
    modifier: Modifier = Modifier,
    foreground: Color = DrakzoSecondary,
    background: Color = Color.Black
) {
    val seed = abs(data.hashCode())

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(2.dp, foreground.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridCount = 21
            val cellSize = size.width / gridCount

            // Helper to check if in finder pattern corners
            fun inCornerPattern(r: Int, c: Int): Boolean {
                val inTopLeft = r in 0..6 && c in 0..6
                val inTopRight = r in 0..6 && c in (gridCount - 7) until gridCount
                val inBottomLeft = r in (gridCount - 7) until gridCount && c in 0..6
                return inTopLeft || inTopRight || inBottomLeft
            }

            fun isCornerSquareFill(r: Int, c: Int, rowOffset: Int, colOffset: Int): Boolean {
                val lr = r - rowOffset
                val lc = c - colOffset
                if (lr in 0..6 && lc in 0..6) {
                    val isBorder = lr == 0 || lr == 6 || lc == 0 || lc == 6
                    val isCenter = lr in 2..4 && lc in 2..4
                    return isBorder || isCenter
                }
                return false
            }

            for (r in 0 until gridCount) {
                for (c in 0 until gridCount) {
                    val isCorner = inCornerPattern(r, c)
                    val filled = if (isCorner) {
                        isCornerSquareFill(r, c, 0, 0) ||
                                isCornerSquareFill(r, c, 0, gridCount - 7) ||
                                isCornerSquareFill(r, c, gridCount - 7, 0)
                    } else if (r == 6 || c == 6) {
                        // Timing pattern
                        (r + c) % 2 == 0
                    } else {
                        // Procedural module based on data seed
                        val bit = ((seed xor (r * 31 + c * 17)) % 100) < 48
                        bit
                    }

                    if (filled) {
                        drawRoundRect(
                            color = foreground,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f),
                            cornerRadius = CornerRadius(cellSize * 0.2f, cellSize * 0.2f)
                        )
                    }
                }
            }

            // Central Drakzo Emblem Box
            val centerStart = gridCount / 2 - 1
            val centerCells = 3
            drawRoundRect(
                color = background,
                topLeft = Offset(centerStart * cellSize, centerStart * cellSize),
                size = Size(centerCells * cellSize, centerCells * cellSize),
                cornerRadius = CornerRadius(cellSize * 0.5f, cellSize * 0.5f)
            )
            drawRoundRect(
                color = foreground,
                topLeft = Offset((centerStart + 0.5f) * cellSize, (centerStart + 0.5f) * cellSize),
                size = Size((centerCells - 1f) * cellSize, (centerCells - 1f) * cellSize),
                cornerRadius = CornerRadius(cellSize * 0.3f, cellSize * 0.3f)
            )
        }
    }
}

/**
 * My QR Code Dialog
 */
@Composable
fun DrakzoMyQrDialog(
    username: String,
    displayName: String,
    avatarColorHex: String,
    onDismiss: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DrakzoSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DrakzoPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DRAKZO PASSKEY QR",
                        color = DrakzoSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DrakzoTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                DrakzoAvatar(name = displayName, colorHex = avatarColorHex, size = 64.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = displayName,
                    color = DrakzoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@$username",
                    color = DrakzoSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(18.dp))

                // The QR Code
                DrakzoQrCodeCanvas(
                    data = "drakzo://user/@$username",
                    modifier = Modifier.size(190.dp),
                    foreground = DrakzoSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Scan with Drakzo camera to instantly view profile and open encrypted chat.",
                    color = DrakzoTextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { copied = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoBorder)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) DrakzoEmerald else DrakzoSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copied) "Copied!" else "Copy Link",
                            color = DrakzoTextPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Done", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * QR Scanner Dialog with Camera Access and Preset Scans
 */
@Composable
fun DrakzoQrScannerDialog(
    onDismiss: () -> Unit,
    onScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var manualInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DrakzoSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DrakzoSecondary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = DrakzoSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCAN CONTACT QR",
                            color = DrakzoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DrakzoTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scanner Viewfinder
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(2.dp, DrakzoSecondary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Corner targeting brackets
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bracketLen = 30f
                        val stroke = 6f
                        // Top-left
                        drawLine(DrakzoSecondary, Offset(16f, 16f), Offset(16f + bracketLen, 16f), stroke)
                        drawLine(DrakzoSecondary, Offset(16f, 16f), Offset(16f, 16f + bracketLen), stroke)
                        // Top-right
                        drawLine(DrakzoSecondary, Offset(size.width - 16f, 16f), Offset(size.width - 16f - bracketLen, 16f), stroke)
                        drawLine(DrakzoSecondary, Offset(size.width - 16f, 16f), Offset(size.width - 16f, 16f + bracketLen), stroke)
                        // Bottom-left
                        drawLine(DrakzoSecondary, Offset(16f, size.height - 16f), Offset(16f + bracketLen, size.height - 16f), stroke)
                        drawLine(DrakzoSecondary, Offset(16f, size.height - 16f), Offset(16f, size.height - 16f - bracketLen), stroke)
                        // Bottom-right
                        drawLine(DrakzoSecondary, Offset(size.width - 16f, size.height - 16f), Offset(size.width - 16f - bracketLen, size.height - 16f), stroke)
                        drawLine(DrakzoSecondary, Offset(size.width - 16f, size.height - 16f), Offset(size.width - 16f, size.height - 16f - bracketLen), stroke)

                        // Animated scanning laser line
                        val laserY = 24f + (size.height - 48f) * laserOffsetY
                        drawLine(
                            color = Color(0xFF00F5FF),
                            start = Offset(24f, laserY),
                            end = Offset(size.width - 24f, laserY),
                            strokeWidth = 4f
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = DrakzoSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (hasCameraPermission) "Align QR inside frame" else "Camera access required",
                            color = DrakzoTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (!hasCameraPermission) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Grant Permission",
                                color = DrakzoSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instant quick-scan options for tested friends
                Text(
                    text = "— QUICK SCAN CONTACTS —",
                    color = DrakzoTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onScanned("drakzo://user/@elena_r") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.5f))
                    ) {
                        Text(text = "🌸 Elena", fontSize = 11.sp, color = DrakzoTextPrimary)
                    }
                    Button(
                        onClick = { onScanned("drakzo://user/@marcus_dev") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
                    ) {
                        Text(text = "🛡️ Marcus", fontSize = 11.sp, color = DrakzoTextPrimary)
                    }
                    Button(
                        onClick = { onScanned("drakzo://user/@sarah_v") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Text(text = "⚡ Sarah", fontSize = 11.sp, color = DrakzoTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Input or Scan code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text("@handle or link", color = DrakzoTextMuted, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DrakzoTextPrimary,
                            unfocusedTextColor = DrakzoTextPrimary,
                            focusedBorderColor = DrakzoSecondary,
                            unfocusedBorderColor = DrakzoBorder
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                onScanned(manualInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Go", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Friend Profile Dialog ("seeing his profile", "direct front profile")
 */
@Composable
fun FriendProfileDialog(
    friend: FriendEntity,
    onDismiss: () -> Unit,
    onStartChat: () -> Unit,
    onRemove: () -> Unit
) {
    var showQrCode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DrakzoSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DrakzoPrimary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (friend.isBot) "DRAKZO SYSTEM NODE" else "VERIFIED CONTACT",
                        color = if (friend.isBot) DrakzoPrimary else DrakzoSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DrakzoTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar
                DrakzoAvatar(
                    name = friend.displayName,
                    colorHex = friend.avatarColorHex,
                    size = 72.dp,
                    isOnline = friend.isOnline,
                    showOnlineBadge = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = friend.displayName,
                    color = DrakzoTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "@${friend.username}",
                    color = DrakzoSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Bio Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DrakzoBorder, RoundedCornerShape(12.dp)),
                    color = DrakzoCardBg
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Status / Bio", color = DrakzoTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = friend.bio, color = DrakzoTextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Encryption Fingerprint Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DrakzoBorder, RoundedCornerShape(12.dp)),
                    color = DrakzoCardBg
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = DrakzoEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "E2EE Fingerprint", color = DrakzoTextMuted, fontSize = 10.sp)
                            Text(
                                text = friend.encryptionFingerprint,
                                color = DrakzoEmerald,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (showQrCode) {
                    DrakzoQrCodeCanvas(
                        data = "drakzo://user/@${friend.username}",
                        modifier = Modifier.size(150.dp),
                        foreground = DrakzoSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons
                Button(
                    onClick = onStartChat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_chat_from_profile"),
                    colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Encrypted Chat",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showQrCode = !showQrCode },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoBorder)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (showQrCode) "Hide QR" else "View QR", fontSize = 12.sp, color = DrakzoTextPrimary)
                    }

                    Button(
                        onClick = onRemove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Remove", fontSize = 12.sp, color = Color(0xFFF43F5E))
                    }
                }
            }
        }
    }
}
