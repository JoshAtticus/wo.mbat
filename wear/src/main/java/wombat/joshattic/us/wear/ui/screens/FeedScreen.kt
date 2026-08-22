package wombat.joshattic.us.wear.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import coil.compose.SubcomposeAsyncImage
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Post
import java.text.DateFormat
import java.util.Date

@Composable
fun FeedScreen(
    uiState: WearUiState,
    onPostClick: (Post) -> Unit,
    onLoveClick: (Post) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // Disable autoCentering so the first item starts near the top instead of the center
                autoCentering = null,
                // Add top padding so it doesn't overlap the clock and clears the sharp top curve
                contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
                // Gentle scaling only — edgeScale = 0.4 crushed the first card into a
                // sliver at the top bezel, which made the feed look pre-scrolled on open
                scalingParams = ScalingLazyColumnDefaults.scalingParams(edgeScale = 0.75f)
            ) {
                val distinctFeed = uiState.feed.distinctBy { it.id }
                
                distinctFeed.forEach { post ->
                    val isPureRepost = post.repost != null &&
                        (post.content?.replace(Regex("<.*?>"), "")?.trim()?.isBlank() ?: true) &&
                        !(post.content?.contains("<img", ignoreCase = true) ?: false)

                    if (isPureRepost) {
                        item(key = "${post.id}_repost") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Repeat,
                                    contentDescription = "Repost",
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colors.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "@${post.poster.name} reposted",
                                    style = MaterialTheme.typography.caption3,
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        item(key = post.repost!!.id) {
                            WearPostCard(
                                post = post.repost,
                                showImages = uiState.showImages,
                                showPfp = uiState.showPfp,
                                onClick = { onPostClick(post) }, // click original post wrapper to open
                                onLoveClick = { onLoveClick(post.repost) }
                            )
                        }
                    } else {
                        item(key = post.id) {
                            WearPostCard(
                                post = post,
                                showImages = uiState.showImages,
                                showPfp = uiState.showPfp,
                                onClick = { onPostClick(post) },
                                onLoveClick = { onLoveClick(post) }
                            )
                        }
                    }
                }

                if (uiState.feedHasMore && uiState.feed.isNotEmpty()) {
                    item {
                        Chip(
                            onClick = onLoadMore,
                            label = { Text("Load more", style = MaterialTheme.typography.body2) },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.padding(top = 4.dp).height(32.dp)
                        )
                    }
                }
            }

            // Layer loading/empty states over the list so they don't break scroll state
            if (uiState.feedLoading && uiState.feed.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
            } else if (uiState.feed.isEmpty()) {
                Text(
                    "Nothing here yet",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
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
    onLoveClick: () -> Unit,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val plainTextContent = androidx.compose.runtime.remember(post.content) {
        post.content?.stripHtml() ?: ""
    }

    Chip(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            // 20 dp each side ensures cards don't clip at the circular bezel edges
            .padding(horizontal = 20.dp, vertical = 2.dp),
        colors = ChipDefaults.chipColors(
            backgroundColor = MaterialTheme.colors.surface
        ),
        label = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                // Author row with avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (onAuthorClick != null)
                        Modifier.clickable { onAuthorClick(post.poster.name) }
                    else Modifier
                ) {
                    if (showPfp) {
                        SubcomposeAsyncImage(
                            model = "https://wasteof-image-proxy.tnix.dev/${post.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                            contentDescription = post.poster.name,
                            loading = {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(2.dp),
                                    strokeWidth = 1.5.dp
                                )
                            },
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = post.poster.name,
                        style = MaterialTheme.typography.caption1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Post content
                Text(
                    text = plainTextContent,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
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
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = if (onAuthorClick != null)
                                    Modifier.clickable { onAuthorClick(post.repost.poster.name) }
                                else Modifier
                            ) {
                                if (showPfp) {
                                    SubcomposeAsyncImage(
                                        model = "https://wasteof-image-proxy.tnix.dev/${post.repost.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                        contentDescription = null,
                                        loading = {
                                            CircularProgressIndicator(
                                                modifier = Modifier.padding(1.dp),
                                                strokeWidth = 1.dp
                                            )
                                        },
                                        modifier = Modifier.size(14.dp).clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    post.repost.poster.name,
                                    style = MaterialTheme.typography.caption3,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                repostText,
                                style = MaterialTheme.typography.caption3,
                                color = MaterialTheme.colors.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else if (post.content?.contains("<img", ignoreCase = true) == true) {
                    val imageUrls = androidx.compose.runtime.remember(post.content) { extractImages(post.content) }
                    if (showImages && imageUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        SubcomposeAsyncImage(
                            model = imageUrls.first(),
                            contentDescription = "Post image",
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to view media",
                            style = MaterialTheme.typography.caption3.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                // Love & Comment action buttons
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
                            modifier = Modifier.size(13.dp),
                            tint = if (post.isLoving == true) Color(0xFFEF4444) else MaterialTheme.colors.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${post.loves}",
                        style = MaterialTheme.typography.caption3,
                        color = if (post.isLoving == true) Color(0xFFEF4444) else MaterialTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 1.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Comments",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.caption3,
                        color = MaterialTheme.colors.onSurfaceVariant,
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
