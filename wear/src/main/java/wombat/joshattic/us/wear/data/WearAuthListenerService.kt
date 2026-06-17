package wombat.joshattic.us.wear.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import wombat.joshattic.us.wear.data.model.AuthSession

private val Context.wearAuthDataStore by preferencesDataStore(name = "wear_auth")

/**
 * Listens for auth data pushed from the phone via the Wearable Data Layer.
 * Persists token + username to DataStore so they survive watch restarts.
 */
class WearAuthListenerService : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.dataItem.uri.path == PATH_AUTH) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val token = dataMap.getString(KEY_TOKEN, "")
                val username = dataMap.getString(KEY_USERNAME, "")
                scope.launch {
                    WearAuthPreferences(applicationContext).saveSession(token, username)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val PATH_AUTH = "/wombat/auth"
        const val KEY_TOKEN = "token"
        const val KEY_USERNAME = "username"
    }
}

/**
 * Simple DataStore wrapper for the watch-side auth credentials.
 */
class WearAuthPreferences(private val context: Context) {
    private object Keys {
        val Token = stringPreferencesKey("token")
        val Username = stringPreferencesKey("username")
    }

    val sessionFlow: Flow<AuthSession?> = context.wearAuthDataStore.data.map { prefs ->
        val token = prefs[Keys.Token]
        val username = prefs[Keys.Username]
        if (!token.isNullOrBlank() && !username.isNullOrBlank()) AuthSession(token, username)
        else null
    }

    suspend fun saveSession(token: String, username: String) {
        context.wearAuthDataStore.edit { prefs ->
            prefs[Keys.Token] = token
            prefs[Keys.Username] = username
        }
    }

    suspend fun clearSession() {
        context.wearAuthDataStore.edit { prefs ->
            prefs.remove(Keys.Token)
            prefs.remove(Keys.Username)
        }
    }
}
