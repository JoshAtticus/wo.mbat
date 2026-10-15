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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Notification

@Composable
fun NotificationsScreen(
    uiState: WearUiState,
    onNotificationClick: (Notification) -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0, initialCenterItemScrollOffset = 0)

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            autoCentering = null,
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 36.dp, bottom = 48.dp),
            scalingParams = ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.5f,
                edgeAlpha = 0.0f,
                minTransitionArea = 0.2f,
                maxTransitionArea = 0.35f
            )
        ) {
            item(key = "notif_header") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.caption1,
                        color = MaterialTheme.colors.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            if (uiState.notificationsLoading && uiState.notifications.isEmpty()) {
                item(key = "notif_loading") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            } else if (uiState.notifications.isEmpty()) {
                item(key = "notif_empty") {
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
                items(unreadNotifs, key = { "unread_${it.id}" }) { notif ->
                    NotificationItem(notif, onNotificationClick, onAuthorClick)
                }
            }

            if (readNotifs.isNotEmpty()) {
                if (unreadNotifs.isNotEmpty()) {
                    item(key = "prev_header") {
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

                items(filteredRead, key = { "read_${it.id}" }) { notif ->
                    NotificationItem(notif, onNotificationClick, onAuthorClick)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notif: Notification,
    onNotificationClick: (Notification) -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
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
            "comment", "wall_comment", "wall_comment_reply", "comment_reply" -> {
                val commentContent = notif.data.comment?.content
                if (commentContent == null) "This comment was deleted." else "commented: " + commentContent.stripHtml()
            }
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
            .padding(horizontal = 4.dp, vertical = 2.dp),
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
                    val actorName = notif.data.actor?.name
                    if (actorName != null) {
                        val canClickActor = onAuthorClick != null && actorName.isNotBlank() && actorName != "Unknown"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    if (canClickActor)
                                        Modifier.clickable { onAuthorClick?.invoke(actorName) }
                                    else Modifier
                                )
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = "https://wasteof-image-proxy.tnix.dev/$actorName?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                contentDescription = actorName,
                                loading = {
                                    CircularProgressIndicator(modifier = Modifier.padding(1.dp), strokeWidth = 1.dp)
                                },
                                modifier = Modifier.size(18.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = actorName,
                                style = MaterialTheme.typography.caption2,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.onSurface
                            )
                        }
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
