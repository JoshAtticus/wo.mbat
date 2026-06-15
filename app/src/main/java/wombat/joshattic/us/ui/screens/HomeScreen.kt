@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.key
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import android.text.TextPaint
import android.view.View
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.ui.state.BottomTab
import wombat.joshattic.us.ui.theme.getUserColorSchemeColors
import wombat.joshattic.us.ui.viewmodel.HomeViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(titleForTab(uiState.selectedTab)) }
            )
        },
        floatingActionButton = {
            if (uiState.selectedTab == BottomTab.Home) {
                FloatingActionButton(
                    onClick = {
                        if (uiState.session != null) {
                            viewModel.toggleComposer()
                        } else {
                            viewModel.selectTab(BottomTab.Account)
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = "Create post", modifier = Modifier.size(28.dp))
                }
            }
        },
        bottomBar = {
            WombatBottomNavigationBar(
                selectedTab = uiState.selectedTab,
                unreadCount = uiState.unreadNotificationCount,
                accountLabel = uiState.accountLabel,
                profilePictureUrl = uiState.session?.username?.let { "https://api.wasteof.money/users/$it/picture" },
                onTabSelected = viewModel::selectTab
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (uiState.viewingProfileUsername != null) {
                ProfileScreen(
                    profile = uiState.viewingProfile,
                    posts = uiState.viewingProfilePosts,
                    loading = uiState.viewingProfileLoading,
                    isBlocked = uiState.viewingProfileUsername?.let { uiState.blockedUsernames.contains(it.lowercase()) } == true,
                    isFollowing = uiState.viewingProfileIsFollowing,
                    followLoading = uiState.viewingProfileFollowLoading,
                    currentUsername = uiState.session?.username,
                    onClose = viewModel::closeProfile,
                    onFollowClick = viewModel::toggleViewedProfileFollow,
                    onBlockClick = { username -> viewModel.blockUser(username) },
                    onBlockReportClick = { username -> viewModel.blockUser(username, reported = true) },
                    onUnblockClick = viewModel::unblockViewedProfile,
                    onPostClick = viewModel::openPost,
                    onMentionClick = { username ->
                        viewModel.openProfile(username)
                    },
                    onProfileClick = viewModel::openProfile,
                    onLoveClick = viewModel::togglePostLove,
                    onImageClick = viewModel::openFullScreenImages,
                    onLoadNextPage = viewModel::loadNextProfilePage
                )
            } else {
                val pagerState = rememberPagerState(initialPage = uiState.selectedTab.ordinal) { 4 }

                LaunchedEffect(pagerState.settledPage) {
                    val targetTab = BottomTab.values()[pagerState.settledPage]
                    if (uiState.selectedTab != targetTab) {
                        viewModel.selectTab(targetTab)
                    }
                }

                LaunchedEffect(uiState.selectedTab) {
                    if (pagerState.currentPage != uiState.selectedTab.ordinal) {
                        pagerState.animateScrollToPage(uiState.selectedTab.ordinal)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (BottomTab.values()[page]) {
                        BottomTab.Home -> FeedTab(
                            posts = uiState.feed,
                            loading = uiState.feedLoading,
                            onRefresh = viewModel::refreshFeed,
                            onPostClick = viewModel::openPost,
                            onMentionClick = { username ->
                                viewModel.openProfile(username)
                            },
                            onProfileClick = viewModel::openProfile,
                            onLoveClick = viewModel::togglePostLove,
                            onImageClick = viewModel::openFullScreenImages,
                            onBlockUser = { username -> viewModel.blockUser(username) },
                            onBlockReportUser = { username -> viewModel.blockUser(username, reported = true) },
                            onLoadNextPage = viewModel::loadNextFeedPage
                        )
                        BottomTab.Explore -> ExploreTab(
                            trendingPosts = uiState.exploreTrendingPosts,
                            trendingLoading = uiState.exploreTrendingLoading,
                            onRefresh = viewModel::loadExploreTrending,
                            onOpenPost = viewModel::openPost,
                            onMentionClick = { username ->
                                viewModel.openProfile(username)
                            },
                            onProfileClick = viewModel::openProfile,
                            onLoveClick = viewModel::togglePostLove,
                            onImageClick = viewModel::openFullScreenImages,
                            onBlockUser = { username -> viewModel.blockUser(username) },
                            onBlockReportUser = { username -> viewModel.blockUser(username, reported = true) }
                        )
                        BottomTab.Notifications -> NotificationsTab(
                            session = uiState.session,
                            unreadNotifications = uiState.unreadNotifications,
                            readNotifications = uiState.readNotifications,
                            loading = uiState.notificationsLoading,
                            onRefresh = viewModel::refreshNotifications,
                            onMarkAllRead = viewModel::markAllNotificationsRead,
                            onNotificationClick = viewModel::handleNotificationClick
                        )
                        BottomTab.Account -> AccountTab(
                            session = uiState.session,
                            profile = uiState.accountProfile,
                            posts = uiState.accountPosts,
                            loading = uiState.accountLoading,
                            loginUsername = uiState.loginUsername,
                            loginPassword = uiState.loginPassword,
                            loginLoading = uiState.authLoading,
                            loginError = uiState.loginError,
                            onUsernameChange = viewModel::setLoginUsername,
                            onPasswordChange = viewModel::setLoginPassword,
                            onLogin = viewModel::login,
                            onLogout = viewModel::logout,
                            onRefresh = viewModel::refreshAccount,
                            onPostClick = viewModel::openPost,
                            onMentionClick = { username ->
                                viewModel.openProfile(username)
                            },
                            onLoveClick = viewModel::togglePostLove,
                            onImageClick = viewModel::openFullScreenImages,
                            onLoadNextPage = viewModel::loadNextAccountPage
                        )
                    }
                }
            }

            if (uiState.showComposer) {
                ComposerSheet(
                    draft = uiState.composeDraft,
                    onDraftChange = viewModel::setComposeDraft,
                    onSubmit = viewModel::submitPost,
                    onDismiss = {
                        viewModel.saveCurrentDraft()
                        viewModel.setComposeDraft("")
                        viewModel.toggleComposer()
                    },
                    drafts = uiState.composerDrafts,
                    onRestoreDraft = viewModel::restoreDraft,
                    onDeleteDraft = viewModel::deleteDraft
                )
            }

            uiState.selectedPost?.let { post ->
                PostDetailsSheet(
                    post = post,
                    comments = uiState.comments,
                    loading = uiState.commentsLoading,
                    draft = uiState.commentDraft,
                    onDraftChange = viewModel::setCommentDraft,
                    onSubmit = viewModel::submitComment,
                    onDismiss = viewModel::closePost,
                    onExpandComments = viewModel::loadCommentsForCurrentPost,
                    onCollapseComments = viewModel::clearComments,
                    onMentionClick = { username ->
                        viewModel.openProfile(username)
                    },
                    replyingTo = uiState.commentReplyParent,
                    onCancelReply = { viewModel.setCommentReplyParent(null) },
                    onReplyToComment = viewModel::setCommentReplyParent,
                    onProfileClick = viewModel::openProfile,
                    onLoveClick = viewModel::togglePostLove,
                    onPostClick = viewModel::openPost,
                    onBlockUser = { username: String -> viewModel.blockUser(username) },
                    onBlockReportUser = { username: String -> viewModel.blockUser(username, reported = true) },
                    scrollToCommentId = uiState.scrollToCommentId,
                    onScrollToCommentComplete = viewModel::clearScrollToComment,
                    onImageClick = viewModel::openFullScreenImages
                )
            }
        }
    }

    uiState.fullScreenImages?.let { images ->
        FullScreenImageViewer(
            images = images,
            initialIndex = uiState.initialFullScreenImageIndex,
            onDismiss = viewModel::closeFullScreenImages
        )
    }
}

@Composable
private fun FeedTab(
    posts: List<Post>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onBlockReportUser: ((String) -> Unit)? = null,
    onLoadNextPage: () -> Unit = {}
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
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

            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onClick = { onPostClick(post) },
                    truncated = true,
                    onMentionClick = onMentionClick,
                    onProfileClick = onProfileClick,
                    onLoveClick = onLoveClick,
                    onPostClick = onPostClick,
                    onImageClick = onImageClick,
                    onBlockUser = onBlockUser,
                    onBlockReportUser = onBlockReportUser
                )
            }
        }
    }
}

@Composable
private fun ExploreTab(
    trendingPosts: List<Post>,
    trendingLoading: Boolean,
    onRefresh: () -> Unit,
    onOpenPost: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onBlockReportUser: ((String) -> Unit)? = null
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = trendingLoading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (trendingPosts.isEmpty() && !trendingLoading) {
                item { EmptyStateCard("No trending posts (bug?)", "Pull to refresh to load trending posts.") }
            }

            items(trendingPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onClick = { onOpenPost(post) },
                    truncated = true,
                    onMentionClick = onMentionClick,
                    onProfileClick = onProfileClick,
                    onLoveClick = onLoveClick,
                    onPostClick = onOpenPost,
                    onImageClick = onImageClick,
                    onBlockUser = onBlockUser,
                    onBlockReportUser = onBlockReportUser
                )
            }
        }
    }
}

@Composable
private fun NotificationsTab(
    session: Any?,
    unreadNotifications: List<Notification>,
    readNotifications: List<Notification>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (Notification) -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val unreadCount = unreadNotifications.size
                        Text(
                            text = "$unreadCount unread notification${if (unreadCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (session == null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sign in to receive alerts about loves, comments, reposts, and follows.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (unreadNotifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onMarkAllRead) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark all read")
                            }
                        }
                    }
                }
            }

            if (unreadNotifications.isEmpty() && !loading) {
                item { EmptyStateCard("All clear", "No unread notifications right now.") }
            }

            items(unreadNotifications, key = { it.id }) { notification ->
                NotificationCard(notification, onClick = { onNotificationClick(notification) })
            }

            if (readNotifications.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "Read notifications",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                items(readNotifications, key = { it.id }) { notification ->
                    NotificationCard(notification, onClick = { onNotificationClick(notification) })
                }
            }
        }
    }
}

@Composable
private fun AccountTab(
    session: Any?,
    profile: User?,
    posts: List<Post>,
    loading: Boolean,
    loginUsername: String,
    loginPassword: String,
    loginLoading: Boolean,
    loginError: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onLoadNextPage: () -> Unit = {}
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        val listState = rememberLazyListState()

        // Load more when reaching near the end
        LaunchedEffect(listState, posts.size) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 5 && session != null) {
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
            if (session != null) {
                // Profile details card
                item {
                    if (profile != null) {
                        val accent = getUserColorSchemeColors(profile.color).first
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProfilePicture(username = profile.name, size = 56.dp, borderColor = accent)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(profile.name, style = MaterialTheme.typography.titleLarge, color = accent)
                                        Text(
                                            text = if (profile.online) "Online now" else "Offline",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                HtmlText(autoLinkAndMentions(stripImages(profile.bio ?: "<p>No bio yet.</p>")))
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ProfileStat("Followers", profile.stats?.followers ?: 0, accentColor = accent)
                                    ProfileStat("Following", profile.stats?.following ?: 0, accentColor = accent)
                                    ProfileStat("Posts", profile.stats?.posts ?: 0, accentColor = accent)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    TextButton(onClick = onLogout) {
                                        Text("Sign out")
                                    }
                                }
                            }
                        }
                    }
                    // no loading box - PTR handles it
                }

                // User's posts under the account details (only after profile available)
                if (profile != null) {
                    if (!loading && posts.isEmpty()) {
                        item { EmptyStateCard(title = "No posts yet", message = "Pull down to refresh or create your first post.") }
                    }

                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onClick = { onPostClick(post) },
                            truncated = true,
                            onMentionClick = onMentionClick,
                            onProfileClick = onProfileClick,
                            onLoveClick = onLoveClick,
                            onPostClick = onPostClick,
                            onImageClick = onImageClick
                        )
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sign in", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Use your wasteof.money account to post, comment, and see notifications.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = loginUsername,
                                onValueChange = onUsernameChange,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = onPasswordChange,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation()
                            )
                            if (!loginError.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(loginError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onLogin, enabled = !loginLoading) {
                                if (loginLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Sign in")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun htmlToAnnotated(html: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val openTags = mutableListOf<Pair<String, Int>>()
        while (i < html.length) {
            if (html[i] == '<') {
                val j = html.indexOf('>', i)
                if (j == -1) {
                    append(html.substring(i))
                    break
                }
                val tagContent = html.substring(i + 1, j).trim().lowercase()
                val isClose = tagContent.startsWith("/")
                val tag = if (isClose) tagContent.substring(1).split(" ")[0] else tagContent.split(" ")[0]
                if (tag == "br" || tag == "br/") {
                    append("\n")
                } else if (tag == "img") {
                    // images handled separately via currentImages
                } else if (isClose) {
                    val idx = openTags.indexOfLast { it.first == tag }
                    if (idx != -1) {
                        val (_, startPos) = openTags.removeAt(idx)
                        val style = when (tag) {
                            "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                            "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
                            "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                            "s" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                            else -> null
                        }
                        if (style != null) {
                            addStyle(style, startPos, length)
                        }
                    }
                } else {
                    openTags.add(tag to length)
                }
                i = j + 1
                continue
            }
            val nextTag = html.indexOf('<', i)
            val chunkEnd = if (nextTag == -1) html.length else nextTag
            append(html.substring(i, chunkEnd))
            i = chunkEnd
        }
        // close any unclosed tags
        openTags.forEach { (tag, startPos) ->
            val style = when (tag) {
                "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
                "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                "s" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                else -> null
            }
            if (style != null) {
                addStyle(style, startPos, length)
            }
        }
    }
}

private fun annotatedToHtml(annotated: AnnotatedString): String {
    val text = annotated.text
    if (text.isBlank()) return ""
    val events = mutableListOf<Pair<Int, String>>()
    annotated.spanStyles.forEach { range ->
        val style = range.item
        val tag = when {
            style.fontWeight == FontWeight.Bold || (style.fontWeight?.weight ?: 0) >= 600 -> "b"
            style.fontStyle == FontStyle.Italic -> "i"
            style.textDecoration == TextDecoration.Underline -> "u"
            style.textDecoration == TextDecoration.LineThrough -> "s"
            else -> null
        }
        if (tag != null) {
            events.add(range.start to "<$tag>")
            events.add(range.end to "</$tag>")
        }
    }
    events.sortBy { it.first }
    val sb = StringBuilder()
    var pos = 0
    for ((p, tagStr) in events) {
        if (p > pos) sb.append(text.substring(pos, p))
        sb.append(tagStr)
        pos = p
    }
    if (pos < text.length) sb.append(text.substring(pos))
    val inner = sb.toString().replace("\n", "</p><p>")
    return "<p>$inner</p>"
}

@Composable
private fun ComposerSheet(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    drafts: List<String>,
    onRestoreDraft: (String) -> Unit,
    onDeleteDraft: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddImage by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf<String?>(null) }
    var showDraftsDialog by remember { mutableStateOf(false) }

    // Images attached (separate from text markdown, shown as thumbnails below)
    var currentImages by remember(draft) { mutableStateOf(extractImages(draft)) }

    fun addImage() {
        val url = imageUrl.trim()
        if (url.isBlank()) {
            imageError = "Enter an image URL"
            return
        }
        if (!isAllowedImageHost(url)) {
            imageError = "Image URL must be from i.ibb.co or u.cubeupload.com"
            return
        }
        currentImages = currentImages + url
        imageUrl = ""
        imageError = null
        showAddImage = false
    }

    if (showDraftsDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDraftsDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Drafts Manager", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = { showDraftsDialog = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                if (drafts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No saved drafts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(drafts) { d ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        IconButton(onClick = {
                                            onRestoreDraft(d)
                                            showDraftsDialog = false
                                        }) {
                                            Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { onDeleteDraft(d) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New post", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { showDraftsDialog = true }) {
                    Text("Drafts (${drafts.size})")
                }
            }

            // Image attach button only (styling removed - use markdown manually in the text box)
            IconButton(onClick = { showAddImage = !showAddImage; imageError = null }, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.Image, contentDescription = "Add image", modifier = Modifier.size(24.dp))
            }

            if (showAddImage) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it; imageError = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://i.ibb.co/xxx or https://u.cubeupload.com/xxx") },
                        label = { Text("Image URL (i.ibb.co or u.cubeupload.com only)") }
                    )
                    if (imageError != null) {
                        Text(imageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { addImage() }, enabled = imageUrl.isNotBlank()) {
                            Text("Insert Image")
                        }
                        TextButton(onClick = {
                            showAddImage = false
                            imageUrl = ""
                            imageError = null
                        }) {
                            Text("Cancel")
                        }
                    }
                }
            }

            // Direct editable text input (no outer box, input itself sized and styled)
            val wordCount = draft.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    placeholder = { Text("What's happening? Write words here, markdown supported.") }
                )
                Text(
                    text = "$wordCount / ${HomeViewModel.MAX_WORD_COUNT} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (wordCount > HomeViewModel.MAX_WORD_COUNT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Image attachments preview - shown below the text editor like normal post composer (thumbnails, removable)
            if (currentImages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentImages.forEachIndexed { index, url ->
                        Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Attached image",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = {
                                    currentImages = currentImages.toMutableList().apply { removeAt(index) }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove image",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Button(
                    onClick = {
                        // Convert markdown to HTML AFTER pressing post. Append attached images.
                        val md = draft
                        val htmlText = markdownToHtml(md)
                        val imgTags = currentImages.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
                        val fullHtml = if (imgTags.isBlank()) htmlText else "$htmlText\n$imgTags"
                        onDraftChange(fullHtml)
                        onSubmit()
                    },
                    enabled = (draft.isNotBlank() || currentImages.isNotEmpty()) && wordCount <= HomeViewModel.MAX_WORD_COUNT,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PostDetailsSheet(
    post: Post,
    comments: List<Comment>,
    loading: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onExpandComments: () -> Unit,
    onCollapseComments: () -> Unit,
    onMentionClick: (String) -> Unit,
    replyingTo: Comment? = null,
    onCancelReply: () -> Unit = {},
    onReplyToComment: (Comment) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onPostClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onBlockReportUser: ((String) -> Unit)? = null,
    scrollToCommentId: String? = null,
    onScrollToCommentComplete: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val listState = rememberLazyListState()

    // Use local state to track if user has expanded (to prevent auto-expand on future post opens)
    var hasUserExpandedComments by remember { mutableStateOf(false) }
    var isFirstValueReport by remember { mutableStateOf(true) }

    val currentComments by rememberUpdatedState(comments)
    val currentOnCollapse by rememberUpdatedState(onCollapseComments)
    val currentOnExpand by rememberUpdatedState(onExpandComments)

    LaunchedEffect(scrollToCommentId, loading, comments) {
        if (scrollToCommentId != null && !loading && comments.isNotEmpty()) {
            val index = comments.indexOfFirst { it.id == scrollToCommentId }
            if (index != -1) {
                hasUserExpandedComments = true
                onExpandComments()
                listState.animateScrollToItem(index + 1) // +1 for post header
                onScrollToCommentComplete()
            }
        }
    }

    // React to sheet value changes, but *ignore the very first value report* after the composable mounts for this post.
    // This ensures that even if the sheet opens already reporting Expanded (due to tall content or animation),
    // we do not auto-load or show comments. The user must perform a swipe that causes a *subsequent* change
    // to Expanded.
    LaunchedEffect(sheetState.currentValue) {
        val current = sheetState.currentValue
        if (isFirstValueReport) {
            isFirstValueReport = false
            return@LaunchedEffect
        }
        when (current) {
            SheetValue.Expanded -> {
                hasUserExpandedComments = true
                currentOnExpand()
            }
            SheetValue.PartiallyExpanded -> {
                if (currentComments.isEmpty()) {
                    hasUserExpandedComments = false
                    currentOnCollapse()
                }
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            val showCommentsSection = hasUserExpandedComments || comments.isNotEmpty()

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PostCard(
                        post = post,
                        onClick = {},
                        clickable = false,
                        truncated = false,
                        onMentionClick = onMentionClick,
                        onProfileClick = onProfileClick,
                        onLoveClick = onLoveClick,
                        onPostClick = onPostClick,
                        onImageClick = onImageClick,
                        onBlockUser = onBlockUser,
                        onBlockReportUser = onBlockReportUser
                    )
                }

                if (showCommentsSection) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Comments", style = MaterialTheme.typography.titleMedium)
                            if (loading) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Loading comments...", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else if (comments.isEmpty()) {
                                EmptyStateCard("No comments yet", "Start the conversation.")
                            }
                        }
                    }

                    if (!loading && comments.isNotEmpty()) {
                        items(comments, key = { it.id }) { comment ->
                            CommentCard(
                                comment = comment,
                                onReply = onReplyToComment,
                                onProfileClick = onProfileClick
                            )
                        }
                    }
                } else {
                    item {
                        // Hint for less verbose UI; user must swipe up on the sheet to reveal comments
                        Text(
                            "Swipe up to view comments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Input area (TextField + Reply button) — imePadding lifts it above the software keyboard
            if (showCommentsSection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (replyingTo != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Replying to @${replyingTo.poster.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = onCancelReply) {
                                Text("Cancel")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = { Text("Write a reply") }
                    )
                    Button(onClick = onSubmit, enabled = draft.isNotBlank()) {
                        Text("Reply")
                    }
                }
            }
        }
    }
}

@Composable
private fun WombatBottomNavigationBar(
    selectedTab: BottomTab,
    unreadCount: Int,
    accountLabel: String,
    profilePictureUrl: String?,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = selectedTab == BottomTab.Home,
            onClick = { onTabSelected(BottomTab.Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.Explore,
            onClick = { onTabSelected(BottomTab.Explore) },
            icon = { Icon(Icons.Filled.Tag, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.Notifications,
            onClick = { onTabSelected(BottomTab.Notifications) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                }
            },
            label = { Text("Notifications") }
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.Account,
            onClick = { onTabSelected(BottomTab.Account) },
            icon = {
                if (profilePictureUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePictureUrl)
                            .decoderFactory(SvgDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = accountLabel,
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Account")
                }
            },
            label = { Text(accountLabel) }
        )
    }
}

@Composable
private fun ProfileHeader(profile: User) {
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
                        text = if (profile.online) "Online now" else "Offline",
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
private fun ProfileScreen(
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
    onUnblockClick: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onLoadNextPage: () -> Unit = {}
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
        // Keep the banner modest so it doesn't eat the screen.
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
                        .height(80.dp)
                        .background(accent.copy(alpha = 0.18f))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bannerUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProfilePicture(username = profile.name, size = 56.dp, borderColor = accent)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = accent
                                )
                                Text(
                                    text = if (profile.online) "Online now" else "Offline",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (currentUsername?.equals(profile.name, ignoreCase = true) != true) {
                                Button(
                                    onClick = onFollowClick,
                                    enabled = currentUsername != null && !followLoading
                                ) {
                                    Text(if (isFollowing == true) "Unfollow" else "Follow")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HtmlText(
                            autoLinkAndMentions(stripImages(profile.bio ?: "<p>No bio yet.</p>")),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileStat("Followers", profile.stats?.followers ?: 0, accentColor = accent)
                            ProfileStat("Following", profile.stats?.following ?: 0, accentColor = accent)
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

            if (posts.isNotEmpty()) {
                items(posts, key = { it.id }) { post ->
                    Box() {
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
                            onBlockReportUser = onBlockReportClick
                        )
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No posts yet")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: Int, accentColor: Color? = null) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = accentColor ?: MaterialTheme.colorScheme.onSurface
            )
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UserActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onBlock: () -> Unit,
    onBlockReport: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Block") },
            onClick = onBlock
        )
        DropdownMenuItem(
            text = { Text("Block & Report") },
            onClick = onBlockReport
        )
    }
}

@Composable
private fun PostCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    clickable: Boolean = true,
    truncated: Boolean = false,
    onMentionClick: ((String) -> Unit)? = null,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: ((Post) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onBlockReportUser: ((String) -> Unit)? = null
) {
    val imageUrls = extractImages(post.content)
    val displayContent = autoLinkAndMentions(stripImages(post.content))
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = if (clickable) Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp) else Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProfileClick(post.poster.name) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePicture(username = post.poster.name, size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(post.poster.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(formatTime(post.time), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (onBlockUser != null && onBlockReportUser != null) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Post options")
                        }
                        UserActionsMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            onBlock = {
                                menuExpanded = false
                                onBlockUser?.invoke(post.poster.name)
                            },
                            onBlockReport = {
                                menuExpanded = false
                                onBlockReportUser?.invoke(post.poster.name)
                            }
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HtmlText(
                    displayContent,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = if (truncated) 6 else Int.MAX_VALUE,
                    onMentionClick = onMentionClick,
                    onClick = onClick
                )
                post.repost?.let { repostPost ->
                    val repostDisplay = autoLinkAndMentions(stripImages(repostPost.content))
                    Card(
                        shape = androidx.compose.ui.graphics.RectangleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = onPostClick != null) {
                                onPostClick?.invoke(repostPost)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProfilePicture(username = repostPost.poster.name, size = 24.dp)
                                Column {
                                    Text("Repost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(repostPost.poster.name, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            HtmlText(repostDisplay, maxLines = 4, onClick = { onPostClick?.invoke(repostPost) })
                        }
                    }
                }
            }
            if (imageUrls.isNotEmpty()) {
                PostImageCarousel(
                    images = imageUrls,
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    isDetailView = !truncated
                )
            }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PostMetric(
                    value = post.loves,
                    label = "loves",
                    icon = if (post.isLoving == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    isActive = post.isLoving == true,
                    onClick = onLoveClick?.let { { onLoveClick(post) } }
                )
                PostMetric(post.comments, "comments", Icons.Filled.Chat)
                PostMetric(post.reposts, "reposts", Icons.Filled.Repeat)
            }
        }
    }
}

@Composable
private fun CommentCard(comment: Comment, onReply: (Comment) -> Unit = {}, onProfileClick: (String) -> Unit = {}) {
    val isReply = comment.parent != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReply) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.clickable { onProfileClick(comment.poster.name) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfilePicture(username = comment.poster.name, size = 32.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(comment.poster.name, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(formatTime(comment.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = { onReply(comment) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Chat, contentDescription = "Reply", modifier = Modifier.size(16.dp))
                }
            }
            HtmlText(autoLinkAndMentions(stripImages(comment.content)))

            val safeReplies = comment.replies ?: emptyList()
            if (safeReplies.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    safeReplies.forEach { reply ->
                        CommentCard(comment = reply, onReply = onReply, onProfileClick = onProfileClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!notification.read) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = notification.data.actor.name, size = 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(notification.data.actor.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = notificationLabel(notification.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    formatTime(notification.time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val content = when (notification.type.lowercase()) {
                "comment", "wall_comment", "wall_comment_reply" -> notification.data.comment?.content
                "post_mention", "repost" -> notification.data.post?.content
                else -> null
            }

            if (content != null) {
                HtmlText(
                    html = content,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    onClick = onClick
                )
            } else if (notification.data.post != null) {
                HtmlText(
                    html = notification.data.post.content,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun ProfilePicture(username: String, size: androidx.compose.ui.unit.Dp, borderColor: Color? = null) {
    val context = LocalContext.current
    val imageRequest = remember(username) {
        ImageRequest.Builder(context)
            .data("https://api.wasteof.money/users/$username/picture")
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build()
    }

    if (borderColor != null) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(3.dp, borderColor, CircleShape)
            )
            AsyncImage(
                model = imageRequest,
                contentDescription = username,
                modifier = Modifier
                    .size(size - 6.dp)
                    .clip(CircleShape)
            )
        }
    } else {
        AsyncImage(
            model = imageRequest,
            contentDescription = username,
            modifier = Modifier.size(size).clip(CircleShape)
        )
    }
}

@Composable
private fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onMentionClick: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.onBackground.toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 16f
                if (maxLines != Int.MAX_VALUE) {
                    this.maxLines = maxLines
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            textView.setLinkTextColor(linkColor)

            // Override click listener on the TextView to trigger the parent's onClick
            // while LinkMovementMethod still handles the spans.
            textView.setOnClickListener { onClick?.invoke() }
            textView.isClickable = onClick != null
            textView.isFocusable = false

            if (maxLines != Int.MAX_VALUE) {
                textView.maxLines = maxLines
                textView.ellipsize = android.text.TextUtils.TruncateAt.END
            }
            // Normalize <br> and paragraph breaks so multiple <p> elements (and <br>) produce visible newlines/separation in the TextView
            val htmlToRender = html.trim()
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("</p>\\s*<p", RegexOption.IGNORE_CASE), "</p>\n\n<p")
                .replace(Regex("^\\n+"), "")
                .replace(Regex("\\n+$"), "")
            val spanned = HtmlCompat.fromHtml(htmlToRender, HtmlCompat.FROM_HTML_MODE_LEGACY)
            if (onMentionClick != null) {
                val spannable = SpannableStringBuilder(spanned)
                val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
                for (span in urlSpans) {
                    val url = span.url
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    spannable.removeSpan(span)
                    if (url.startsWith("wombat://user/")) {
                        val username = url.substringAfter("wombat://user/")
                        val clickable = object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                onMentionClick.invoke(username)
                            }
                            override fun updateDrawState(ds: TextPaint) {
                                ds.isUnderlineText = true
                                ds.isFakeBoldText = true
                                ds.color = linkColor
                            }
                        }
                        spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        // keep normal links (http etc) working via default
                        spannable.setSpan(URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                textView.text = spannable
            } else {
                textView.text = spanned
            }
            // Ensure all links (URLSpans) are slightly bold + underlined (in addition to theme color)
            (textView.text as? Spannable)?.let { spannable ->
                spannable.getSpans(0, spannable.length, URLSpan::class.java).forEach { span ->
                    val s = spannable.getSpanStart(span)
                    val e = spannable.getSpanEnd(span)
                    if (spannable.getSpans(s, e, StyleSpan::class.java).none { it.style == Typeface.BOLD }) {
                        spannable.setSpan(StyleSpan(Typeface.BOLD), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    if (spannable.getSpans(s, e, UnderlineSpan::class.java).isEmpty()) {
                        spannable.setSpan(UnderlineSpan(), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
    )
}

@Composable
private fun PostMetric(
    value: Int,
    label: String,
    icon: ImageVector,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleSmall)
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyStateCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun titleForTab(tab: BottomTab): String {
    return when (tab) {
        BottomTab.Home -> "wo.mbat"
        BottomTab.Explore -> "Explore"
        BottomTab.Notifications -> "Notifications"
        BottomTab.Account -> "Account"
    }
}

private fun notificationLabel(type: String): String {
    return when (type.lowercase()) {
        "love" -> "Loved your post"
        "comment" -> "Commented on your post"
        "repost" -> "Reposted your content"
        "follow" -> "Followed you"
        "post_mention" -> "Mentioned you in a post"
        "wall_comment" -> "Left a comment on your wall"
        "wall_comment_reply" -> "Replied to a comment on your wall"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}

private fun formatTime(time: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(time))
}

private fun markdownToHtml(md: String): String {
    var html = md

    // Code blocks
    html = html.replace(Regex("(?s)```\\s*(.*?)\\s*```"), "<pre>$1</pre>")

    // Inline code
    html = html.replace(Regex("`([^`]+)`"), "<code>$1</code>")

    // Bold
    html = html.replace(Regex("\\*\\*([^*]+)\\*\\*"), "<b>$1</b>")
    html = html.replace(Regex("__(.+?)__"), "<b>$1</b>")

    // Italic
    html = html.replace(Regex("\\*([^*]+)\\*"), "<i>$1</i>")
    html = html.replace(Regex("_([^_]+)_"), "<i>$1</i>")

    // Strikethrough
    html = html.replace(Regex("~~(.+?)~~"), "<s>$1</s>")

    // Headings
    html = html.replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")

    // Blockquotes
    html = html.replace(Regex("^> (.+)$", RegexOption.MULTILINE), "<blockquote>$1</blockquote>")

    // Unordered lists
    html = html.replace(Regex("(?m)^[-*] (.+)$"), "<li>$1</li>")
    html = html.replace(Regex("(<li>.*?</li>(\\s*<li>.*?</li>)*)"), "<ul>$1</ul>")

    // Ordered lists
    html = html.replace(Regex("(?m)^\\d+\\. (.+)$"), "<li>$1</li>")
    html = html.replace(Regex("(<li>.*?</li>(\\s*<li>.*?</li>)*)"), "<ol>$1</ol>")

    // Paragraphs
    html = html.split("\n\n")
        .filter { it.isNotBlank() }
        .joinToString("\n") { if (it.trim().startsWith("<")) it else "<p>$it</p>" }

    // Line breaks
    html = html.replace("\n", "<br>")

    return html
}

private fun isAllowedImageHost(url: String): Boolean {
    return try {
        val host = java.net.URL(url).host.lowercase()
        host == "i.ibb.co" || host == "u.cubeupload.com"
    } catch (e: Exception) {
        false
    }
}

// Content processing helpers for links, mentions, and images (only i.ibb.co or u.cubeupload.com)
private fun extractImages(html: String): List<String> {
    val imgRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    return imgRegex.findAll(html)
        .mapNotNull { it.groupValues.getOrNull(1) }
        .filter { url -> isAllowedImageHost(url) }
        .distinct()
        .toList()
}

private fun stripImages(html: String): String {
    val imgRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    return imgRegex.replace(html) { match ->
        val src = match.groupValues.getOrNull(1) ?: ""
        if (isAllowedImageHost(src)) {
            "" // remove supported images (they go to carousel at bottom)
        } else {
            match.value // keep unrelated images (though we only support listed ones)
        }
    }
}

private fun autoLinkAndMentions(html: String): String {
    var result = html
    // Plain http/https links (not already in href or quotes)
    val urlRegex = """(?<!["'=/>])(https?://[^\s<>"']+)""".toRegex(RegexOption.IGNORE_CASE)
    result = urlRegex.replace(result) { m ->
        val url = m.value
        """<a href="$url">$url</a>"""
    }
    // @mentions → special href for in-app handling
    val mentionRegex = """@([A-Za-z0-9_]+)""".toRegex()
    result = mentionRegex.replace(result) { m ->
        val user = m.groupValues[1]
        """<a href="wombat://user/$user">@$user</a>"""
    }
    return result
}

@Composable
private fun PostImageCarousel(
    images: List<String>,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    isDetailView: Boolean = false
) {
    if (images.isEmpty()) return

    if (images.size == 1) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(images[0])
                .crossfade(true)
                .build(),
            contentDescription = "Post image",
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (isDetailView) {
                        Modifier.heightIn(max = 600.dp)
                    } else {
                        Modifier.height(220.dp)
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable { onImageClick(images, 0) },
            contentScale = if (isDetailView) ContentScale.FillWidth else ContentScale.Crop
        )
        return
    }

    // Keying by the images list hash ensures the state resets when the images change (e.g., when a different post is selected)
    val pagerState = key(images) {
        rememberPagerState(pageCount = { images.size })
    }
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDetailView) 400.dp else 220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(images[page])
                    .crossfade(true)
                    .build(),
                contentDescription = "Image ${page + 1} of ${images.size}",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImageClick(images, page) },
                contentScale = ContentScale.Crop
            )
        }
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "${pagerState.currentPage + 1} / ${images.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FullScreenImageViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { images.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 16.dp,
                    userScrollEnabled = true // Ensure scrolling is enabled
                ) { page ->
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        // Only allow panning if zoomed in
                        if (scale > 1f) {
                            offset += offsetChange
                        } else {
                            offset = Offset.Zero
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .transformable(state = state)
                            .pointerInput(Unit) {
                                // Reset scale and offset on double tap
                                detectTapGestures(
                                    onDoubleTap = {
                                        scale = if (scale > 1f) 1f else 3f
                                        offset = Offset.Zero
                                    }
                                )
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(images[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Full screen image ${page + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Top Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            downloadImage(context, images[pagerState.currentPage])
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
                    }
                }

                // Bottom Indicator
                if (images.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun downloadImage(context: Context, url: String) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Wombat Image")
        .setDescription("Downloading image from Wombat")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "wombat_${System.currentTimeMillis()}.jpg")
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
}
