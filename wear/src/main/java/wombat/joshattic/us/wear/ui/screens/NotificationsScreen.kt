package wombat.joshattic.us.wear.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import coil.compose.SubcomposeAsyncImage
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Notification

@Composable
fun NotificationsScreen(
    uiState: WearUiState,
    onNotificationClick: (Notification) -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()

        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // Start at the top instead of centering the header mid-screen
            autoCentering = null,
            contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp)
        ) {
            item {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (uiState.notificationsLoading && uiState.notifications.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            } else if (uiState.notifications.isEmpty()) {
                item {
                    Text(
                        "No unread notifications",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            val unreadNotifs = uiState.notifications.filter { !it.read }.distinctBy { it.id }
            val readNotifs = uiState.notifications.filter { it.read }.distinctBy { it.id }

            if (unreadNotifs.isNotEmpty()) {
                items(unreadNotifs, key = { it.id }) { notif ->
                    NotificationItem(notif, onNotificationClick)
                }
            }

            if (readNotifs.isNotEmpty()) {
                if (unreadNotifs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Previous",
                            style = MaterialTheme.typography.caption2,
                            color = MaterialTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }
                }
                
                val filteredRead = readNotifs.filter { read ->
                    unreadNotifs.none { unread -> unread.id == read.id }
                }

                items(filteredRead, key = { it.id }) { notif ->
                    NotificationItem(notif, onNotificationClick)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notif: Notification, onNotificationClick: (Notification) -> Unit) {
    val icon = androidx.compose.runtime.remember(notif.type) {
        when (notif.type) {
            "comment" -> Icons.AutoMirrored.Filled.Comment
            "love" -> Icons.Filled.Favorite
            "follow" -> Icons.Filled.PersonAdd
            else -> Icons.Filled.Notifications
        }
    }
    
    val summary = androidx.compose.runtime.remember(notif) {
        when (notif.type) {
            "comment", "wall_comment", "wall_comment_reply", "comment_reply" -> if (notif.data.comment == null) "This comment was deleted." else "commented: " + (notif.data.comment.content.stripHtml())
            "love" -> "loved your post"
            "follow" -> "followed you"
            "mention", "post_mention", "comment_mention" -> if (notif.data.post == null && notif.data.comment == null) "This post/comment was deleted." else "mentioned you"
            "repost" -> if (notif.data.post == null) "This post was deleted." else "reposted your post"
            else -> notif.type
        }
    }

    Chip(
        onClick = { onNotificationClick(notif) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        colors = ChipDefaults.chipColors(
            backgroundColor = MaterialTheme.colors.surface
        ),
        label = {
            Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = notif.type,
                        modifier = Modifier.size(13.dp),
                        tint = if (notif.type == "love") Color(0xFFEF4444) else MaterialTheme.colors.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    val actor = notif.data.actor
                    if (actor != null) {
                        SubcomposeAsyncImage(
                            model = "https://wasteof-image-proxy.tnix.dev/${actor.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                            contentDescription = actor.name,
                            loading = {
                                CircularProgressIndicator(modifier = Modifier.padding(1.dp), strokeWidth = 1.dp)
                            },
                            modifier = Modifier.size(18.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = actor.name,
                            style = MaterialTheme.typography.caption2,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                    } else {
                        Text("System", style = MaterialTheme.typography.caption2, color = MaterialTheme.colors.onSurface)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.caption2,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}
