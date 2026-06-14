@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.TextPaint
import android.view.View
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.ui.state.BottomTab
import wombat.joshattic.us.ui.viewmodel.HomeViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
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
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = "Create post")
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
            when (uiState.selectedTab) {
                BottomTab.Home -> FeedTab(
                    posts = uiState.feed,
                    loading = uiState.feedLoading,
                    onRefresh = viewModel::refreshFeed,
                    onPostClick = viewModel::openPost,
                    onMentionClick = { username ->
                        viewModel.setExploreQuery(username)
                        viewModel.searchExplore()
                        viewModel.selectTab(BottomTab.Explore)
                    }
                )
                BottomTab.Explore -> ExploreTab(
                    query = uiState.exploreQuery,
                    loading = uiState.exploreLoading,
                    profile = uiState.exploreProfile,
                    posts = uiState.explorePosts,
                    onQueryChange = viewModel::setExploreQuery,
                    onSearch = viewModel::searchExplore,
                    onRefresh = viewModel::searchExplore,
                    onOpenPost = viewModel::openPost,
                    onMentionClick = { username ->
                        viewModel.setExploreQuery(username)
                        viewModel.searchExplore()
                        viewModel.selectTab(BottomTab.Explore)
                    }
                )
                BottomTab.Notifications -> NotificationsTab(
                    session = uiState.session,
                    notifications = uiState.unreadNotifications,
                    loading = uiState.notificationsLoading,
                    onRefresh = viewModel::refreshNotifications,
                    onMarkAllRead = viewModel::markAllNotificationsRead
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
                        viewModel.setExploreQuery(username)
                        viewModel.searchExplore()
                        viewModel.selectTab(BottomTab.Explore)
                    }
                )
            }

            if (uiState.showComposer) {
                ComposerSheet(
                    draft = uiState.composeDraft,
                    onDraftChange = viewModel::setComposeDraft,
                    onSubmit = viewModel::submitPost,
                    onDismiss = viewModel::toggleComposer
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
                        viewModel.setExploreQuery(username)
                        viewModel.searchExplore()
                        viewModel.selectTab(BottomTab.Explore)
                    }
                )
            }
        }
    }
}

@Composable
private fun FeedTab(
    posts: List<Post>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!loading && posts.isEmpty()) {
                item { EmptyStateCard(title = "Nothing here yet", message = "Pull down to refresh.") }
            }

            items(posts, key = { it.id }) { post ->
                PostCard(post = post, onClick = { onPostClick(post) }, truncated = true, onMentionClick = onMentionClick)
            }
        }
    }
}

@Composable
private fun ExploreTab(
    query: String,
    loading: Boolean,
    profile: User?,
    posts: List<Post>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRefresh: () -> Unit,
    onOpenPost: (Post) -> Unit,
    onMentionClick: (String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Search a creator", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Enter a username") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onSearch, enabled = query.isNotBlank() || true) {
                            Icon(Icons.Filled.Tag, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Discover")
                        }
                    }
                }
            }

            when {
                profile == null && posts.isEmpty() -> item { EmptyStateCard("No creator loaded", "Search by username to browse a profile and their posts.") }
                profile != null -> {
                    item { ProfileHeader(profile) }
                    if (posts.isEmpty()) {
                        item { EmptyStateCard("No posts found", "This user has no visible posts yet.") }
                    }
                    items(posts, key = { it.id }) { post ->
                        PostCard(post = post, onClick = { onOpenPost(post) }, truncated = true, onMentionClick = onMentionClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsTab(
    session: Any?,
    notifications: List<Notification>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notifications", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (session == null) "Sign in to receive alerts about loves, comments, reposts, and follows." else "Unread items are shown here until you mark them read.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (notifications.isNotEmpty()) {
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

            if (notifications.isEmpty() && !loading) {
                item { EmptyStateCard("All clear", "No unread notifications right now.") }
            }

            items(notifications, key = { it.id }) { notification ->
                NotificationCard(notification)
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
    onMentionClick: (String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (session != null) {
                // Profile details card
                item {
                    if (profile != null) {
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProfilePicture(username = profile.name, size = 56.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
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
                        PostCard(post = post, onClick = { onPostClick(post) }, truncated = true, onMentionClick = onMentionClick)
                    }
                }
            } else {
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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

@Composable
private fun ComposerSheet(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New post", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                maxLines = 10,
                placeholder = { Text("Write HTML, for example <p>Hello <strong>world</strong></p>") }
            )
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Preview", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (draft.isBlank()) {
                        Text("Your rendered HTML will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        HtmlText(draft)
                    }
                }
            }
            Button(onClick = onSubmit, enabled = draft.isNotBlank()) {
                Icon(Icons.Filled.PostAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Post")
            }
            Spacer(modifier = Modifier.height(8.dp))
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
    onMentionClick: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // React to sheet drag: swipe up (to Expanded) loads comments; swipe down (to Partial) hides comments
    LaunchedEffect(sheetState.currentValue) {
        when (sheetState.currentValue) {
            SheetValue.Expanded -> onExpandComments()
            SheetValue.PartiallyExpanded -> onCollapseComments()
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PostCard(
                post = post,
                onClick = {},
                clickable = false,
                truncated = false,
                onMentionClick = onMentionClick
            )

            val showCommentsSection = sheetState.currentValue == SheetValue.Expanded || comments.isNotEmpty()
            if (showCommentsSection) {
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
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(240.dp)) {
                        items(comments, key = { it.id }) { comment ->
                            CommentCard(comment)
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
                Spacer(modifier = Modifier.height(8.dp))
            } else {
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
                        model = ImageRequest.Builder(LocalContext.current).data(profilePictureUrl).crossfade(true).build(),
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
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
private fun ProfileStat(label: String, value: Int) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    onClick: () -> Unit,
    clickable: Boolean = true,
    truncated: Boolean = false,
    onMentionClick: ((String) -> Unit)? = null
) {
    val imageUrls = extractImages(post.content)
    val displayContent = autoLinkAndMentions(stripImages(post.content))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = if (clickable) Modifier.fillMaxWidth().clickable(onClick = onClick) else Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = post.poster.name, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.poster.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatTime(post.time), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${post.loves} loves", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HtmlText(
                displayContent,
                maxLines = if (truncated) 6 else Int.MAX_VALUE,
                onMentionClick = onMentionClick
            )
            post.repost?.let {
                val repostDisplay = autoLinkAndMentions(stripImages(it.content))
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Repost", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(it.poster.name, style = MaterialTheme.typography.titleSmall)
                        HtmlText(repostDisplay)
                    }
                }
            }
            if (imageUrls.isNotEmpty()) {
                PostImageCarousel(imageUrls)
            }
            Divider()
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PostMetric(post.comments, "comments")
                PostMetric(post.reposts, "reposts")
            }
        }
    }
}

@Composable
private fun CommentCard(comment: Comment) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = comment.poster.name, size = 32.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(comment.poster.name, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(formatTime(comment.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HtmlText(autoLinkAndMentions(stripImages(comment.content)))
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = notification.data.actor.name, size = 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(notification.data.actor.name, style = MaterialTheme.typography.titleMedium)
                    Text(notificationLabel(notification.type), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(formatTime(notification.time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            notification.data.post?.let {
                Text(
                    text = "Post reference: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfilePicture(username: String, size: androidx.compose.ui.unit.Dp) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://api.wasteof.money/users/$username/picture")
            .crossfade(true)
            .build(),
        contentDescription = username,
        modifier = Modifier.size(size).clip(CircleShape)
    )
}

@Composable
private fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onMentionClick: ((String) -> Unit)? = null
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor)
                textSize = 16f
                if (maxLines != Int.MAX_VALUE) {
                    this.maxLines = maxLines
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            if (maxLines != Int.MAX_VALUE) {
                textView.maxLines = maxLines
                textView.ellipsize = android.text.TextUtils.TruncateAt.END
            }
            val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
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
        }
    )
}

@Composable
private fun PostMetric(value: Int, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value.toString(), style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyStateCard(title: String, message: String) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
        else -> type.replaceFirstChar { it.uppercase() }
    }
}

private fun formatTime(time: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(time))
}

// Content processing helpers for links, mentions, and images (imgbb / cubeupload only)
private fun extractImages(html: String): List<String> {
    val imgRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    return imgRegex.findAll(html)
        .mapNotNull { it.groupValues.getOrNull(1) }
        .filter { url ->
            val lower = url.lowercase()
            lower.contains("imgbb") || lower.contains("cubeupload") || lower.contains("i.ibb.co")
        }
        .distinct()
        .toList()
}

private fun stripImages(html: String): String {
    val imgRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    return imgRegex.replace(html) { match ->
        val src = match.groupValues.getOrNull(1) ?: ""
        val lower = src.lowercase()
        if (lower.contains("imgbb") || lower.contains("cubeupload") || lower.contains("i.ibb.co")) {
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
private fun PostImageCarousel(images: List<String>, modifier: Modifier = Modifier) {
    if (images.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { images.size })
    Column(modifier = modifier.padding(top = 8.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(images[page])
                    .crossfade(true)
                    .build(),
                contentDescription = "Image ${page + 1} of ${images.size}",
                modifier = Modifier.fillMaxSize(),
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