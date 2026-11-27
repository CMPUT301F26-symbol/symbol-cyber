/*
 * FakeMessageRepository.kt
 * In-memory MessageRepository for the messaging tests (US 02.12.01, US 01.14.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.fakes

import com.example.code.data.MessageRepository
import com.example.code.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** [MessageRepository] that keeps messages in memory. */
class FakeMessageRepository : MessageRepository {
    val sent = MutableStateFlow<List<Message>>(emptyList())

    override fun observeConversation(reservationId: String): Flow<List<Message>> =
        sent.map { list -> list.filter { it.reservationId == reservationId }.sortedBy { it.sentAt } }

    override suspend fun send(message: Message) {
        sent.value = sent.value + message.copy(id = "m${sent.value.size + 1}")
    }
}
