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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatEntity
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

@Composable
fun ChatListScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.chats.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val chatFilter by viewModel.chatFilter.collectAsState()

    var showCreateGroupDialog by remember { mutableStateOf(false) }

    val filteredChats = chats.filter { chat ->
        val matchesSearch = chat.title.contains(searchQuery, ignoreCase = true) ||
                chat.participantNames.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (chatFilter) {
            "Unread" -> chat.unreadCount > 0
            "Groups" -> chat.isGroup
            "Direct" -> !chat.isGroup
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val matchingFriends = if (searchQuery.isNotBlank()) {
        friends.filter { frd ->
            frd.displayName.contains(searchQuery, ignoreCase = true) ||
                    frd.username.contains(searchQuery, ignoreCase = true)
        }
    } else emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("chat_list_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar & Filter Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search user, contacts or messages...", color = DrakzoTextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = DrakzoSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = DrakzoTextMuted)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_user_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DrakzoTextPrimary,
                            unfocusedTextColor = DrakzoTextPrimary,
                            focusedContainerColor = DrakzoSurface,
                            unfocusedContainerColor = DrakzoSurface,
                            focusedBorderColor = DrakzoSecondary,
                            unfocusedBorderColor = DrakzoBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.openQrScanner() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DrakzoSurface)
                            .border(1.dp, DrakzoSecondary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .testTag("btn_scan_qr_from_chats")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = DrakzoSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val filters = listOf("All", "Unread", "Groups", "Direct")
                    items(filters) { f ->
                        val isSelected = chatFilter == f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) DrakzoPrimary else DrakzoSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) DrakzoPrimary else DrakzoBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.chatFilter.value = f }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("filter_tab_$f")
                        ) {
                            Text(
                                text = f,
                                color = if (isSelected) Color.White else DrakzoTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Chats & Contacts List
            if (filteredChats.isEmpty() && matchingFriends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = DrakzoTextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No chats in this view" else "No matching conversations or contacts found",
                            color = DrakzoTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Matching Contacts / Friends Section
                    if (matchingFriends.isNotEmpty()) {
                        item {
                            Text(
                                text = "CONTACTS IN NETWORK (${matchingFriends.size})",
                                color = DrakzoSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        items(matchingFriends, key = { "frd_${it.friendId}" }) { frd ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 3.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, DrakzoBorder, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.startChatWithFriend(frd) },
                                color = DrakzoSurface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DrakzoAvatar(
                                        name = frd.displayName,
                                        colorHex = frd.avatarColorHex,
                                        size = 42.dp,
                                        isOnline = frd.isOnline,
                                        showOnlineBadge = true
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = frd.displayName,
                                            color = DrakzoTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${frd.username} • ${frd.bio}",
                                            color = DrakzoTextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.startChatWithFriend(frd) },
                                        colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Chat", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            if (filteredChats.isNotEmpty()) {
                                Text(
                                    text = "CONVERSATIONS (${filteredChats.size})",
                                    color = DrakzoTextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Filtered Chats
                    items(filteredChats, key = { it.chatId }) { chat ->
                        ChatItemRow(
                            chat = chat,
                            onClick = { viewModel.openChat(chat.chatId) }
                        )
                    }
                }
            }
        }

        // FAB: Create Group or Chat
        FloatingActionButton(
            onClick = { showCreateGroupDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("fab_new_group"),
            containerColor = DrakzoPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Group, contentDescription = "New Group Chat")
        }

        if (showCreateGroupDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { title, members ->
                    viewModel.createGroup(title, members)
                    showCreateGroupDialog = false
                }
            )
        }
    }
}

@Composable
fun ChatItemRow(
    chat: ChatEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chat_item_${chat.chatId}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            DrakzoAvatar(
                name = chat.title,
                colorHex = chat.avatarColorHex,
                size = 52.dp,
                isOnline = true,
                showOnlineBadge = !chat.isGroup
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Info & snippet
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chat.title,
                            color = DrakzoTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (chat.isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = DrakzoSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Timestamp
                    Text(
                        text = formatTimestamp(chat.lastMessageTime),
                        color = if (chat.unreadCount > 0) DrakzoSecondary else DrakzoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Icon
                        when (chat.lastMessageType) {
                            "PHOTO" -> {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = DrakzoSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            "FILE" -> {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = DrakzoPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            "VOICE" -> {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = DrakzoEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }

                        Text(
                            text = chat.lastMessage,
                            color = if (chat.unreadCount > 0) DrakzoTextPrimary else DrakzoTextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DrakzoSecondary)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, members: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("Elena, Marcus, Sarah") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Group Chat", color = DrakzoTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Establish an encrypted multiparty channel over Drakzo protocol.",
                    color = DrakzoTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Group Name") },
                    placeholder = { Text("e.g. ⚡ Drakzo Vanguard") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_group_name"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it },
                    label = { Text("Members (comma separated)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_group_members"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onCreate(title, members)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoPrimary),
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_create_group")
            ) {
                Text("Create Channel")
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

fun formatTimestamp(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}
