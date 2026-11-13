package wombat.joshattic.us.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.platform.PlatformActions

class AuthPreferences {
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val ActiveUsername = stringPreferencesKey("username")
        val Sessions = stringPreferencesKey("sessions")
        val LegacyToken = stringPreferencesKey("token")
    }

    val sessionsFlow: Flow<List<AuthSession>> = dataStore.data.map { preferences ->
        preferences.toSessions()
    }

    val sessionFlow: Flow<AuthSession?> = dataStore.data.map { preferences ->
        val sessions = preferences.toSessions()
        val activeUsername = preferences[Keys.ActiveUsername]
        if (activeUsername != null) {
            sessions.find { it.username.trim().equals(activeUsername.trim(), ignoreCase = true) } ?: sessions.firstOrNull()
        } else {
            sessions.firstOrNull()
        }
    }

    suspend fun saveSession(token: String, username: String) {
        dataStore.edit { preferences ->
            val cleanUsername = username.trim().lowercase()
            val sessions = preferences.toSessions().toMutableList()
            sessions.removeAll { it.username.trim().lowercase() == cleanUsername }
            sessions.add(AuthSession(token = token, username = cleanUsername))

            preferences[Keys.Sessions] = json.encodeToString(sessions)
            preferences[Keys.ActiveUsername] = cleanUsername
        }
    }

    suspend fun switchAccount(username: String) {
        dataStore.edit { preferences ->
            val cleanUsername = username.trim().lowercase()
            val sessions = preferences.toSessions()
            val matching = sessions.find { it.username.trim().lowercase() == cleanUsername }
            preferences[Keys.ActiveUsername] = matching?.username ?: cleanUsername
        }
    }

    suspend fun removeSession(username: String) {
        dataStore.edit { preferences ->
            val cleanUsername = username.trim().lowercase()
            val sessions = preferences.toSessions().toMutableList()
            sessions.removeAll { it.username.trim().lowercase() == cleanUsername }
            preferences[Keys.Sessions] = json.encodeToString(sessions)

            if (preferences[Keys.ActiveUsername]?.trim()?.lowercase() == cleanUsername) {
                val nextActive = sessions.firstOrNull()?.username
                if (nextActive != null) {
                    preferences[Keys.ActiveUsername] = nextActive
                } else {
                    preferences.remove(Keys.ActiveUsername)
                }
            }
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.LegacyToken)
            preferences.remove(Keys.ActiveUsername)
            preferences.remove(Keys.Sessions)
        }
    }

    private fun Preferences.toSessions(): List<AuthSession> {
        val jsonStr = this[Keys.Sessions]
        if (jsonStr != null) {
            val rawList: List<AuthSession> = runCatching {
                json.decodeFromString<List<AuthSession>>(jsonStr)
            }.getOrDefault(emptyList())
            return rawList.map { it.copy(username = it.username.trim().lowercase()) }
        }

        // Legacy fallback
        val legacyToken = this[Keys.LegacyToken]
        val legacyUsername = this[Keys.ActiveUsername]
        if (legacyToken != null && legacyUsername != null) {
            return listOf(AuthSession(token = legacyToken, username = legacyUsername.trim().lowercase()))
        }

        return emptyList()
    }

    private companion object {
        val dataStore: DataStore<Preferences> by lazy {
            PreferenceDataStoreFactory.createWithPath {
                PlatformActions.dataStorePath("auth")
            }
        }
    }
}