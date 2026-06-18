@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
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
    val feedListState = rememberLazyListState()
    val exploreListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    var confirmTitle by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var lastBackgroundTime by remember { mutableLongStateOf(0L) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                lastBackgroundTime = System.currentTimeMillis()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (lastBackgroundTime > 0 && System.currentTimeMillis() - lastBackgroundTime > 15 * 60 * 1000) {
                    viewModel.refreshFeed()
                    viewModel.loadExploreTrending()
                    coroutineScope.launch {
                        feedListState.animateScrollToItem(0)
                        exploreListState.animateScrollToItem(0)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.showBannedPopup) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.dismissBannedPopup() },
            title = { androidx.compose.material3.Text("Banned :(") },
            text = { androidx.compose.material3.Text("\"${uiState.banReason ?: "unknown reasons"}\". You can still browse your feed as read only, but you won't be able to make new posts or interact with anyone") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.dismissBannedPopup() }) {
                    androidx.compose.material3.Text("OK")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { 
                    viewModel.dismissBannedPopup()
                    viewModel.logout() 
                }) {
                    androidx.compose.material3.Text("Log out")
                }
            }
        )
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    confirmAction?.let { action ->
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text(confirmTitle) },
            text = { Text(confirmMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    action()
                    confirmAction = null 
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            if (uiState.selectedTab == BottomTab.Home && !uiState.isBanned) {
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
                profilePictureUrl = uiState.session?.username?.let { "https://wasteof-image-proxy.tnix.dev/$it?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3" },
                onTabSelected = viewModel::selectTab
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(
            top = innerPadding.calculateTopPadding(),
            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
        )) {
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
                    onBlockClick = { username -> 
                        confirmTitle = "Block User"
                        confirmMessage = "Are you sure you want to block @$username?"
                        confirmAction = { viewModel.blockUser(username) }
                    },
                    onBlockReportClick = { username -> 
                        confirmTitle = "Report & Block User"
                        confirmMessage = "Are you sure you want to report and block @$username?"
                        confirmAction = { viewModel.blockUser(username, reported = true) }
                    },
                    onReportPost = { post -> viewModel.openReportDialog(post.id) },
                    onUnblockClick = viewModel::unblockViewedProfile,
                    onPostClick = viewModel::openPost,
                    onMentionClick = { username ->
                        viewModel.openProfile(username)
                    },
                    onProfileClick = viewModel::openProfile,
                    onLoveClick = viewModel::togglePostLove,
                    onImageClick = viewModel::openFullScreenImages,
                    onLoadNextPage = viewModel::loadNextProfilePage,
                    onRepostClick = { viewModel.submitRepost(it.id) },
                    onQuoteClick = { viewModel.openQuoteComposer(it.id) },
                    onDeletePost = { viewModel.deletePost(it.id) },
                    onShowFollowers = viewModel::showFollowers,
                    onShowFollowing = viewModel::showFollowing
                )
            } else {
                val pagerState = rememberPagerState(initialPage = uiState.selectedTab.ordinal) { 4 }

                LaunchedEffect(pagerState.settledPage) {
                    val targetTab = BottomTab.entries[pagerState.settledPage]
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
                    when (BottomTab.entries[page]) {
                        BottomTab.Home -> FeedTab(
                            posts = uiState.feed,
                            loading = uiState.feedLoading,
                            listState = feedListState,
                            onRefresh = {
                                viewModel.refreshFeed()
                                coroutineScope.launch { feedListState.animateScrollToItem(0) }
                            },
                            onPostClick = viewModel::openPost,
                            onMentionClick = { username ->
                                viewModel.openProfile(username)
                            },
                            onProfileClick = viewModel::openProfile,
                            onLoveClick = viewModel::togglePostLove,
                            onImageClick = viewModel::openFullScreenImages,
                            onBlockUser = { username -> 
                                confirmTitle = "Block User"
                                confirmMessage = "Are you sure you want to block @$username?"
                                confirmAction = { viewModel.blockUser(username) }
                            },
                            onReportPost = { post -> viewModel.openReportDialog(post.id) },
                            onLoadNextPage = viewModel::loadNextFeedPage,
                            onRepostClick = { 
                                viewModel.submitRepost(it.id) {
                                    coroutineScope.launch { feedListState.animateScrollToItem(0) }
                                }
                            },
                            onQuoteClick = { viewModel.openQuoteComposer(it.id) },
                            currentUsername = uiState.session?.username,
                            onDeletePost = { viewModel.deletePost(it.id) }
                        )
                        BottomTab.Explore -> ExploreTab(
                            trendingPosts = uiState.exploreTrendingPosts,
                            trendingLoading = uiState.exploreTrendingLoading,
                            listState = exploreListState,
                            onRefresh = {
                                viewModel.loadExploreTrending()
                                coroutineScope.launch { exploreListState.animateScrollToItem(0) }
                            },
                            onOpenPost = viewModel::openPost,
                            onMentionClick = { username ->
                                viewModel.openProfile(username)
                            },
                            onProfileClick = viewModel::openProfile,
                            onLoveClick = viewModel::togglePostLove,
                            onImageClick = viewModel::openFullScreenImages,
                            onBlockUser = { username -> 
                                confirmTitle = "Block User"
                                confirmMessage = "Are you sure you want to block @$username?"
                                confirmAction = { viewModel.blockUser(username) }
                            },
                            onReportPost = { post -> viewModel.openReportDialog(post.id) },
                            onRepostClick = { 
                                viewModel.submitRepost(it.id) {
                                    coroutineScope.launch { exploreListState.animateScrollToItem(0) }
                                }
                            },
                            onQuoteClick = { viewModel.openQuoteComposer(it.id) },
                            currentUsername = uiState.session?.username,
                            onDeletePost = { viewModel.deletePost(it.id) }
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
                            savedAccounts = uiState.savedAccounts,
                            savedAccountUnreadCounts = uiState.savedAccountUnreadCounts,
                            isAddingAccount = uiState.isAddingAccount,
                            onUsernameChange = viewModel::setLoginUsername,
                            onPasswordChange = viewModel::setLoginPassword,
                            onLogin = viewModel::login,
                            onLogout = { 
                                confirmTitle = "Sign Out"
                                confirmMessage = "Are you sure you want to sign out?"
                                confirmAction = { viewModel.logout() }
                            },
                            onSwitchAccount = viewModel::switchAccount,
                            onAddAccount = { viewModel.setAddingAccount(true) },
                            onCancelAddAccount = { viewModel.setAddingAccount(false) },
                            onRefresh = viewModel::refreshAccount,
                            onPostClick = viewModel::openPost,
                            onMentionClick = { username ->
                                viewModel.openProfile(username)
                            },
                            onLoveClick = viewModel::togglePostLove,
                            onImageClick = viewModel::openFullScreenImages,
                            onLoadNextPage = viewModel::loadNextAccountPage,
                            onRepostClick = { viewModel.submitRepost(it.id) },
                            onQuoteClick = { viewModel.openQuoteComposer(it.id) },
                            currentUsername = uiState.session?.username,
                            onDeletePost = { viewModel.deletePost(it.id) },
                            onShowFollowers = viewModel::showFollowers,
                            onShowFollowing = viewModel::showFollowing
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
                    onDeleteDraft = viewModel::deleteDraft,
                    currentUsername = uiState.session?.username,
                    onDeletePost = { viewModel.deletePost(it.id) }
                )
            }

            uiState.selectedPost?.let { post ->
                PostDetailsSheet(
                    post = post,
                    comments = uiState.comments,
                    loading = uiState.commentsLoading,
                    draft = uiState.commentDraft,
                    isBanned = uiState.isBanned,
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
                    onBlockUser = { username: String -> 
                        confirmTitle = "Block User"
                        confirmMessage = "Are you sure you want to block @$username?"
                        confirmAction = { viewModel.blockUser(username) }
                    },
                    onReportPost = { post -> viewModel.openReportDialog(post.id) },
                    scrollToCommentId = uiState.scrollToCommentId,
                    onScrollToCommentComplete = viewModel::clearScrollToComment,
                    onImageClick = viewModel::openFullScreenImages,
                    onRepostClick = { viewModel.submitRepost(it.id) },
                    onQuoteClick = { viewModel.openQuoteComposer(it.id) },
                    currentUsername = uiState.session?.username,
                    onDeletePost = { viewModel.deletePost(it.id) }
                )
            }
        }
    }

    uiState.fullScreenImages?.let { images ->
        FullScreenImageViewer(
            images = images,
            initialIndex = uiState.initialFullScreenImageIndex,
            username = uiState.fullScreenImageUsername,
            onDismiss = viewModel::closeFullScreenImages
        )
    }

    if (uiState.showReportDialog && uiState.reportPostId != null) {
        ReportDialog(
            reason = uiState.reportReason,
            onReasonChange = viewModel::setReportReason,
            loading = uiState.reportLoading,
            onDismiss = viewModel::closeReportDialog,
            onSubmit = viewModel::submitReport
        )
    }

    // In-App Notification Overlay
    AnimatedVisibility(
        visible = uiState.inAppNotification != null,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = 16.dp)
            .padding(top = 64.dp, bottom = 16.dp)
            .zIndex(100f)
    ) {
        uiState.inAppNotification?.let { notif ->
            NotificationCard(
                notification = notif,
                isOverlay = true,
                onClick = { viewModel.handleNotificationClick(notif) }
            )
            
            LaunchedEffect(notif.id) {
                delay(5000)
                viewModel.clearInAppNotification()
            }
        }
    }

    if (uiState.userListToShow != null) {
        UserListBottomSheet(
            title = uiState.userListTitle,
            users = uiState.userListToShow,
            loading = uiState.userListLoading,
            loadingMore = uiState.userListLoadingMore,
            onDismiss = viewModel::dismissUserList,
            onUserClick = { user ->
                viewModel.dismissUserList()
                viewModel.openProfile(user.name)
            },
            onLoadNextPage = viewModel::loadNextUserListPage
        )
    }
}
}

@Composable
private fun FeedTab(
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

@Composable
private fun ExploreTab(
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
    onDeletePost: ((Post) -> Unit)? = null
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
                    onDeletePost = onDeletePost
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

            items(unreadNotifications, key = { it.id }, contentType = { "notification" }) { notification ->
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

                items(readNotifications, key = { it.id }, contentType = { "notification" }) { notification ->
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
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession>,
    savedAccountUnreadCounts: Map<String, Int>,
    isAddingAccount: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onCancelAddAccount: () -> Unit,
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onLoadNextPage: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null,
    onShowFollowers: (String) -> Unit = {},
    onShowFollowing: (String) -> Unit = {}
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
            if (session != null && !isAddingAccount) {
                // Profile details card
                if (profile != null) {
                    val accent = getUserColorSchemeColors(profile.color).first
                    item {
                        val bannerUrl = "https://api.wasteof.money/users/${profile.name}/banner"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
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
                            // Gradient scrim at the bottom of the banner for smoother blending
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-24).dp)
                                .padding(horizontal = 4.dp)
                                .background(
                                    Brush.verticalGradient(
                                        0.0f to Color.Transparent,
                                        0.1f to accent.copy(alpha = 0.08f),
                                        1.0f to accent.copy(alpha = 0.08f)
                                    ),
                                    RoundedCornerShape(20.dp)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box {
                                        ProfilePicture(username = profile.name, size = 72.dp, borderColor = accent)
                                        if (profile.online) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(Alignment.BottomEnd)
                                                    .offset(x = (-2).dp, y = (-2).dp)
                                                    .background(Color(0xFF22C55E), CircleShape)
                                                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        var expanded by remember { mutableStateOf(false) }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { expanded = true }
                                        ) {
                                            Text(profile.name, style = MaterialTheme.typography.titleLarge, color = accent)
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch Account", tint = accent)
                                            
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                savedAccounts.forEach { account ->
                                                    DropdownMenuItem(
                                                        text = { Text(account.username) },
                                                        onClick = { 
                                                            expanded = false
                                                            onSwitchAccount(account.username)
                                                        },
                                                        leadingIcon = {
                                                            ProfilePicture(username = account.username, size = 24.dp)
                                                        },
                                                        trailingIcon = {
                                                            if (account.username == profile.name) {
                                                                Icon(Icons.Filled.Check, contentDescription = "Active")
                                                            } else {
                                                                val unread = savedAccountUnreadCounts[account.username] ?: 0
                                                                if (unread > 0) {
                                                                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                                        Text(unread.toString(), color = MaterialTheme.colorScheme.onError)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                                HorizontalDivider()
                                                DropdownMenuItem(
                                                    text = { Text("Add Account") },
                                                    onClick = {
                                                        expanded = false
                                                        onAddAccount()
                                                    },
                                                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Sign out") },
                                                    onClick = {
                                                        expanded = false
                                                        onLogout()
                                                    },
                                                    leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null) }
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (profile.online) "Online" else "Offline",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (profile.online) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                HtmlText(autoLinkAndMentions(stripImages(profile.bio ?: "<p>im a wasteof user, yay!</p>")), maxLines = 4)
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    ProfileStat("Followers", profile.stats?.followers ?: 0, accentColor = accent, onClick = { onShowFollowers(profile.name) })
                                    ProfileStat("Following", profile.stats?.following ?: 0, accentColor = accent, onClick = { onShowFollowing(profile.name) })
                                    ProfileStat("Posts", profile.stats?.posts ?: 0, accentColor = accent)
                                }
                            }
                        }
                    }
                }

                // User's posts under the account details (only after profile available)
                if (profile != null) {
                    if (!loading && posts.isEmpty()) {
                        item { EmptyStateCard(title = "No posts yet", message = "Pull down to refresh or create your first post.") }
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
                            onRepostClick = onRepostClick,
                            onQuoteClick = onQuoteClick,
                            onDeletePost = onDeletePost
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onLogin, enabled = !loginLoading) {
                                    if (loginLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Sign in")
                                }
                                if (isAddingAccount) {
                                    TextButton(onClick = onCancelAddAccount) {
                                        Text("Cancel")
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
    onDeleteDraft: (String) -> Unit,
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null
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
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.PostAdd,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                "No saved drafts yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Drafts are saved automatically when you\nclose the composer with text in it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
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
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                val charCount = draft.length
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("What's happening? Write words here, markdown supported.") }
                    )
                    Text(
                        text = "$charCount / ${HomeViewModel.MAX_CHAR_COUNT} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (charCount > HomeViewModel.MAX_CHAR_COUNT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                SubcomposeAsyncImage(
                                    model = url,
                                    contentDescription = "Attached image",
                                    loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) } },
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
            }
            
            Spacer(modifier = Modifier.height(16.dp))

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
                    enabled = (draft.isNotBlank() || currentImages.isNotEmpty()) && draft.length <= HomeViewModel.MAX_CHAR_COUNT,
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
    isBanned: Boolean,
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
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    scrollToCommentId: String? = null,
    onScrollToCommentComplete: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val listState = rememberLazyListState()

    val currentOnExpand by rememberUpdatedState(onExpandComments)

    LaunchedEffect(post.id) {
        currentOnExpand()
    }

    LaunchedEffect(scrollToCommentId, loading, comments) {
        if (scrollToCommentId != null && !loading && comments.isNotEmpty()) {
            val index = comments.indexOfFirst { it.id == scrollToCommentId }
            if (index != -1) {
                listState.animateScrollToItem(index + 1) // +1 for post header
                onScrollToCommentComplete()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            val isReplyBoxVisible by remember {
                derivedStateOf {
                    listState.layoutInfo.visibleItemsInfo.any { it.index > 0 }
                }
            }

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
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
                        onReportPost = onReportPost,
                        onRepostClick = onRepostClick,
                        onQuoteClick = onQuoteClick
                    )
                }

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
                            isBanned = isBanned,
                            onReply = onReplyToComment,
                            onProfileClick = onProfileClick
                        )
                    }
                }
            }

            // Input area (TextField + Reply button) — imePadding lifts it above the software keyboard
            if (!isBanned) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isReplyBoxVisible,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .imePadding()
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 16.dp)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(visible = replyingTo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Replying to @${replyingTo?.poster?.name ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Cancel reply",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 2,
                        maxLines = 5,
                        placeholder = { Text("Write a reply") }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onSubmit,
                            enabled = draft.isNotBlank(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reply")
                        }
                    }
                }
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
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
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
                            .crossfade(true)
                            .build(),
                        contentDescription = accountLabel,
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Account")
                }
            },
            label = { Text(accountLabel, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
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
                        text = if (profile.online) "Online" else "Offline",
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
    onReportPost: ((Post) -> Unit)? = null,
    onUnblockClick: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onLoadNextPage: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    onDeletePost: ((Post) -> Unit)? = null,
    onShowFollowers: (String) -> Unit = {},
    onShowFollowing: (String) -> Unit = {}
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
                        .height(140.dp)
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
                    // Gradient scrim at the bottom of the banner for smoother blending
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }
            }

            item {
                // Profile card overlapping the banner slightly via negative offset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.1f to accent.copy(alpha = 0.08f),
                                1.0f to accent.copy(alpha = 0.08f)
                            ),
                            RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Profile picture with online dot indicator
                            Box {
                                ProfilePicture(username = profile.name, size = 72.dp, borderColor = accent)
                                if (profile.online) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.BottomEnd)
                                            .offset(x = (-2).dp, y = (-2).dp)
                                            .background(Color(0xFF22C55E), CircleShape)
                                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = accent
                                )
                                Text(
                                    text = if (profile.online) "Online" else "Offline",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (profile.online) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (currentUsername?.equals(profile.name, ignoreCase = true) != true) {
                                Button(
                                    onClick = onFollowClick,
                                    enabled = currentUsername != null && !followLoading,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(if (isFollowing == true) "Unfollow" else "Follow")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        HtmlText(
                            autoLinkAndMentions(stripImages(profile.bio ?: "<p>No bio yet.</p>")),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileStat("Followers", profile.stats?.followers ?: 0, accentColor = accent, onClick = { onShowFollowers(profile.name) })
                            ProfileStat("Following", profile.stats?.following ?: 0, accentColor = accent, onClick = { onShowFollowing(profile.name) })
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
                items(posts, key = { it.id }, contentType = { "post" }) { post ->
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
                        onReportPost = onReportPost
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

@Composable
private fun ProfileStat(label: String, value: Int, accentColor: Color? = null, onClick: (() -> Unit)? = null) {
    Surface(
        shape = RoundedCornerShape(16.dp), 
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
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
private fun PostActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onBlock: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if (onBlock != null) {
            DropdownMenuItem(
                text = { Text("Block") },
                onClick = onBlock
            )
        }
        if (onReport != null) {
            DropdownMenuItem(
                text = { Text("Report Post") },
                onClick = onReport
            )
        }
        if (onDelete != null) {
            DropdownMenuItem(
                text = { Text("Delete Post", color = MaterialTheme.colorScheme.error) },
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    clickable: Boolean = true,
    truncated: Boolean = false,
    currentUsername: String? = null,
    onMentionClick: ((String) -> Unit)? = null,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: ((Post) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onImageClick: (List<String>, Int, String?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    onRepostClick: ((Post) -> Unit)? = null,
    onQuoteClick: ((Post) -> Unit)? = null,
    onDeletePost: ((Post) -> Unit)? = null
) {
    val imageUrls = remember(post.content) { extractImages(post.content) }
    val displayContent = remember(post.content) { autoLinkAndMentions(stripImages(post.content)) }
    var menuExpanded by remember { mutableStateOf(false) }

    val isPureRepost = remember(post) {
        post.repost != null &&
        post.content.replace(Regex("<.*?>"), "").trim().isBlank() &&
        imageUrls.isEmpty()
    }

    if (isPureRepost) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .clickable { onProfileClick(post.poster.name) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = "Repost",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "@${post.poster.name} reposted this",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PostCard(
                post = post.repost!!,
                onClick = onClick,
                modifier = Modifier,
                clickable = clickable,
                truncated = truncated,
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
        return
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.poster.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (post.pinned == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.PushPin,
                                    contentDescription = "Pinned",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(formatTime(post.time), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                val isOwnPost = currentUsername == post.poster.name
                if (onBlockUser != null || onReportPost != null || onDeletePost != null) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Post options")
                        }
                        PostActionsMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            onBlock = if (!isOwnPost) onBlockUser?.let { { menuExpanded = false; it(post.poster.name) } } else null,
                            onReport = if (!isOwnPost) onReportPost?.let { { menuExpanded = false; it(post) } } else null,
                            onDelete = if (isOwnPost) onDeletePost?.let { { menuExpanded = false; it(post) } } else null
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
                    val repostDisplay = remember(repostPost.content) { autoLinkAndMentions(stripImages(repostPost.content)) }
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
                    onImageClick = { images, index -> onImageClick(images, index, post.poster.name) },
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
                Box {
                    var repostMenuExpanded by remember { mutableStateOf(false) }
                    PostMetric(
                        value = post.reposts,
                        label = "reposts",
                        icon = Icons.Filled.Repeat,
                        onClick = if (onRepostClick != null || onQuoteClick != null) { { repostMenuExpanded = true } } else null
                    )
                    DropdownMenu(
                        expanded = repostMenuExpanded,
                        onDismissRequest = { repostMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Repost") },
                            onClick = {
                                repostMenuExpanded = false
                                onRepostClick?.invoke(post)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Quote") },
                            onClick = {
                                repostMenuExpanded = false
                                onQuoteClick?.invoke(post)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(comment: Comment, isBanned: Boolean = false, onReply: (Comment) -> Unit = {}, onProfileClick: (String) -> Unit = {}) {
    val isReply = comment.parent != null
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(300)),
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
                if (!isBanned) {
                    IconButton(onClick = { onReply(comment) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Chat, contentDescription = "Reply", modifier = Modifier.size(16.dp))
                    }
                }
            }
            val displayContent = remember(comment.content) { autoLinkAndMentions(stripImages(comment.content)) }
            HtmlText(displayContent)

            val safeReplies = comment.replies ?: emptyList()
            if (safeReplies.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    safeReplies.forEach { reply ->
                        CommentCard(comment = reply, isBanned = isBanned, onReply = onReply, onProfileClick = onProfileClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification, isOverlay: Boolean = false, onClick: () -> Unit) {
    val type = notification.type.lowercase()
    val actorName = notification.data.actor?.name ?: if (type == "admin_notification") "Admin" else "Unknown user"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .then(
                if (!notification.read) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = actorName, size = 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(actorName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = notificationLabel(notification.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isOverlay) {
                    Text(
                        formatTime(notification.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val content = remember(notification.id) {
                when (type) {
                    "admin_notification" -> notification.data.content
                    "comment", "wall_comment", "wall_comment_reply", "comment_reply", "comment_mention" -> notification.data.comment?.content
                    "post_mention", "mention", "repost" -> notification.data.post?.content
                    else -> null
                }
            }

            if (content != null) {
                HtmlText(
                    html = content,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    onClick = onClick
                )
            } else if (notification.data.post != null && notification.data.post.content != null) {
                val postContent = remember(notification.id) { notification.data.post.content }
                HtmlText(
                    html = postContent,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    onClick = onClick
                )
            } else if (type in listOf("repost", "comment", "comment_reply", "mention", "post_mention", "comment_mention", "wall_comment", "wall_comment_reply")) {
                Text(
                    text = "This post/comment was deleted.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
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
            .data("https://wasteof-image-proxy.tnix.dev/$username?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3")
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
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
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = username,
                loading = { CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp) },
                modifier = Modifier
                    .size(size - 6.dp)
                    .clip(CircleShape)
            )
        }
    } else {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = username,
            loading = { CircularProgressIndicator(modifier = Modifier.padding(8.dp), strokeWidth = 2.dp) },
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

    val spannedText = remember(html, textColor, linkColor, onMentionClick) {
        val processedHtml = html.trim()
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>\\s*<p", RegexOption.IGNORE_CASE), "</p>\n\n<p")

        val spanned = HtmlCompat.fromHtml(processedHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableStringBuilder(spanned)

        if (onMentionClick != null) {
            val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
            for (span in urlSpans) {
                val url = span.url
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                if (url.startsWith("wombat://user/")) {
                    spannable.removeSpan(span)
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
                }
            }
        }

        // Ensure all links (URLSpans) are slightly bold + underlined
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
        spannable
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                // Use a custom movement method that handles link clicks without scrolling.
                // LinkMovementMethod internally calls scrollTo() which makes the
                // TextView scrollable inside notification cards and post cards.
                movementMethod = object : LinkMovementMethod() {
                    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: android.view.MotionEvent): Boolean {
                        // Save scroll position before handling
                        val scrollX = widget.scrollX
                        val scrollY = widget.scrollY
                        val result = super.onTouchEvent(widget, buffer, event)
                        // Reset scroll back to prevent movement method from scrolling
                        widget.scrollTo(scrollX, scrollY)
                        return result
                    }
                }
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 16f
                // Prevent the TextView from scrolling its content
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
            }
        },
        update = { textView ->
            if (textView.text != spannedText) {
                textView.text = spannedText
            }
            
            val currentTextColor = textView.currentTextColor
            if (currentTextColor != textColor) {
                textView.setTextColor(textColor)
            }
            
            val currentLinkColor = textView.linkTextColors.defaultColor
            if (currentLinkColor != linkColor) {
                textView.setLinkTextColor(linkColor)
            }

            textView.setOnClickListener { onClick?.invoke() }
            textView.isClickable = onClick != null
            textView.isFocusable = false
            // Ensure text doesn't become scrollable
            textView.isVerticalScrollBarEnabled = false
            textView.setHorizontallyScrolling(false)

            if (maxLines != Int.MAX_VALUE) {
                if (textView.maxLines != maxLines) {
                    textView.maxLines = maxLines
                    textView.ellipsize = android.text.TextUtils.TruncateAt.END
                }
            } else {
                if (textView.maxLines != Int.MAX_VALUE) {
                    textView.maxLines = Int.MAX_VALUE
                    textView.ellipsize = null
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
        "comment_reply" -> "Replied to your comment"
        "repost" -> "Reposted your post"
        "follow" -> "Followed you"
        "mention" -> "Mentioned you"
        "post_mention" -> "Mentioned you in a post"
        "comment_mention" -> "Mentioned you in a comment"
        "wall_comment" -> "Left a comment on your wall"
        "wall_comment_reply" -> "Replied to a comment on your wall"
        "admin_notification" -> "Admin notification"
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

    val context = LocalContext.current

    if (images.size == 1) {
        val imageRequest = remember(images[0]) {
            ImageRequest.Builder(context)
                .data(images[0])
                .crossfade(true)
                .precision(Precision.INEXACT)
                .build()
        }
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = "Post image",
            loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
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
            val imageRequest = remember(images[page]) {
                ImageRequest.Builder(context)
                    .data(images[page])
                    .crossfade(true)
                    .precision(Precision.INEXACT)
                    .build()
            }
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = "Post image ${page + 1}",
                loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
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
    username: String?,
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
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(images[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Full screen image ${page + 1}",
                            loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
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
                            downloadImage(context, images[pagerState.currentPage], username)
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

private fun downloadImage(context: Context, url: String, username: String?) {
    val title = if (username != null) "Image from @$username" else "wo.mbat image"
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(title)
        .setDescription("Downloading image from Wombat")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "wombat_${System.currentTimeMillis()}.jpg")
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
}

@Composable
private fun ReportDialog(
    reason: String,
    onReasonChange: (String) -> Unit,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val presets = listOf(
        "Spam",
        "Harrassment",
        "Abuse/Threats",
        "Personal Information",
        "Impersonation",
        "Intentional Misinformation",
        "Other"
    )
    var selectedPreset by remember { mutableStateOf(presets.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                presets.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPreset = preset }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(preset, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (selectedPreset == "Other") {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = onReasonChange,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = { Text("Reason") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedPreset == "Other") {
                        reason
                    } else {
                        "$selectedPreset - Reported with wo.mbat"
                    }
                    onSubmit(finalReason)
                },
                enabled = !loading && (selectedPreset != "Other" || reason.isNotBlank())
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Report")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun UserListBottomSheet(
    title: String,
    users: List<User>?,
    loading: Boolean,
    loadingMore: Boolean = false,
    onDismiss: () -> Unit,
    onUserClick: (User) -> Unit,
    onLoadNextPage: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
            if (loading && users.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (users.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val listState = rememberLazyListState()
                
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                        .map { visibleItems ->
                            if (visibleItems.isEmpty()) false else {
                                val lastVisibleItem = visibleItems.last()
                                lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
                            }
                        }
                        .distinctUntilChanged()
                        .filter { it }
                        .collect { onLoadNextPage() }
                }

                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(users, key = { it.id }) { user ->
                        val accent = getUserColorSchemeColors(user.color).first
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserClick(user) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    ProfilePicture(username = user.name, size = 48.dp, borderColor = accent)
                                    if (user.online) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.BottomEnd)
                                                .offset(x = (-2).dp, y = (-2).dp)
                                                .background(Color(0xFF22C55E), CircleShape)
                                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, style = MaterialTheme.typography.titleMedium, color = accent)
                                    if (!user.bio.isNullOrBlank()) {
                                        HtmlText(
                                            stripImages(user.bio),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (loadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
