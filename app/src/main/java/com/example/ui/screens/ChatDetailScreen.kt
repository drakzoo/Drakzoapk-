package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.ui.components.DrakzoAvatar
import com.example.ui.components.DrakzoMyQrDialog
import com.example.ui.components.DrakzoQrScannerDialog
import com.example.ui.components.FriendProfileDialog
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.theme.DrakzoBg
import com.example.ui.theme.DrakzoBorder
import com.example.ui.theme.DrakzoBubblePeer
import com.example.ui.theme.DrakzoBubbleUser
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: ChatEntity,
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.activeChatMessages.collectAsState()
    val inputText by viewModel.messageInputText.collectAsState()
    val isTyping by viewModel.isPeerTyping.collectAsState()
    val isRecording by viewModel.isRecordingVoice.collectAsState()
    val recordingSec by viewModel.recordingDurationSec.collectAsState()
    val selectedProfile by viewModel.selectedFriendProfile.collectAsState()
    val showMyQr by viewModel.showMyQrCode.collectAsState()
    val showScanner by viewModel.showQrScanner.collectAsState()

    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendMessage(
                type = "PHOTO",
                attachmentUrl = uri.toString(),
                attachmentName = "Photo"
            )
        }
    }

    var showAttachmentSheet by remember { mutableStateOf(false) }
    var selectedMessageForReaction by remember { mutableStateOf<MessageEntity?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("chat_detail_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (!chat.isGroup) {
                                val friendMatch = viewModel.friends.value.find { it.displayName == chat.title || it.username == chat.title }
                                if (friendMatch != null) {
                                    viewModel.openFriendProfile(friendMatch)
                                }
                            }
                        }
                    ) {
                        DrakzoAvatar(
                            name = chat.title,
                            colorHex = chat.avatarColorHex,
                            size = 40.dp,
                            isOnline = true,
                            showOnlineBadge = !chat.isGroup
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chat.title,
                                color = DrakzoTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (chat.isGroup) "${chat.participantNames}" else if (isTyping) "typing..." else "E2EE Secured Node",
                                color = if (isTyping) DrakzoSecondary else DrakzoTextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeChat() },
                        modifier = Modifier.testTag("btn_back_chat")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DrakzoTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val friendMatch = viewModel.friends.value.find { it.displayName == chat.title || it.username == chat.title }
                        if (friendMatch != null) {
                            viewModel.openFriendProfile(friendMatch)
                        } else {
                            viewModel.openMyQrCode()
                        }
                    }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = DrakzoSecondary)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = DrakzoSecondary)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video", tint = DrakzoPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DrakzoSurface
                )
            )

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    MessageBubble(
                        message = msg,
                        isSelf = msg.senderId == "self" || msg.senderId.startsWith("usr_"),
                        onLongClick = { selectedMessageForReaction = msg }
                    )
                }

                if (isTyping) {
                    item {
                        TypingIndicatorBubble(senderName = chat.title)
                    }
                }
            }

            // Quick Emoji Reaction Bar (if selected)
            if (selectedMessageForReaction != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DrakzoPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    color = DrakzoSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "React:",
                            color = DrakzoTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val emojis = listOf("🔥", "❤️", "👍", "⚡", "🚀", "😂")
                        emojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedMessageForReaction?.let {
                                            viewModel.reactToMessage(it.messageId, emoji)
                                        }
                                        selectedMessageForReaction = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                        IconButton(
                            onClick = { selectedMessageForReaction = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = DrakzoTextMuted)
                        }
                    }
                }
            }

            // Voice Recording Bar or Input Bar
            if (isRecording) {
                VoiceRecordingBar(
                    durationSec = recordingSec,
                    onCancel = { viewModel.cancelVoiceRecording() },
                    onFinish = { viewModel.finishVoiceRecording() }
                )
            } else {
                ChatInputBar(
                    text = inputText,
                    onTextChange = { viewModel.messageInputText.value = it },
                    onSend = { viewModel.sendMessage() },
                    onOpenAttachments = { showAttachmentSheet = true },
                    onStartVoice = { viewModel.startVoiceRecording() }
                )
            }
        }

        // Attachments Modal Sheet: Photo / File / Emoji / Voice Message
        if (showAttachmentSheet) {
            AttachmentBottomSheet(
                onDismiss = { showAttachmentSheet = false },
                onSendPhoto = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    showAttachmentSheet = false
                },
                onSendPresetPhoto = { url ->
                    viewModel.sendMessage(
                        type = "PHOTO",
                        attachmentUrl = url,
                        attachmentName = "Cyber Visual"
                    )
                    showAttachmentSheet = false
                },
                onSendFile = {
                    viewModel.sendMessage(
                        type = "FILE",
                        attachmentName = "drakzo_protocol_keys.pdf",
                        attachmentUrl = "mock_file"
                    )
                    showAttachmentSheet = false
                },
                onSendEmoji = { emoji ->
                    viewModel.sendEmoji(emoji)
                    showAttachmentSheet = false
                }
            )
        }

        // Overlay: Friend Profile Dialog
        selectedProfile?.let { friend ->
            FriendProfileDialog(
                friend = friend,
                onDismiss = { viewModel.closeFriendProfile() },
                onStartChat = { viewModel.startChatWithFriend(friend) },
                onRemove = {
                    viewModel.removeFriend(friend.friendId)
                    viewModel.closeFriendProfile()
                }
            )
        }

        // Overlay: My QR Code Dialog
        if (showMyQr) {
            val session = viewModel.activeSession.collectAsState().value
            DrakzoMyQrDialog(
                username = session?.username ?: "alex_drakzo",
                displayName = session?.displayName ?: "Alex Drakzo",
                avatarColorHex = session?.avatarColorHex ?: "#8B5CF6",
                onDismiss = { viewModel.closeMyQrCode() }
            )
        }

        // Overlay: QR Code Camera Scanner Dialog
        if (showScanner) {
            DrakzoQrScannerDialog(
                onDismiss = { viewModel.closeQrScanner() },
                onScanned = { rawCode ->
                    viewModel.handleScannedQr(rawCode)
                }
            )
        }

        // Overlay: Cross-friend In-App Notification Banner
        val bannerNotification by viewModel.bannerNotification.collectAsState()
        InAppNotificationBanner(
            notification = bannerNotification,
            onDismiss = { viewModel.dismissBannerNotification() },
            onTap = { chatId ->
                viewModel.dismissBannerNotification()
                viewModel.openChat(chatId)
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    isSelf: Boolean,
    onLongClick: () -> Unit
) {
    val bubbleColor = if (isSelf) DrakzoBubbleUser else DrakzoBubblePeer
    val align = if (isSelf) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLongClick() },
        horizontalAlignment = align
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSelf) 16.dp else 4.dp,
                        bottomEnd = if (isSelf) 4.dp else 16.dp
                    )
                )
                .border(
                    1.dp,
                    if (isSelf) DrakzoPrimary.copy(alpha = 0.5f) else DrakzoBorder,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSelf) 16.dp else 4.dp,
                        bottomEnd = if (isSelf) 4.dp else 16.dp
                    )
                ),
            color = bubbleColor
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isSelf) {
                    Text(
                        text = message.senderName,
                        color = DrakzoSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Render based on message type: Photo, File, Emoji, Voice, Text
                when (message.type) {
                    "PHOTO" -> {
                        PhotoMessageContent(message)
                    }
                    "FILE" -> {
                        FileMessageContent(message)
                    }
                    "VOICE" -> {
                        VoiceMessageContent(message)
                    }
                    "EMOJI" -> {
                        Text(
                            text = message.content,
                            fontSize = 36.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = message.content,
                            color = DrakzoTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time & Status Tick
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        color = DrakzoTextSecondary.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (isSelf) {
                        when (message.status) {
                            "SENDING" -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color = DrakzoSecondary
                                )
                            }
                            "SYNCED" -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Synced",
                                    tint = DrakzoTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = DrakzoSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reaction badge overlay
        if (message.reaction != null) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, start = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DrakzoCardBg)
                    .border(1.dp, DrakzoPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = message.reaction, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PhotoMessageContent(message: MessageEntity) {
    Column {
        val hasUrl = !message.attachmentUrl.isNullOrBlank() &&
                (message.attachmentUrl.startsWith("http://") ||
                        message.attachmentUrl.startsWith("https://") ||
                        message.attachmentUrl.startsWith("content://") ||
                        message.attachmentUrl.startsWith("file://"))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2A1549), Color(0xFF0E3047))
                    )
                )
                .border(1.dp, DrakzoSecondary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (hasUrl) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(message.attachmentUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Encrypted Drakzo Visual",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = DrakzoSecondary
                            )
                        }
                    },
                    error = {
                        CyberPhotoPlaceholder()
                    }
                )
            } else {
                CyberPhotoPlaceholder()
            }
        }
        if (message.content.isNotBlank() && message.content != "PHOTO") {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.content,
                color = DrakzoTextPrimary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun CyberPhotoPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = DrakzoSecondary,
            modifier = Modifier.size(38.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Encrypted Drakzo Visual",
            color = DrakzoTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun FileMessageContent(message: MessageEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DrakzoCardBg)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DrakzoPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = DrakzoPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.attachmentName ?: "attachment_file.pdf",
                color = DrakzoTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "1.4 MB • SHA256 Encrypted",
                color = DrakzoTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun VoiceMessageContent(message: MessageEntity) {
    var isPlaying by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(DrakzoSecondary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Visual Waveform bars
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bars = listOf(8, 14, 22, 10, 26, 18, 12, 28, 20, 16, 24, 10, 18, 12)
            bars.forEach { height ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isPlaying) DrakzoSecondary else DrakzoTextSecondary.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "0:${message.voiceDurationSec.toString().padStart(2, '0')}",
            color = DrakzoTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TypingIndicatorBubble(senderName: String) {
    Surface(
        modifier = Modifier
            .widthIn(max = 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DrakzoBorder, RoundedCornerShape(16.dp)),
        color = DrakzoBubblePeer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 2.dp,
                color = DrakzoSecondary
            )
            Text(
                text = "typing...",
                color = DrakzoSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onOpenAttachments: () -> Unit,
    onStartVoice: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DrakzoBorder, RoundedCornerShape(0.dp)),
        color = DrakzoSurface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenAttachments,
                modifier = Modifier.testTag("btn_attachments")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attachments",
                    tint = DrakzoSecondary
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Type encrypted message...", color = DrakzoTextMuted, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_message_text"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DrakzoTextPrimary,
                    unfocusedTextColor = DrakzoTextPrimary,
                    focusedContainerColor = DrakzoCardBg,
                    unfocusedContainerColor = DrakzoCardBg,
                    focusedBorderColor = DrakzoPrimary,
                    unfocusedBorderColor = DrakzoBorder
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (text.isNotBlank()) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DrakzoPrimary)
                        .testTag("btn_send_message")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onStartVoice,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DrakzoSecondary.copy(alpha = 0.2f))
                        .testTag("btn_voice_record")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice Message",
                        tint = DrakzoSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceRecordingBar(
    durationSec: Int,
    onCancel: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(0.dp)),
        color = DrakzoSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF43F5E))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = DrakzoSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recording Voice: ${durationSec}s",
                    color = DrakzoTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = DrakzoTextMuted)
                }
                IconButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DrakzoEmerald)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Voice Message",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSendPhoto: () -> Unit,
    onSendPresetPhoto: (String) -> Unit,
    onSendFile: () -> Unit,
    onSendEmoji: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DrakzoSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Share Encrypted Media",
                color = DrakzoTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentActionItem(
                    icon = Icons.Default.Image,
                    label = "Choose Photo",
                    color = DrakzoSecondary,
                    onClick = onSendPhoto
                )
                AttachmentActionItem(
                    icon = Icons.Default.GraphicEq,
                    label = "Cyber Visual",
                    color = DrakzoEmerald,
                    onClick = {
                        onSendPresetPhoto("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80")
                    }
                )
                AttachmentActionItem(
                    icon = Icons.Default.AttachFile,
                    label = "Document",
                    color = DrakzoPrimary,
                    onClick = onSendFile
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Quick Emoji:",
                color = DrakzoTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val emojis = listOf("⚡", "🔥", "🚀", "🤖", "🛡️", "🎉")
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onSendEmoji(emoji) }
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AttachmentActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = DrakzoTextPrimary, fontSize = 13.sp)
    }
}
