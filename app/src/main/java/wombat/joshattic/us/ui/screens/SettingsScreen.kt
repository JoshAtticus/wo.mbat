package wombat.joshattic.us.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.ui.state.HomeUiState
import wombat.joshattic.us.ui.state.SettingsCategory
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import wombat.joshattic.us.ui.theme.WombatThemeColorNames
import wombat.joshattic.us.ui.theme.getWombatColorPalette

@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onClose: () -> Unit,
    onCategorySelect: (SettingsCategory?) -> Unit,
    onShowImagesInFeedChange: (Boolean) -> Unit,
    onShowNewPostsPopupChange: (Boolean) -> Unit,
    onInAppNotificationsChange: (Boolean) -> Unit,
    onMarkReadWhenOpenedChange: (Boolean) -> Unit,
    onMarkReadWhenTabOpenedChange: (Boolean) -> Unit,
    onOpenLinksInAppChange: (Boolean) -> Unit,
    linkPreviewPriority: String,
    onLinkPreviewPriorityChange: (String) -> Unit,
    themeSource: String,
    customThemeColor: String,
    customThemeDynamic: Boolean,
    onThemeSourceChange: (String) -> Unit,
    onCustomThemeColorChange: (String) -> Unit,
    onCustomThemeDynamicChange: (Boolean) -> Unit,
    onWearAccountChange: (String) -> Unit,
    onWearShowImagesChange: (Boolean) -> Unit,
    onWearShowProfilePicturesChange: (Boolean) -> Unit,
    onWearFeedTypeChange: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onFollowJosh: () -> Unit,
    onBlockedQuoteHandlingChange: (String) -> Unit,
    onShowBlockedRevealButtonChange: (Boolean) -> Unit,
    frogMessage: String? = null
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    BackHandler(enabled = uiState.showSettings) {
        if (!isTablet && uiState.settingsCategory != null) {
            onCategorySelect(null)
        } else {
            onClose()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isTablet && uiState.settingsCategory != null) {
                    IconButton(onClick = { onCategorySelect(null) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to settings menu"
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTablet) "Settings" else when (uiState.settingsCategory) {
                        SettingsCategory.FEED -> "Feed"
                        SettingsCategory.THEMING -> "Theming"
                        SettingsCategory.NOTIFICATIONS -> "Notifications"
                        SettingsCategory.LINKS -> "Links"
                        SettingsCategory.BLOCKED_USERS -> "Blocked Users"
                        SettingsCategory.WEAR_OS -> "Wear OS"
                        SettingsCategory.ABOUT -> "About"
                        null -> "Settings"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Settings")
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            )

            if (isTablet) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                    ) {
                        val selectedCategory = uiState.settingsCategory ?: SettingsCategory.FEED
                        SettingsMenu(
                            onCategorySelect = onCategorySelect,
                            selectedCategory = selectedCategory
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val category = uiState.settingsCategory ?: SettingsCategory.FEED
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (category) {
                                        SettingsCategory.FEED -> "Feed Settings"
                                        SettingsCategory.THEMING -> "Theming Settings"
                                        SettingsCategory.NOTIFICATIONS -> "Notification Settings"
                                        SettingsCategory.LINKS -> "Link Settings"
                                        SettingsCategory.BLOCKED_USERS -> "Blocked Users"
                                        SettingsCategory.WEAR_OS -> "Wear OS Settings"
                                        SettingsCategory.ABOUT -> "About wo.mbat"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                when (category) {
                                    SettingsCategory.FEED -> FeedSettings(
                                        showImages = uiState.showImagesInFeed,
                                        showNewPosts = uiState.showNewPostsPopup,
                                        onShowImagesChange = onShowImagesInFeedChange,
                                        onShowNewPostsChange = onShowNewPostsPopupChange
                                    )
                                    SettingsCategory.THEMING -> ThemingSettings(
                                        themeSource = themeSource,
                                        customThemeColor = customThemeColor,
                                        customThemeDynamic = customThemeDynamic,
                                        onThemeSourceChange = onThemeSourceChange,
                                        onCustomThemeColorChange = onCustomThemeColorChange,
                                        onCustomThemeDynamicChange = onCustomThemeDynamicChange
                                    )
                                    SettingsCategory.NOTIFICATIONS -> NotificationSettings(
                                        inApp = uiState.inAppNotifications,
                                        readOnOpened = uiState.markReadWhenOpened,
                                        readOnTabOpened = uiState.markReadWhenTabOpened,
                                        onInAppChange = onInAppNotificationsChange,
                                        onReadOnOpenedChange = onMarkReadWhenOpenedChange,
                                        onReadOnTabOpenedChange = onMarkReadWhenTabOpenedChange
                                    )
                                    SettingsCategory.LINKS -> LinkSettings(
                                        openInApp = uiState.openLinksInApp,
                                        onOpenInAppChange = onOpenLinksInAppChange,
                                        linkPreviewPriority = uiState.linkPreviewPriority,
                                        onLinkPreviewPriorityChange = onLinkPreviewPriorityChange
                                    )
                                    SettingsCategory.BLOCKED_USERS -> BlockedUsersSettings(
                                        blockedUsers = uiState.blockedUsernames.toList().sorted(),
                                        onUnblockUser = onUnblockUser,
                                        blockedQuoteHandling = uiState.blockedQuoteHandling,
                                        onBlockedQuoteHandlingChange = onBlockedQuoteHandlingChange,
                                        showRevealButton = uiState.showBlockedRevealButton,
                                        onShowRevealButtonChange = onShowBlockedRevealButtonChange
                                    )
                                    SettingsCategory.WEAR_OS -> WearSettings(
                                        currentAccount = uiState.wearAccount,
                                        savedAccounts = uiState.savedAccounts,
                                        showImages = uiState.wearShowImages,
                                        showPfp = uiState.wearShowProfilePictures,
                                        feedType = uiState.wearFeedType,
                                        onAccountChange = onWearAccountChange,
                                        onShowImagesChange = onWearShowImagesChange,
                                        onShowPfpChange = onWearShowProfilePicturesChange,
                                        onFeedTypeChange = onWearFeedTypeChange
                                    )
                                    SettingsCategory.ABOUT -> AboutSettings(
                                        onFollowJosh = onFollowJosh,
                                        frogMessage = frogMessage
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = uiState.settingsCategory,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "SettingsScreenContent"
                ) { category ->
                    when (category) {
                        null -> SettingsMenu(onCategorySelect = onCategorySelect)
                        SettingsCategory.FEED -> FeedSettings(
                            showImages = uiState.showImagesInFeed,
                            showNewPosts = uiState.showNewPostsPopup,
                            onShowImagesChange = onShowImagesInFeedChange,
                            onShowNewPostsChange = onShowNewPostsPopupChange
                        )
                        SettingsCategory.THEMING -> ThemingSettings(
                            themeSource = themeSource,
                            customThemeColor = customThemeColor,
                            customThemeDynamic = customThemeDynamic,
                            onThemeSourceChange = onThemeSourceChange,
                            onCustomThemeColorChange = onCustomThemeColorChange,
                            onCustomThemeDynamicChange = onCustomThemeDynamicChange
                        )
                        SettingsCategory.NOTIFICATIONS -> NotificationSettings(
                            inApp = uiState.inAppNotifications,
                            readOnOpened = uiState.markReadWhenOpened,
                            readOnTabOpened = uiState.markReadWhenTabOpened,
                            onInAppChange = onInAppNotificationsChange,
                            onReadOnOpenedChange = onMarkReadWhenOpenedChange,
                            onReadOnTabOpenedChange = onMarkReadWhenTabOpenedChange
                        )
                        SettingsCategory.LINKS -> LinkSettings(
                            openInApp = uiState.openLinksInApp,
                            onOpenInAppChange = onOpenLinksInAppChange,
                            linkPreviewPriority = uiState.linkPreviewPriority,
                            onLinkPreviewPriorityChange = onLinkPreviewPriorityChange
                        )
                        SettingsCategory.BLOCKED_USERS -> BlockedUsersSettings(
                            blockedUsers = uiState.blockedUsernames.toList().sorted(),
                            onUnblockUser = onUnblockUser,
                            blockedQuoteHandling = uiState.blockedQuoteHandling,
                            onBlockedQuoteHandlingChange = onBlockedQuoteHandlingChange,
                            showRevealButton = uiState.showBlockedRevealButton,
                            onShowRevealButtonChange = onShowBlockedRevealButtonChange
                        )
                        SettingsCategory.WEAR_OS -> WearSettings(
                            currentAccount = uiState.wearAccount,
                            savedAccounts = uiState.savedAccounts,
                            showImages = uiState.wearShowImages,
                            showPfp = uiState.wearShowProfilePictures,
                            feedType = uiState.wearFeedType,
                            onAccountChange = onWearAccountChange,
                            onShowImagesChange = onWearShowImagesChange,
                            onShowPfpChange = onWearShowProfilePicturesChange,
                            onFeedTypeChange = onWearFeedTypeChange
                        )
                        SettingsCategory.ABOUT -> AboutSettings(
                            onFollowJosh = onFollowJosh,
                            frogMessage = frogMessage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenu(
    onCategorySelect: (SettingsCategory) -> Unit,
    selectedCategory: SettingsCategory? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            CategoryMenuItem(
                title = "Feed",
                icon = Icons.Filled.Feed,
                iconContainerColor = Color(0xFF6366F1),
                iconColor = Color.White,
                selected = selectedCategory == SettingsCategory.FEED,
                onClick = { onCategorySelect(SettingsCategory.FEED) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Theming",
                icon = Icons.Filled.Palette,
                iconContainerBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFE42DAC), // magenta (right)
                        Color(0xFF5B6DFD), // indigo (bottom-right)
                        Color(0xFF2B97FA), // blue (bottom)
                        Color(0xFF16DFCF), // turquoise (bottom-left)
                        Color(0xFF67FF6C), // green (left)
                        Color(0xFF95FF55), // lime (top-left)
                        Color(0xFFEBBB11), // yellow (top)
                        Color(0xFFFF6C62), // coral-red (top-right)
                        Color(0xFFE42DAC)  // magenta wrap
                    )
                ),
                iconColor = Color.White,
                badge = "NEW",
                selected = selectedCategory == SettingsCategory.THEMING,
                onClick = { onCategorySelect(SettingsCategory.THEMING) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Notifications",
                icon = Icons.Filled.Notifications,
                iconContainerColor = Color(0xFFEF5350),
                iconColor = Color.White,
                selected = selectedCategory == SettingsCategory.NOTIFICATIONS,
                onClick = { onCategorySelect(SettingsCategory.NOTIFICATIONS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Links",
                icon = Icons.Filled.Link,
                iconContainerColor = Color(0xFF008AFF),
                iconColor = Color.White,
                selected = selectedCategory == SettingsCategory.LINKS,
                onClick = { onCategorySelect(SettingsCategory.LINKS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Blocked Users",
                icon = Icons.Filled.Block,
                iconContainerColor = Color(0xFFFFEB3B),
                iconColor = Color.Black,
                selected = selectedCategory == SettingsCategory.BLOCKED_USERS,
                onClick = { onCategorySelect(SettingsCategory.BLOCKED_USERS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Wear OS",
                icon = Icons.Filled.Watch,
                iconContainerColor = Color(0xFFC6FF00),
                iconColor = Color.Black,
                selected = selectedCategory == SettingsCategory.WEAR_OS,
                onClick = { onCategorySelect(SettingsCategory.WEAR_OS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "About",
                icon = Icons.Filled.Info,
                iconContainerColor = Color.White,
                iconColor = Color.Black,
                selected = selectedCategory == SettingsCategory.ABOUT,
                onClick = { onCategorySelect(SettingsCategory.ABOUT) }
            )
        }
    }
}

@Composable
private fun CategoryMenuItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainerColor: Color = Color.Unspecified,
    iconColor: Color,
    selected: Boolean = false,
    iconContainerBrush: Brush? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val containerModifier = if (iconContainerBrush != null) {
                Modifier.size(36.dp).background(brush = iconContainerBrush, shape = CircleShape)
            } else {
                Modifier.size(36.dp).background(color = iconContainerColor, shape = CircleShape)
            }
            Box(
                modifier = containerModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FeedSettings(
    showImages: Boolean,
    showNewPosts: Boolean,
    onShowImagesChange: (Boolean) -> Unit,
    onShowNewPostsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingToggleCard(
            title = "Show images in feed",
            subtitle = "Enable loading media grid files and attachments directly inside posts on the main feed",
            checked = showImages,
            onCheckedChange = onShowImagesChange
        )
        SettingToggleCard(
            title = "Show New Posts popup",
            subtitle = "Show the floating pill button at the top of your feed when new posts are loaded",
            checked = showNewPosts,
            onCheckedChange = onShowNewPostsChange
        )
    }
}

@Composable
private fun ThemingSettings(
    themeSource: String,
    customThemeColor: String,
    customThemeDynamic: Boolean,
    onThemeSourceChange: (String) -> Unit,
    onCustomThemeColorChange: (String) -> Unit,
    onCustomThemeDynamicChange: (Boolean) -> Unit
) {
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Choose where wo.mbat gets its colours from",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSourceChange("account") }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = themeSource == "account", onClick = { onThemeSourceChange("account") })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Account theme", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("Follow the theme colour of your wasteof account (default)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSourceChange("custom") }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = themeSource == "custom", onClick = { onThemeSourceChange("custom") })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Custom theme", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("Pick your own theme colour below", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        if (themeSource == "custom") {
            item {
                Text(
                    "Theme colour",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        WombatThemeColorNames.chunked(4).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowColors.forEach { colorName ->
                                    val palette = getWombatColorPalette(colorName)
                                    val isSelected = colorName == customThemeColor && !customThemeDynamic
                                    val borderColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(palette.brand)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = borderColor,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                onCustomThemeDynamicChange(false)
                                                onCustomThemeColorChange(colorName)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = colorName,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (supportsDynamic) {
                item {
                    SettingToggleCard(
                        title = "Use system Material You theme",
                        subtitle = "Use the wallpaper-based dynamic colour scheme from your Android 12L+ system instead of a fixed colour",
                        checked = customThemeDynamic,
                        onCheckedChange = onCustomThemeDynamicChange
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSettings(
    inApp: Boolean,
    readOnOpened: Boolean,
    readOnTabOpened: Boolean,
    onInAppChange: (Boolean) -> Unit,
    onReadOnOpenedChange: (Boolean) -> Unit,
    onReadOnTabOpenedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingToggleCard(
            title = "In-app Notifications",
            subtitle = "Show in-app notification banners when notifications arrive while you're browsing",
            checked = inApp,
            onCheckedChange = onInAppChange
        )
        SettingToggleCard(
            title = "Mark notifications as read when opened",
            subtitle = "Automatically mark a notification as read when you tap on it from the notification card or banner",
            checked = readOnOpened,
            onCheckedChange = onReadOnOpenedChange
        )
        SettingToggleCard(
            title = "Mark all as read when tab opened",
            subtitle = "Automatically mark all unread notifications as read when entering the Notifications tab",
            checked = readOnTabOpened,
            onCheckedChange = onReadOnTabOpenedChange
        )
    }
}

@Composable
private fun LinkSettings(
    openInApp: Boolean,
    onOpenInAppChange: (Boolean) -> Unit,
    linkPreviewPriority: String,
    onLinkPreviewPriorityChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Links opened in wo.mbat should open in...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInAppChange(true) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = openInApp, onClick = { onOpenInAppChange(true) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("wo.mbat (in-app browser)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Opens external links inside wo.mbat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenInAppChange(false) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = !openInApp, onClick = { onOpenInAppChange(false) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Default web browser", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Opens links in you default browser", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }
        LinkApprovalCard()
        Text("Prioritise in post cards", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLinkPreviewPriorityChange("images") }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = linkPreviewPriority != "opengraph", onClick = { onLinkPreviewPriorityChange("images") })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Images", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Show attached images first; link previews stay compact", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLinkPreviewPriorityChange("opengraph") }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = linkPreviewPriority == "opengraph", onClick = { onLinkPreviewPriorityChange("opengraph") })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Link previews", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Show large Open Graph previews first; images become thumbnails", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

/**
 * Shows whether Android currently lets wo.mbat open wasteof.money links, and a button
 * that opens the system "Open by default" page where the user can approve them.
 * State is re-checked on every resume so returning from system settings updates the card.
 */
@Composable
private fun LinkApprovalCard() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var approved by remember { mutableStateOf<Boolean?>(null) }

    fun refreshState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            approved = try {
                val manager = context.getSystemService(android.content.pm.verify.domain.DomainVerificationManager::class.java)
                val state = manager?.getDomainVerificationUserState(context.packageName)
                val hostState = state?.hostToStateMap?.get("wasteof.money")
                hostState == 1 || hostState == 2
            } catch (_: Exception) {
                null
            }
        } else {
            approved = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("wasteof.money links", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = when (approved) {
                    true -> "Android is set to open wasteof.money links in wo.mbat."
                    false -> "Android is currently opening wasteof.money links in your browser. Tap below to allow wo.mbat to open them."
                    null -> "If wasteof.money links don't open in wo.mbat, approve them in Android's app settings."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Button(onClick = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${context.packageName}"))
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }) {
                Text(if (approved == false) "Allow in system settings" else "Open system settings")
            }
        }
    }
}

@Composable
private fun BlockedUsersSettings(
    blockedUsers: List<String>,
    onUnblockUser: (String) -> Unit,
    blockedQuoteHandling: String,
    onBlockedQuoteHandlingChange: (String) -> Unit,
    showRevealButton: Boolean,
    onShowRevealButtonChange: (Boolean) -> Unit
) {
    var showBlockHandlingSettings by remember { mutableStateOf(false) }

    if (showBlockHandlingSettings) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { showBlockHandlingSettings = false }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Block Handling Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Block Handling", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "Choose what happens when a post quotes a post by a user you blocked.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onBlockedQuoteHandlingChange("warning") }
            ) {
                RadioButton(selected = blockedQuoteHandling == "warning", onClick = { onBlockedQuoteHandlingChange("warning") })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Replace with warning (Default)", style = MaterialTheme.typography.bodyLarge)
                    Text("Show a warning placeholder instead of the quoted post content", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (blockedQuoteHandling == "warning") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                ) {
                    Text(
                        "Add a Show button to reveal blocked content",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = showRevealButton,
                        onCheckedChange = onShowRevealButtonChange
                    )
                }
                Text(
                    "Adds a one-time Show button to blocked quotes and comments. It must be pressed again for each item and never unblocks anyone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onBlockedQuoteHandlingChange("hide_repost") }
            ) {
                RadioButton(selected = blockedQuoteHandling == "hide_repost", onClick = { onBlockedQuoteHandlingChange("hide_repost") })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Hide quoted post entirely", style = MaterialTheme.typography.bodyLarge)
                    Text("Completely hide the quoted post container, but keep the parent post", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onBlockedQuoteHandlingChange("hide_post") }
            ) {
                RadioButton(selected = blockedQuoteHandling == "hide_post", onClick = { onBlockedQuoteHandlingChange("hide_post") })
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Hide quote posts entirely", style = MaterialTheme.typography.bodyLarge)
                    Text("Completely filter out any quote post of a blocked user's post from your feed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showBlockHandlingSettings = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quote Repost Behavior", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Manage how quote posts of blocked users are handled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (blockedUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No blocked users (yet) :D", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blockedUsers, key = { it }) { username ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "@$username",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onUnblockUser(username) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Unblock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WearSettings(
    currentAccount: String,
    savedAccounts: List<AuthSession>,
    showImages: Boolean,
    showPfp: Boolean,
    feedType: String,
    onAccountChange: (String) -> Unit,
    onShowImagesChange: (Boolean) -> Unit,
    onShowPfpChange: (Boolean) -> Unit,
    onFeedTypeChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Watch Feed Account", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    val accounts = listOf("Last used on phone") + savedAccounts.map { it.username }
                    accounts.forEach { acc ->
                        val isSelected = acc == currentAccount
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAccountChange(acc) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { onAccountChange(acc) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(acc, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        item {
            SettingToggleCard(
                title = "Show Images on Feed",
                subtitle = "Load and render post images directly inside post cards on your watch",
                checked = showImages,
                onCheckedChange = onShowImagesChange
            )
        }

        item {
            SettingToggleCard(
                title = "Show Profile Pictures on Feed",
                subtitle = "Display user profile pictures next to authors' names on your watch",
                checked = showPfp,
                onCheckedChange = onShowPfpChange
            )
        }

        item {
            Text("Watch Feed Source", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    val feeds = listOf("Home", "Explore")
                    feeds.forEach { f ->
                        val isSelected = f == feedType
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFeedTypeChange(f) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { onFeedTypeChange(f) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(f, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun HorizontalDivider(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(color)
    )
}

@Composable
private fun PaddingValues(
    horizontal: androidx.compose.ui.unit.Dp = 0.dp,
    vertical: androidx.compose.ui.unit.Dp = 0.dp
): androidx.compose.foundation.layout.PaddingValues {
    return androidx.compose.foundation.layout.PaddingValues(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical
    )
}

@Composable
private fun AboutSettings(onFollowJosh: () -> Unit, frogMessage: String? = null) {
    val uriHandler = LocalUriHandler.current
    var frogRevealed by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = wombat.joshattic.us.R.drawable.ic_logo),
                    contentDescription = "wo.mbat Logo",
                    modifier = Modifier.size(80.dp)
                )
                
                Text(
                    text = "wo.mbat",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "Made with <3 by JoshAtticus",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        item {
            CategoryMenuItem(
                title = "Website",
                icon = Icons.Filled.Link,
                iconContainerColor = Color(0xFF6366F1),
                iconColor = Color.White,
                onClick = { uriHandler.openUri("https://joshattic.us") }
            )
        }
        
        item {
            CategoryMenuItem(
                title = "Blog",
                icon = Icons.Filled.Feed,
                iconContainerColor = Color(0xFF008AFF),
                iconColor = Color.White,
                onClick = { uriHandler.openUri("https://blog.joshattic.us") }
            )
        }
        
        item {
            CategoryMenuItem(
                title = "Follow @joshatticus",
                icon = Icons.Filled.PersonAdd,
                iconContainerColor = Color(0xFFEF5350),
                iconColor = Color.White,
                onClick = onFollowJosh
            )
        }

        if (frogMessage != null) {
            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { frogRevealed = !frogRevealed },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (frogRevealed) 1f else 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (frogRevealed) "🐸" else "• • •",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (frogRevealed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = frogMessage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
