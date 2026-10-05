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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FriendEntity
import com.example.ui.components.DrakzoAvatar
import com.example.ui.components.DrakzoMyQrDialog
import com.example.ui.components.DrakzoQrScannerDialog
import com.example.ui.components.FriendProfileDialog
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
fun FriendsScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val friends by viewModel.friends.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") } // All, Verified, Bots
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendToRemove by remember { mutableStateOf<FriendEntity?>(null) }

    val filteredFriends = friends.filter { friend ->
        val matchesSearch = friend.displayName.contains(searchQuery, ignoreCase = true) ||
                friend.username.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterType) {
            "Verified" -> !friend.isBot
            "Bots" -> friend.isBot
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DrakzoBg)
            .testTag("friends_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input & QR Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, ID or @handle...", color = DrakzoTextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = DrakzoSecondary)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_friends_input"),
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

                // Scan QR Button
                IconButton(
                    onClick = { viewModel.openQrScanner() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DrakzoSurface)
                        .border(1.dp, DrakzoSecondary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .testTag("btn_scan_qr_friends")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = DrakzoSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // My QR Code Button
                IconButton(
                    onClick = { viewModel.openMyQrCode() },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DrakzoSurface)
                        .border(1.dp, DrakzoPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .testTag("btn_my_qr_friends")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "My QR Code",
                        tint = DrakzoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Filter Tabs (All, Verified, Bots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All" to "All Contacts", "Verified" to "Verified Peers", "Bots" to "AI / System Nodes").forEach { (type, label) ->
                    val isSelected = filterType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) DrakzoSecondary else DrakzoSurface)
                            .border(1.dp, if (isSelected) DrakzoSecondary else DrakzoBorder, RoundedCornerShape(20.dp))
                            .clickable { filterType = type }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else DrakzoTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Header summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NETWORK CONTACTS (${filteredFriends.size})",
                    color = DrakzoTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${filteredFriends.count { it.isOnline }} Online",
                    color = DrakzoEmerald,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Friends List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredFriends, key = { it.friendId }) { friend ->
                    FriendItemRow(
                        friend = friend,
                        onOpenProfile = { viewModel.openFriendProfile(friend) },
                        onMessage = { viewModel.startChatWithFriend(friend) },
                        onRemove = { friendToRemove = friend }
                    )
                }
            }
        }

        // FAB: Add Friend
        FloatingActionButton(
            onClick = { showAddFriendDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("fab_add_friend"),
            containerColor = DrakzoSecondary,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend")
        }

        // Dialog: Add Friend
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onAdd = { username, name, bio ->
                    viewModel.addFriend(username, name, bio)
                    showAddFriendDialog = false
                }
            )
        }

        // Dialog: Confirm Remove Friend
        if (friendToRemove != null) {
            val target = friendToRemove!!
            AlertDialog(
                onDismissRequest = { friendToRemove = null },
                title = { Text("Remove Contact", color = DrakzoTextPrimary) },
                text = {
                    Text(
                        "Remove ${target.displayName} (@${target.username}) from your Drakzo network contact list?",
                        color = DrakzoTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeFriend(target.friendId)
                            friendToRemove = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { friendToRemove = null }) {
                        Text("Cancel", color = DrakzoTextMuted)
                    }
                },
                containerColor = DrakzoSurface
            )
        }
    }
}

@Composable
fun FriendItemRow(
    friend: FriendEntity,
    onOpenProfile: () -> Unit,
    onMessage: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DrakzoBorder, RoundedCornerShape(14.dp))
            .clickable { onOpenProfile() }
            .testTag("friend_item_${friend.username}"),
        color = DrakzoSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DrakzoAvatar(
                name = friend.displayName,
                colorHex = friend.avatarColorHex,
                size = 46.dp,
                isOnline = friend.isOnline,
                showOnlineBadge = true
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.displayName,
                        color = DrakzoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "@${friend.username}",
                        color = DrakzoSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (friend.isBot) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "[BOT]",
                            color = DrakzoPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = friend.bio,
                    color = DrakzoTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Chat Button
            IconButton(
                onClick = onMessage,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DrakzoPrimary.copy(alpha = 0.2f))
                    .testTag("btn_chat_friend_${friend.username}")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Message",
                    tint = DrakzoPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Remove Button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Friend",
                    tint = DrakzoTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAdd: (username: String, displayName: String, bio: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Drakzo Contact", color = DrakzoTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Connect with a node peer via their @handle or username.",
                    color = DrakzoTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Suggested Contacts
                Text(text = "Suggested Network Peers:", color = DrakzoTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("david_k", "David Kim", "Security Researcher 🛡️"),
                        Triple("aisha_p", "Aisha Patel", "Distributed Systems ⚡")
                    ).forEach { (u, d, b) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DrakzoCardBg)
                                .border(1.dp, DrakzoSecondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    username = u
                                    displayName = d
                                    bio = b
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            color = DrakzoCardBg
                        ) {
                            Text("+$d", color = DrakzoSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username (@handle)") },
                    placeholder = { Text("e.g. john_doe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_add_friend_username"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DrakzoTextPrimary,
                        unfocusedTextColor = DrakzoTextPrimary,
                        focusedBorderColor = DrakzoSecondary,
                        unfocusedBorderColor = DrakzoBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DrakzoTextPrimary,
                        unfocusedTextColor = DrakzoTextPrimary,
                        focusedBorderColor = DrakzoSecondary,
                        unfocusedBorderColor = DrakzoBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Status / Bio") },
                    placeholder = { Text("e.g. Mobile Developer & Crypto enthusiast") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DrakzoTextPrimary,
                        unfocusedTextColor = DrakzoTextPrimary,
                        focusedBorderColor = DrakzoSecondary,
                        unfocusedBorderColor = DrakzoBorder
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        onAdd(
                            username.trim(),
                            displayName.ifBlank { username.trim() },
                            bio.ifBlank { "Drakzo network peer" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DrakzoSecondary),
                modifier = Modifier.testTag("btn_confirm_add_friend")
            ) {
                Text("Add Contact", color = Color.Black, fontWeight = FontWeight.Bold)
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
