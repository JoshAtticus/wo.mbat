package wombat.joshattic.us.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wombat.joshattic.us.platform.PlatformActions

class BlockedUsersDatabase {
    private object Keys {
        val Usernames = stringSetPreferencesKey("usernames")
    }

    val blockedUsernamesFlow: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[Keys.Usernames].orEmpty()
    }

    suspend fun block(username: String) {
        val normalized = username.trim().lowercase()
        if (normalized.isBlank()) return
        dataStore.edit { preferences ->
            preferences[Keys.Usernames] = preferences[Keys.Usernames].orEmpty() + normalized
        }
    }

    suspend fun unblock(username: String) {
        val normalized = username.trim().lowercase()
        dataStore.edit { preferences ->
            preferences[Keys.Usernames] = preferences[Keys.Usernames].orEmpty() - normalized
        }
    }

    private companion object {
        val dataStore: DataStore<Preferences> by lazy {
            PreferenceDataStoreFactory.createWithPath {
                PlatformActions.dataStorePath("blocked_users")
            }
        }
    }
}
