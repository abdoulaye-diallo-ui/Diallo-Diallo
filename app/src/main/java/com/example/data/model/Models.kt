package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val encryptedContent: String,
    val mediaUrl: String? = null,
    val type: String = "TEXT", // "TEXT", "IMAGE", "AUDIO", "VIDEO", "DOC", "LOCATION"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // "SENT", "DELIVERED", "READ"
    val audioDurationSec: Int = 0,
    val caption: String? = null,
    val fileName: String? = null
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerName: String,
    val peerPhoneNumber: String,
    val peerAvatarUrl: String? = null,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val isOnline: Boolean = true,
    val aboutText: String = "Discutons sur Samawakhtane !"
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String? = null,
    val mediaUrl: String? = null,
    val textContent: String? = null,
    val bgGradientIndex: Int = 0,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)

@Entity(tableName = "calls")
data class CallRecordEntity(
    @PrimaryKey val id: String,
    val peerName: String,
    val peerPhoneNumber: String,
    val peerAvatarUrl: String? = null,
    val isVideo: Boolean = false,
    val isIncoming: Boolean = false,
    val isMissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int = 0
)
