package wombat.joshattic.us.data.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wombat.joshattic.us.data.model.AuthSession

private val Context.dataStore by preferencesDataStore(name = "auth")

class AuthPreferences(private val context: Context) {
    private object Keys {
        val Token = stringPreferencesKey("token")
        val Username = stringPreferencesKey("username")
    }

    val sessionFlow: Flow<AuthSession?> = context.dataStore.data.map { preferences ->
        preferences.toSession()
    }

    suspend fun saveSession(token: String, username: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.Token] = token
            preferences[Keys.Username] = username
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.Token)
            preferences.remove(Keys.Username)
        }
    }

    private fun Preferences.toSession(): AuthSession? {
        val token = this[Keys.Token] ?: return null
        val username = this[Keys.Username] ?: return null
        return AuthSession(token = token, username = username)
    }
}