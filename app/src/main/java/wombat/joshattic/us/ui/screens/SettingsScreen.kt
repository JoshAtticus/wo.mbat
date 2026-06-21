package wombat.joshattic.us.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.Composable
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
    onWearAccountChange: (String) -> Unit,
    onWearShowImagesChange: (Boolean) -> Unit,
    onWearShowProfilePicturesChange: (Boolean) -> Unit,
    onWearFeedTypeChange: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onFollowJosh: () -> Unit
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
            // Header bar
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
                    // Left Pane: Categories list
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

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Right Pane: Active Category Details
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
                                        onOpenInAppChange = onOpenLinksInAppChange
                                    )
                                    SettingsCategory.BLOCKED_USERS -> BlockedUsersSettings(
                                        blockedUsers = uiState.blockedUsernames.toList().sorted(),
                                        onUnblockUser = onUnblockUser
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
                                    SettingsCategory.ABOUT -> AboutSettings(onFollowJosh = onFollowJosh)
                                }
                            }
                        }
                    }
                }
            } else {
                // Animated transition between main menu and subcategories (for mobile)
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
                            onOpenInAppChange = onOpenLinksInAppChange
                        )
                        SettingsCategory.BLOCKED_USERS -> BlockedUsersSettings(
                            blockedUsers = uiState.blockedUsernames.toList().sorted(),
                            onUnblockUser = onUnblockUser
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
                        SettingsCategory.ABOUT -> AboutSettings(onFollowJosh = onFollowJosh)
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
            Spacer(modifier = Modifier.height(12.dp)) // padding above the first menu
        }
        item {
            CategoryMenuItem(
                title = "Feed",
                icon = Icons.Filled.Feed,
                iconContainerColor = Color(0xFF6366F1), // wasteof colour
                iconColor = Color.White,
                selected = selectedCategory == SettingsCategory.FEED,
                onClick = { onCategorySelect(SettingsCategory.FEED) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Notifications",
                icon = Icons.Filled.Notifications,
                iconContainerColor = Color(0xFFEF5350), // red
                iconColor = Color.White,
                selected = selectedCategory == SettingsCategory.NOTIFICATIONS,
                onClick = { onCategorySelect(SettingsCategory.NOTIFICATIONS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Links",
                icon = Icons.Filled.Link,
                iconContainerColor = Color(0xFF008AFF), // blue
                iconColor = Color.White,
                selected = selectedCategory == SettingsCategory.LINKS,
                onClick = { onCategorySelect(SettingsCategory.LINKS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Blocked Users",
                icon = Icons.Filled.Block,
                iconContainerColor = Color(0xFFFFEB3B), // yellow
                iconColor = Color.Black,
                selected = selectedCategory == SettingsCategory.BLOCKED_USERS,
                onClick = { onCategorySelect(SettingsCategory.BLOCKED_USERS) }
            )
        }
        item {
            CategoryMenuItem(
                title = "Wear OS",
                icon = Icons.Filled.Watch,
                iconContainerColor = Color(0xFFC6FF00), // pixel watch lime
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
    iconContainerColor: Color,
    iconColor: Color,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = iconContainerColor, shape = CircleShape),
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            subtitle = "Show in-app notification banners when replies or loves arrive while you're browsing",
            checked = inApp,
            onCheckedChange = onInAppChange
        )
        SettingToggleCard(
            title = "Mark notifications as read when opened",
            subtitle = "Automatically clear/read a notification when you tap on it from the notification card or banner",
            checked = readOnOpened,
            onCheckedChange = onReadOnOpenedChange
        )
        SettingToggleCard(
            title = "Mark all as read when tab opened",
            subtitle = "Immediately mark all unread notifications as read when entering the Notifications tab",
            checked = readOnTabOpened,
            onCheckedChange = onReadOnTabOpenedChange
        )
    }
}

@Composable
private fun LinkSettings(
    openInApp: Boolean,
    onOpenInAppChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Links opened in wo.mbat should open in...", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                        Text("wo.mbat (in-app browser)", style = MaterialTheme.typography.bodyLarge)
                        Text("Opens external links inside a premium Custom Tab wrapper", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Default web browser", style = MaterialTheme.typography.bodyLarge)
                        Text("Launches links in Chrome, Firefox, or your default system browser", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUsersSettings(
    blockedUsers: List<String>,
    onUnblockUser: (String) -> Unit
) {
    if (blockedUsers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Block, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No blocked users yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.titleMedium,
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
                            Text("Unblock", style = MaterialTheme.typography.labelMedium)
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
                subtitle = "Load and render post images directly inside post cards on the Wear OS home/explore feed list",
                checked = showImages,
                onCheckedChange = onShowImagesChange
            )
        }

        item {
            SettingToggleCard(
                title = "Show Profile Pictures on Feed",
                subtitle = "Display user profile pictures next to authors' names on Wear OS posts list",
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun AboutSettings(onFollowJosh: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    
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
    }
}
