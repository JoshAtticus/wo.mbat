package wombat.joshattic.us.wear.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wearSettingsDataStore by preferencesDataStore(name = "wear_settings")

class WearSettingsPreferences(private val context: Context) {
    private object Keys {
        val ShowImages = booleanPreferencesKey("show_images")
        val ShowPfp = booleanPreferencesKey("show_pfp")
        val FeedType = stringPreferencesKey("feed_type")
    }

    val showImages: Flow<Boolean> = context.wearSettingsDataStore.data.map { prefs ->
        prefs[Keys.ShowImages] ?: false
    }

    val showPfp: Flow<Boolean> = context.wearSettingsDataStore.data.map { prefs ->
        prefs[Keys.ShowPfp] ?: true
    }

    val feedType: Flow<String> = context.wearSettingsDataStore.data.map { prefs ->
        prefs[Keys.FeedType] ?: "Home"
    }

    suspend fun saveSettings(showImages: Boolean, showPfp: Boolean, feedType: String) {
        context.wearSettingsDataStore.edit { prefs ->
            prefs[Keys.ShowImages] = showImages
            prefs[Keys.ShowPfp] = showPfp
            prefs[Keys.FeedType] = feedType
        }
    }
}
