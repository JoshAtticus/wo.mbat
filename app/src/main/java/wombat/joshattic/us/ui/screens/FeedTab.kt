@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onDeletePost: ((Post) -> Unit)? = null
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        // Load more when reaching near the end
        LaunchedEffect(listState, posts.size) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 5) {
                        onLoadNextPage()
                    }
                }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!loading && posts.isEmpty()) {
                item { EmptyStateCard(title = "Nothing here yet", message = "Pull down to refresh.") }
            }

            items(posts, key = { it.id }, contentType = { "post" }) { post ->
                PostCard(
                    post = post,
                    onClick = { onPostClick(post) },
                    truncated = true,
                    currentUsername = currentUsername,
                    onMentionClick = onMentionClick,
                    onProfileClick = onProfileClick,
                    onLoveClick = onLoveClick,
                    onPostClick = onPostClick,
                    onImageClick = onImageClick,
                    onBlockUser = onBlockUser,
                    onReportPost = onReportPost,
                    onRepostClick = onRepostClick,
                    onQuoteClick = onQuoteClick,
                    onDeletePost = onDeletePost
                )
            }
        }
    }
}
