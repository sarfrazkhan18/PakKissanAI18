package com.example.data

import kotlinx.coroutines.flow.Flow

class KisaanRepository(private val dao: KisaanDao) {
    val allKnowledge: Flow<List<AgriKnowledge>> = dao.getAllKnowledge()

    fun getKnowledgeByCategory(category: String): Flow<List<AgriKnowledge>> =
        dao.getKnowledgeByCategory(category)

    suspend fun getKnowledgeCount(): Int = dao.getKnowledgeCount()

    suspend fun insertKnowledge(knowledge: List<AgriKnowledge>) {
        dao.insertKnowledge(knowledge)
    }

    suspend fun searchKnowledge(query: String): List<AgriKnowledge> =
        dao.searchKnowledge(query)

    val allSessions: Flow<List<ChatSession>> = dao.getAllSessions()

    fun getSessionsForFarmer(phoneNumber: String): Flow<List<ChatSession>> =
        dao.getSessionsForFarmer(phoneNumber)

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> =
        dao.getMessagesForSession(sessionId)

    suspend fun createSession(session: ChatSession) {
        dao.insertSession(session)
    }

    suspend fun updateSession(session: ChatSession) {
        dao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: String) {
        dao.deleteMessagesForSession(sessionId)
        dao.deleteSessionById(sessionId)
    }

    suspend fun insertMessage(message: ChatMessage) {
        dao.insertMessage(message)
    }

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun getProfileByPhone(phoneNumber: String): UserProfile? =
        dao.getProfileByPhone(phoneNumber)

    suspend fun deactivateAllProfiles() {
        dao.deactivateAllProfiles()
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.insertProfile(profile)
    }

    suspend fun clearProfile() {
        dao.clearProfile()
    }
}
