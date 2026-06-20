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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wombat.joshattic.us.data.model.Post

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
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    onPostClickById: ((String) -> Unit)? = null
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = trendingLoading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (trendingPosts.isEmpty() && !trendingLoading) {
                item { EmptyStateCard("No trending posts (bug?)", "Pull to refresh to load trending posts.") }
            }

            items(trendingPosts, key = { it.id }, contentType = { "post" }) { post ->
                PostCard(
                    post = post,
                    onClick = { onOpenPost(post) },
                    truncated = true,
                    currentUsername = currentUsername,
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
                    onPostClickById = onPostClickById
                )
            }
        }
    }
}
