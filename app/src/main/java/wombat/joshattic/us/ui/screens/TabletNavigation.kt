package wombat.joshattic.us.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import wombat.joshattic.us.ui.state.BottomTab

@Composable
fun WombatNavigationRail(
    selectedTab: BottomTab,
    unreadCount: Int,
    accountLabel: String,
    profilePictureUrl: String?,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        NavigationRailItem(
            selected = selectedTab == BottomTab.Home,
            onClick = { onTabSelected(BottomTab.Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = if (selectedTab == BottomTab.Home) FontWeight.Bold else FontWeight.Normal) },
            colors = navRailItemColors
        )
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == BottomTab.Explore,
            onClick = { onTabSelected(BottomTab.Explore) },
            icon = { Icon(Icons.Filled.Tag, contentDescription = "Explore") },
            label = { Text("Explore", fontWeight = if (selectedTab == BottomTab.Explore) FontWeight.Bold else FontWeight.Normal) },
            colors = navRailItemColors
        )
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == BottomTab.Notifications,
            onClick = { onTabSelected(BottomTab.Notifications) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) { Text(if (unreadCount > 99) "99+" else unreadCount.toString(), fontWeight = FontWeight.Bold) }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
            },
            label = { Text("Notifications", fontWeight = if (selectedTab == BottomTab.Notifications) FontWeight.Bold else FontWeight.Normal) },
            colors = navRailItemColors
        )
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = selectedTab == BottomTab.Account,
            onClick = { onTabSelected(BottomTab.Account) },
            icon = {
                if (profilePictureUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePictureUrl)
                            .crossfade(true)
                            .addHeader("Cache-Control", "no-cache")
                            .addHeader("Pragma", "no-cache")
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = accountLabel,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .then(if (selectedTab == BottomTab.Account) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Account")
                }
            },
            label = { Text(accountLabel, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, fontWeight = if (selectedTab == BottomTab.Account) FontWeight.Bold else FontWeight.Normal) },
            colors = navRailItemColors
        )
    }
}
