package wombat.joshattic.us.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import wombat.joshattic.us.platform.PlatformActions

class SettingsPreferences {
    private val settingsDataStore: DataStore<Preferences>
        get() = dataStore

    private object Keys {
        val ShowImagesInFeed = booleanPreferencesKey("show_images_in_feed")
        val ShowNewPostsPopup = booleanPreferencesKey("show_new_posts_popup")
        val InAppNotifications = booleanPreferencesKey("in_app_notifications")
        val MarkReadWhenOpened = booleanPreferencesKey("mark_read_when_opened")
        val MarkReadWhenTabOpened = booleanPreferencesKey("mark_read_when_tab_opened")
        val OpenLinksInApp = booleanPreferencesKey("open_links_in_app")
        val LinkPreviewPriority = stringPreferencesKey("link_preview_priority")

        val WearAccount = stringPreferencesKey("wear_account")
        val WearShowImages = booleanPreferencesKey("wear_show_images")
        val WearShowProfilePictures = booleanPreferencesKey("wear_show_profile_pictures")
        val WearFeedType = stringPreferencesKey("wear_feed_type")
        val BlockedQuoteHandling = stringPreferencesKey("blocked_quote_handling")
        val ShowBlockedRevealButton = booleanPreferencesKey("show_blocked_reveal_button")
        val ShowBlockedQuoteRevealButton = booleanPreferencesKey("show_blocked_quote_reveal_button")
        val ShowBlockedCommentRevealButton = booleanPreferencesKey("show_blocked_comment_reveal_button")

        val ThemeSource = stringPreferencesKey("theme_source")
        val CustomThemeColor = stringPreferencesKey("custom_theme_color")
        val CustomThemeDynamic = booleanPreferencesKey("custom_theme_dynamic")

        // Stored newest-first, joined by newlines
        val SearchHistory = stringPreferencesKey("search_history")
        val AnalyticsDeviceId = stringPreferencesKey("analytics_device_id")
    }

    val showImagesInFeed: Flow<Boolean> = settingsDataStore.data.map { it[Keys.ShowImagesInFeed] ?: true }
    val showNewPostsPopup: Flow<Boolean> = settingsDataStore.data.map { it[Keys.ShowNewPostsPopup] ?: true }
    val inAppNotifications: Flow<Boolean> = settingsDataStore.data.map { it[Keys.InAppNotifications] ?: true }
    val markReadWhenOpened: Flow<Boolean> = settingsDataStore.data.map { it[Keys.MarkReadWhenOpened] ?: true }
    val markReadWhenTabOpened: Flow<Boolean> = settingsDataStore.data.map { it[Keys.MarkReadWhenTabOpened] ?: false }
    val openLinksInApp: Flow<Boolean> = settingsDataStore.data.map { it[Keys.OpenLinksInApp] ?: true }
    val linkPreviewPriority: Flow<String> = settingsDataStore.data.map { it[Keys.LinkPreviewPriority] ?: "images" }

    val wearAccount: Flow<String> = settingsDataStore.data.map { it[Keys.WearAccount] ?: "Last used on phone" }
    val wearShowImages: Flow<Boolean> = settingsDataStore.data.map { it[Keys.WearShowImages] ?: false }
    val wearShowProfilePictures: Flow<Boolean> = settingsDataStore.data.map { it[Keys.WearShowProfilePictures] ?: true }
    val wearFeedType: Flow<String> = settingsDataStore.data.map { it[Keys.WearFeedType] ?: "Home" }
    val blockedQuoteHandling: Flow<String> = settingsDataStore.data.map { it[Keys.BlockedQuoteHandling] ?: "warning" }
    val showBlockedQuoteRevealButton: Flow<Boolean> = settingsDataStore.data.map {
        it[Keys.ShowBlockedQuoteRevealButton] ?: it[Keys.ShowBlockedRevealButton] ?: false
    }
    val showBlockedCommentRevealButton: Flow<Boolean> = settingsDataStore.data.map {
        it[Keys.ShowBlockedCommentRevealButton] ?: it[Keys.ShowBlockedRevealButton] ?: false
    }

    val themeSource: Flow<String> = settingsDataStore.data.map { it[Keys.ThemeSource] ?: "account" }
    val customThemeColor: Flow<String> = settingsDataStore.data.map { it[Keys.CustomThemeColor] ?: "indigo" }
    val customThemeDynamic: Flow<Boolean> = settingsDataStore.data.map { it[Keys.CustomThemeDynamic] ?: false }

    val searchHistory: Flow<List<String>> = settingsDataStore.data.map { prefs ->
        prefs[Keys.SearchHistory]?.split('\n')?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        settingsDataStore.edit { prefs ->
            val current = prefs[Keys.SearchHistory]
                ?.split('\n')
                ?.filter { it.isNotBlank() }
                .orEmpty()
            // Newest first, deduped, capped at 10
            prefs[Keys.SearchHistory] =
                (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) })
                    .take(10)
                    .joinToString("\n")
        }
    }

    suspend fun clearSearchHistory() {
        settingsDataStore.edit { it.remove(Keys.SearchHistory) }
    }

    suspend fun setShowImagesInFeed(value: Boolean) {
        settingsDataStore.edit { it[Keys.ShowImagesInFeed] = value }
    }

    suspend fun setShowNewPostsPopup(value: Boolean) {
        settingsDataStore.edit { it[Keys.ShowNewPostsPopup] = value }
    }

    suspend fun setInAppNotifications(value: Boolean) {
        settingsDataStore.edit { it[Keys.InAppNotifications] = value }
    }

    suspend fun setMarkReadWhenOpened(value: Boolean) {
        settingsDataStore.edit { it[Keys.MarkReadWhenOpened] = value }
    }

    suspend fun setMarkReadWhenTabOpened(value: Boolean) {
        settingsDataStore.edit { it[Keys.MarkReadWhenTabOpened] = value }
    }

    suspend fun setOpenLinksInApp(value: Boolean) {
        settingsDataStore.edit { it[Keys.OpenLinksInApp] = value }
    }

    suspend fun setLinkPreviewPriority(value: String) {
        settingsDataStore.edit { it[Keys.LinkPreviewPriority] = value }
    }

    suspend fun setWearAccount(value: String) {
        settingsDataStore.edit { it[Keys.WearAccount] = value }
    }

    suspend fun setWearShowImages(value: Boolean) {
        settingsDataStore.edit { it[Keys.WearShowImages] = value }
    }

    suspend fun setWearShowProfilePictures(value: Boolean) {
        settingsDataStore.edit { it[Keys.WearShowProfilePictures] = value }
    }

    suspend fun setWearFeedType(value: String) {
        settingsDataStore.edit { it[Keys.WearFeedType] = value }
    }

    suspend fun setBlockedQuoteHandling(value: String) {
        settingsDataStore.edit { it[Keys.BlockedQuoteHandling] = value }
    }

    suspend fun setShowBlockedQuoteRevealButton(value: Boolean) {
        settingsDataStore.edit { it[Keys.ShowBlockedQuoteRevealButton] = value }
    }

    suspend fun setShowBlockedCommentRevealButton(value: Boolean) {
        settingsDataStore.edit { it[Keys.ShowBlockedCommentRevealButton] = value }
    }

    suspend fun setThemeSource(value: String) {
        settingsDataStore.edit { it[Keys.ThemeSource] = value }
    }

    suspend fun setCustomThemeColor(value: String) {
        settingsDataStore.edit { it[Keys.CustomThemeColor] = value }
    }

    suspend fun setCustomThemeDynamic(value: Boolean) {
        settingsDataStore.edit { it[Keys.CustomThemeDynamic] = value }
    }

    suspend fun getOrCreateAnalyticsDeviceId(): String {
        val existing = settingsDataStore.data.first()[Keys.AnalyticsDeviceId]
        if (!existing.isNullOrBlank()) return existing
        val generated = PlatformActions.randomUuid()
        settingsDataStore.edit { prefs ->
            if (prefs[Keys.AnalyticsDeviceId].isNullOrBlank()) {
                prefs[Keys.AnalyticsDeviceId] = generated
            }
        }
        return settingsDataStore.data.first()[Keys.AnalyticsDeviceId] ?: generated
    }

    private companion object {
        val dataStore: DataStore<Preferences> by lazy {
            PreferenceDataStoreFactory.createWithPath {
                PlatformActions.dataStorePath("settings")
            }
        }
    }
}
