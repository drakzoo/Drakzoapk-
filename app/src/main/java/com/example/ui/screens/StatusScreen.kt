package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.StatusStoryEntity
import com.example.ui.components.DrakzoAvatar
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

val StatusGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF06B6D4))),
    Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF97316))),
    Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0F172A))),
    Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)))
)

@Composable
fun StatusScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val stories by viewModel.statusStories.collectAsState()
    val session by viewModel.activeSession.collectAsState()
    val activeStory by viewModel.activeStoryToView.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }

    val myStories = stories.filter { it.isSelf || it.userId == (session?.userId ?: "self") }
    val friendStories = stories.filter { !it.isSelf && it.userId != (session?.userId ?: "self") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("status_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STATUS UPDATES",
                            color = DrakzoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Disappearing encrypted stories (24h)",
                            color = DrakzoTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DrakzoCardBg)
                            .border(1.dp, DrakzoEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "E2EE Feed",
                            color = DrakzoEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // My Status Section
            item {
                Text(
                    text = "MY STATUS",
                    color = DrakzoTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DrakzoBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            if (myStories.isNotEmpty()) {
                                viewModel.openStory(myStories.first())
                            } else {
                                showUploadDialog = true
                            }
                        }
                        .testTag("card_my_status"),
                    color = DrakzoSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            DrakzoAvatar(
                                name = session?.displayName ?: "Me",
                                colorHex = session?.avatarColorHex ?: "#8B5CF6",
                                size = 52.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(DrakzoSecondary)
                                    .border(1.5.dp, DrakzoSurface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add status",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "My Status",
                                color = DrakzoTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (myStories.isNotEmpty()) {
                                    val count = myStories.first().viewCount
                                    "Tap to view • ${myStories.size} update • $count views"
                                } else {
                                    "Tap to upload encrypted photo or text status"
                                },
                                color = if (myStories.isNotEmpty()) DrakzoSecondary else DrakzoTextMuted,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { showUploadDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Upload Status",
                                tint = DrakzoSecondary
                            )
                        }
                    }
                }
            }

            // Recent Updates Section
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "FRIENDS' RECENT UPDATES (${friendStories.size})",
                    color = DrakzoTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            if (friendStories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent status updates from friends yet.",
                            color = DrakzoTextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(friendStories, key = { it.statusId }) { story ->
                    StatusStoryItemCard(
                        story = story,
                        onClick = { viewModel.openStory(story) }
                    )
                }
            }
        }

        // Floating Action Button to post status
        FloatingActionButton(
            onClick = { showUploadDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_status"),
            containerColor = DrakzoSecondary,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Status")
        }

        // Story Upload Sheet/Dialog
        if (showUploadDialog) {
            UploadStatusBottomSheet(
                onDismiss = { showUploadDialog = false },
                onPostStatus = { text, mediaUrl, gradient, privacy ->
                    viewModel.postStatus(text, mediaUrl, gradient, privacy)
                    showUploadDialog = false
                }
            )
        }

        // Active Story Viewer Overlay
        activeStory?.let { story ->
            StatusStoryViewer(
                story = story,
                isOwner = story.isSelf || story.userId == (session?.userId ?: "self"),
                onClose = { viewModel.closeStory() },
                onDelete = {
                    viewModel.deleteStatus(story.statusId)
                },
                onReplyInChat = { replyText ->
                    // Find or open friend chat
                    val friendMatch = viewModel.friends.value.find {
                        it.displayName == story.userName || it.username == story.userName || it.friendId == story.userId
                    }
                    if (friendMatch != null) {
                        viewModel.sendReplyToStory(friendMatch, story.textContent.take(30), replyText)
                    }
                    viewModel.closeStory()
                }
            )
        }
    }
}

@Composable
fun StatusStoryItemCard(
    story: StatusStoryEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (story.isViewed) DrakzoBorder else DrakzoSecondary.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        color = DrakzoSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Story Ring Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(
                        2.5.dp,
                        if (story.isViewed) DrakzoTextMuted else DrakzoSecondary,
                        CircleShape
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                DrakzoAvatar(
                    name = story.userName,
                    colorHex = story.userAvatarColor,
                    size = 46.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = story.userName,
                        color = DrakzoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (story.privacy == "PUBLIC") "🌐 Public" else "👥 Friends",
                        color = if (story.privacy == "PUBLIC") DrakzoEmerald else DrakzoSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (story.textContent.isNotBlank()) story.textContent else "Photo update",
                    color = DrakzoTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(story.timestamp)),
                    color = DrakzoTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (story.mediaUrl != null) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Photo Story",
                    tint = DrakzoSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Upload Status Modal Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadStatusBottomSheet(
    onDismiss: () -> Unit,
    onPostStatus: (text: String, mediaUrl: String?, gradientIndex: Int, privacy: String) -> Unit
) {
    var textContent by remember { mutableStateOf("") }
    var selectedMediaUrl by remember { mutableStateOf<String?>(null) }
    var gradientIndex by remember { mutableIntStateOf(0) }
    var privacy by remember { mutableStateOf("PUBLIC") } // PUBLIC, FRIENDS, PRIVATE

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DrakzoSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "New Status Update",
                color = DrakzoTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (selectedMediaUrl == null) {
                            Modifier.background(StatusGradients[gradientIndex])
                        } else {
                            Modifier.background(Color.Black)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMediaUrl != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedMediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = textContent.ifBlank { "Type status thoughts here..." },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                placeholder = { Text("What's on your mind? (End-to-End Encrypted)", color = DrakzoTextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_status_text"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DrakzoTextPrimary,
                    unfocusedTextColor = DrakzoTextPrimary,
                    focusedBorderColor = DrakzoSecondary,
                    unfocusedBorderColor = DrakzoBorder
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Background & Photo Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Background Gradient Presets
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusGradients.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(StatusGradients[idx])
                                .border(
                                    2.dp,
                                    if (gradientIndex == idx && selectedMediaUrl == null) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    gradientIndex = idx
                                    selectedMediaUrl = null
                                }
                        )
                    }
                }

                // Choose Photo button
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoSecondary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Photo", fontSize = 11.sp, color = DrakzoTextPrimary)
                    }

                    Button(
                        onClick = {
                            selectedMediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoCardBg),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DrakzoPrimary.copy(alpha = 0.5f))
                    ) {
                        Text(text = "Preset", fontSize = 11.sp, color = DrakzoTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy Selector
            Text(text = "Audience Privacy:", color = DrakzoTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("PUBLIC" to "🌐 Public", "FRIENDS" to "👥 Friends Only", "PRIVATE" to "🔒 Only Me").forEach { (key, label) ->
                    val isSelected = privacy == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DrakzoSecondary else DrakzoCardBg)
                            .clickable { privacy = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else DrakzoTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val finalContent = if (textContent.isBlank() && selectedMediaUrl == null) {
                        "⚡ Active on Drakzo Encrypted Mesh"
                    } else {
                        textContent.trim()
                    }
                    onPostStatus(finalContent, selectedMediaUrl, gradientIndex, privacy)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_publish_status"),
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Publish Status", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Fullscreen Interactive Story Viewer
 */
@Composable
fun StatusStoryViewer(
    story: StatusStoryEntity,
    isOwner: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onReplyInChat: (String) -> Unit
) {
    val progress = remember { Animatable(0f) }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(story.statusId, replyText.isNotEmpty()) {
        if (replyText.isEmpty()) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 6000, easing = LinearEasing)
            )
            onClose()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (story.mediaUrl != null) {
                        Modifier.background(Color.Black)
                    } else {
                        Modifier.background(StatusGradients[story.bgGradientIndex.coerceIn(0, 3)])
                    }
                )
        ) {
            // Story Media (if photo)
            if (story.mediaUrl != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(story.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Story visual",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DrakzoSecondary)
                        }
                    }
                )
            }

            // Text overlay / story content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (story.textContent.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        color = if (story.mediaUrl != null) Color.Black.copy(alpha = 0.65f) else Color.Transparent
                    ) {
                        Text(
                            text = story.textContent,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Top Header: Progress Bar + User details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DrakzoAvatar(
                            name = story.userName,
                            colorHex = story.userAvatarColor,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = story.userName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(story.timestamp)),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${story.privacy}",
                                    color = DrakzoSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOwner) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }

            // Bottom bar: If owner, shows view count. If peer, shows reply box.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(16.dp)
            ) {
                if (isOwner) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = DrakzoSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${story.viewCount} views",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Reply to ${story.userName}...", color = Color.White.copy(alpha = 0.6f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = DrakzoSecondary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReplyInChat(replyText)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DrakzoSecondary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
