@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wombat.joshattic.us.ui.state.BottomTab
import wombat.joshattic.us.ui.viewmodel.HomeViewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Info

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

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val pagerState = rememberPagerState(initialPage = uiState.selectedTab.ordinal) { 4 }

    LaunchedEffect(pagerState.settledPage) {
        val targetTab = BottomTab.entries[pagerState.settledPage]
        if (uiState.selectedTab != targetTab) {
            viewModel.selectTab(targetTab)
        }
    }

    LaunchedEffect(uiState.selectedTab) {
        val targetPage = uiState.selectedTab.ordinal
        // Use scrollToPage (instant) to avoid fighting with an in-progress animation
        if (pagerState.currentPage != targetPage || pagerState.isScrollInProgress) {
            pagerState.scrollToPage(targetPage)
        }
    }


    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAppResumed {
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
        AlertDialog(
            onDismissRequest = { viewModel.dismissBannedPopup() },
            title = { Text("Banned :(") },
            text = { Text("\"${uiState.banReason ?: "unknown reasons"}\". You can still browse your feed as read only, but you won't be able to make new posts or interact with anyone") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissBannedPopup() }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.dismissBannedPopup()
                    viewModel.logout() 
                }) {
                    Text("Log out")
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

    uiState.blockedWarningTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { viewModel.clearBlockedWarning() },
            title = { Text("Hold up") },
            text = { Text("This link leads to something from a user you blocked. Would you like to view it anyways?") },
            confirmButton = {
                TextButton(onClick = { viewModel.bypassBlockedWarning() }) {
                    Text("View Anyways")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearBlockedWarning() }) {
                    Text("Go Back")
                }
            }
        )
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
        if (!uiState.isOnline) {
            OfflineScreen(onRetry = { viewModel.refreshFeedAndExplore() })
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (!isTablet) {
                        CenterAlignedTopAppBar(
                            title = { Text(titleForTab(uiState.selectedTab)) }
                        )
                    }
                },
                floatingActionButton = {
                    if (!isTablet && uiState.selectedTab == BottomTab.Home && !uiState.isBanned) {
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
                    if (!isTablet) {
                        WombatBottomNavigationBar(
                            selectedTab = uiState.selectedTab,
                            unreadCount = uiState.unreadNotificationCount,
                            accountLabel = uiState.accountLabel,
                            profilePictureUrl = uiState.session?.username?.let {
                                "https://wasteof-image-proxy.tnix.dev/$it?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3"
                            },
                            onTabSelected = viewModel::selectTab
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                )) {
                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left side: Navigation Rail
                            WombatNavigationRail(
                                selectedTab = uiState.selectedTab,
                                unreadCount = uiState.unreadNotificationCount,
                                accountLabel = uiState.accountLabel,
                                profilePictureUrl = uiState.session?.username?.let {
                                    "https://wasteof-image-proxy.tnix.dev/$it?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3"
                                },
                                onTabSelected = viewModel::selectTab
                            )

                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            // Left Pane: Current list view
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    CenterAlignedTopAppBar(
                                        title = { Text(titleForTab(uiState.selectedTab)) }
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        when (BottomTab.entries[page]) {
                                            BottomTab.Home -> FeedTab(
                                                posts = uiState.feed,
                                                loading = uiState.feedLoading,
                                                listState = feedListState,
                                                onRefresh = viewModel::refreshFeed,
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
                                                onDeletePost = { viewModel.deletePost(it.id) },
                                                onEditPost = viewModel::openEditComposer,
                                                scrollToTop = uiState.scrollToTop,
                                                onScrollToTopComplete = viewModel::clearScrollToTop,
                                                newPostsUsernames = uiState.newPostsUsernames,
                                                onClearNewPosts = viewModel::clearNewPostsUsernames,
                                                showImages = uiState.showImagesInFeed,
                                                showNewPosts = uiState.showNewPostsPopup,
                                                openLinksInApp = uiState.openLinksInApp,
                                                onPostClickById = viewModel::openPostById,
                                                blockedUsernames = uiState.blockedUsernames,
                                                blockedQuoteHandling = uiState.blockedQuoteHandling
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
                                                onDeletePost = { viewModel.deletePost(it.id) },
                                                onEditPost = viewModel::openEditComposer,
                                                showImages = uiState.showImagesInFeed,
                                                openLinksInApp = uiState.openLinksInApp,
                                                onPostClickById = viewModel::openPostById,
                                                followedUsernames = uiState.followedUsernames,
                                                followLoadingUsernames = uiState.followLoadingUsernames,
                                                onFollowClick = viewModel::toggleFollowUser,
                                                blockedUsernames = uiState.blockedUsernames,
                                                blockedQuoteHandling = uiState.blockedQuoteHandling
                                            )
                                            BottomTab.Notifications -> NotificationsTab(
                                                session = uiState.session,
                                                unreadNotifications = uiState.unreadNotifications,
                                                readNotifications = uiState.readNotifications,
                                                loading = uiState.notificationsLoading,
                                                loadingMore = uiState.notificationsLoadingMore,
                                                isLastPage = uiState.unreadNotificationsLast && uiState.readNotificationsLast,
                                                onRefresh = viewModel::refreshNotifications,
                                                onMarkAllRead = viewModel::markAllNotificationsRead,
                                                onNotificationClick = viewModel::handleNotificationClick,
                                                onLoadNextPage = viewModel::loadNextNotificationsPage,
                                                openLinksInApp = uiState.openLinksInApp,
                                                onMentionClick = { viewModel.openProfile(it) },
                                                onPostClickById = viewModel::openPostById
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
                                                onShowFollowing = viewModel::showFollowing,
                                                onWallClick = viewModel::openWall,
                                                onSettingsClick = viewModel::openSettings,
                                                onEditProfileClick = viewModel::openEditProfile,
                                                profileCacheBuster = uiState.profileCacheBuster,
                                                showImages = uiState.showImagesInFeed,
                                                openLinksInApp = uiState.openLinksInApp,
                                                onPostClickById = viewModel::openPostById,
                                                followedUsernames = uiState.followedUsernames,
                                                followLoadingUsernames = uiState.followLoadingUsernames,
                                                onFollowClick = viewModel::toggleFollowUser,
                                                blockedUsernames = uiState.blockedUsernames,
                                                blockedQuoteHandling = uiState.blockedQuoteHandling
                                            )
                                        }
                                    }
                                }

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
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                            .size(64.dp)
                                    ) {
                                        Icon(Icons.Filled.PostAdd, contentDescription = "Create post", modifier = Modifier.size(28.dp))
                                    }
                                }
                            }

                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            // Right Pane: selection details fallback stack
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .fillMaxHeight()
                            ) {
                                when {
                                    uiState.selectedPost != null -> {
                                        PostDetailsContent(
                                            post = uiState.selectedPost!!,
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
                                            onDeletePost = { viewModel.deletePost(it.id) },
                                            onEditPost = viewModel::openEditComposer,
                                            showImages = uiState.showImagesInFeed,
                                            openLinksInApp = uiState.openLinksInApp,
                                            onPostClickById = viewModel::openPostById,
                                            showCloseButton = true,
                                            focusedComment = uiState.focusedComment,
                                            onFocusComment = viewModel::focusComment,
                                            onClearFocusComment = viewModel::clearFocusComment,
                                            blockedUsernames = uiState.blockedUsernames,
                                            blockedQuoteHandling = uiState.blockedQuoteHandling
                                        )
                                    }
                                    uiState.viewingWallUsername != null -> {
                                        WallDetailsContent(
                                            username = uiState.viewingWallUsername!!,
                                            comments = uiState.wallComments,
                                            loading = uiState.wallCommentsLoading,
                                            draft = uiState.wallCommentDraft,
                                            isBanned = uiState.isBanned,
                                            onDraftChange = viewModel::setWallCommentDraft,
                                            onSubmit = viewModel::submitWallComment,
                                            onDismiss = viewModel::closeWall,
                                            onLoadNextPage = viewModel::loadNextWallCommentsPage,
                                            replyingTo = uiState.wallCommentReplyParent,
                                            onCancelReply = { viewModel.setWallCommentReplyParent(null) },
                                            onReplyToComment = viewModel::setWallCommentReplyParent,
                                            onProfileClick = viewModel::openProfile,
                                            openLinksInApp = uiState.openLinksInApp,
                                            onPostClickById = viewModel::openPostById,
                                            focusedComment = uiState.focusedComment,
                                            onFocusComment = viewModel::focusComment,
                                            onClearFocusComment = viewModel::clearFocusComment
                                        )
                                    }
                                    uiState.viewingProfileUsername != null -> {
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
                                            onShowFollowing = viewModel::showFollowing,
                                            onEditPost = viewModel::openEditComposer,
                                            onWallClick = viewModel::openWall,
                                            showImages = uiState.showImagesInFeed,
                                            openLinksInApp = uiState.openLinksInApp,
                                            onPostClickById = viewModel::openPostById,
                                            blockedUsernames = uiState.blockedUsernames,
                                            blockedQuoteHandling = uiState.blockedQuoteHandling
                                        )
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Filled.Info,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "Open something to view it here",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Mobile layout
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
                                onShowFollowing = viewModel::showFollowing,
                                onEditPost = viewModel::openEditComposer,
                                onWallClick = viewModel::openWall,
                                showImages = uiState.showImagesInFeed,
                                openLinksInApp = uiState.openLinksInApp,
                                onPostClickById = viewModel::openPostById,
                                blockedUsernames = uiState.blockedUsernames,
                                blockedQuoteHandling = uiState.blockedQuoteHandling
                            )
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (BottomTab.entries[page]) {
                                    BottomTab.Home -> FeedTab(
                                        posts = uiState.feed,
                                        loading = uiState.feedLoading,
                                        listState = feedListState,
                                        onRefresh = viewModel::refreshFeed,
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
                                        onDeletePost = { viewModel.deletePost(it.id) },
                                        onEditPost = viewModel::openEditComposer,
                                        scrollToTop = uiState.scrollToTop,
                                        onScrollToTopComplete = viewModel::clearScrollToTop,
                                        newPostsUsernames = uiState.newPostsUsernames,
                                        onClearNewPosts = viewModel::clearNewPostsUsernames,
                                        showImages = uiState.showImagesInFeed,
                                        showNewPosts = uiState.showNewPostsPopup,
                                        openLinksInApp = uiState.openLinksInApp,
                                        onPostClickById = viewModel::openPostById,
                                        blockedUsernames = uiState.blockedUsernames,
                                        blockedQuoteHandling = uiState.blockedQuoteHandling
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
                                        onDeletePost = { viewModel.deletePost(it.id) },
                                        onEditPost = viewModel::openEditComposer,
                                        showImages = uiState.showImagesInFeed,
                                        openLinksInApp = uiState.openLinksInApp,
                                        onPostClickById = viewModel::openPostById,
                                        followedUsernames = uiState.followedUsernames,
                                        followLoadingUsernames = uiState.followLoadingUsernames,
                                        onFollowClick = viewModel::toggleFollowUser,
                                        blockedUsernames = uiState.blockedUsernames,
                                        blockedQuoteHandling = uiState.blockedQuoteHandling
                                    )
                                    BottomTab.Notifications -> NotificationsTab(
                                        session = uiState.session,
                                        unreadNotifications = uiState.unreadNotifications,
                                        readNotifications = uiState.readNotifications,
                                        loading = uiState.notificationsLoading,
                                        loadingMore = uiState.notificationsLoadingMore,
                                        isLastPage = uiState.unreadNotificationsLast && uiState.readNotificationsLast,
                                        onRefresh = viewModel::refreshNotifications,
                                        onMarkAllRead = viewModel::markAllNotificationsRead,
                                        onNotificationClick = viewModel::handleNotificationClick,
                                        onLoadNextPage = viewModel::loadNextNotificationsPage,
                                        openLinksInApp = uiState.openLinksInApp,
                                        onMentionClick = { viewModel.openProfile(it) },
                                        onPostClickById = viewModel::openPostById
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
                                        onShowFollowing = viewModel::showFollowing,
                                        onWallClick = viewModel::openWall,
                                        onSettingsClick = viewModel::openSettings,
                                        onEditProfileClick = viewModel::openEditProfile,
                                        profileCacheBuster = uiState.profileCacheBuster,
                                        showImages = uiState.showImagesInFeed,
                                        openLinksInApp = uiState.openLinksInApp,
                                        onPostClickById = viewModel::openPostById,
                                        followedUsernames = uiState.followedUsernames,
                                        followLoadingUsernames = uiState.followLoadingUsernames,
                                        onFollowClick = viewModel::toggleFollowUser,
                                        blockedUsernames = uiState.blockedUsernames,
                                        blockedQuoteHandling = uiState.blockedQuoteHandling
                                    )
                                }
                            }
                        }
                    }

            if (uiState.showComposer) {
                ComposerSheet(
                    draft = uiState.composeDraft,
                    onDraftChange = viewModel::setComposeDraft,
                    onSubmit = { htmlContent -> viewModel.submitPost(contentOverride = htmlContent) },
                    onDismiss = {
                        viewModel.saveCurrentDraft()
                        viewModel.setComposeDraft("")
                        viewModel.clearEditPostId()
                        viewModel.toggleComposer()
                    },
                    drafts = uiState.composerDrafts,
                    onRestoreDraft = viewModel::restoreDraft,
                    onDeleteDraft = viewModel::deleteDraft,
                    currentUsername = uiState.session?.username,
                    onDeletePost = { viewModel.deletePost(it.id) },
                    isEditing = uiState.composeEditPostId != null
                )
            }

            if (!isTablet) {
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
                        onDeletePost = { viewModel.deletePost(it.id) },
                        onEditPost = viewModel::openEditComposer,
                        showImages = uiState.showImagesInFeed,
                        openLinksInApp = uiState.openLinksInApp,
                        onPostClickById = viewModel::openPostById,
                        focusedComment = uiState.focusedComment,
                        onFocusComment = viewModel::focusComment,
                        onClearFocusComment = viewModel::clearFocusComment,
                        blockedUsernames = uiState.blockedUsernames,
                        blockedQuoteHandling = uiState.blockedQuoteHandling
                    )
                }

                uiState.viewingWallUsername?.let { username ->
                    WallDetailsSheet(
                        username = username,
                        comments = uiState.wallComments,
                        loading = uiState.wallCommentsLoading,
                        draft = uiState.wallCommentDraft,
                        isBanned = uiState.isBanned,
                        onDraftChange = viewModel::setWallCommentDraft,
                        onSubmit = viewModel::submitWallComment,
                        onDismiss = viewModel::closeWall,
                        onLoadNextPage = viewModel::loadNextWallCommentsPage,
                        replyingTo = uiState.wallCommentReplyParent,
                        onCancelReply = { viewModel.setWallCommentReplyParent(null) },
                        onReplyToComment = viewModel::setWallCommentReplyParent,
                        onProfileClick = viewModel::openProfile,
                        openLinksInApp = uiState.openLinksInApp,
                        onPostClickById = viewModel::openPostById,
                        focusedComment = uiState.focusedComment,
                        onFocusComment = viewModel::focusComment,
                        onClearFocusComment = viewModel::clearFocusComment
                    )
                }
            }
        }
    }
}

    if (uiState.showEditProfile && uiState.accountProfile != null) {
        EditProfileSheet(
            profile = uiState.accountProfile!!,
            bioDraft = uiState.editProfileBio,
            loading = uiState.editProfileLoading,
            error = uiState.editProfileError,
            cacheBuster = uiState.profileCacheBuster,
            onBioChange = viewModel::setEditProfileBio,
            onSaveBio = viewModel::updateProfileBio,
            onUploadPfp = viewModel::uploadProfilePicture,
            onDeletePfp = viewModel::deleteProfilePicture,
            onUploadBanner = viewModel::uploadBanner,
            onDeleteBanner = viewModel::deleteBanner,
            onDismiss = viewModel::closeEditProfile
        )
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
        visible = uiState.inAppNotifications && uiState.inAppNotification != null,
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
                openLinksInApp = uiState.openLinksInApp,
                onMentionClick = { viewModel.openProfile(it) },
                onPostClick = viewModel::openPostById,
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

    AnimatedVisibility(
        visible = uiState.showSettings,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = Modifier.zIndex(10f)
    ) {
        SettingsScreen(
            uiState = uiState,
            onClose = viewModel::closeSettings,
            onCategorySelect = viewModel::selectSettingsCategory,
            onShowImagesInFeedChange = viewModel::setShowImagesInFeed,
            onShowNewPostsPopupChange = viewModel::setShowNewPostsPopup,
            onInAppNotificationsChange = viewModel::setInAppNotifications,
            onMarkReadWhenOpenedChange = viewModel::setMarkReadWhenOpened,
            onMarkReadWhenTabOpenedChange = viewModel::setMarkReadWhenTabOpened,
            onOpenLinksInAppChange = viewModel::setOpenLinksInApp,
            onWearAccountChange = viewModel::setWearAccount,
            onWearShowImagesChange = viewModel::setWearShowImages,
            onWearShowProfilePicturesChange = viewModel::setWearShowProfilePictures,
            onWearFeedTypeChange = viewModel::setWearFeedType,
            onUnblockUser = viewModel::unblockUser,
            onFollowJosh = viewModel::followJoshAtticus,
            onBlockedQuoteHandlingChange = viewModel::setBlockedQuoteHandling
        )
    }
}
}

private fun titleForTab(tab: BottomTab): String {
    return when (tab) {
        BottomTab.Home -> "wo.mbat"
        BottomTab.Explore -> "Explore"
        BottomTab.Notifications -> "Notifications"
        BottomTab.Account -> "Account"
    }
}
