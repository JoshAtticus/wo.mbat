package wombat.joshattic.us.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import wombat.joshattic.us.wear.WearViewModel
import wombat.joshattic.us.wear.ui.screens.ComposeScreen
import wombat.joshattic.us.wear.ui.screens.FeedScreen
import wombat.joshattic.us.wear.ui.screens.NotificationsScreen
import wombat.joshattic.us.wear.ui.screens.PostDetailScreen
import wombat.joshattic.us.wear.data.model.Post

object WearRoutes {
    const val FEED = "feed"
    const val POST_DETAIL = "post_detail"
    const val NOTIFICATIONS = "notifications"
    const val COMPOSE = "compose"
}

@Composable
fun WearApp(viewModel: WearViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberSwipeDismissableNavController()

    if (!uiState.isSessionLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.session == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open wo.mbat on your phone to log in.",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
    } else {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = WearRoutes.FEED
        ) {
        composable(WearRoutes.FEED) {
            FeedScreen(
                uiState = uiState,
                onPostClick = { post ->
                    viewModel.openPost(post)
                    navController.navigate(WearRoutes.POST_DETAIL)
                },
                onLoveClick = viewModel::toggleLove,
                onNotificationsClick = {
                    viewModel.loadNotifications()
                    navController.navigate(WearRoutes.NOTIFICATIONS)
                },
                onComposeClick = { navController.navigate(WearRoutes.COMPOSE) },
                onLoadMore = { viewModel.loadFeed() },
                onRefresh = { viewModel.loadFeed(refresh = true) }
            )
        }

        composable(WearRoutes.POST_DETAIL) {
            PostDetailScreen(
                uiState = uiState,
                onLoveClick = { viewModel.toggleLove(uiState.selectedPost!!) },
                onSubmitComment = { text ->
                    uiState.selectedPost?.let { viewModel.submitComment(it.id, text) }
                },
                onRepostClick = { post ->
                    viewModel.submitRepost(post.id)
                    navController.popBackStack()
                },
                onQuoteClick = { post ->
                    viewModel.setComposeQuote(post.id)
                    navController.navigate(WearRoutes.COMPOSE)
                },
                onBack = {
                    viewModel.closePost()
                    navController.popBackStack()
                }
            )
        }

        composable(WearRoutes.NOTIFICATIONS) {
            NotificationsScreen(
                uiState = uiState,
                onNotificationClick = { notif ->
                    notif.data.post?.let { post ->
                        viewModel.openPost(post)
                        viewModel.markNotificationRead(notif.id)
                        navController.navigate(WearRoutes.POST_DETAIL)
                    } ?: viewModel.markNotificationRead(notif.id)
                }
            )
        }

        composable(WearRoutes.COMPOSE) {
            ComposeScreen(
                uiState = uiState,
                onSubmit = { text ->
                    if (uiState.composeQuotePostId != null) {
                        viewModel.submitQuote(uiState.composeQuotePostId!!, text)
                        viewModel.setComposeQuote(null)
                    } else {
                        viewModel.submitPost(text)
                    }
                    navController.popBackStack()
                },
                onCancel = {
                    viewModel.setComposeQuote(null)
                    navController.popBackStack()
                }
            )
        }
    }
}
}
