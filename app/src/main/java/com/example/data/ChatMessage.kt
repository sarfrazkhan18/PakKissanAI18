package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General",

    // Safety layer (P2.5). Whether this answer was grounded in the verified local knowledge
    // base — drives the "تصدیق شدہ" (verified) vs "AI مشورہ" (AI advice) badge so the farmer
    // can tell trustworthy answers apart. feedback: 0 none, 1 = 👍, -1 = 👎.
    val usedVerifiedSource: Boolean = false,
    val feedback: Int = 0
)
