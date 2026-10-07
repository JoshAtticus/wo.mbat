@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
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
    followStatusLoading: Boolean = false,
    onFollowClick: ((String) -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning",
    searchQuery: String = "",
    searchPostResults: List<Post> = emptyList(),
    searchUserResults: List<User> = emptyList(),
    searchLoading: Boolean = false,
    searchHistory: List<String> = emptyList(),
    onClearSearchHistory: () -> Unit = {},
    onSaveSearch: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    selectedTimeframe: String? = null,
    onTimeframeChange: (String?) -> Unit = {}
) {
    val refreshState = rememberPullToRefreshState()
    var searchBarExpanded by remember { mutableStateOf(false) }

    // Back press closes search instead of leaving the app
    BackHandler(enabled = searchBarExpanded) {
        searchBarExpanded = false
        onSearchQueryChange("")
    }
    // Collapse when the query is cleared externally (e.g. re-selecting the explore tab)
    var hadQuery by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            hadQuery = true
        } else if (hadQuery) {
            hadQuery = false
            searchBarExpanded = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val searchFocusRequester = remember { FocusRequester() }
        if (searchBarExpanded) {
            // Fullscreen search composed directly, so there's no expand animation from a pill
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .focusRequester(searchFocusRequester),
                        placeholder = { Text("Search posts and people…") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            // Keyboard tick — commit the search to history and drop the keyboard
                            onSaveSearch()
                            keyboardController?.hide()
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    LaunchedEffect(Unit) { searchFocusRequester.requestFocus() }
            if (searchLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchQuery.isNotBlank()) {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())) {
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
                                                onSaveSearch()
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
                            val followLoading = followLoadingUsernames.contains(post.poster.name.lowercase()) ||
                                (followStatusLoading && !isFollowing)
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                PostCard(
                                    post = post,
                                    onClick = { searchBarExpanded = false; onSaveSearch(); onOpenPost(post) },
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
        }
        } else {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { searchBarExpanded = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Search posts and people…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedVisibility(visible = !searchBarExpanded, enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                            val followLoading = followLoadingUsernames.contains(post.poster.name.lowercase()) ||
                                (followStatusLoading && !isFollowing)
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
