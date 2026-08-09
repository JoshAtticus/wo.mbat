package wombat.joshattic.us.data.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wombat.joshattic.us.data.model.AuthSession

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val Context.dataStore by preferencesDataStore(name = "auth")

class AuthPreferences(private val context: Context) {
    private val gson = Gson()
    private val type = object : TypeToken<List<AuthSession>>() {}.type

    private object Keys {
        val ActiveUsername = stringPreferencesKey("username") // Keep old key for backwards compat
        val Sessions = stringPreferencesKey("sessions")
        val LegacyToken = stringPreferencesKey("token")
    }

    val sessionsFlow: Flow<List<AuthSession>> = context.dataStore.data.map { preferences ->
        preferences.toSessions()
    }

    val sessionFlow: Flow<AuthSession?> = context.dataStore.data.map { preferences ->
        val sessions = preferences.toSessions()
        val activeUsername = preferences[Keys.ActiveUsername]
        if (activeUsername != null) {
            sessions.find { it.username.trim().equals(activeUsername.trim(), ignoreCase = true) } ?: sessions.firstOrNull()
        } else {
            sessions.firstOrNull()
        }
    }

    suspend fun saveSession(token: String, username: String) {
        context.dataStore.edit { preferences ->
            val cleanUsername = username.trim().lowercase()
            val sessions = preferences.toSessions().toMutableList()
            sessions.removeAll { it.username.trim().lowercase() == cleanUsername }
            sessions.add(AuthSession(token = token, username = cleanUsername))
            
            preferences[Keys.Sessions] = gson.toJson(sessions)
            preferences[Keys.ActiveUsername] = cleanUsername
        }
    }

    suspend fun switchAccount(username: String) {
        context.dataStore.edit { preferences ->
            val cleanUsername = username.trim().lowercase()
            val sessions = preferences.toSessions()
            val matching = sessions.find { it.username.trim().lowercase() == cleanUsername }
            preferences[Keys.ActiveUsername] = matching?.username ?: cleanUsername
        }
    }

    suspend fun removeSession(username: String) {
        context.dataStore.edit { preferences ->
            val cleanUsername = username.trim().lowercase()
            val sessions = preferences.toSessions().toMutableList()
            sessions.removeAll { it.username.trim().lowercase() == cleanUsername }
            preferences[Keys.Sessions] = gson.toJson(sessions)
            
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
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.LegacyToken)
            preferences.remove(Keys.ActiveUsername)
            preferences.remove(Keys.Sessions)
        }
    }

    private fun Preferences.toSessions(): List<AuthSession> {
        val json = this[Keys.Sessions]
        if (json != null) {
            val rawList: List<AuthSession> = gson.fromJson(json, type) ?: emptyList()
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
}