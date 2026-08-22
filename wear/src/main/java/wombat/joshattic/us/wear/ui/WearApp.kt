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
import wombat.joshattic.us.wear.ui.screens.ExploreScreen
import wombat.joshattic.us.wear.ui.screens.FeedScreen
import wombat.joshattic.us.wear.ui.screens.HomeScreen
import wombat.joshattic.us.wear.ui.screens.NotificationsScreen
import wombat.joshattic.us.wear.ui.screens.PostDetailScreen
import wombat.joshattic.us.wear.ui.screens.ProfileScreen
import wombat.joshattic.us.wear.ui.screens.WearOfflineScreen

object WearRoutes {
    const val HOME = "home"
    const val FEED = "feed"
    const val EXPLORE = "explore"
    const val POST_DETAIL = "post_detail"
    const val NOTIFICATIONS = "notifications"
    const val COMPOSE = "compose"
    const val PROFILE = "profile"   // navigates to profileUsername already set in uiState
}

@Composable
fun WearApp(viewModel: WearViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberSwipeDismissableNavController()

    if (!uiState.isOnline) {
        WearOfflineScreen(onRetry = { viewModel.loadFeed(refresh = true) })
    } else if (!uiState.isSessionLoaded) {
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
            startDestination = WearRoutes.HOME
        ) {

            // ── Home launcher ──────────────────────────────────────────────
            composable(WearRoutes.HOME) {
                HomeScreen(
                    uiState = uiState,
                    onProfileClick = {
                        viewModel.loadProfile()              // own profile
                        navController.navigate(WearRoutes.PROFILE)
                    },
                    onNotificationsClick = {
                        viewModel.loadNotifications()
                        navController.navigate(WearRoutes.NOTIFICATIONS)
                    },
                    onHomeClick = {
                        viewModel.loadFeed(refresh = true)
                        navController.navigate(WearRoutes.FEED)
                    },
                    onExploreClick = {
                        viewModel.loadExploreFeed(refresh = true)
                        navController.navigate(WearRoutes.EXPLORE)
                    },
                    onComposeClick = {
                        // Clear any stale success flag (e.g. left over from a repost)
                        // so ComposeScreen doesn't think a post just went through
                        viewModel.clearPostSuccess()
                        navController.navigate(WearRoutes.COMPOSE)
                    }
                )
            }

            // ── Home feed ──────────────────────────────────────────────────
            composable(WearRoutes.FEED) {
                FeedScreen(
                    uiState = uiState,
                    onPostClick = { post ->
                        viewModel.openPost(post)
                        navController.navigate(WearRoutes.POST_DETAIL)
                    },
                    onLoveClick = viewModel::toggleLove,
                    onLoadMore = { viewModel.loadFeed() },
                    onRefresh = { viewModel.loadFeed(refresh = true) }
                )
            }

            // ── Explore ────────────────────────────────────────────────────
            composable(WearRoutes.EXPLORE) {
                ExploreScreen(
                    uiState = uiState,
                    onPostClick = { post ->
                        viewModel.openPost(post)
                        navController.navigate(WearRoutes.POST_DETAIL)
                    },
                    onLoveClick = viewModel::toggleLove,
                    onSearch = { query -> viewModel.searchExplorePosts(query) },
                    onClearSearch = { viewModel.clearSearch() },
                    onLoadMore = {
                        if (uiState.searchQuery != null) viewModel.loadMoreSearchResults()
                    },
                    onRefresh = { viewModel.loadExploreFeed(refresh = true) }
                )
            }

            // ── Post detail ────────────────────────────────────────────────
            composable(WearRoutes.POST_DETAIL) {
                PostDetailScreen(
                    uiState = uiState,
                    onLoveClick = { viewModel.toggleLove(uiState.selectedPost!!) },
                    onSubmitComment = { text, parentId ->
                        uiState.selectedPost?.let { viewModel.submitComment(it.id, text, parentId) }
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

            // ── Notifications ──────────────────────────────────────────────
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

            // ── Compose ────────────────────────────────────────────────────
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
                        // No popBackStack here — ComposeScreen waits for the request
                        // to finish, then fires onPosted to open the new post
                    },
                    onPosted = {
                        viewModel.clearPostSuccess()
                        // Open the freshly created post (selectedPost is already set);
                        // pop the composer off the stack so back goes home, not composer
                        navController.navigate(WearRoutes.POST_DETAIL) {
                            popUpTo(WearRoutes.HOME)
                        }
                    },
                    onCancel = {
                        viewModel.setComposeQuote(null)
                        navController.popBackStack()
                    }
                )
            }

            // ── Profile ────────────────────────────────────────────────────
            composable(WearRoutes.PROFILE) {
                ProfileScreen(
                    uiState = uiState,
                    onPostClick = { post ->
                        viewModel.openPost(post)
                        navController.navigate(WearRoutes.POST_DETAIL)
                    },
                    onLoveClick = viewModel::toggleLove,
                    onLoadMorePosts = { viewModel.loadMoreProfilePosts() }
                )
            }
        }
    }
}
