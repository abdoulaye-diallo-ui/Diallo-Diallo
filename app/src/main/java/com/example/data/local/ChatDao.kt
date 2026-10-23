package com.example.data.local

import androidx.room.*
import com.example.data.model.CallRecordEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    // --- Conversations ---
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessage = :lastMsg, lastTimestamp = :timestamp WHERE id = :convoId")
    suspend fun updateConversationSnippet(convoId: String, lastMsg: String, timestamp: Long)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :convoId")
    suspend fun updatePinned(convoId: String, isPinned: Boolean)

    @Query("UPDATE conversations SET isLocked = :isLocked WHERE id = :convoId")
    suspend fun updateLocked(convoId: String, isLocked: Boolean)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :convoId")
    suspend fun clearUnread(convoId: String)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE conversationId = :convoId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convoId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET status = :status WHERE messageId = :msgId")
    suspend fun updateMessageStatus(msgId: String, status: String)

    // --- Stories ---
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryViewed(storyId: String)

    // --- Calls ---
    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallRecordEntity>)
}
