package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdminReportEntity
import com.example.data.model.ChatEntity
import com.example.data.model.FriendEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusStoryEntity
import com.example.data.model.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrakzoDao {
    // --- Session Operations ---
    @Query("SELECT * FROM user_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSessionEntity)

    @Update
    suspend fun updateSession(session: UserSessionEntity)

    @Query("UPDATE user_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()

    @Query("DELETE FROM user_sessions")
    suspend fun clearSessions()

    // --- Chat Operations ---
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllChatsFlow(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?>

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Query("UPDATE chats SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun clearUnread(chatId: String)

    @Query("UPDATE chats SET lastMessage = :lastMsg, lastMessageTime = :time, lastMessageType = :type, unreadCount = unreadCount + :incUnread WHERE chatId = :chatId")
    suspend fun updateLastMessage(chatId: String, lastMsg: String, time: Long, type: String, incUnread: Int = 0)

    // --- Message Operations ---
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET reaction = :reaction WHERE messageId = :messageId")
    suspend fun updateMessageReaction(messageId: String, reaction: String?)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    // --- Friend Operations ---
    @Query("SELECT * FROM friends ORDER BY isOnline DESC, displayName ASC")
    fun getAllFriendsFlow(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Query("DELETE FROM friends WHERE friendId = :friendId")
    suspend fun deleteFriendById(friendId: String)

    @Query("SELECT * FROM friends WHERE friendId = :friendId")
    suspend fun getFriendById(friendId: String): FriendEntity?

    @Query("SELECT * FROM friends WHERE username = :username LIMIT 1")
    suspend fun getFriendByUsername(username: String): FriendEntity?

    // --- Status Story Operations ---
    @Query("SELECT * FROM status_stories ORDER BY timestamp DESC")
    fun getAllStatusStoriesFlow(): Flow<List<StatusStoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusStory(story: StatusStoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusStories(stories: List<StatusStoryEntity>)

    @Query("DELETE FROM status_stories WHERE statusId = :statusId")
    suspend fun deleteStatusStory(statusId: String)

    @Query("UPDATE status_stories SET isViewed = 1, viewCount = viewCount + 1 WHERE statusId = :statusId")
    suspend fun markStatusViewed(statusId: String)

    // --- Admin Reports Operations ---
    @Query("SELECT * FROM admin_reports ORDER BY timestamp DESC")
    fun getAllAdminReportsFlow(): Flow<List<AdminReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminReport(report: AdminReportEntity)
}
