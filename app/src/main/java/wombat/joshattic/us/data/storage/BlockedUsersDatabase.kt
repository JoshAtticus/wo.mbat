package wombat.joshattic.us.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.blockedUsersDataStore by preferencesDataStore(name = "blocked_users")

class BlockedUsersDatabase(private val context: Context) {
    private object Keys {
        val Usernames = stringSetPreferencesKey("usernames")
    }

    val blockedUsernamesFlow: Flow<Set<String>> = context.blockedUsersDataStore.data.map { preferences ->
        preferences[Keys.Usernames].orEmpty()
    }

    suspend fun block(username: String) {
        val normalized = username.trim().lowercase()
        if (normalized.isBlank()) return
        context.blockedUsersDataStore.edit { preferences ->
            preferences[Keys.Usernames] = preferences[Keys.Usernames].orEmpty() + normalized
        }
    }

    suspend fun unblock(username: String) {
        val normalized = username.trim().lowercase()
        context.blockedUsersDataStore.edit { preferences ->
            preferences[Keys.Usernames] = preferences[Keys.Usernames].orEmpty() - normalized
        }
    }
}
