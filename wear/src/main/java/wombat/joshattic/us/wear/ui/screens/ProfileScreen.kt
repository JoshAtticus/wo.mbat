package wombat.joshattic.us.wear.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import coil.compose.SubcomposeAsyncImage
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Post
import wombat.joshattic.us.wear.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile screen for any user (own profile or someone else's).
 *
 * The profile picture fills the first "page" as a full-screen image with:
 *  - A top gradient (transparent→black) so the watch clock stays readable
 *  - A bottom gradient (transparent→black) that the @username + bio float over
 *
 * Scrolling down reveals: joined date, follower/following/post count pills,
 * then the user's posts rendered as standard [WearPostCard]s.
 */
@Composable
fun ProfileScreen(
    uiState: WearUiState,
    onPostClick: (Post) -> Unit,
    onLoveClick: (Post) -> Unit,
    onLoadMorePosts: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val profile = uiState.profileUser
    val username = uiState.profileUsername ?: uiState.session?.username ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        // No TimeText() — the profile picture header fills the screen and the
        // clock widget from the watch face shows through the OS chrome layer.

        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            // Start with zero auto-centering so the pfp header fills the screen
            autoCentering = null,
            scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.85f,
                edgeAlpha = 0.5f
            )
        ) {
            // ── Header: full-screen profile picture ─────────────────────────
            item {
                ProfileHeaderItem(
                    username = username,
                    bio = profile?.bio,
                    isLoading = uiState.profileLoading && profile == null
                )
            }

            // ── Stats row: joined / followers / following / posts ────────────
            if (profile != null) {
                item {
                    ProfileStatsSection(profile)
                }
            }

            // ── Posts ────────────────────────────────────────────────────────
            if (uiState.profileLoading && uiState.profilePosts.isEmpty() && profile != null) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            } else if (uiState.profilePosts.isNotEmpty()) {
                item {
                    Text(
                        text = "Posts",
                        style = MaterialTheme.typography.caption1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                }
                items(uiState.profilePosts.distinctBy { it.id }, key = { it.id }) { post ->
                    WearPostCard(
                        post = post,
                        showImages = uiState.showImages,
                        showPfp = uiState.showPfp,
                        onClick = { onPostClick(post) },
                        onLoveClick = { onLoveClick(post) }
                    )
                }
                if (uiState.profilePostsHasMore) {
                    item {
                        Chip(
                            onClick = onLoadMorePosts,
                            label = { Text("Load more", style = MaterialTheme.typography.body2) },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.padding(top = 4.dp).height(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderItem(
    username: String,
    bio: String?,
    isLoading: Boolean
) {
    // Match the screen height so it fills the first "page" before scroll
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
    ) {
        SubcomposeAsyncImage(
            model = "https://wasteof-image-proxy.tnix.dev/$username?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
            contentDescription = "$username's profile picture",
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    Modifier.fillMaxSize().background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient + username + bio, text centered and raised
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f))
                    )
                )
                .padding(start = 12.dp, end = 12.dp, top = 28.dp, bottom = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!bio.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = bio.stripHtml(),
                        style = MaterialTheme.typography.caption3,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatsSection(user: User) {
    val joinedText = androidx.compose.runtime.remember(user) {
        user.history?.joined?.let {
            "Joined " + SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (joinedText != null) {
            Text(
                text = joinedText,
                style = MaterialTheme.typography.caption3,
                color = MaterialTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        val stats = user.stats
        if (stats != null) {
            // Equal-weight pills so "Following" never wraps.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatPill(modifier = Modifier.weight(1f), label = "Posts", value = stats.posts)
                StatPill(modifier = Modifier.weight(1f), label = "Followers", value = stats.followers)
                StatPill(modifier = Modifier.weight(1f), label = "Following", value = stats.following)
            }
        }
    }
}

@Composable
private fun StatPill(modifier: Modifier = Modifier, label: String, value: Int) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatStatCount(value),
            style = MaterialTheme.typography.caption1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption3,
            color = MaterialTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

private fun formatStatCount(n: Int): String = when {
    n >= 1_000_000 -> "${n / 1_000_000}M"
    n >= 1_000     -> "${n / 1_000}k"
    else           -> n.toString()
}
