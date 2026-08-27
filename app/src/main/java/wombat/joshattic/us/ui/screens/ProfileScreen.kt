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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.zIndex
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
            HtmlText(autoLinkAndMentions(stripImages(profile.bio ?: "<p>im a wasteof user, yay!</p>")))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStat("Followers", profile.stats?.followers ?: 0, modifier = Modifier.weight(1f))
                ProfileStat("Following", profile.stats?.following ?: 0, modifier = Modifier.weight(1f))
                ProfileStat("Posts", profile.stats?.posts ?: 0, modifier = Modifier.weight(1f))
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
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession> = emptyList(),
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
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    onLoadNextPage: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    onDeletePost: ((Post) -> Unit)? = null,
    onShowFollowers: (String) -> Unit = {},
    onShowFollowing: (String) -> Unit = {},
    onEditPost: ((Post) -> Unit)? = null,
    onWallClick: (String) -> Unit = {},
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    onPostClickById: ((String) -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning"
) {
    var profileMenuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {

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
            contentPadding = PaddingValues(
                // 96dp clears the floating bottom bar; the inset keeps it clear of
                // the system navigation bar too.
                bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val hourlyBuster = remember { System.currentTimeMillis() / (1000 * 60 * 60) }
                val bannerUrl = "https://api.wasteof.money/users/${profile.name}/banner?cb=$hourlyBuster"
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Banner image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
                    }

                    // 2. Discord-style Avatar overlapping half on banner, half on content card below
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .offset(y = 90.dp)
                            .zIndex(3f)
                    ) {
                        ProfilePicture(
                            username = profile.name,
                            size = 84.dp,
                            borderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            borderWidth = 4.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .background(if (profile.online) Color(0xFF22C55E) else Color(0xFF64748B), CircleShape)
                                .border(3.5.dp, MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
                        )
                    }

                    // 3. Profile Content Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 132.dp),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            // Top Row: Uniform Circle Icon Buttons (Follow, Wall, More) on the right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (currentUsername?.equals(profile.name, ignoreCase = true) != true) {
                                        val isFollowingUser = isFollowing == true
                                        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                                        FilledTonalIconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                onFollowClick()
                                            },
                                            enabled = currentUsername != null && !followLoading,
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp),
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = if (isFollowingUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                contentColor = if (isFollowingUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Icon(
                                                imageVector = if (isFollowingUser) Icons.Filled.Check else Icons.Filled.PersonAdd,
                                                contentDescription = if (isFollowingUser) "Unfollow" else "Follow",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    FilledTonalIconButton(
                                        onClick = { onWallClick(profile.name) },
                                        shape = CircleShape,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Filled.Forum, contentDescription = "Wall", modifier = Modifier.size(20.dp), tint = accent)
                                    }
                                    Box {
                                        FilledTonalIconButton(
                                            onClick = { profileMenuExpanded = true },
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Filled.MoreVert, contentDescription = "Profile options", modifier = Modifier.size(20.dp))
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
                                            },
                                            username = profile.name
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = accent,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        HtmlText(
                            html = autoLinkAndMentions(stripImages(profile.bio ?: "<p>im a wasteof user, yay!</p>")),
                            maxLines = 4,
                            onMentionClick = onMentionClick,
                            onPostClick = onPostClickById,
                            openLinksInApp = openLinksInApp
                        )

                        // Joined Date / History Chip below bio
                        profile.history?.joined?.let { joinedTime ->
                            if (joinedTime > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val joinedDateStr = remember(joinedTime) {
                                    val date = java.util.Date(joinedTime)
                                    val format = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
                                    "Joined " + format.format(date)
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Forum,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = joinedDateStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStat(
                                label = "Followers",
                                value = profile.stats?.followers ?: 0,
                                accentColor = accent,
                                modifier = Modifier.weight(1f),
                                onClick = { onShowFollowers(profile.name) }
                            )
                            ProfileStat(
                                label = "Following",
                                value = profile.stats?.following ?: 0,
                                accentColor = accent,
                                modifier = Modifier.weight(1f),
                                onClick = { onShowFollowing(profile.name) }
                            )
                            ProfileStat(
                                label = "Posts",
                                value = profile.stats?.posts ?: 0,
                                accentColor = accent,
                                modifier = Modifier.weight(1f)
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
                items(posts.distinctBy { it.id }, key = { it.id }, contentType = { "post" }) { post ->
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
                        currentUsername = currentUsername,
                        savedAccounts = savedAccounts,
                        onEditPost = onEditPost,
                        showImages = showImages,
                        openLinksInApp = openLinksInApp,
                        linkPreviewPriority = linkPreviewPriority,
                        onPostClickById = onPostClickById,
                        blockedUsernames = blockedUsernames,
                        blockedQuoteHandling = blockedQuoteHandling
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
