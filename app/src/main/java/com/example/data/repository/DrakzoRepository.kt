package com.example.data.repository

import com.example.data.local.DrakzoDao
import com.example.data.model.AdminReportEntity
import com.example.data.model.ChatEntity
import com.example.data.model.FriendEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusStoryEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DrakzoRepository(private val dao: DrakzoDao) {

    val activeSessionFlow: Flow<UserSessionEntity?> = dao.getActiveSessionFlow()
    val allChatsFlow: Flow<List<ChatEntity>> = dao.getAllChatsFlow()
    val allFriendsFlow: Flow<List<FriendEntity>> = dao.getAllFriendsFlow()
    val allStatusStoriesFlow: Flow<List<StatusStoryEntity>> = dao.getAllStatusStoriesFlow()
    val allAdminReportsFlow: Flow<List<AdminReportEntity>> = dao.getAllAdminReportsFlow()

    suspend fun getActiveSession(): UserSessionEntity? = dao.getActiveSession()

    suspend fun createSecureSession(
        username: String,
        email: String,
        displayName: String,
        phoneNumber: String = "",
        avatarColorHex: String = "#8B5CF6",
        statusBio: String = "Active on Drakzo Matrix"
    ): UserSessionEntity {
        dao.deactivateAllSessions()

        val token = "DKZ-SEC-" + UUID.randomUUID().toString().take(12).uppercase()
        val deviceId = "NODE-" + UUID.randomUUID().toString().take(8).uppercase()
        val fingerprint = "E2EE:SHA256:" + UUID.randomUUID().toString().replace("-", "").take(20).uppercase()

        val session = UserSessionEntity(
            sessionToken = token,
            userId = "usr_" + username.lowercase().trim(),
            username = username.lowercase().trim(),
            displayName = displayName.ifBlank { username },
            email = email,
            phoneNumber = phoneNumber,
            avatarColorHex = avatarColorHex,
            statusBio = statusBio,
            deviceId = deviceId,
            encryptionFingerprint = fingerprint,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        dao.insertSession(session)
        seedInitialDataIfEmpty()
        ensurePersonalVaultForUser(session)
        return session
    }

    suspend fun ensurePersonalVaultForUser(session: UserSessionEntity) {
        val vaultChatId = "vault_" + session.username.lowercase().trim()
        val existing = dao.getChatById(vaultChatId)
        if (existing == null) {
            val vaultChat = ChatEntity(
                chatId = vaultChatId,
                title = "🔐 ${session.displayName}'s Private Vault",
                isGroup = false,
                participantNames = "Personal Cloud Storage (Photos, Videos, Numbers)",
                avatarColorHex = session.avatarColorHex,
                lastMessage = "🔐 Cloud Vault ready. Save personal photos, videos, numbers & chats.",
                lastMessageTime = System.currentTimeMillis(),
                lastMessageType = "TEXT",
                unreadCount = 0,
                isPinned = true
            )
            dao.insertChat(vaultChat)

            val now = System.currentTimeMillis()
            val welcomeMessages = listOf(
                MessageEntity(
                    messageId = "vmsg_${UUID.randomUUID()}",
                    chatId = vaultChatId,
                    senderId = session.userId,
                    senderName = session.displayName,
                    content = "🔐 Welcome ${session.displayName}! Your personal cloud vault keeps your photos, videos, chats, and emergency phone numbers separately encrypted and stored.",
                    type = "TEXT",
                    timestamp = now - 180_000,
                    status = "READ"
                ),
                MessageEntity(
                    messageId = "vmsg_${UUID.randomUUID()}",
                    chatId = vaultChatId,
                    senderId = session.userId,
                    senderName = session.displayName,
                    content = "📸 Personal Vault Photo Backup",
                    type = "PHOTO",
                    attachmentUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                    timestamp = now - 120_000,
                    status = "READ"
                ),
                MessageEntity(
                    messageId = "vmsg_${UUID.randomUUID()}",
                    chatId = vaultChatId,
                    senderId = session.userId,
                    senderName = session.displayName,
                    content = "📞 Emergency Contact & Admin Support:\nAdmin Reporting: drakzooapk@gmail.com\nRegistered Node: ${if (session.phoneNumber.isNotBlank()) session.phoneNumber else session.email}",
                    type = "TEXT",
                    timestamp = now - 60_000,
                    status = "READ"
                )
            )
            dao.insertMessages(welcomeMessages)
        }
    }

    suspend fun ensureInitialDataSeeded() {
        seedInitialDataIfEmpty()
    }

    suspend fun recordAdminDispatch(
        account: String,
        otpCode: String,
        type: String = "ACCOUNT_CREATED_OTP",
        details: String = ""
    ) {
        val report = AdminReportEntity(
            reportId = "adm_" + UUID.randomUUID().toString().take(8),
            senderAccount = account,
            reportType = type,
            title = if (type == "ACCOUNT_CREATED_OTP") "New Account Created with OTP" else "Auth OTP Dispatched",
            description = "Account [$account] processed OTP passkey [$otpCode]. Admin node record dispatched to drakzooapk@gmail.com. $details",
            adminRecipient = "drakzooapk@gmail.com",
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED_TO_ADMIN"
        )
        dao.insertAdminReport(report)
    }

    suspend fun recordBugReport(
        account: String,
        category: String,
        title: String,
        description: String
    ): AdminReportEntity {
        val report = AdminReportEntity(
            reportId = "bug_" + UUID.randomUUID().toString().take(8),
            senderAccount = account,
            reportType = "BUG_REPORT",
            title = "[$category] $title",
            description = description,
            adminRecipient = "drakzooapk@gmail.com",
            timestamp = System.currentTimeMillis(),
            status = "SUBMITTED_TO_ADMIN"
        )
        dao.insertAdminReport(report)
        return report
    }

    suspend fun updateProfile(displayName: String, statusBio: String, avatarColorHex: String) {
        val current = dao.getActiveSession() ?: return
        val updated = current.copy(
            displayName = displayName,
            statusBio = statusBio,
            avatarColorHex = avatarColorHex
        )
        dao.updateSession(updated)
    }

    suspend fun clearActiveSession() {
        dao.clearSessions()
    }

    // --- Chat Operations ---
    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?> = dao.getChatByIdFlow(chatId)

    suspend fun getChatById(chatId: String): ChatEntity? = dao.getChatById(chatId)

    suspend fun clearUnread(chatId: String) = dao.clearUnread(chatId)

    suspend fun createDirectChat(friend: FriendEntity): ChatEntity {
        val targetChatId = when (friend.friendId) {
            "frd_elena" -> "chat_elena"
            "frd_marcus" -> "chat_marcus"
            "frd_sarah" -> "chat_sarah"
            "frd_bot" -> "chat_bot"
            else -> "chat_direct_${friend.friendId}"
        }
        val existing = dao.getChatById(targetChatId)
        if (existing != null) return existing

        val chat = ChatEntity(
            chatId = targetChatId,
            title = friend.displayName,
            isGroup = false,
            participantNames = friend.displayName,
            avatarColorHex = friend.avatarColorHex,
            lastMessage = "Started secure conversation",
            lastMessageTime = System.currentTimeMillis(),
            lastMessageType = "TEXT",
            unreadCount = 0
        )
        dao.insertChat(chat)
        return chat
    }

    suspend fun createGroupChat(title: String, participantNames: String, colorHex: String = "#EC4899"): ChatEntity {
        val chatId = "group_" + UUID.randomUUID().toString().take(8)
        val chat = ChatEntity(
            chatId = chatId,
            title = title,
            isGroup = true,
            participantNames = participantNames,
            avatarColorHex = colorHex,
            lastMessage = "Group created",
            lastMessageTime = System.currentTimeMillis(),
            lastMessageType = "TEXT",
            unreadCount = 0
        )
        dao.insertChat(chat)
        return chat
    }

    suspend fun deleteChat(chatId: String) {
        dao.deleteMessagesForChat(chatId)
        dao.deleteChatById(chatId)
    }

    // --- Message Operations ---
    fun getMessagesForChatFlow(chatId: String): Flow<List<MessageEntity>> =
        dao.getMessagesForChatFlow(chatId)

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        content: String,
        type: String = "TEXT",
        attachmentUrl: String? = null,
        attachmentName: String? = null,
        voiceDurationSec: Int = 0
    ): MessageEntity {
        val msg = MessageEntity(
            messageId = "msg_" + UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            type = type,
            attachmentUrl = attachmentUrl,
            attachmentName = attachmentName,
            voiceDurationSec = voiceDurationSec,
            timestamp = System.currentTimeMillis(),
            status = "SYNCED"
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(
            chatId = chatId,
            lastMsg = when (type) {
                "PHOTO" -> "📷 Photo"
                "FILE" -> "📎 Document: " + (attachmentName ?: "file")
                "VOICE" -> "🎤 Voice message (${voiceDurationSec}s)"
                "EMOJI" -> content
                else -> content
            },
            time = msg.timestamp,
            type = type,
            incUnread = 0
        )
        return msg
    }

    suspend fun receiveMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        content: String,
        type: String = "TEXT",
        attachmentUrl: String? = null,
        attachmentName: String? = null,
        voiceDurationSec: Int = 0,
        isCurrentChatActive: Boolean = false
    ): MessageEntity {
        val msg = MessageEntity(
            messageId = "msg_" + UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            type = type,
            attachmentUrl = attachmentUrl,
            attachmentName = attachmentName,
            voiceDurationSec = voiceDurationSec,
            timestamp = System.currentTimeMillis(),
            status = "READ"
        )
        dao.insertMessage(msg)
        dao.updateLastMessage(
            chatId = chatId,
            lastMsg = when (type) {
                "PHOTO" -> "📷 Photo"
                "FILE" -> "📎 Document: " + (attachmentName ?: "file")
                "VOICE" -> "🎤 Voice message (${voiceDurationSec}s)"
                "EMOJI" -> content
                else -> content
            },
            time = msg.timestamp,
            type = type,
            incUnread = if (isCurrentChatActive) 0 else 1
        )
        return msg
    }

    suspend fun updateMessageReaction(messageId: String, reaction: String?) {
        dao.updateMessageReaction(messageId, reaction)
    }

    suspend fun deleteMessage(messageId: String) {
        dao.deleteMessageById(messageId)
    }

    // --- Friends Operations ---
    suspend fun addFriend(
        username: String,
        displayName: String,
        bio: String = "Drakzo Member",
        avatarColorHex: String = "#06B6D4"
    ): FriendEntity {
        val cleanUser = username.lowercase().replace("@", "").trim()
        val friend = FriendEntity(
            friendId = "frd_$cleanUser",
            username = cleanUser,
            displayName = displayName.ifBlank { "@$cleanUser" },
            bio = bio,
            avatarColorHex = avatarColorHex,
            isOnline = true,
            lastSeenText = "Online now",
            isBot = false,
            encryptionFingerprint = "SHA256:" + UUID.randomUUID().toString().take(12).uppercase(),
            addedTimestamp = System.currentTimeMillis()
        )
        dao.insertFriend(friend)
        return friend
    }

    suspend fun removeFriend(friendId: String) {
        dao.deleteFriendById(friendId)
    }

    suspend fun getFriendById(friendId: String): FriendEntity? = dao.getFriendById(friendId)

    suspend fun getFriendByUsername(username: String): FriendEntity? {
        val clean = username.lowercase().replace("@", "").trim()
        return dao.getFriendByUsername(clean)
    }

    // --- Status Story Operations ---
    suspend fun postStatus(
        textContent: String,
        mediaUrl: String? = null,
        bgGradientIndex: Int = 0,
        privacy: String = "PUBLIC"
    ): StatusStoryEntity {
        val session = dao.getActiveSession()
        val story = StatusStoryEntity(
            statusId = "status_" + UUID.randomUUID().toString().take(8),
            userId = session?.userId ?: "self",
            userName = session?.displayName ?: "Me",
            userAvatarColor = session?.avatarColorHex ?: "#8B5CF6",
            isSelf = true,
            textContent = textContent,
            mediaUrl = mediaUrl,
            bgGradientIndex = bgGradientIndex,
            privacy = privacy,
            timestamp = System.currentTimeMillis(),
            viewCount = 0,
            isViewed = true
        )
        dao.insertStatusStory(story)
        return story
    }

    suspend fun deleteStatus(statusId: String) {
        dao.deleteStatusStory(statusId)
    }

    suspend fun markStatusViewed(statusId: String) {
        dao.markStatusViewed(statusId)
    }

    // --- Initial Seed Data ---
    private suspend fun seedInitialDataIfEmpty() {
        // Only seed if chats are empty
        val existing = dao.getChatById("chat_elena")
        if (existing != null) return

        val friends = listOf(
            FriendEntity(
                friendId = "frd_elena",
                username = "elena_r",
                displayName = "Elena Rostova",
                bio = "Cryptographer & UI Designer ⚡",
                avatarColorHex = "#EC4899",
                isOnline = true,
                lastSeenText = "Online now",
                isBot = false,
                encryptionFingerprint = "SHA256:7B8F..E92A"
            ),
            FriendEntity(
                friendId = "frd_marcus",
                username = "marcus_dev",
                displayName = "Marcus Chen",
                bio = "Android Systems Architect 🛡️",
                avatarColorHex = "#3B82F6",
                isOnline = true,
                lastSeenText = "Online now",
                isBot = false,
                encryptionFingerprint = "SHA256:3C1D..AA48"
            ),
            FriendEntity(
                friendId = "frd_sarah",
                username = "sarah_v",
                displayName = "Sarah Vance",
                bio = "SecOps Lead at Drakzo Core",
                avatarColorHex = "#10B981",
                isOnline = true,
                lastSeenText = "Online now",
                isBot = false,
                encryptionFingerprint = "SHA256:9E4A..F510"
            ),
            FriendEntity(
                friendId = "frd_bot",
                username = "drakzo_ai",
                displayName = "DRAKZO Node Assistant",
                bio = "Automated Protocol Responder 🤖",
                avatarColorHex = "#8B5CF6",
                isOnline = true,
                lastSeenText = "Active 24/7",
                isBot = true,
                encryptionFingerprint = "SHA256:NODE..BOT0"
            )
        )
        dao.insertFriends(friends)

        val chats = listOf(
            ChatEntity(
                chatId = "chat_saved_vault",
                title = "Saved Messages & Cloud Vault",
                isGroup = false,
                participantNames = "Personal Storage Vault",
                avatarColorHex = "#F59E0B",
                lastMessage = "🔐 Cloud Vault: Store photos, videos, notes & numbers separately",
                lastMessageTime = System.currentTimeMillis() - 30_000,
                lastMessageType = "TEXT",
                unreadCount = 0,
                isPinned = true
            ),
            ChatEntity(
                chatId = "chat_elena",
                title = "Elena Rostova",
                isGroup = false,
                participantNames = "Elena Rostova",
                avatarColorHex = "#EC4899",
                lastMessage = "Did you test the new secure voice compression? 🎙️",
                lastMessageTime = System.currentTimeMillis() - 120_000,
                lastMessageType = "TEXT",
                unreadCount = 1,
                isPinned = true
            ),
            ChatEntity(
                chatId = "group_core",
                title = "⚡ Drakzo Core Team",
                isGroup = true,
                participantNames = "Alex, Elena, Marcus, Sarah",
                avatarColorHex = "#8B5CF6",
                lastMessage = "Marcus: Encrypted socket handshake confirmed.",
                lastMessageTime = System.currentTimeMillis() - 900_000,
                lastMessageType = "TEXT",
                unreadCount = 0,
                isPinned = true
            ),
            ChatEntity(
                chatId = "chat_marcus",
                title = "Marcus Chen",
                isGroup = false,
                participantNames = "Marcus Chen",
                avatarColorHex = "#3B82F6",
                lastMessage = "120Hz display refresh rate synced smoothly.",
                lastMessageTime = System.currentTimeMillis() - 1_800_000,
                lastMessageType = "TEXT",
                unreadCount = 1
            ),
            ChatEntity(
                chatId = "chat_sarah",
                title = "Sarah Vance",
                isGroup = false,
                participantNames = "Sarah Vance",
                avatarColorHex = "#10B981",
                lastMessage = "E2EE security audit passed 100%.",
                lastMessageTime = System.currentTimeMillis() - 3_600_000,
                lastMessageType = "TEXT",
                unreadCount = 0
            ),
            ChatEntity(
                chatId = "chat_bot",
                title = "DRAKZO Node Assistant",
                isGroup = false,
                participantNames = "DRAKZO Bot",
                avatarColorHex = "#06B6D4",
                lastMessage = "System integrity 100%. Node ready.",
                lastMessageTime = System.currentTimeMillis() - 86_400_000,
                lastMessageType = "TEXT",
                unreadCount = 0
            )
        )
        dao.insertChats(chats)

        // Seed initial messages for Elena
        val now = System.currentTimeMillis()
        val elenaMessages = listOf(
            MessageEntity(
                messageId = "m1",
                chatId = "chat_elena",
                senderId = "frd_elena",
                senderName = "Elena Rostova",
                content = "Hey Alex! The Drakzo single-stream algorithm is running flawlessly.",
                type = "TEXT",
                timestamp = now - 600_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m2",
                chatId = "chat_elena",
                senderId = "self",
                senderName = "Alex",
                content = "Awesome Elena! Clean session verification and Room database are completely synced.",
                type = "TEXT",
                timestamp = now - 400_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m3",
                chatId = "chat_elena",
                senderId = "frd_elena",
                senderName = "Elena Rostova",
                content = "Check out this quick design mockup for the chat waveform:",
                type = "PHOTO",
                attachmentUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                timestamp = now - 250_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m4",
                chatId = "chat_elena",
                senderId = "frd_elena",
                senderName = "Elena Rostova",
                content = "Did you test the new secure voice compression? 🎙️",
                type = "VOICE",
                voiceDurationSec = 14,
                timestamp = now - 120_000,
                status = "READ",
                reaction = "🔥"
            )
        )
        dao.insertMessages(elenaMessages)

        // Seed messages for Marcus Chen
        val marcusMessages = listOf(
            MessageEntity(
                messageId = "mm1",
                chatId = "chat_marcus",
                senderId = "frd_marcus",
                senderName = "Marcus Chen",
                content = "Hey! I tested the 120Hz display rendering on Android — silky smooth animation pacing 🚀",
                type = "TEXT",
                timestamp = now - 2_400_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "mm2",
                chatId = "chat_marcus",
                senderId = "self",
                senderName = "Alex",
                content = "Awesome Marcus! The frame rate and touch latencies feel immediate.",
                type = "TEXT",
                timestamp = now - 2_000_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "mm3",
                chatId = "chat_marcus",
                senderId = "frd_marcus",
                senderName = "Marcus Chen",
                content = "120Hz display refresh rate synced smoothly.",
                type = "TEXT",
                timestamp = now - 1_800_000,
                status = "READ",
                reaction = "⚡"
            )
        )
        dao.insertMessages(marcusMessages)

        // Seed messages for Sarah Vance
        val sarahMessages = listOf(
            MessageEntity(
                messageId = "sm1",
                chatId = "chat_sarah",
                senderId = "frd_sarah",
                senderName = "Sarah Vance",
                content = "Zero packet leaks on the E2EE encryption audit. Ready for global mesh connection!",
                type = "TEXT",
                timestamp = now - 4_000_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "sm2",
                chatId = "chat_sarah",
                senderId = "frd_sarah",
                senderName = "Sarah Vance",
                content = "E2EE security audit passed 100%.",
                type = "TEXT",
                timestamp = now - 3_600_000,
                status = "READ"
            )
        )
        dao.insertMessages(sarahMessages)

        // Seed group messages
        val groupMessages = listOf(
            MessageEntity(
                messageId = "gm1",
                chatId = "group_core",
                senderId = "frd_sarah",
                senderName = "Sarah Vance",
                content = "Broadcasting security keys to all cluster members...",
                type = "TEXT",
                timestamp = now - 3_600_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "gm2",
                chatId = "group_core",
                senderId = "frd_marcus",
                senderName = "Marcus Chen",
                content = "Handshake verified. Zero packet leakage.",
                type = "TEXT",
                timestamp = now - 1_800_000,
                status = "READ",
                reaction = "⚡"
            )
        )
        dao.insertMessages(groupMessages)

        // Seed personal Saved Messages Cloud Vault (separate photos, videos, notes, numbers)
        val vaultMessages = listOf(
            MessageEntity(
                messageId = "vm1",
                chatId = "chat_saved_vault",
                senderId = "self",
                senderName = "My Vault",
                content = "🔐 Welcome to your Private Cloud Vault! Your personal photos, videos, voice memos, documents, and phone numbers are kept completely separate and encrypted here.",
                type = "TEXT",
                timestamp = now - 180_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "vm2",
                chatId = "chat_saved_vault",
                senderId = "self",
                senderName = "My Vault",
                content = "📸 Personal Vault Photo Backup:",
                type = "PHOTO",
                attachmentUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                timestamp = now - 120_000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "vm3",
                chatId = "chat_saved_vault",
                senderId = "self",
                senderName = "My Vault",
                content = "📞 Emergency Node Contacts:\nAdmin Support: drakzooapk@gmail.com\nSecondary Node: +1 (555) 839-2041",
                type = "TEXT",
                timestamp = now - 60_000,
                status = "READ"
            )
        )
        dao.insertMessages(vaultMessages)

        // Seed initial Status Stories
        val initialStories = listOf(
            StatusStoryEntity(
                statusId = "story_elena",
                userId = "frd_elena",
                userName = "Elena Rostova",
                userAvatarColor = "#EC4899",
                isSelf = false,
                textContent = "🚀 Quantum UI alignment & visual cryptography complete! #Drakzo",
                mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                bgGradientIndex = 0,
                privacy = "PUBLIC",
                timestamp = now - 1_200_000,
                viewCount = 14,
                isViewed = false
            ),
            StatusStoryEntity(
                statusId = "story_marcus",
                userId = "frd_marcus",
                userName = "Marcus Chen",
                userAvatarColor = "#3B82F6",
                isSelf = false,
                textContent = "🛡️ 120Hz refresh rate display pipeline configured and locked. Zero stutter across all scrolls.",
                mediaUrl = null,
                bgGradientIndex = 1,
                privacy = "PUBLIC",
                timestamp = now - 3_600_000,
                viewCount = 28,
                isViewed = false
            ),
            StatusStoryEntity(
                statusId = "story_sarah",
                userId = "frd_sarah",
                userName = "Sarah Vance",
                userAvatarColor = "#10B981",
                isSelf = false,
                textContent = "⚡ SecOps cluster heartbeat 100% nominal. Mesh encryption active across all nodes.",
                mediaUrl = null,
                bgGradientIndex = 2,
                privacy = "FRIENDS",
                timestamp = now - 7_200_000,
                viewCount = 9,
                isViewed = false
            )
        )
        dao.insertStatusStories(initialStories)
    }
}
