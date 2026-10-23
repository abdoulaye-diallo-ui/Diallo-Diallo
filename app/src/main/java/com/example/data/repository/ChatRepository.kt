package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.model.CallRecordEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StoryEntity
import com.example.security.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val appScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val currentUserId = "user_me_samawakhtane"

    fun getConversations(): Flow<List<ConversationEntity>> = chatDao.getAllConversations()

    suspend fun getConversation(id: String): ConversationEntity? = chatDao.getConversationById(id)

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        chatDao.getMessagesForConversation(conversationId)

    fun getStories(): Flow<List<StoryEntity>> = chatDao.getAllStories()

    fun getCalls(): Flow<List<CallRecordEntity>> = chatDao.getAllCalls()

    suspend fun clearUnread(conversationId: String) = chatDao.clearUnread(conversationId)

    suspend fun togglePin(conversationId: String, currentPinned: Boolean) {
        chatDao.updatePinned(conversationId, !currentPinned)
    }

    suspend fun toggleLock(conversationId: String, currentLocked: Boolean) {
        chatDao.updateLocked(conversationId, !currentLocked)
    }

    suspend fun markStoryViewed(storyId: String) {
        chatDao.markStoryViewed(storyId)
    }

    suspend fun sendMessage(
        conversationId: String,
        plainText: String,
        mediaUrl: String? = null,
        type: String = "TEXT",
        audioDurationSec: Int = 0,
        caption: String? = null,
        fileName: String? = null
    ) {
        val encrypted = CryptoManager.encrypt(plainText)
        val msgId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val msg = MessageEntity(
            messageId = msgId,
            conversationId = conversationId,
            senderId = currentUserId,
            receiverId = "peer",
            encryptedContent = encrypted,
            mediaUrl = mediaUrl,
            type = type,
            timestamp = now,
            status = "SENT",
            audioDurationSec = audioDurationSec,
            caption = caption,
            fileName = fileName
        )

        chatDao.insertMessage(msg)
        val snippet = when (type) {
            "AUDIO" -> "🎤 Message vocal (${audioDurationSec}s)"
            "IMAGE" -> "📷 Photo ${caption?.let { ": $it" } ?: ""}"
            "DOC" -> "📄 Document: ${fileName ?: "Fichier"}"
            else -> plainText
        }
        chatDao.updateConversationSnippet(conversationId, snippet, now)

        // Simulate delivery tick and automated peer response for an interactive experience
        appScope.launch {
            delay(800)
            chatDao.updateMessageStatus(msgId, "DELIVERED")
            delay(1200)
            chatDao.updateMessageStatus(msgId, "READ")

            // Automated contextual peer reply
            delay(1500)
            val replyText = generatePeerReply(conversationId, plainText, type)
            val replyEncrypted = CryptoManager.encrypt(replyText)
            val replyId = UUID.randomUUID().toString()
            val replyTime = System.currentTimeMillis()

            val replyMsg = MessageEntity(
                messageId = replyId,
                conversationId = conversationId,
                senderId = "peer",
                receiverId = currentUserId,
                encryptedContent = replyEncrypted,
                mediaUrl = null,
                type = "TEXT",
                timestamp = replyTime,
                status = "READ"
            )
            chatDao.insertMessage(replyMsg)
            chatDao.updateConversationSnippet(conversationId, replyText, replyTime)
        }
    }

    private fun generatePeerReply(conversationId: String, userText: String, type: String): String {
        val lower = userText.lowercase()
        return when {
            type == "AUDIO" -> "Bien reçu ton audio vocal frangin ! La qualité sonore chiffrée est impeccable."
            type == "IMAGE" -> "Magnifique cliché ! C'est vraiment l'esprit de la Teranga 🇸🇳"
            lower.contains("salam") || lower.contains("salaam") || lower.contains("bonjour") ->
                "Mālékoum Salaam ! Naka nga def ? J'espère que tout se passe bien."
            lower.contains("webrtc") || lower.contains("appel") ->
                "Le flux WebRTC fonctionne à merveille avec chiffrement DTLS-SRTP P2P !"
            lower.contains("chiffrement") || lower.contains("e2ee") || lower.contains("sécurité") ->
                "Toutes nos discussions sont protégées par clé matérielle AES-256 GCM dans AndroidKeyStore."
            lower.contains("dakar") || lower.contains("senegal") ->
                "Dakar ne dort jamais ! On s'organise une rencontre ce week-end ?"
            conversationId == "conv_1" ->
                "Waaw kay ! Je finalise quelques courses et je te rappelle dès que possible."
            conversationId == "conv_3" ->
                "Mamadou: Superbe mise à jour de l'architecture. La synchronisation Room + E2EE tourne comme une horloge."
            else ->
                "Parfait, c'est bien noté ! Merci pour le message sécurisé."
        }
    }

    suspend fun postStory(
        userName: String = "Moi",
        textContent: String? = null,
        mediaUrl: String? = null,
        caption: String = "",
        bgGradientIndex: Int = 0
    ) {
        val story = StoryEntity(
            id = UUID.randomUUID().toString(),
            userId = currentUserId,
            userName = userName,
            userAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
            mediaUrl = mediaUrl,
            textContent = textContent,
            bgGradientIndex = bgGradientIndex,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            isViewed = false
        )
        chatDao.insertStory(story)
    }

    suspend fun logCall(
        peerName: String,
        peerPhoneNumber: String,
        peerAvatarUrl: String?,
        isVideo: Boolean,
        isIncoming: Boolean,
        isMissed: Boolean,
        durationSec: Int
    ) {
        val record = CallRecordEntity(
            id = UUID.randomUUID().toString(),
            peerName = peerName,
            peerPhoneNumber = peerPhoneNumber,
            peerAvatarUrl = peerAvatarUrl,
            isVideo = isVideo,
            isIncoming = isIncoming,
            isMissed = isMissed,
            timestamp = System.currentTimeMillis(),
            durationSec = durationSec
        )
        chatDao.insertCall(record)
    }

    suspend fun createNewConversation(name: String, phone: String): String {
        val id = "conv_${UUID.randomUUID().toString().take(8)}"
        val conv = ConversationEntity(
            id = id,
            peerName = name,
            peerPhoneNumber = phone,
            peerAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
            lastMessage = "Discussion chiffrée de bout en bout initiée",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isPinned = false,
            isLocked = false,
            isOnline = true
        )
        chatDao.insertConversation(conv)
        // Send initial greeting
        sendMessage(id, "Salaam Aleykoum ! Bienvenue sur Samawakhtane.")
        return id
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = chatDao.getAllConversations().firstOrNull()
        if (!existing.isNullOrEmpty()) return

        val now = System.currentTimeMillis()

        val conversations = listOf(
            ConversationEntity(
                id = "conv_1",
                peerName = "Fatou Ndiaye",
                peerPhoneNumber = "+221 77 123 45 67",
                peerAvatarUrl = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=200&q=80",
                lastMessage = "D'accord, on se parle tout à l'heure ! Naka wa kër ga ?",
                lastTimestamp = now - 180000,
                unreadCount = 2,
                isPinned = true,
                isLocked = false,
                isOnline = true,
                aboutText = "Entrepreneuse & Passionnée de Tech Dakar 🇸🇳"
            ),
            ConversationEntity(
                id = "conv_2",
                peerName = "Mamadou Sow",
                peerPhoneNumber = "+221 78 987 65 43",
                peerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                lastMessage = "🎤 Message vocal (0:38)",
                lastTimestamp = now - 3600000,
                unreadCount = 0,
                isPinned = true,
                isLocked = true,
                isOnline = true,
                aboutText = "Ingénieur Télécoms & WebRTC P2P"
            ),
            ConversationEntity(
                id = "conv_3",
                peerName = "Groupe Développeurs Dakar",
                peerPhoneNumber = "+221 70 555 12 34",
                peerAvatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?auto=format&fit=crop&w=200&q=80",
                lastMessage = "Amadou: L'architecture WebRTC P2P est prête !",
                lastTimestamp = now - 14400000,
                unreadCount = 5,
                isPinned = false,
                isLocked = false,
                isOnline = true,
                aboutText = "Communauté Mobile & Sécurité Sénégal"
            ),
            ConversationEntity(
                id = "conv_4",
                peerName = "Awa Diop",
                peerPhoneNumber = "+221 76 345 89 12",
                peerAvatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80",
                lastMessage = "📷 Photo : Gorée au coucher du soleil 🌅",
                lastTimestamp = now - 86400000,
                unreadCount = 0,
                isPinned = false,
                isLocked = false,
                isOnline = false,
                aboutText = "Artiste Visuelle & Voyageuse"
            ),
            ConversationEntity(
                id = "conv_5",
                peerName = "Ousmane Ba",
                peerPhoneNumber = "+221 77 654 32 10",
                peerAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80",
                lastMessage = "📄 Document: Samawakhtane_Specs_v1.pdf",
                lastTimestamp = now - 172800000,
                unreadCount = 0,
                isPinned = false,
                isLocked = false,
                isOnline = true,
                aboutText = "Cybersécurité & Audit Cryptographique"
            )
        )
        chatDao.insertConversations(conversations)

        // Initial encrypted messages for conv_1
        val messagesConv1 = listOf(
            MessageEntity(
                messageId = "m1_1",
                conversationId = "conv_1",
                senderId = "peer",
                receiverId = currentUserId,
                encryptedContent = CryptoManager.encrypt("Salaam Aleykoum ! Comment avance le projet Samawakhtane ?"),
                timestamp = now - 600000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m1_2",
                conversationId = "conv_1",
                senderId = currentUserId,
                receiverId = "peer",
                encryptedContent = CryptoManager.encrypt("Mālékoum Salaam Fatou ! L'implémentation AES-256 GCM et les appels WebRTC sont opérationnels."),
                timestamp = now - 450000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m1_3",
                conversationId = "conv_1",
                senderId = "peer",
                receiverId = currentUserId,
                encryptedContent = CryptoManager.encrypt("Mach'Allah c'est une excellente nouvelle pour la souveraineté numérique africaine !"),
                timestamp = now - 300000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m1_4",
                conversationId = "conv_1",
                senderId = "peer",
                receiverId = currentUserId,
                encryptedContent = CryptoManager.encrypt("D'accord, on se parle tout à l'heure ! Naka wa kër ga ?"),
                timestamp = now - 180000,
                status = "DELIVERED"
            )
        )
        chatDao.insertMessages(messagesConv1)

        // Initial messages for conv_2 (Audio note demo)
        val messagesConv2 = listOf(
            MessageEntity(
                messageId = "m2_1",
                conversationId = "conv_2",
                senderId = currentUserId,
                receiverId = "peer",
                encryptedContent = CryptoManager.encrypt("Mamadou, as-tu pu tester la clé maître dans le Keystore ?"),
                timestamp = now - 7200000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m2_2",
                conversationId = "conv_2",
                senderId = "peer",
                receiverId = currentUserId,
                encryptedContent = CryptoManager.encrypt("Salam frangin, je viens de tester la dérivation de clé avec le KeyGenParameterSpec. Écoute mon retour vocal ci-dessous :"),
                timestamp = now - 3700000,
                status = "READ"
            ),
            MessageEntity(
                messageId = "m2_3",
                conversationId = "conv_2",
                senderId = "peer",
                receiverId = currentUserId,
                encryptedContent = CryptoManager.encrypt("Note vocale chiffrée"),
                type = "AUDIO",
                audioDurationSec = 38,
                timestamp = now - 3600000,
                status = "READ"
            )
        )
        chatDao.insertMessages(messagesConv2)

        // Seed initial stories
        val initialStories = listOf(
            StoryEntity(
                id = "s_1",
                userId = "fatou_ndiaye",
                userName = "Fatou Ndiaye",
                userAvatarUrl = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=200&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
                caption = "Vue magnifique sur l'océan aux Almadies, Dakar 🌊🇸🇳",
                timestamp = now - 7200000,
                isViewed = false
            ),
            StoryEntity(
                id = "s_2",
                userId = "mamadou_sow",
                userName = "Mamadou Sow",
                userAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                textContent = "« Ku am kulay ñaax, boo daanoo nga jòog » — Proverbe Wolof\nConstruisons l'Afrique numérique de demain avec fierté et excellence !",
                bgGradientIndex = 1,
                caption = "Pensée du jour",
                timestamp = now - 14400000,
                isViewed = false
            ),
            StoryEntity(
                id = "s_3",
                userId = "awa_diop",
                userName = "Awa Diop",
                userAvatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80",
                mediaUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=800&q=80",
                caption = "Exposition d'art contemporain au Musée des Civilisations Noires ✨",
                timestamp = now - 28800000,
                isViewed = true
            )
        )
        chatDao.insertStories(initialStories)

        // Seed initial calls
        val initialCalls = listOf(
            CallRecordEntity(
                id = "call_1",
                peerName = "Fatou Ndiaye",
                peerPhoneNumber = "+221 77 123 45 67",
                peerAvatarUrl = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=200&q=80",
                isVideo = true,
                isIncoming = true,
                isMissed = false,
                timestamp = now - 1800000,
                durationSec = 245
            ),
            CallRecordEntity(
                id = "call_2",
                peerName = "Mamadou Sow",
                peerPhoneNumber = "+221 78 987 65 43",
                peerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                isVideo = false,
                isIncoming = false,
                isMissed = false,
                timestamp = now - 10800000,
                durationSec = 412
            ),
            CallRecordEntity(
                id = "call_3",
                peerName = "Ousmane Ba",
                peerPhoneNumber = "+221 77 654 32 10",
                peerAvatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80",
                isVideo = false,
                isIncoming = true,
                isMissed = true,
                timestamp = now - 86400000,
                durationSec = 0
            )
        )
        chatDao.insertCalls(initialCalls)
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = ChatRepository(db.chatDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
