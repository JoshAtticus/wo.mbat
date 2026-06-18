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
import coil.compose.AsyncImage
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
            contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (uiState.notificationsLoading && uiState.notifications.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            } else if (uiState.notifications.isEmpty()) {
                item {
                    Text(
                        "No unread notifications",
                        style = MaterialTheme.typography.caption2,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(uiState.notifications, key = { it.id }) { notif ->
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
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    colors = ChipDefaults.secondaryChipColors(),
                    label = {
                        Column(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = notif.type,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colors.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                // Actor avatar and name
                                val actor = notif.data.actor
                                if (actor != null) {
                                    AsyncImage(
                                        model = "https://wasteof-image-proxy.tnix.dev/${actor.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                        contentDescription = actor.name,
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = actor.name,
                                        style = MaterialTheme.typography.caption2,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text("System", style = MaterialTheme.typography.caption2)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            // Summary text
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.caption2,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }
        }
    }
}
