package wombat.joshattic.us.wear.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyColumnDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
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
    val context = LocalContext.current
    var guestMode by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (!error.isNullOrBlank()) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    if (!uiState.isOnline) {
        WearOfflineScreen(onRetry = { viewModel.loadFeed(refresh = true) })
    } else if (!uiState.isSessionLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.session == null && !guestMode) {
        WearWelcomeScreen(
            onExploreClick = {
                guestMode = true
                viewModel.loadFeed(refresh = true)
            },
            onRefreshLoginClick = {
                viewModel.refreshSession()
                Toast.makeText(context, "Checking login...", Toast.LENGTH_SHORT).show()
            }
        )
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
                        if (uiState.session != null) {
                            viewModel.loadProfile()
                            navController.navigate(WearRoutes.PROFILE)
                        } else {
                            Toast.makeText(context, "Log in on phone to view profile", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onNotificationsClick = {
                        if (uiState.session != null) {
                            viewModel.loadNotifications()
                            navController.navigate(WearRoutes.NOTIFICATIONS)
                        } else {
                            Toast.makeText(context, "Log in on phone for notifications", Toast.LENGTH_SHORT).show()
                        }
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
                        if (uiState.session != null) {
                            viewModel.clearPostSuccess()
                            navController.navigate(WearRoutes.COMPOSE)
                        } else {
                            Toast.makeText(context, "Log in on phone to create posts", Toast.LENGTH_SHORT).show()
                        }
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
                    onLoveClick = {
                        uiState.selectedPost?.let { viewModel.toggleLove(it) }
                    },
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
                        val quoteId = uiState.composeQuotePostId
                        if (quoteId != null) {
                            viewModel.submitQuote(quoteId, text)
                            viewModel.setComposeQuote(null)
                        } else {
                            viewModel.submitPost(text)
                        }
                    },
                    onPosted = {
                        viewModel.clearPostSuccess()
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

@Composable
private fun WearWelcomeScreen(
    onExploreClick: () -> Unit,
    onRefreshLoginClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            autoCentering = AutoCenteringParams(itemIndex = 0),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 28.dp, bottom = 36.dp),
            scalingParams = ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.5f,
                edgeAlpha = 0.0f,
                minTransitionArea = 0.2f,
                maxTransitionArea = 0.35f
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "wo.mbat",
                    style = MaterialTheme.typography.title2,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            item {
                Text(
                    text = "Open wo.mbat on your phone to log in, or continue as guest.",
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    onClick = onExploreClick,
                    label = { Text("Browse Feeds", maxLines = 1) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = null) },
                    colors = ChipDefaults.primaryChipColors()
                )
            }
            item {
                CompactChip(
                    onClick = onRefreshLoginClick,
                    label = { Text("Check Login", maxLines = 1) },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}
