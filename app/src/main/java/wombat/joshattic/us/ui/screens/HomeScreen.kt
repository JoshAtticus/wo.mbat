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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.painterResource
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import wombat.joshattic.us.ui.state.SettingsCategory
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
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
    val useAltLogo = rememberSaveable { kotlin.random.Random.nextInt(500) == 0 }

    LaunchedEffect(pagerState.settledPage) {
        val targetTab = BottomTab.entries[pagerState.settledPage]
        if (uiState.selectedTab != targetTab) {
            viewModel.selectTab(targetTab)
        }
    }

    LaunchedEffect(uiState.selectedTab) {
        val targetPage = uiState.selectedTab.ordinal
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
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
                        val viewingUser = uiState.viewingProfileUsername
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                if (viewingUser != null) {
                                    IconButton(onClick = viewModel::closeProfile) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Close profile"
                                        )
                                    }
                                }
                            },
                            title = {
                                if (useAltLogo && viewingUser == null && uiState.selectedTab == BottomTab.Home) {
                                    Image(
                                        painter = painterResource(id = wombat.joshattic.us.R.drawable.ic_logo_alt),
                                        contentDescription = "wo.mbat",
                                        modifier = Modifier.height(26.dp),
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (viewingUser == null) {
                                            Image(
                                                painter = painterResource(id = wombat.joshattic.us.R.drawable.ic_logo),
                                                contentDescription = "wo.mbat logo",
                                                modifier = Modifier.size(28.dp),
                                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                            )
                                        }
                                        Text(
                                            text = viewingUser?.let { "@$it" } ?: titleForTab(uiState.selectedTab),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (viewingUser == null && uiState.selectedTab == BottomTab.Account) {
                                    IconButton(onClick = viewModel::openSettings) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                },
                floatingActionButton = {},
                bottomBar = {}

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
                                        title = {
                                            if (useAltLogo && uiState.selectedTab == BottomTab.Home) {
                                                Image(
                                                    painter = painterResource(id = wombat.joshattic.us.R.drawable.ic_logo_alt),
                                                    contentDescription = "wo.mbat",
                                                    modifier = Modifier.height(26.dp),
                                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                                )
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = wombat.joshattic.us.R.drawable.ic_logo),
                                                        contentDescription = "wo.mbat logo",
                                                        modifier = Modifier.size(28.dp),
                                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                                    )
                                                    Text(
                                                        text = titleForTab(uiState.selectedTab),
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        },
                                        actions = {
                                            IconButton(onClick = viewModel::openSettings) {
                                                Icon(
                                                    imageVector = Icons.Filled.Settings,
                                                    contentDescription = "Settings",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.background,
                                            titleContentColor = MaterialTheme.colorScheme.onBackground
                                        )
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
                                                savedAccounts = uiState.savedAccounts,
                                                onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                                onEditPost = viewModel::openEditComposer,
                                                scrollToTop = uiState.scrollToTop,
                                                onScrollToTopComplete = viewModel::clearScrollToTop,
                                                newPostsUsernames = uiState.newPostsUsernames,
                                                onClearNewPosts = viewModel::clearNewPostsUsernames,
                                                showImages = uiState.showImagesInFeed,
                                                showNewPosts = uiState.showNewPostsPopup,
                                                openLinksInApp = uiState.openLinksInApp,
                                                linkPreviewPriority = uiState.linkPreviewPriority,
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
                                                onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                                onEditPost = viewModel::openEditComposer,
                                                showImages = uiState.showImagesInFeed,
                                                openLinksInApp = uiState.openLinksInApp,
                                                linkPreviewPriority = uiState.linkPreviewPriority,
                                                onPostClickById = viewModel::openPostById,
                                                followedUsernames = uiState.followedUsernames,
                                                followLoadingUsernames = uiState.followLoadingUsernames,
                                                onFollowClick = viewModel::toggleFollowUser,
                                                blockedUsernames = uiState.blockedUsernames,
                                                blockedQuoteHandling = uiState.blockedQuoteHandling,
                                                searchQuery = uiState.exploreSearchQuery,
                                                searchPostResults = uiState.exploreSearchPostResults,
                                                searchUserResults = uiState.exploreSearchUserResults,
                                                searchLoading = uiState.exploreSearchLoading,
                                                onSearchQueryChange = viewModel::setExploreSearchQuery,
                                                selectedTimeframe = uiState.exploreTrendingTimeframe,
                                                onTimeframeChange = viewModel::setExploreTrendingTimeframe
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
                                                onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                                onShowFollowers = viewModel::showFollowers,
                                                onShowFollowing = viewModel::showFollowing,
                                                onWallClick = viewModel::openWall,
                                                onSettingsClick = viewModel::openSettings,
                                                onEditProfileClick = viewModel::openEditProfile,
                                                profileCacheBuster = uiState.profileCacheBuster,
                                                showImages = uiState.showImagesInFeed,
                                                openLinksInApp = uiState.openLinksInApp,
                                                linkPreviewPriority = uiState.linkPreviewPriority,
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
                                        shape = RoundedCornerShape(20.dp),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                                            onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                            onEditPost = viewModel::openEditComposer,
                                            showImages = uiState.showImagesInFeed,
                                            openLinksInApp = uiState.openLinksInApp,
                                            linkPreviewPriority = uiState.linkPreviewPriority,
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
                                 savedAccounts = uiState.savedAccounts,
                                            onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                            onShowFollowers = viewModel::showFollowers,
                                            onShowFollowing = viewModel::showFollowing,
                                            onEditPost = viewModel::openEditComposer,
                                            onWallClick = viewModel::openWall,
                                            showImages = uiState.showImagesInFeed,
                                            openLinksInApp = uiState.openLinksInApp,
                                            linkPreviewPriority = uiState.linkPreviewPriority,
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
                                savedAccounts = uiState.savedAccounts,
                                onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                onShowFollowers = viewModel::showFollowers,
                                onShowFollowing = viewModel::showFollowing,
                                onEditPost = viewModel::openEditComposer,
                                onWallClick = viewModel::openWall,
                                showImages = uiState.showImagesInFeed,
                                openLinksInApp = uiState.openLinksInApp,
                                linkPreviewPriority = uiState.linkPreviewPriority,
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
                                        savedAccounts = uiState.savedAccounts,
                                        onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                        onEditPost = viewModel::openEditComposer,
                                        scrollToTop = uiState.scrollToTop,
                                        onScrollToTopComplete = viewModel::clearScrollToTop,
                                        newPostsUsernames = uiState.newPostsUsernames,
                                        onClearNewPosts = viewModel::clearNewPostsUsernames,
                                        showImages = uiState.showImagesInFeed,
                                        showNewPosts = uiState.showNewPostsPopup,
                                        openLinksInApp = uiState.openLinksInApp,
                                        linkPreviewPriority = uiState.linkPreviewPriority,
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
                                        savedAccounts = uiState.savedAccounts,
                                        onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                        onEditPost = viewModel::openEditComposer,
                                        showImages = uiState.showImagesInFeed,
                                        openLinksInApp = uiState.openLinksInApp,
                                        linkPreviewPriority = uiState.linkPreviewPriority,
                                        onPostClickById = viewModel::openPostById,
                                        followedUsernames = uiState.followedUsernames,
                                        followLoadingUsernames = uiState.followLoadingUsernames,
                                        onFollowClick = viewModel::toggleFollowUser,
                                        blockedUsernames = uiState.blockedUsernames,
                                        blockedQuoteHandling = uiState.blockedQuoteHandling,
                                        searchQuery = uiState.exploreSearchQuery,
                                        searchPostResults = uiState.exploreSearchPostResults,
                                        searchUserResults = uiState.exploreSearchUserResults,
                                        searchLoading = uiState.exploreSearchLoading,
                                        onSearchQueryChange = viewModel::setExploreSearchQuery,
                                        selectedTimeframe = uiState.exploreTrendingTimeframe,
                                        onTimeframeChange = viewModel::setExploreTrendingTimeframe
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
                                        onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                                        onShowFollowers = viewModel::showFollowers,
                                        onShowFollowing = viewModel::showFollowing,
                                        onWallClick = viewModel::openWall,
                                        onSettingsClick = viewModel::openSettings,
                                        onEditProfileClick = viewModel::openEditProfile,
                                        profileCacheBuster = uiState.profileCacheBuster,
                                        showImages = uiState.showImagesInFeed,
                                        openLinksInApp = uiState.openLinksInApp,
                                        linkPreviewPriority = uiState.linkPreviewPriority,
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

                    // ── Floating bottom bar (phone only) ────────────────────────────
                    if (!isTablet) {
                        val showFab = uiState.selectedTab == BottomTab.Home && !uiState.isBanned

                        // Single Animatable drives all FAB size transitions, sequenced in a coroutine
                        // so each animateTo() fully completes before the next starts — no race conditions.
                        val fabAnim = remember { androidx.compose.animation.core.Animatable(if (showFab) 68f else 0f) }

                        LaunchedEffect(showFab) {
                            if (showFab) {
                                // Appear: overshoot to 76, then spring back to 68
                                fabAnim.animateTo(76f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                                fabAnim.animateTo(68f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                            } else {
                                // Disappear: expand to 76, then tween smoothly to 0
                                fabAnim.animateTo(76f, tween(durationMillis = 80))
                                fabAnim.animateTo(0f, tween(durationMillis = 260))
                            }
                        }

                        val fabSizeDp = fabAnim.value.dp

                        // Nav bar weight expands to fill space as FAB shrinks
                        val navWeight by animateFloatAsState(
                            targetValue = if (fabAnim.value < 1f) 1f else 0.72f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                            label = "navWeight"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WombatBottomNavigationBar(
                                    selectedTab = uiState.selectedTab,
                                    unreadCount = uiState.unreadNotificationCount,
                                    accountLabel = uiState.accountLabel,
                                    profilePictureUrl = uiState.session?.username?.let {
                                        "https://wasteof-image-proxy.tnix.dev/$it?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3"
                                    },
                                    onTabSelected = { tab ->
                                        viewModel.selectTab(tab)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(tab.ordinal)
                                        }
                                    },
                                    modifier = Modifier.weight(navWeight)
                                )

                                if (fabSizeDp > 0.dp) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shadowElevation = 8.dp,
                                        tonalElevation = 6.dp,
                                        onClick = {
                                            if (uiState.session != null) {
                                                viewModel.toggleComposer()
                                            } else {
                                                viewModel.selectTab(BottomTab.Account)
                                            }
                                        },
                                        modifier = Modifier.size(fabSizeDp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.PostAdd,
                                                contentDescription = "Create post",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
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
                    currentUsername = uiState.composeEditPostAuthor ?: uiState.session?.username,
                    onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                    isEditing = uiState.composeEditPostId != null,
                    viewModel = viewModel,
                    savedAccounts = uiState.savedAccounts,
                    onSwitchAccount = { session -> viewModel.switchAccount(session.username) }
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
                        savedAccounts = uiState.savedAccounts,
                        onDeletePost = { viewModel.deletePost(it.id, it.poster.name) },
                        onEditPost = viewModel::openEditComposer,
                        showImages = uiState.showImagesInFeed,
                        openLinksInApp = uiState.openLinksInApp,
                        linkPreviewPriority = uiState.linkPreviewPriority,
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
            post = uiState.fullScreenPost,
            username = uiState.fullScreenImageUsername,
            onDismiss = viewModel::closeFullScreenImages,
            onLoveClick = viewModel::togglePostLove,
            onCommentClick = viewModel::openPost,
            onRepostClick = { post -> viewModel.submitRepost(post.id) },
            onProfileClick = viewModel::openProfile
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
            onCategorySelect = { category ->
                viewModel.selectSettingsCategory(category)
                if (category == SettingsCategory.ABOUT) {
                    viewModel.loadFrog()
                }
            },
            onShowImagesInFeedChange = viewModel::setShowImagesInFeed,
            onShowNewPostsPopupChange = viewModel::setShowNewPostsPopup,
            onInAppNotificationsChange = viewModel::setInAppNotifications,
            onMarkReadWhenOpenedChange = viewModel::setMarkReadWhenOpened,
            onMarkReadWhenTabOpenedChange = viewModel::setMarkReadWhenTabOpened,
            onOpenLinksInAppChange = viewModel::setOpenLinksInApp,
            linkPreviewPriority = uiState.linkPreviewPriority,
            onLinkPreviewPriorityChange = viewModel::setLinkPreviewPriority,
            onWearAccountChange = viewModel::setWearAccount,
            onWearShowImagesChange = viewModel::setWearShowImages,
            onWearShowProfilePicturesChange = viewModel::setWearShowProfilePictures,
            onWearFeedTypeChange = viewModel::setWearFeedType,
            onUnblockUser = viewModel::unblockUser,
            onFollowJosh = viewModel::followJoshAtticus,
            onBlockedQuoteHandlingChange = viewModel::setBlockedQuoteHandling,
            frogMessage = uiState.exploreFrogMessage
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
