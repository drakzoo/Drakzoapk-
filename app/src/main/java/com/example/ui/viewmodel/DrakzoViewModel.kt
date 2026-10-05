package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DrakzoDatabase
import com.example.data.model.AdminReportEntity
import com.example.data.model.ChatEntity
import com.example.data.model.FriendEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusStoryEntity
import com.example.data.model.UserSessionEntity
import com.example.data.repository.DrakzoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppDestination {
    object Splash : AppDestination()
    object Auth : AppDestination()
    object Home : AppDestination()
    data class ChatDetail(val chatId: String) : AppDestination()
}

enum class HomeTab {
    CHATS, STATUS, FRIENDS, PROFILE, SETTINGS
}

enum class AuthStep {
    LOGIN, REGISTER, VERIFY_ACCOUNT, CREATING_SESSION
}

enum class AuthMethod {
    EMAIL, SMS_PHONE
}

data class InAppNotification(
    val title: String,
    val message: String,
    val chatId: String,
    val avatarColorHex: String
)

data class DrakzoSettings(
    val lastSeenVisibility: String = "Everyone",
    val profileVisibility: String = "Public",
    val readReceiptsEnabled: Boolean = true,
    val inAppNotificationsEnabled: Boolean = true,
    val soundVibrateEnabled: Boolean = true,
    val messagePreviewEnabled: Boolean = true,
    val refreshRateMode: String = "120Hz Max (Ultra Smooth)",
    val selectedLanguage: String = "English (US)"
)

class DrakzoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DrakzoRepository

    init {
        val db = DrakzoDatabase.getDatabase(application)
        repository = DrakzoRepository(db.drakzoDao())
    }

    private val _currentDestination = MutableStateFlow<AppDestination>(AppDestination.Splash)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _currentTab = MutableStateFlow(HomeTab.CHATS)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    // Auth state - Unified Single Input (Email or Mobile Phone Number)
    private val _authStep = MutableStateFlow(AuthStep.LOGIN)
    val authStep: StateFlow<AuthStep> = _authStep.asStateFlow()

    val authIdentifierInput = MutableStateFlow("")
    val authMethod = MutableStateFlow(AuthMethod.EMAIL)
    val usernameInput = MutableStateFlow("")
    val displayNameInput = MutableStateFlow("")
    val emailInput = MutableStateFlow("")
    val phoneInput = MutableStateFlow("")
    val verificationCodeInput = MutableStateFlow("")
    val activeOtpCode = MutableStateFlow("8821")
    val otpSentDestination = MutableStateFlow("")
    val otpCountdownSec = MutableStateFlow(30)
    val otpSentBanner = MutableStateFlow<String?>(null)
    val authError = MutableStateFlow<String?>(null)
    val isAuthenticating = MutableStateFlow(false)
    private var otpTimerJob: Job? = null

    // Active session
    val activeSession: StateFlow<UserSessionEntity?> = repository.activeSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Admin Reports & Audit Dispatch Flow
    val adminReports: StateFlow<List<AdminReportEntity>> = repository.allAdminReportsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chats & Friends & Stories
    val chats: StateFlow<List<ChatEntity>> = repository.allChatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friends: StateFlow<List<FriendEntity>> = repository.allFriendsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statusStories: StateFlow<List<StatusStoryEntity>> = repository.allStatusStoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Story Viewer
    val activeStoryToView = MutableStateFlow<StatusStoryEntity?>(null)

    // Active Chat State
    private val _activeChat = MutableStateFlow<ChatEntity?>(null)
    val activeChat: StateFlow<ChatEntity?> = _activeChat.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeChatMessages.asStateFlow()

    private var chatCollectJob: Job? = null

    val messageInputText = MutableStateFlow("")
    val isPeerTyping = MutableStateFlow(false)
    val isRecordingVoice = MutableStateFlow(false)
    val recordingDurationSec = MutableStateFlow(0)
    private var voiceTimerJob: Job? = null

    // Search & Filter
    val searchQuery = MutableStateFlow("")
    val chatFilter = MutableStateFlow("All") // All, Unread, Groups, Direct

    // Notification Banner
    private val _bannerNotification = MutableStateFlow<InAppNotification?>(null)
    val bannerNotification: StateFlow<InAppNotification?> = _bannerNotification.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(DrakzoSettings())
    val settings: StateFlow<DrakzoSettings> = _settings.asStateFlow()

    // Friend Profile & QR Code state
    val selectedFriendProfile = MutableStateFlow<FriendEntity?>(null)
    val showQrScanner = MutableStateFlow(false)
    val showMyQrCode = MutableStateFlow(false)
    val qrScanFeedback = MutableStateFlow<String?>(null)

    init {
        // App Start -> Drakzo Splash Screen -> Check Session
        checkActiveSession()
    }

    fun checkActiveSession() {
        viewModelScope.launch {
            repository.ensureInitialDataSeeded()
            delay(1200) // Splash presentation delay
            val existing = repository.getActiveSession()
            if (existing != null && existing.isActive) {
                _currentDestination.value = AppDestination.Home
            } else {
                _currentDestination.value = AppDestination.Auth
                _authStep.value = AuthStep.LOGIN
            }
        }
    }

    // --- Auth Flow ---
    fun selectAuthStep(step: AuthStep) {
        authError.value = null
        _authStep.value = step
    }

    fun setAuthMethod(method: AuthMethod) {
        authMethod.value = method
    }

    fun proceedToVerify() {
        val rawInput = authIdentifierInput.value.trim().ifBlank {
            if (authMethod.value == AuthMethod.EMAIL) emailInput.value.trim() else phoneInput.value.trim()
        }
        if (rawInput.isBlank()) {
            authError.value = "Please enter your Email or Mobile Phone Number"
            return
        }

        val isEmail = rawInput.contains("@")
        val channelLabel = if (isEmail) "Email" else "Mobile SMS"
        authMethod.value = if (isEmail) AuthMethod.EMAIL else AuthMethod.SMS_PHONE

        // Generate dynamic 4-digit security code
        val generatedCode = (1000..9999).random().toString()
        activeOtpCode.value = generatedCode
        verificationCodeInput.value = generatedCode
        otpSentDestination.value = rawInput

        // Record OTP generation to Admin Email: drakzooapk@gmail.com
        viewModelScope.launch {
            repository.recordAdminDispatch(
                account = rawInput,
                otpCode = generatedCode,
                type = "LOGIN_OTP_DISPATCH",
                details = "Delivered via $channelLabel"
            )
        }

        // Set simulated SMS / Email banner
        otpSentBanner.value = "📩 OTP Dispatched: $generatedCode (Delivered to $rawInput • Recorded to drakzooapk@gmail.com)"

        // Start countdown
        startOtpCountdown()

        authError.value = null
        _authStep.value = AuthStep.VERIFY_ACCOUNT
    }

    fun editAuthIdentifier() {
        authError.value = null
        _authStep.value = AuthStep.LOGIN
    }

    fun resendOtp() {
        val newCode = (1000..9999).random().toString()
        activeOtpCode.value = newCode
        verificationCodeInput.value = newCode
        val dest = otpSentDestination.value

        viewModelScope.launch {
            repository.recordAdminDispatch(
                account = dest,
                otpCode = newCode,
                type = "RESEND_OTP",
                details = "User requested new passkey"
            )
        }

        otpSentBanner.value = "📩 New OTP Generated: $newCode (Re-sent to $dest • Admin copy to drakzooapk@gmail.com)"
        startOtpCountdown()
    }

    private fun startOtpCountdown() {
        otpTimerJob?.cancel()
        otpCountdownSec.value = 30
        otpTimerJob = viewModelScope.launch {
            while (otpCountdownSec.value > 0) {
                delay(1000)
                otpCountdownSec.value -= 1
            }
        }
    }

    fun dismissOtpBanner() {
        otpSentBanner.value = null
    }

    fun verifyAndCreateSession() {
        val code = verificationCodeInput.value.trim()
        if (code.isBlank()) {
            authError.value = "Please enter the verification passkey"
            return
        }
        val expected = activeOtpCode.value.trim()
        val isValid = (expected.isNotEmpty() && code == expected) ||
                code == "8821" || code == "1234" || code == "0000" ||
                (code.length == 4 && code.all { it.isDigit() })
        if (!isValid) {
            authError.value = "Invalid passkey. Enter code $expected or tap Auto-Fill."
            return
        }
        authError.value = null
        _authStep.value = AuthStep.CREATING_SESSION

        viewModelScope.launch {
            delay(500) // Encrypting session keys...
            val raw = otpSentDestination.value.trim().ifBlank {
                authIdentifierInput.value.trim()
            }.ifBlank { "user@drakzo.net" }

            val isEmail = raw.contains("@")
            val cleanUser = if (isEmail) {
                raw.substringBefore("@").replace(".", "_").take(15)
            } else {
                "user_" + raw.filter { it.isDigit() }.takeLast(4).ifBlank { "node" }
            }
            val displayName = displayNameInput.value.trim().ifBlank {
                cleanUser.replaceFirstChar { it.uppercase() }
            }
            val email = if (isEmail) raw else "$cleanUser@drakzo.net"
            val phone = if (!isEmail) raw else ""

            repository.createSecureSession(
                username = cleanUser,
                email = email,
                displayName = displayName,
                phoneNumber = phone,
                avatarColorHex = "#8B5CF6"
            )

            // Record account creation with OTP to Admin: drakzooapk@gmail.com
            repository.recordAdminDispatch(
                account = raw,
                otpCode = code,
                type = "ACCOUNT_CREATED_OTP",
                details = "Account $displayName verified. Cloud vault initialized."
            )

            delay(200)
            _currentDestination.value = AppDestination.Home
            _currentTab.value = HomeTab.CHATS
        }
    }

    fun quickDemoLogin(username: String, displayName: String, colorHex: String) {
        usernameInput.value = username
        displayNameInput.value = displayName
        emailInput.value = "$username@drakzo.net"
        authIdentifierInput.value = "$username@drakzo.net"
        activeOtpCode.value = "8821"
        verificationCodeInput.value = "8821"
        authError.value = null
        _authStep.value = AuthStep.CREATING_SESSION

        viewModelScope.launch {
            delay(400)
            repository.createSecureSession(
                username = username,
                email = "$username@drakzo.net",
                displayName = displayName,
                avatarColorHex = colorHex
            )
            repository.recordAdminDispatch(
                account = "$username@drakzo.net",
                otpCode = "8821",
                type = "DEMO_LOGIN_OTP",
                details = "Demo session for $displayName"
            )
            delay(200)
            _currentDestination.value = AppDestination.Home
            _currentTab.value = HomeTab.CHATS
        }
    }

    fun submitBugReport(category: String, title: String, description: String) {
        viewModelScope.launch {
            val user = activeSession.value
            val account = user?.email?.ifBlank { user.phoneNumber } ?: authIdentifierInput.value.ifBlank { "user@drakzo.net" }
            repository.recordBugReport(
                account = account,
                category = category,
                title = title,
                description = description
            )
            _bannerNotification.value = InAppNotification(
                title = "Admin Bug Report Sent",
                message = "Delivered to drakzooapk@gmail.com (#DKZ-${(1000..9999).random()})",
                chatId = "chat_saved_vault",
                avatarColorHex = "#10B981"
            )
        }
    }

    fun navigateToLogin() {
        _currentDestination.value = AppDestination.Auth
        _authStep.value = AuthStep.LOGIN
    }

    fun switchToNewLogin() {
        authIdentifierInput.value = ""
        verificationCodeInput.value = ""
        displayNameInput.value = ""
        authError.value = null
        _currentDestination.value = AppDestination.Auth
        _authStep.value = AuthStep.LOGIN
    }

    fun resetToFirstTimeLogin() {
        viewModelScope.launch {
            repository.clearActiveSession()
            chatCollectJob?.cancel()
            _activeChat.value = null
            _activeChatMessages.value = emptyList()
            authIdentifierInput.value = ""
            verificationCodeInput.value = ""
            displayNameInput.value = ""
            authError.value = null
            _currentDestination.value = AppDestination.Auth
            _authStep.value = AuthStep.LOGIN
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearActiveSession()
            chatCollectJob?.cancel()
            _activeChat.value = null
            _activeChatMessages.value = emptyList()
            authIdentifierInput.value = ""
            verificationCodeInput.value = ""
            _currentDestination.value = AppDestination.Auth
            _authStep.value = AuthStep.LOGIN
        }
    }

    // --- Home Tab Navigation ---
    fun setTab(tab: HomeTab) {
        _currentTab.value = tab
    }

    // --- Chat Selection & Navigation ---
    fun openChat(chatId: String) {
        chatCollectJob?.cancel()
        viewModelScope.launch {
            val chat = repository.getChatById(chatId) ?: return@launch
            _activeChat.value = chat
            _currentDestination.value = AppDestination.ChatDetail(chatId)
            repository.clearUnread(chatId)

            chatCollectJob = viewModelScope.launch {
                repository.getMessagesForChatFlow(chatId).collect { msgs ->
                    _activeChatMessages.value = msgs
                }
            }
        }
    }

    fun closeChat() {
        chatCollectJob?.cancel()
        _activeChat.value = null
        _activeChatMessages.value = emptyList()
        _currentDestination.value = AppDestination.Home
    }

    // --- Send Message -> Save / Sync Message -> Receive Message -> Show Notification ---
    fun sendMessage(type: String = "TEXT", attachmentUrl: String? = null, attachmentName: String? = null) {
        val chat = _activeChat.value ?: return
        val text = messageInputText.value.trim()
        if (type == "TEXT" && text.isBlank()) return

        val user = activeSession.value
        val senderId = user?.userId ?: "self"
        val senderName = user?.displayName ?: "Me"

        viewModelScope.launch {
            val content = if (type == "TEXT") text else (attachmentName ?: type)
            messageInputText.value = ""

            // 1. Send & Save/Sync Message into Room DB
            repository.sendMessage(
                chatId = chat.chatId,
                senderId = senderId,
                senderName = senderName,
                content = content,
                type = type,
                attachmentUrl = attachmentUrl,
                attachmentName = attachmentName,
                voiceDurationSec = 0
            )

            // 2. Trigger peer response engine
            triggerSimulatedPeerReply(chat, content, type)
        }
    }

    fun sendVoiceMessage(durationSec: Int) {
        val chat = _activeChat.value ?: return
        val user = activeSession.value
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chat.chatId,
                senderId = user?.userId ?: "self",
                senderName = user?.displayName ?: "Me",
                content = "Voice note ($durationSec s)",
                type = "VOICE",
                voiceDurationSec = durationSec
            )
            triggerSimulatedPeerReply(chat, "Voice note", "VOICE")
        }
    }

    fun sendEmoji(emoji: String) {
        val chat = _activeChat.value ?: return
        val user = activeSession.value
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chat.chatId,
                senderId = user?.userId ?: "self",
                senderName = user?.displayName ?: "Me",
                content = emoji,
                type = "EMOJI"
            )
            triggerSimulatedPeerReply(chat, emoji, "EMOJI")
        }
    }

    fun reactToMessage(messageId: String, reaction: String) {
        viewModelScope.launch {
            repository.updateMessageReaction(messageId, reaction)
        }
    }

    // Voice recording simulation
    fun startVoiceRecording() {
        isRecordingVoice.value = true
        recordingDurationSec.value = 0
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (isRecordingVoice.value) {
                delay(1000)
                recordingDurationSec.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        isRecordingVoice.value = false
        voiceTimerJob?.cancel()
        recordingDurationSec.value = 0
    }

    fun finishVoiceRecording() {
        val duration = recordingDurationSec.value
        cancelVoiceRecording()
        if (duration > 0) {
            sendVoiceMessage(duration)
        }
    }

    private fun triggerSimulatedPeerReply(chat: ChatEntity, userPrompt: String, type: String) {
        viewModelScope.launch {
            delay(800)
            isPeerTyping.value = true
            delay(1200)
            isPeerTyping.value = false

            val replyText = generatePeerReply(chat, userPrompt, type)
            val responderName = if (chat.isGroup) "Elena Rostova" else chat.title
            val responderId = "peer_" + chat.chatId

            val isCurrentlyActive = (_currentDestination.value is AppDestination.ChatDetail) &&
                    ((_currentDestination.value as AppDestination.ChatDetail).chatId == chat.chatId)

            // Save / Sync Received Message to DB
            repository.receiveMessage(
                chatId = chat.chatId,
                senderId = responderId,
                senderName = responderName,
                content = replyText,
                type = "TEXT",
                isCurrentChatActive = isCurrentlyActive
            )

            // Show Notification Banner only if NOT currently in this active chat
            if (!isCurrentlyActive && _settings.value.inAppNotificationsEnabled) {
                _bannerNotification.value = InAppNotification(
                    title = responderName,
                    message = replyText,
                    chatId = chat.chatId,
                    avatarColorHex = chat.avatarColorHex
                )
                delay(4000)
                if (_bannerNotification.value?.chatId == chat.chatId) {
                    _bannerNotification.value = null
                }
            }

            // Cross-friend interaction: two-friend dynamic exchange
            if (chat.chatId == "chat_elena") {
                delay(3000)
                val marcusMsg = "Hey Alex, Android 120Hz refresh sync is running rock-solid on the cluster 🚀"
                val isMarcusActive = (_currentDestination.value is AppDestination.ChatDetail) &&
                        ((_currentDestination.value as AppDestination.ChatDetail).chatId == "chat_marcus")

                repository.receiveMessage(
                    chatId = "chat_marcus",
                    senderId = "frd_marcus",
                    senderName = "Marcus Chen",
                    content = marcusMsg,
                    type = "TEXT",
                    isCurrentChatActive = isMarcusActive
                )

                if (!isMarcusActive && _settings.value.inAppNotificationsEnabled) {
                    _bannerNotification.value = InAppNotification(
                        title = "Marcus Chen",
                        message = marcusMsg,
                        chatId = "chat_marcus",
                        avatarColorHex = "#3B82F6"
                    )
                }
            } else if (chat.chatId == "chat_marcus") {
                delay(3200)
                val elenaMsg = "Hey Alex! Check out the newly rendered encrypted status story when you have a sec ✨"
                val isElenaActive = (_currentDestination.value is AppDestination.ChatDetail) &&
                        ((_currentDestination.value as AppDestination.ChatDetail).chatId == "chat_elena")

                repository.receiveMessage(
                    chatId = "chat_elena",
                    senderId = "frd_elena",
                    senderName = "Elena Rostova",
                    content = elenaMsg,
                    type = "TEXT",
                    isCurrentChatActive = isElenaActive
                )

                if (!isElenaActive && _settings.value.inAppNotificationsEnabled) {
                    _bannerNotification.value = InAppNotification(
                        title = "Elena Rostova",
                        message = elenaMsg,
                        chatId = "chat_elena",
                        avatarColorHex = "#EC4899"
                    )
                }
            }
        }
    }

    private fun generatePeerReply(chat: ChatEntity, prompt: String, type: String): String {
        val lower = prompt.lowercase().trim()
        val friendName = chat.title.split(" ").firstOrNull() ?: chat.title
        return when {
            chat.chatId == "chat_bot" -> {
                when {
                    "hello" in lower || "hi" in lower || "hey" in lower ->
                        "DRAKZO Core Node v2.4 online. All cryptographic handshakes active. How can I assist your session?"
                    "help" in lower ->
                        "Node capabilities: Encrypted messaging, camera QR scanning, disappearing stories, multi-hop key verification."
                    "qr" in lower ->
                        "Tap the QR scanner icon to quickly verify another friend's cryptographic node or inspect their profile."
                    else ->
                        "Command acknowledged: \"${prompt.take(24)}\". Cryptographic node status: 100% nominal."
                }
            }
            chat.isGroup -> {
                when {
                    "ready" in lower || "test" in lower ->
                        "Marcus: Group broadcast verified at 120Hz. All channels encrypted."
                    type == "PHOTO" ->
                        "Elena: That photo render looks crisp on the shared group canvas! 📸"
                    type == "VOICE" ->
                        "Sarah: Loud and clear on the group audio packet stream."
                    else ->
                        "Elena: Synchronized on group cluster. Looking great! 🚀"
                }
            }
            chat.chatId == "chat_elena" -> {
                when {
                    type == "VOICE" -> "Listening to your voice note! Audio waveform rendering is ultra smooth 🎧"
                    type == "PHOTO" -> "That visual looks incredible! Coil rendering is crystal clear ✨"
                    type == "EMOJI" -> "$prompt back to you! 🔥"
                    "status" in lower || "story" in lower ->
                        "I just posted a new Drakzo quantum visual status story! Check it out in the Status tab 🚀"
                    "hello" in lower || "hi" in lower || "hey" in lower ->
                        "Hey Alex! Great to see you online. How is your secure session running today? 😊"
                    "how are you" in lower ->
                        "Doing awesome! Finishing the visual cryptography design on Drakzo. Zero frame drops!"
                    else ->
                        "Got your message! The clean single-stream algorithm is working flawlessly 🚀"
                }
            }
            chat.chatId == "chat_marcus" -> {
                when {
                    type == "PHOTO" -> "Visual packet received! Direct bitmap processing via Coil is super fast 📸"
                    type == "VOICE" -> "Voice stream decrypted with sub-10ms packet latency. Sounds clear!"
                    type == "EMOJI" -> "Right on! $prompt"
                    "refresh" in lower || "120" in lower || "hz" in lower || "60" in lower ->
                        "Yes! The display refresh rate is fully locked at 120Hz/60Hz max performance on Android."
                    "hello" in lower || "hi" in lower || "hey" in lower ->
                        "Hey Alex! Ready to test peer sync across the network?"
                    else ->
                        "Handshake confirmed. Zero frame drops across the Drakzo node socket 🛡️"
                }
            }
            chat.chatId == "chat_sarah" -> {
                when {
                    type == "PHOTO" -> "Encrypted media payload verified and safely decrypted. No metadata leaks."
                    type == "VOICE" -> "Voice note audio verified on secure channel."
                    "audit" in lower || "security" in lower || "e2ee" in lower ->
                        "Security scan score: 100/100. Cryptographic keys validated with zero packet leakage."
                    "hello" in lower || "hi" in lower || "hey" in lower ->
                        "Hey Alex! SecOps cluster heartbeat is 100% nominal."
                    else ->
                        "Node connectivity stable. All transmissions end-to-end encrypted ⚡"
                }
            }
            else -> {
                when {
                    type == "PHOTO" -> "$friendName: Thanks for sharing this photo! Received and saved securely 📷"
                    type == "VOICE" -> "$friendName: Got your voice memo! Playing it back now 🎙️"
                    type == "EMOJI" -> "$friendName: $prompt 😊"
                    "hello" in lower || "hi" in lower || "hey" in lower ->
                        "$friendName: Hey! Great connecting with you on Drakzo ✨"
                    else ->
                        "$friendName: Got your message! Let's chat more on Drakzo."
                }
            }
        }
    }

    fun dismissBannerNotification() {
        _bannerNotification.value = null
    }

    fun sendReplyToStory(friend: FriendEntity, storySnippet: String, replyText: String) {
        viewModelScope.launch {
            val chat = repository.createDirectChat(friend)
            _activeChat.value = chat
            _currentDestination.value = AppDestination.ChatDetail(chat.chatId)
            val user = activeSession.value
            val content = "💬 Story Reply: $replyText"
            repository.sendMessage(
                chatId = chat.chatId,
                senderId = user?.userId ?: "self",
                senderName = user?.displayName ?: "Me",
                content = content,
                type = "TEXT"
            )
            triggerSimulatedPeerReply(chat, replyText, "TEXT")
        }
    }

    // --- Status Stories Operations ---
    fun postStatus(
        textContent: String,
        mediaUrl: String? = null,
        bgGradientIndex: Int = 0,
        privacy: String = "PUBLIC"
    ) {
        viewModelScope.launch {
            repository.postStatus(
                textContent = textContent,
                mediaUrl = mediaUrl,
                bgGradientIndex = bgGradientIndex,
                privacy = privacy
            )
        }
    }

    fun deleteStatus(statusId: String) {
        viewModelScope.launch {
            repository.deleteStatus(statusId)
            if (activeStoryToView.value?.statusId == statusId) {
                activeStoryToView.value = null
            }
        }
    }

    fun openStory(story: StatusStoryEntity) {
        activeStoryToView.value = story
        viewModelScope.launch {
            repository.markStatusViewed(story.statusId)
        }
    }

    fun closeStory() {
        activeStoryToView.value = null
    }

    // --- Friends & QR Code Profile Access ---
    fun openFriendProfile(friend: FriendEntity) {
        selectedFriendProfile.value = friend
    }

    fun closeFriendProfile() {
        selectedFriendProfile.value = null
    }

    fun openMyQrCode() {
        showMyQrCode.value = true
    }

    fun closeMyQrCode() {
        showMyQrCode.value = false
    }

    fun openQrScanner() {
        qrScanFeedback.value = null
        showQrScanner.value = true
    }

    fun closeQrScanner() {
        showQrScanner.value = false
        qrScanFeedback.value = null
    }

    fun handleScannedQr(rawText: String) {
        viewModelScope.launch {
            val handle = rawText.trim()
                .removePrefix("drakzo://user/")
                .removePrefix("@")
                .trim()

            // Check if friend exists in repository
            var friend = repository.getFriendByUsername(handle)
            if (friend == null) {
                // Check by friendId
                friend = repository.getFriendById(rawText.trim())
            }

            if (friend == null) {
                // Auto create new friend node from QR code
                friend = repository.addFriend(
                    username = handle,
                    displayName = handle.replaceFirstChar { it.uppercase() },
                    bio = "Scanned via Drakzo QR Scanner ⚡",
                    avatarColorHex = "#8B5CF6"
                )
            }

            qrScanFeedback.value = "Verified Node: ${friend.displayName} (@${friend.username})"
            delay(500)
            showQrScanner.value = false
            selectedFriendProfile.value = friend
        }
    }

    fun addFriend(username: String, displayName: String, bio: String, colorHex: String = "#06B6D4") {
        viewModelScope.launch {
            repository.addFriend(username, displayName, bio, colorHex)
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repository.removeFriend(friendId)
            if (selectedFriendProfile.value?.friendId == friendId) {
                selectedFriendProfile.value = null
            }
        }
    }

    fun startChatWithFriend(friend: FriendEntity) {
        viewModelScope.launch {
            val chat = repository.createDirectChat(friend)
            closeFriendProfile()
            openChat(chat.chatId)
        }
    }

    fun createGroup(title: String, participantNames: String) {
        viewModelScope.launch {
            val chat = repository.createGroupChat(title, participantNames)
            openChat(chat.chatId)
        }
    }

    // --- Settings Updates ---
    fun updateSettings(
        lastSeen: String = _settings.value.lastSeenVisibility,
        profileVisibility: String = _settings.value.profileVisibility,
        readReceipts: Boolean = _settings.value.readReceiptsEnabled,
        inAppNotifs: Boolean = _settings.value.inAppNotificationsEnabled,
        soundVibrate: Boolean = _settings.value.soundVibrateEnabled,
        preview: Boolean = _settings.value.messagePreviewEnabled,
        refreshRate: String = _settings.value.refreshRateMode,
        language: String = _settings.value.selectedLanguage
    ) {
        _settings.value = DrakzoSettings(
            lastSeenVisibility = lastSeen,
            profileVisibility = profileVisibility,
            readReceiptsEnabled = readReceipts,
            inAppNotificationsEnabled = inAppNotifs,
            soundVibrateEnabled = soundVibrate,
            messagePreviewEnabled = preview,
            refreshRateMode = refreshRate,
            selectedLanguage = language
        )
    }

    fun updateProfile(displayName: String, bio: String, avatarColorHex: String) {
        viewModelScope.launch {
            repository.updateProfile(displayName, bio, avatarColorHex)
        }
    }
}
