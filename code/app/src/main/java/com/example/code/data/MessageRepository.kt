/*
 * MessageRepository.kt
 * Saves and loads reservation messages (Firestore: messages/{id}). The
 * interface lets controllers and tests use a fake instead of Firestore.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: messages are sorted on the device.
 */

package com.example.code.data

import android.util.Log
import com.example.code.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Access to reservation messages. */
interface MessageRepository {

    /** Emits the messages about [reservationId], oldest first, whenever they change. */
    fun observeConversation(reservationId: String): Flow<List<Message>>

    /** Saves [message] under a new ID. */
    suspend fun send(message: Message)
}

/** [MessageRepository] backed by Cloud Firestore. */
class FirestoreMessageRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : MessageRepository {

    private val messages = db.collection(MESSAGES)

    override fun observeConversation(reservationId: String): Flow<List<Message>> = callbackFlow {
        val registration = messages
            .whereEqualTo("reservationId", reservationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to load messages", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.data?.let { messageFromMap(document.id, it) }
                }
                trySend(list.sortedBy { it.sentAt })
            }
        awaitClose { registration.remove() }
    }

    override suspend fun send(message: Message) {
        messages.add(messageToMap(message)).awaitResult()
    }

    companion object {
        /** Name of the messages collection. */
        const val MESSAGES = "messages"

        private const val TAG = "MessageRepository"
    }
}

/** Builds a [Message] from Firestore document [data]. */
fun messageFromMap(id: String, data: Map<String, Any?>): Message = Message(
    id = id,
    reservationId = data["reservationId"] as? String ?: "",
    campsiteName = data["campsiteName"] as? String ?: "",
    senderId = data["senderId"] as? String ?: "",
    senderName = data["senderName"] as? String ?: "",
    recipientId = data["recipientId"] as? String ?: "",
    fromOwner = data["fromOwner"] as? Boolean ?: false,
    text = data["text"] as? String ?: "",
    sentAt = (data["sentAt"] as? Number)?.toLong() ?: 0L,
)

/** Converts [message] to the fields stored in Firestore. */
fun messageToMap(message: Message): Map<String, Any> = mapOf(
    "reservationId" to message.reservationId,
    "campsiteName" to message.campsiteName,
    "senderId" to message.senderId,
    "senderName" to message.senderName,
    "recipientId" to message.recipientId,
    "fromOwner" to message.fromOwner,
    "text" to message.text,
    "sentAt" to message.sentAt,
)
