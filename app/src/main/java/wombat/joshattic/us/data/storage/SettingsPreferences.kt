package wombat.joshattic.us.data.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {
    private object Keys {
        val ShowImagesInFeed = booleanPreferencesKey("show_images_in_feed")
        val ShowNewPostsPopup = booleanPreferencesKey("show_new_posts_popup")
        val InAppNotifications = booleanPreferencesKey("in_app_notifications")
        val MarkReadWhenOpened = booleanPreferencesKey("mark_read_when_opened")
        val MarkReadWhenTabOpened = booleanPreferencesKey("mark_read_when_tab_opened")
        val OpenLinksInApp = booleanPreferencesKey("open_links_in_app")
        
        val WearAccount = stringPreferencesKey("wear_account")
        val WearShowImages = booleanPreferencesKey("wear_show_images")
        val WearShowProfilePictures = booleanPreferencesKey("wear_show_profile_pictures")
        val WearFeedType = stringPreferencesKey("wear_feed_type")
        val BlockedQuoteHandling = stringPreferencesKey("blocked_quote_handling")
    }

    val showImagesInFeed: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.ShowImagesInFeed] ?: true }
    val showNewPostsPopup: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.ShowNewPostsPopup] ?: true }
    val inAppNotifications: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.InAppNotifications] ?: true }
    val markReadWhenOpened: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.MarkReadWhenOpened] ?: true }
    val markReadWhenTabOpened: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.MarkReadWhenTabOpened] ?: false }
    val openLinksInApp: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.OpenLinksInApp] ?: true }

    val wearAccount: Flow<String> = context.settingsDataStore.data.map { it[Keys.WearAccount] ?: "Last used on phone" }
    val wearShowImages: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.WearShowImages] ?: false }
    val wearShowProfilePictures: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.WearShowProfilePictures] ?: true }
    val wearFeedType: Flow<String> = context.settingsDataStore.data.map { it[Keys.WearFeedType] ?: "Home" }
    val blockedQuoteHandling: Flow<String> = context.settingsDataStore.data.map { it[Keys.BlockedQuoteHandling] ?: "warning" }

    suspend fun setShowImagesInFeed(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ShowImagesInFeed] = value }
    }

    suspend fun setShowNewPostsPopup(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ShowNewPostsPopup] = value }
    }

    suspend fun setInAppNotifications(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.InAppNotifications] = value }
    }

    suspend fun setMarkReadWhenOpened(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.MarkReadWhenOpened] = value }
    }

    suspend fun setMarkReadWhenTabOpened(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.MarkReadWhenTabOpened] = value }
    }

    suspend fun setOpenLinksInApp(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.OpenLinksInApp] = value }
    }

    suspend fun setWearAccount(value: String) {
        context.settingsDataStore.edit { it[Keys.WearAccount] = value }
    }

    suspend fun setWearShowImages(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.WearShowImages] = value }
    }

    suspend fun setWearShowProfilePictures(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.WearShowProfilePictures] = value }
    }

    suspend fun setWearFeedType(value: String) {
        context.settingsDataStore.edit { it[Keys.WearFeedType] = value }
    }

    suspend fun setBlockedQuoteHandling(value: String) {
        context.settingsDataStore.edit { it[Keys.BlockedQuoteHandling] = value }
    }
}
