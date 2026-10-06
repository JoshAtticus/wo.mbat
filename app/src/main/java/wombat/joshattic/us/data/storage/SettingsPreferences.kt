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
    }

    val showImagesInFeed: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.ShowImagesInFeed] ?: true }
    val showNewPostsPopup: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.ShowNewPostsPopup] ?: true }
    val inAppNotifications: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.InAppNotifications] ?: true }
    val markReadWhenOpened: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.MarkReadWhenOpened] ?: true }
    val markReadWhenTabOpened: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.MarkReadWhenTabOpened] ?: false }
    val openLinksInApp: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.OpenLinksInApp] ?: true }
    val linkPreviewPriority: Flow<String> = context.settingsDataStore.data.map { it[Keys.LinkPreviewPriority] ?: "images" }

    val wearAccount: Flow<String> = context.settingsDataStore.data.map { it[Keys.WearAccount] ?: "Last used on phone" }
    val wearShowImages: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.WearShowImages] ?: false }
    val wearShowProfilePictures: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.WearShowProfilePictures] ?: true }
    val wearFeedType: Flow<String> = context.settingsDataStore.data.map { it[Keys.WearFeedType] ?: "Home" }
    val blockedQuoteHandling: Flow<String> = context.settingsDataStore.data.map { it[Keys.BlockedQuoteHandling] ?: "warning" }
    val showBlockedQuoteRevealButton: Flow<Boolean> = context.settingsDataStore.data.map {
        it[Keys.ShowBlockedQuoteRevealButton] ?: it[Keys.ShowBlockedRevealButton] ?: false
    }
    val showBlockedCommentRevealButton: Flow<Boolean> = context.settingsDataStore.data.map {
        it[Keys.ShowBlockedCommentRevealButton] ?: it[Keys.ShowBlockedRevealButton] ?: false
    }

    val themeSource: Flow<String> = context.settingsDataStore.data.map { it[Keys.ThemeSource] ?: "account" }
    val customThemeColor: Flow<String> = context.settingsDataStore.data.map { it[Keys.CustomThemeColor] ?: "indigo" }
    val customThemeDynamic: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.CustomThemeDynamic] ?: false }

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

    suspend fun setLinkPreviewPriority(value: String) {
        context.settingsDataStore.edit { it[Keys.LinkPreviewPriority] = value }
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

    suspend fun setShowBlockedQuoteRevealButton(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ShowBlockedQuoteRevealButton] = value }
    }

    suspend fun setShowBlockedCommentRevealButton(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.ShowBlockedCommentRevealButton] = value }
    }

    suspend fun setThemeSource(value: String) {
        context.settingsDataStore.edit { it[Keys.ThemeSource] = value }
    }

    suspend fun setCustomThemeColor(value: String) {
        context.settingsDataStore.edit { it[Keys.CustomThemeColor] = value }
    }

    suspend fun setCustomThemeDynamic(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.CustomThemeDynamic] = value }
    }
}
