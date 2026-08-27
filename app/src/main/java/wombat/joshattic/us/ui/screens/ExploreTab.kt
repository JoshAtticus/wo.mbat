@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User

private val TIMEFRAMES = listOf(
    null to "Best",
    "day" to "Today",
    "week" to "This week",
    "month" to "This month",
    "all" to "All time"
)

@Composable
fun ExploreTab(
    trendingPosts: List<Post>,
    trendingLoading: Boolean,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit,
    onOpenPost: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession> = emptyList(),
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    onPostClickById: ((String) -> Unit)? = null,
    followedUsernames: Set<String> = emptySet(),
    followLoadingUsernames: Set<String> = emptySet(),
    onFollowClick: ((String) -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning",
    // Search
    searchQuery: String = "",
    searchPostResults: List<Post> = emptyList(),
    searchUserResults: List<User> = emptyList(),
    searchLoading: Boolean = false,
    onSearchQueryChange: (String) -> Unit = {},
    // Timeframe
    selectedTimeframe: String? = null,
    onTimeframeChange: (String?) -> Unit = {}
) {
    val refreshState = rememberPullToRefreshState()
    var searchBarExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Search bar ────────────────────────────────────────────────────────
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = {
                        onSearchQueryChange(it)
                        if (it.isNotBlank()) searchBarExpanded = true
                    },
                    onSearch = {},
                    expanded = searchBarExpanded,
                    onExpandedChange = { searchBarExpanded = it },
                    placeholder = { Text("Search posts and people…") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    }
                )
            },
            expanded = searchBarExpanded,
            onExpandedChange = { searchBarExpanded = it },
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (searchBarExpanded) 0.dp else 16.dp)
                .padding(bottom = if (searchBarExpanded) 0.dp else 4.dp),
            colors = SearchBarDefaults.colors(
                containerColor = if (searchBarExpanded) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainerHigh,
                dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            // ── Search results ──────────────────────────────────────────────
            if (searchLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchQuery.isNotBlank()) {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
                    // User results row
                    if (searchUserResults.isNotEmpty()) {
                        item {
                            Text(
                                "People",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(searchUserResults.take(8), key = { it.id }) { user ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(68.dp)
                                            .clickable {
                                                searchBarExpanded = false
                                                onProfileClick(user.name)
                                            }
                                    ) {
                                        ProfilePicture(username = user.name, size = 52.dp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "@${user.name.lowercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Post results
                    if (searchPostResults.isNotEmpty()) {
                        item {
                            Text(
                                "Posts",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(
                            searchPostResults.distinctBy { it.id },
                            key = { it.id },
                            contentType = { "post" }
                        ) { post ->
                            val isFollowing = followedUsernames.contains(post.poster.name.lowercase())
                            val followLoading = followLoadingUsernames.contains(post.poster.name.lowercase())
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                PostCard(
                                    post = post,
                                    onClick = { searchBarExpanded = false; onOpenPost(post) },
                                    truncated = true,
                                    currentUsername = currentUsername,
                                    savedAccounts = savedAccounts,
                                    onMentionClick = onMentionClick,
                                    onProfileClick = onProfileClick,
                                    onLoveClick = onLoveClick,
                                    onPostClick = onOpenPost,
                                    onImageClick = onImageClick,
                                    onBlockUser = onBlockUser,
                                    onReportPost = onReportPost,
                                    onRepostClick = onRepostClick,
                                    onQuoteClick = onQuoteClick,
                                    onDeletePost = onDeletePost,
                                    onEditPost = onEditPost,
                                    showImages = showImages,
                                    openLinksInApp = openLinksInApp,
                                    linkPreviewPriority = linkPreviewPriority,
                                    onPostClickById = onPostClickById,
                                    isFollowing = isFollowing,
                                    followLoading = followLoading,
                                    onFollowClick = onFollowClick?.let { { it(post.poster.name) } },
                                    blockedUsernames = blockedUsernames,
                                    blockedQuoteHandling = blockedQuoteHandling
                                )
                            }
                        }
                    }

                    if (searchPostResults.isEmpty() && searchUserResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No results for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Trending content (hidden while search is expanded) ───────────────
        AnimatedVisibility(visible = !searchBarExpanded, enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Timeframe chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TIMEFRAMES) { (key, label) ->
                        FilterChip(
                            selected = selectedTimeframe == key,
                            onClick = { onTimeframeChange(key) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    state = refreshState,
                    isRefreshing = trendingLoading,
                    onRefresh = onRefresh
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Trending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (trendingPosts.isEmpty() && !trendingLoading) {
                            item {
                                EmptyStateCard(
                                    "No trending posts",
                                    "Pull to refresh or pick a different timeframe."
                                )
                            }
                        }

                        items(
                            trendingPosts.distinctBy { it.id },
                            key = { it.id },
                            contentType = { "post" }
                        ) { post ->
                            val isFollowing = followedUsernames.contains(post.poster.name.lowercase())
                            val followLoading = followLoadingUsernames.contains(post.poster.name.lowercase())
                            PostCard(
                                post = post,
                                onClick = { onOpenPost(post) },
                                truncated = true,
                                currentUsername = currentUsername,
                                savedAccounts = savedAccounts,
                                onMentionClick = onMentionClick,
                                onProfileClick = onProfileClick,
                                onLoveClick = onLoveClick,
                                onPostClick = onOpenPost,
                                onImageClick = onImageClick,
                                onBlockUser = onBlockUser,
                                onReportPost = onReportPost,
                                onRepostClick = onRepostClick,
                                onQuoteClick = onQuoteClick,
                                onDeletePost = onDeletePost,
                                onEditPost = onEditPost,
                                showImages = showImages,
                                openLinksInApp = openLinksInApp,
                                linkPreviewPriority = linkPreviewPriority,
                                onPostClickById = onPostClickById,
                                isFollowing = isFollowing,
                                followLoading = followLoading,
                                onFollowClick = onFollowClick?.let { { it(post.poster.name) } },
                                blockedUsernames = blockedUsernames,
                                blockedQuoteHandling = blockedQuoteHandling
                            )
                        }
                    }
                }
            }
        }
    }
}
