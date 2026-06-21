package wombat.joshattic.us.wear.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.wear.compose.material.Icon
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import coil.compose.AsyncImage
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Post
import java.text.DateFormat
import java.util.Date

@Composable
fun FeedScreen(
    uiState: WearUiState,
    onPostClick: (Post) -> Unit,
    onLoveClick: (Post) -> Unit,
    onNotificationsClick: () -> Unit,
    onComposeClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()

        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
        ) {
            // Header action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notifications chip
                    val unreadCount = uiState.notifications.count { !it.read }
                    Chip(
                        onClick = onNotificationsClick,
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(18.dp)
                                )
                                if (unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = unreadCount.toString(),
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Compose chip
                    Chip(
                        onClick = onComposeClick,
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "New post",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.feedLoading && uiState.feed.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            } else if (uiState.feed.isEmpty()) {
                item {
                    Text(
                        "Nothing here yet",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(uiState.feed.distinctBy { it.id }, key = { it.id }) { post ->
                WearPostCard(
                    post = post,
                    showImages = uiState.showImages,
                    showPfp = uiState.showPfp,
                    onClick = { onPostClick(post) },
                    onLoveClick = { onLoveClick(post) }
                )
            }

            if (uiState.feedHasMore && uiState.feed.isNotEmpty()) {
                item {
                    Chip(
                        onClick = onLoadMore,
                        label = { Text("Load more", style = MaterialTheme.typography.body2) },
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }
        }
    }
}

@Composable
fun WearPostCard(
    post: Post,
    showImages: Boolean = false,
    showPfp: Boolean = true,
    onClick: () -> Unit,
    onLoveClick: () -> Unit
) {
    val isPureRepost = androidx.compose.runtime.remember(post) {
        post.repost != null &&
        (post.content?.replace(Regex("<.*?>"), "")?.trim()?.isBlank() ?: true) &&
        !(post.content?.contains("<img", ignoreCase = true) ?: false)
    }

    if (isPureRepost) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = "Repost",
                    modifier = Modifier.size(12.dp),
                    tint = androidx.wear.compose.material.MaterialTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "@${post.poster.name} reposted this",
                    style = androidx.wear.compose.material.MaterialTheme.typography.caption3,
                    color = androidx.wear.compose.material.MaterialTheme.colors.onSurfaceVariant
                )
            }
            WearPostCard(
                post = post.repost!!,
                showImages = showImages,
                showPfp = showPfp,
                onClick = onClick,
                onLoveClick = onLoveClick
            )
        }
        return
    }

    val plainTextContent = androidx.compose.runtime.remember(post.content) {
        post.content?.stripHtml() ?: ""
    }

    Chip(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        colors = ChipDefaults.secondaryChipColors(),
        label = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Author row with avatar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showPfp) {
                        AsyncImage(
                            model = "https://wasteof-image-proxy.tnix.dev/${post.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                            contentDescription = post.poster.name,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = post.poster.name,
                        style = MaterialTheme.typography.caption1,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Post content (HTML stripped to plain text)
                Text(
                    text = plainTextContent,
                    style = MaterialTheme.typography.body2,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                // Repost / Image placeholders
                if (post.repost != null) {
                    val repostText = androidx.compose.runtime.remember(post.repost.content) {
                        post.repost.content?.stripHtml() ?: ""
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (showPfp) {
                                    AsyncImage(
                                        model = "https://wasteof-image-proxy.tnix.dev/${post.repost.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp).clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(post.repost.poster.name, style = MaterialTheme.typography.caption3, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(repostText, style = MaterialTheme.typography.caption3, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                } else if (post.content?.contains("<img", ignoreCase = true) == true) {
                    val imageUrls = androidx.compose.runtime.remember(post.content) { extractImages(post.content) }
                    if (showImages && imageUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        AsyncImage(
                            model = imageUrls.first(),
                            contentDescription = "Post image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap post to view images",
                            style = MaterialTheme.typography.caption3.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                // Love button row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onLoveClick,
                        modifier = Modifier.size(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (post.isLoving == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Love",
                            modifier = Modifier.size(14.dp),
                            tint = if (post.isLoving == true) Color(0xFFEF4444) else MaterialTheme.colors.onSurface
                        )
                    }
                    Text(
                        text = "${post.loves}",
                        style = MaterialTheme.typography.caption2,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Comments",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    )
}

/** Strip HTML tags to plain text for watch display. */
fun String.stripHtml(): String = replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("</p>\\s*<p", RegexOption.IGNORE_CASE), "\n\n<p")
    .replace(Regex("<[^>]+>"), "")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .trim()

fun extractImages(html: String): List<String> {
    val imgRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    return imgRegex.findAll(html)
        .mapNotNull { it.groupValues.getOrNull(1) }
        .distinct()
        .toList()
}
