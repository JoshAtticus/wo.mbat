@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.ui.theme.getUserColorSchemeColors

@Composable
fun ProfileHeader(profile: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = profile.name, size = 56.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(profile.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (profile.online) "Online" else "Offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HtmlText(autoLinkAndMentions(stripImages(profile.bio ?: "<p>No bio yet.</p>")))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStat("Followers", profile.stats?.followers ?: 0)
                ProfileStat("Following", profile.stats?.following ?: 0)
                ProfileStat("Posts", profile.stats?.posts ?: 0)
            }
        }
    }
}

@Composable
fun ProfileScreen(
    profile: User?,
    posts: List<Post>,
    loading: Boolean,
    isBlocked: Boolean,
    isFollowing: Boolean?,
    followLoading: Boolean,
    currentUsername: String?,
    onClose: () -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: (String) -> Unit,
    onBlockReportClick: (String) -> Unit,
    onReportPost: ((Post) -> Unit)? = null,
    onUnblockClick: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onLoadNextPage: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    onDeletePost: ((Post) -> Unit)? = null,
    onShowFollowers: (String) -> Unit = {},
    onShowFollowing: (String) -> Unit = {}
) {
    var profileMenuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .offset(x = (-8).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close profile")
            }
            Text(
                text = profile?.name?.let { "@$it" } ?: "Profile",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isBlocked) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("You've blocked this user", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onUnblockClick) {
                        Text("Unblock")
                    }
                }
            }
            return@Column
        }

        if (loading && posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Profile not found")
            }
            return@Column
        }

        val listState = rememberLazyListState()

        // Load more when reaching near the end
        LaunchedEffect(listState, posts.size) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 5) {
                        onLoadNextPage()
                    }
                }
        }

        // Load user's banner with a subtle themed fallback background using their profile colour.
        val accent = getUserColorSchemeColors(profile.color).first
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val bannerUrl = "https://api.wasteof.money/users/${profile.name}/banner"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                        .background(accent.copy(alpha = 0.18f))
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bannerUrl)
                            .crossfade(true)
                            .build(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        },
                        contentDescription = "Profile banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient scrim at the bottom of the banner for smoother blending
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
            }

            item {
                // Profile card overlapping the banner slightly via negative offset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.1f to accent.copy(alpha = 0.08f),
                                1.0f to accent.copy(alpha = 0.08f)
                            ),
                            RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        ),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Profile picture with online dot indicator
                            Box {
                                ProfilePicture(username = profile.name, size = 72.dp, borderColor = accent)
                                if (profile.online) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.BottomEnd)
                                            .offset(x = (-2).dp, y = (-2).dp)
                                            .background(Color(0xFF22C55E), CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = accent
                                )
                                Text(
                                    text = if (profile.online) "Online" else "Offline",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (profile.online) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (currentUsername?.equals(profile.name, ignoreCase = true) != true) {
                                Button(
                                    onClick = onFollowClick,
                                    enabled = currentUsername != null && !followLoading,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(if (isFollowing == true) "Unfollow" else "Follow")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        HtmlText(
                            autoLinkAndMentions(stripImages(profile.bio ?: "<p>No bio yet.</p>")),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileStat("Followers", profile.stats?.followers ?: 0, accentColor = accent, onClick = { onShowFollowers(profile.name) })
                            ProfileStat("Following", profile.stats?.following ?: 0, accentColor = accent, onClick = { onShowFollowing(profile.name) })
                            ProfileStat("Posts", profile.stats?.posts ?: 0, accentColor = accent)
                            Spacer(modifier = Modifier.weight(1f))
                            Box {
                                IconButton(onClick = { profileMenuExpanded = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "Profile options")
                                }
                                UserActionsMenu(
                                    expanded = profileMenuExpanded,
                                    onDismiss = { profileMenuExpanded = false },
                                    onBlock = {
                                        profileMenuExpanded = false
                                        onBlockClick(profile.name)
                                    },
                                    onBlockReport = {
                                        profileMenuExpanded = false
                                        onBlockReportClick(profile.name)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Posts section with a subtle header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Posts",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            if (posts.isNotEmpty()) {
                items(posts, key = { it.id }, contentType = { "post" }) { post ->
                    PostCard(
                        post = post,
                        onClick = { onPostClick(post) },
                        truncated = true,
                        onMentionClick = onMentionClick,
                        onProfileClick = onProfileClick,
                        onLoveClick = onLoveClick,
                        onPostClick = onPostClick,
                        onImageClick = onImageClick,
                        onBlockUser = onBlockClick,
                        onReportPost = onReportPost,
                        onRepostClick = onRepostClick,
                        onQuoteClick = onQuoteClick,
                        onDeletePost = onDeletePost,
                        currentUsername = currentUsername
                    )
                }
            } else {
                item {
                    EmptyStateCard("No posts yet", "This user hasn't posted anything.")
                }
            }
        }
    }
}
