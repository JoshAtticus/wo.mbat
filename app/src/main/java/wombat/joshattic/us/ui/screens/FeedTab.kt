@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import wombat.joshattic.us.data.model.Post

@Composable
fun FeedTab(
    posts: List<Post>,
    loading: Boolean,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    onLoadNextPage: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession> = emptyList(),
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    scrollToTop: Boolean = false,
    onScrollToTopComplete: () -> Unit = {},
    newPostsUsernames: List<String> = emptyList(),
    onClearNewPosts: () -> Unit = {},
    showImages: Boolean = true,
    showNewPosts: Boolean = true,
    openLinksInApp: Boolean = true,
    onPostClickById: ((String) -> Unit)? = null,
    followedUsernames: Set<String> = emptySet(),
    followLoadingUsernames: Set<String> = emptySet(),
    onFollowClick: ((String) -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning"
) {
    val coroutineScope = rememberCoroutineScope()
    val refreshState = rememberPullToRefreshState()

    var wasAtTopBeforeUpdate by remember { mutableStateOf(true) }
    var previousTopPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 10 }
            .collect { wasAtTopBeforeUpdate = it }
    }

    LaunchedEffect(posts) {
        if (posts.isNotEmpty()) {
            val currentTopId = posts.firstOrNull()?.id
            if (previousTopPostId != null && currentTopId != previousTopPostId) {
                if (wasAtTopBeforeUpdate) {
                    listState.animateScrollToItem(0)
                }
            }
            previousTopPostId = currentTopId
        }
    }

    LaunchedEffect(wasAtTopBeforeUpdate, newPostsUsernames) {
        if (wasAtTopBeforeUpdate && newPostsUsernames.isNotEmpty()) {
            onClearNewPosts()
        }
    }

    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            listState.animateScrollToItem(0)
            onScrollToTopComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            state = rememberPullToRefreshState(),
            isRefreshing = loading,
            onRefresh = onRefresh
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = if (showNewPosts && newPostsUsernames.isNotEmpty()) 48.dp else 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!loading && posts.isEmpty()) {
                    item { EmptyStateCard(title = "Nothing here yet", message = "Pull down to refresh.") }
                }

                items(posts.distinctBy { it.id }, key = { it.id }, contentType = { "post" }) { post ->
                    val isPosterFollowed = followedUsernames.contains(post.poster.name.lowercase())
                    val isFollowLoading = followLoadingUsernames.contains(post.poster.name.lowercase())
                    PostCard(
                        post = post,
                        onClick = { onPostClick(post) },
                        truncated = true,
                        currentUsername = currentUsername,
                        savedAccounts = savedAccounts,
                        onMentionClick = onMentionClick,
                        onProfileClick = onProfileClick,
                        onLoveClick = onLoveClick,
                        onPostClick = onPostClick,
                        onImageClick = onImageClick,
                        onBlockUser = onBlockUser,
                        onReportPost = onReportPost,
                        onRepostClick = onRepostClick,
                        onQuoteClick = onQuoteClick,
                        onDeletePost = onDeletePost,
                        onEditPost = onEditPost,
                        showImages = showImages,
                        openLinksInApp = openLinksInApp,
                        onPostClickById = onPostClickById,
                        isFollowing = isPosterFollowed,
                        followLoading = isFollowLoading,
                        onFollowClick = onFollowClick?.let { action -> { action(post.poster.name) } },
                        blockedUsernames = blockedUsernames,
                        blockedQuoteHandling = blockedQuoteHandling
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showNewPosts && newPostsUsernames.isNotEmpty() && !wasAtTopBeforeUpdate,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .zIndex(10f)
        ) {
            NewPostsButton(
                usernames = newPostsUsernames,
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                    onClearNewPosts()
                }
            )
        }
    }
}

@Composable
fun NewPostsButton(
    usernames: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer(rotationZ = 180f)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(
                        if (usernames.isEmpty()) 0.dp 
                        else (24 + (usernames.size - 1) * 16).dp
                    )
            ) {
                usernames.forEachIndexed { index, username ->
                    Box(
                        modifier = Modifier
                            .offset(x = (index * 16).dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        ProfilePicture(username = username, size = 24.dp)
                    }
                }
            }

            Text(
                text = "New Posts",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
