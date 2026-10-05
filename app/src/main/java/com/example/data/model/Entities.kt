package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey
    val sessionToken: String,
    val userId: String,
    val username: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String = "",
    val avatarColorHex: String,
    val statusBio: String,
    val deviceId: String,
    val encryptionFingerprint: String,
    val createdAt: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val chatId: String,
    val title: String,
    val isGroup: Boolean = false,
    val participantNames: String = "",
    val avatarColorHex: String = "#8B5CF6",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageType: String = "TEXT",
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: String = "TEXT", // TEXT, PHOTO, FILE, EMOJI, VOICE
    val attachmentUrl: String? = null,
    val attachmentName: String? = null,
    val voiceDurationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SYNCED", // SENDING, SYNCED, DELIVERED, READ
    val reaction: String? = null
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey
    val friendId: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarColorHex: String = "#06B6D4",
    val isOnline: Boolean = true,
    val lastSeenText: String = "Online now",
    val isBot: Boolean = false,
    val encryptionFingerprint: String = "SHA256:7B8F..E92A",
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "status_stories")
data class StatusStoryEntity(
    @PrimaryKey
    val statusId: String,
    val userId: String,
    val userName: String,
    val userAvatarColor: String,
    val isSelf: Boolean = false,
    val textContent: String = "",
    val mediaUrl: String? = null,
    val bgGradientIndex: Int = 0,
    val privacy: String = "PUBLIC", // PUBLIC, FRIENDS, PRIVATE
    val timestamp: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val isViewed: Boolean = false
)

@Entity(tableName = "admin_reports")
data class AdminReportEntity(
    @PrimaryKey
    val reportId: String,
    val senderAccount: String,
    val reportType: String, // BUG_REPORT, ACCOUNT_CREATED_OTP, FEEDBACK
    val title: String,
    val description: String,
    val adminRecipient: String = "drakzooapk@gmail.com",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED"
)
