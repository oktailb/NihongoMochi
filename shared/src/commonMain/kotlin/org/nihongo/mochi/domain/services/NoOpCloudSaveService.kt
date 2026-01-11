package org.nihongo.mochi.domain.services

class NoOpCloudSaveService : CloudSaveService {
    override suspend fun signIn(): Boolean = false
    override suspend fun isAuthenticated(): Boolean = false
    override suspend fun saveGame(name: String, data: String, description: String): Boolean = false
    override suspend fun loadGame(name: String): String? = null
}
