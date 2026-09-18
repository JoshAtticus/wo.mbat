@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.ui.zIndex
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlin.random.Random
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.ui.state.LoginStep
import wombat.joshattic.us.ui.theme.WombatTheme
import wombat.joshattic.us.ui.theme.getUserColorSchemeColors

// Pick a friendly sign-in header; different pools for first vs. additional accounts.
internal fun randomSignInHeader(hasSavedAccounts: Boolean): String {
    val firstAccount = listOf(
        "Hey there!",
        "Welcome!",
        "First time?",
        "Let's get started!",
        "Ready to start?"
    )
    val anotherAccount = listOf(
        "Two is better than one",
        "Another one?",
        "Add as many as you want",
        "Yippee, another one!",
        "Another account? Exciting!"
    )
    val pool = if (hasSavedAccounts) anotherAccount else firstAccount
    return pool[Random.nextInt(pool.size)]
}

@Composable
fun AccountTab(
    session: Any?,
    profile: User?,
    posts: List<Post>,
    loading: Boolean,
    loginUsername: String,
    loginPassword: String,
    loginLoading: Boolean,
    loginError: String?,
    loginStep: LoginStep = LoginStep.USERNAME,
    savedAccounts: List<AuthSession>,
    savedAccountUnreadCounts: Map<String, Int>,
    isAddingAccount: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLoginBack: () -> Unit = {},
    onLogout: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onCancelAddAccount: () -> Unit,
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onMentionClick: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: (Post) -> Unit = {},
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    onLoadNextPage: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null,
    onShowFollowers: (String) -> Unit = {},
    onShowFollowing: (String) -> Unit = {},
    onEditPost: ((Post) -> Unit)? = null,
    onWallClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    profileCacheBuster: Long = 0L,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    onPostClickById: ((String) -> Unit)? = null,
    followedUsernames: Set<String> = emptySet(),
    followLoadingUsernames: Set<String> = emptySet(),
    onFollowClick: ((String) -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning"
) {
    // Always theme this screen with the signed-in account colour, even when a custom theme is active
    WombatTheme(userColor = profile?.color) {
    val refreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        // Paint the themed background so the parent (custom-theme) scaffold colour doesn't show through
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
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
            contentPadding = PaddingValues(
                top = 12.dp,
                // 96dp clears the floating bottom bar; the inset keeps it clear of
                // the system navigation bar too.
                bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (session != null && !isAddingAccount) {
                // Profile details card
                if (profile != null) {
                    val accent = getUserColorSchemeColors(profile.color).first
                    item {
                        val bannerUrl = "https://api.wasteof.money/users/${profile.name}/banner" +
                            if (profileCacheBuster > 0) "?cb=$profileCacheBuster" else ""
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 1. Banner image
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
                            }

                            // 2. Discord-style Avatar overlapping half on banner, half on content card below
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .offset(y = 90.dp)
                                    .zIndex(3f)
                            ) {
                                ProfilePicture(
                                    username = profile.name,
                                    size = 84.dp,
                                    borderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    borderWidth = 4.dp,
                                    cacheBuster = if (profileCacheBuster > 0) profileCacheBuster.toString() else null
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-2).dp, y = (-2).dp)
                                        .background(if (profile.online) Color(0xFF22C55E) else Color(0xFF64748B), CircleShape)
                                        .border(3.5.dp, MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
                                )
                            }

                            // 3. Profile Content Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 132.dp),
                                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FilledTonalIconButton(
                                                onClick = onEditProfileClick,
                                                shape = CircleShape,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(20.dp), tint = accent)
                                            }

                                            FilledTonalIconButton(
                                                onClick = { onWallClick(profile.name) },
                                                shape = CircleShape,
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(Icons.Filled.Forum, contentDescription = "Wall", modifier = Modifier.size(20.dp), tint = accent)
                                            }

                                            var expanded by remember { mutableStateOf(false) }
                                            Box {
                                                FilledTonalIconButton(
                                                    onClick = { expanded = true },
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(40.dp)
                                                ) {
                                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch Account", modifier = Modifier.size(20.dp))
                                                }

                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false },
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                                    tonalElevation = 3.dp
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
                                                                        Badge(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error) {
                                                                            Text(unread.toString(), color = androidx.compose.material3.MaterialTheme.colorScheme.onError)
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
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = accent,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        UserBadges(
                                            verified = profile.verified,
                                            admin = profile.permissions?.admin == true,
                                            beta = profile.beta,
                                            accentColor = accent
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HtmlText(
                                        html = autoLinkAndMentions(stripImages(profile.bio ?: "<p>im a wasteof user, yay!</p>")),
                                        maxLines = 4,
                                        onMentionClick = onMentionClick,
                                        onPostClick = onPostClickById,
                                        openLinksInApp = openLinksInApp
                                    )

                                    // Joined Date / History Chip below bio
                                    profile.history?.joined?.let { joinedTime ->
                                        if (joinedTime > 0) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            val joinedDateStr = remember(joinedTime) {
                                                val date = java.util.Date(joinedTime)
                                                val format = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
                                                "Joined " + format.format(date)
                                            }
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Forum,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = joinedDateStr,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProfileStat(
                                            label = "Followers",
                                            value = profile.stats?.followers ?: 0,
                                            accentColor = accent,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onShowFollowers(profile.name) }
                                        )
                                        ProfileStat(
                                            label = "Following",
                                            value = profile.stats?.following ?: 0,
                                            accentColor = accent,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onShowFollowing(profile.name) }
                                        )
                                        ProfileStat(
                                            label = "Posts",
                                            value = profile.stats?.posts ?: 0,
                                            accentColor = accent,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
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

                    items(posts.distinctBy { it.id }, key = { it.id }, contentType = { "post" }) { post ->
                        PostCard(
                            post = post,
                            onClick = { onPostClick(post) },
                            truncated = true,
                            currentUsername = currentUsername,
                            savedAccounts = savedAccounts,
                            onMentionClick = onMentionClick,
                            onProfileClick = onProfileClick,
                            onLoveClick = onLoveClick,
                            onPostClick = onPostClick,
                            onImageClick = onImageClick,
                            onRepostClick = onRepostClick,
                            onQuoteClick = onQuoteClick,
                            onDeletePost = onDeletePost,
                            onEditPost = onEditPost,
                            showImages = showImages,
                            openLinksInApp = openLinksInApp,
                            linkPreviewPriority = linkPreviewPriority,
                            onPostClickById = onPostClickById,
                            blockedUsernames = blockedUsernames,
                            blockedQuoteHandling = blockedQuoteHandling
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
                            Text(
                                text = remember { randomSignInHeader(savedAccounts.isNotEmpty()) },
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Use your wasteof.money account to post, comment, and see notifications.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (loginStep == LoginStep.USERNAME) {
                                OutlinedTextField(
                                    value = loginUsername,
                                    onValueChange = onUsernameChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Username") },
                                    leadingIcon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) }
                                )
                            } else {
                                // Username already validated in step one; show it
                                // read-only so the user knows whose password to enter.
                                OutlinedTextField(
                                    value = loginUsername,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    readOnly = true,
                                    label = { Text("Username") },
                                    leadingIcon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) }
                                )
                                AnimatedVisibility(
                                    visible = loginStep == LoginStep.PASSWORD,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = loginPassword,
                                            onValueChange = onPasswordChange,
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            label = { Text("Password") },
                                            visualTransformation = PasswordVisualTransformation(),
                                            isError = !loginError.isNullOrBlank(),
                                            supportingText = {
                                                AnimatedVisibility(
                                                    visible = !loginError.isNullOrBlank(),
                                                    enter = fadeIn() + expandVertically(),
                                                    exit = fadeOut() + shrinkVertically()
                                                ) {
                                                    Text(loginError.orEmpty(), color = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            // Username-step errors have no password field to attach to,
                            // so they render inline below the username box instead.
                            AnimatedVisibility(
                                visible = loginStep == LoginStep.USERNAME && !loginError.isNullOrBlank(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    loginError.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onLogin, enabled = !loginLoading) {
                                    if (loginLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(if (loginStep == LoginStep.USERNAME) "Next" else "Sign in")
                                }
                                if (loginStep == LoginStep.PASSWORD) {
                                    TextButton(onClick = onLoginBack, enabled = !loginLoading) {
                                        Text("Back")
                                    }
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
}
