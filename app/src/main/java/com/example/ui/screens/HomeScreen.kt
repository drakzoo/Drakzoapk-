package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.DrakzoQrScannerDialog
import com.example.ui.components.FriendProfileDialog
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.theme.DrakzoBg
import com.example.ui.theme.DrakzoCardBg
import com.example.ui.theme.DrakzoEmerald
import com.example.ui.theme.DrakzoPrimary
import com.example.ui.theme.DrakzoSecondary
import com.example.ui.theme.DrakzoSurface
import com.example.ui.theme.DrakzoTextMuted
import com.example.ui.theme.DrakzoTextPrimary
import com.example.ui.theme.DrakzoTextSecondary
import com.example.ui.viewmodel.DrakzoViewModel
import com.example.ui.viewmodel.HomeTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DrakzoViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val session by viewModel.activeSession.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val stories by viewModel.statusStories.collectAsState()
    val bannerNotification by viewModel.bannerNotification.collectAsState()
    val selectedProfile by viewModel.selectedFriendProfile.collectAsState()
    val showScanner by viewModel.showQrScanner.collectAsState()
    val showMyQr by viewModel.showMyQrCode.collectAsState()

    val totalUnread = chats.sumOf { it.unreadCount }
    val unreadStories = stories.count { !it.isViewed && !it.isSelf }

    Box(modifier = modifier.fillMaxSize().background(DrakzoBg)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DrakzoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DRAKZO",
                                    color = DrakzoTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = when (currentTab) {
                                        HomeTab.CHATS -> "Encrypted Chats"
                                        HomeTab.STATUS -> "Encrypted Stories"
                                        HomeTab.FRIENDS -> "Network Friends"
                                        HomeTab.PROFILE -> "Node Identity"
                                        HomeTab.SETTINGS -> "Security & Preferences"
                                    },
                                    color = DrakzoTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // QR Scanner Quick Launch
                        IconButton(
                            onClick = { viewModel.openQrScanner() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = DrakzoSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // E2EE Status Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DrakzoCardBg)
                                .border(1.dp, DrakzoEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DrakzoEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "E2EE",
                                color = DrakzoEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // User Avatar
                        DrakzoAvatar(
                            name = session?.displayName ?: "User",
                            colorHex = session?.avatarColorHex ?: "#8B5CF6",
                            size = 34.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DrakzoSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DrakzoSurface,
                    contentColor = DrakzoTextPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == HomeTab.CHATS,
                        onClick = { viewModel.setTab(HomeTab.CHATS) },
                        icon = {
                            if (totalUnread > 0) {
                                BadgedBox(badge = {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(DrakzoSecondary)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = totalUnread.toString(),
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }) {
                                    Icon(Icons.Default.Chat, contentDescription = "Chats")
                                }
                            } else {
                                Icon(Icons.Default.Chat, contentDescription = "Chats")
                            }
                        },
                        label = { Text("Chats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DrakzoSecondary,
                            indicatorColor = DrakzoPrimary,
                            unselectedIconColor = DrakzoTextMuted,
                            unselectedTextColor = DrakzoTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_chats")
                    )

                    NavigationBarItem(
                        selected = currentTab == HomeTab.STATUS,
                        onClick = { viewModel.setTab(HomeTab.STATUS) },
                        icon = {
                            if (unreadStories > 0) {
                                BadgedBox(badge = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DrakzoEmerald)
                                    )
                                }) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Status")
                                }
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Status")
                            }
                        },
                        label = { Text("Status") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DrakzoSecondary,
                            indicatorColor = DrakzoPrimary,
                            unselectedIconColor = DrakzoTextMuted,
                            unselectedTextColor = DrakzoTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_status")
                    )

                    NavigationBarItem(
                        selected = currentTab == HomeTab.FRIENDS,
                        onClick = { viewModel.setTab(HomeTab.FRIENDS) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Friends") },
                        label = { Text("Friends") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DrakzoSecondary,
                            indicatorColor = DrakzoPrimary,
                            unselectedIconColor = DrakzoTextMuted,
                            unselectedTextColor = DrakzoTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_friends")
                    )

                    NavigationBarItem(
                        selected = currentTab == HomeTab.PROFILE,
                        onClick = { viewModel.setTab(HomeTab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DrakzoSecondary,
                            indicatorColor = DrakzoPrimary,
                            unselectedIconColor = DrakzoTextMuted,
                            unselectedTextColor = DrakzoTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )

                    NavigationBarItem(
                        selected = currentTab == HomeTab.SETTINGS,
                        onClick = { viewModel.setTab(HomeTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = DrakzoSecondary,
                            indicatorColor = DrakzoPrimary,
                            unselectedIconColor = DrakzoTextMuted,
                            unselectedTextColor = DrakzoTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content_transition"
                ) { tab ->
                    when (tab) {
                        HomeTab.CHATS -> ChatListScreen(viewModel = viewModel)
                        HomeTab.STATUS -> StatusScreen(viewModel = viewModel)
                        HomeTab.FRIENDS -> FriendsScreen(viewModel = viewModel)
                        HomeTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                        HomeTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Overlay: In-App Notification Banner (Drop down at top)
        InAppNotificationBanner(
            notification = bannerNotification,
            onDismiss = { viewModel.dismissBannerNotification() },
            onTap = { chatId ->
                viewModel.dismissBannerNotification()
                viewModel.openChat(chatId)
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Overlay: Global Friend Profile Dialog
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

        // Overlay: Global My QR Code Dialog
        if (showMyQr) {
            DrakzoMyQrDialog(
                username = session?.username ?: "alex_drakzo",
                displayName = session?.displayName ?: "Alex Drakzo",
                avatarColorHex = session?.avatarColorHex ?: "#8B5CF6",
                onDismiss = { viewModel.closeMyQrCode() }
            )
        }

        // Overlay: Global QR Code Camera Scanner Dialog
        if (showScanner) {
            DrakzoQrScannerDialog(
                onDismiss = { viewModel.closeQrScanner() },
                onScanned = { rawCode ->
                    viewModel.handleScannedQr(rawCode)
                }
            )
        }
    }
}
