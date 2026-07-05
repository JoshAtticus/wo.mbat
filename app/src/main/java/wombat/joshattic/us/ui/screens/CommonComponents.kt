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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
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
import androidx.compose.material3.NavigationBarItemDefaults
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

fun htmlToAnnotated(html: String): AnnotatedString {
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

fun annotatedToHtml(annotated: AnnotatedString): String {
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
fun WombatBottomNavigationBar(
    selectedTab: BottomTab,
    unreadCount: Int,
    accountLabel: String,
    profilePictureUrl: String?,
    onTabSelected: (BottomTab) -> Unit
) {
    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = selectedTab == BottomTab.Home,
            onClick = { onTabSelected(BottomTab.Home) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = navBarItemColors
        )
        NavigationBarItem(
            selected = selectedTab == BottomTab.Explore,
            onClick = { onTabSelected(BottomTab.Explore) },
            icon = { Icon(Icons.Filled.Tag, contentDescription = "Explore") },
            label = { Text("Explore") },
            colors = navBarItemColors
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
            label = { Text("Notifications") },
            colors = navBarItemColors
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
                            .addHeader("Cache-Control", "no-cache")
                            .addHeader("Pragma", "no-cache")
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = accountLabel,
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Account")
                }
            },
            label = { Text(accountLabel, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
            colors = navBarItemColors
        )
    }
}

@Composable
fun ProfileStat(label: String, value: Int, accentColor: Color? = null, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        shape = RoundedCornerShape(16.dp), 
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = accentColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                label, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UserActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onBlock: () -> Unit,
    onBlockReport: () -> Unit,
    username: String
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Share Profile") },
            onClick = {
                onDismiss()
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, "https://wasteof.money/users/$username")
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        )
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
fun PostActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    postId: String,
    onBlock: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Share Post") },
            onClick = {
                onDismiss()
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, "https://wasteof.money/posts/$postId")
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        )
        if (onEdit != null) {
            DropdownMenuItem(
                text = { Text("Edit Post") },
                onClick = onEdit
            )
        }
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
fun PostCard(
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
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    onPostClickById: ((String) -> Unit)? = null,
    isFollowing: Boolean? = null,
    followLoading: Boolean = false,
    onFollowClick: (() -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning"
) {
    if (blockedUsernames.contains(post.poster.name.lowercase())) {
        Spacer(modifier = Modifier.size(0.dp))
        return
    }

    val isQuoteRepostOfBlocked = post.repost != null &&
        blockedUsernames.contains(post.repost.poster.name.lowercase()) &&
        !post.content.replace(Regex("<.*?>"), "").trim().isBlank()

    if (isQuoteRepostOfBlocked && blockedQuoteHandling == "hide_post") {
        Spacer(modifier = Modifier.size(0.dp))
        return
    }
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
                // When clicking a pure repost, open the inner (actual) post so that
                // comments and love status are loaded for the correct post ID.
                onClick = { onPostClick?.invoke(post.repost!!) ?: onClick() },
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
                onDeletePost = onDeletePost,
                onEditPost = onEditPost,
                showImages = showImages,
                openLinksInApp = openLinksInApp,
                onPostClickById = onPostClickById,
                isFollowing = isFollowing,
                followLoading = followLoading,
                onFollowClick = onFollowClick,
                blockedUsernames = blockedUsernames,
                blockedQuoteHandling = blockedQuoteHandling
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
                if (currentUsername != null && !isOwnPost && onFollowClick != null) {
                    if (followLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onFollowClick) {
                            Icon(
                                imageVector = if (isFollowing == true) Icons.Filled.Check else Icons.Filled.PersonAdd,
                                contentDescription = if (isFollowing == true) "Unfollow" else "Follow",
                                tint = if (isFollowing == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (onBlockUser != null || onReportPost != null || onDeletePost != null || onEditPost != null) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Post options")
                        }
                        PostActionsMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            postId = post.id,
                            onBlock = if (!isOwnPost) onBlockUser?.let { { menuExpanded = false; it(post.poster.name) } } else null,
                            onReport = if (!isOwnPost) onReportPost?.let { { menuExpanded = false; it(post) } } else null,
                            onDelete = if (isOwnPost) onDeletePost?.let { { menuExpanded = false; it(post) } } else null,
                            onEdit = if (isOwnPost) onEditPost?.let { { menuExpanded = false; it(post) } } else null
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                var isActuallyTruncated by remember { mutableStateOf(false) }
                HtmlText(
                    html = displayContent,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = if (truncated) 6 else Int.MAX_VALUE,
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClickById,
                    onClick = onClick,
                    openLinksInApp = openLinksInApp,
                    onTruncatedChanged = { isActuallyTruncated = it }
                )
                if (isActuallyTruncated) {
                    Text(
                        text = "Read more...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .clickable { onClick() }
                    )
                }
                post.repost?.let { repostPost ->
                    val isRepostPosterBlocked = blockedUsernames.contains(repostPost.poster.name.lowercase())
                    if (isRepostPosterBlocked) {
                        if (blockedQuoteHandling == "warning") {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Block,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "This post is from a user you blocked.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    } else {
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
                                HtmlText(
                                    html = repostDisplay,
                                    maxLines = 4,
                                    onMentionClick = onMentionClick,
                                    onPostClick = onPostClickById,
                                    onClick = { onPostClick?.invoke(repostPost) },
                                    openLinksInApp = openLinksInApp
                                )
                            }
                        }
                    }
                }
            }
            if (showImages && imageUrls.isNotEmpty()) {
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
fun CommentCard(
    comment: Comment,
    isBanned: Boolean = false,
    onReply: (Comment) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onMentionClick: ((String) -> Unit)? = null,
    onPostClick: ((String) -> Unit)? = null,
    openLinksInApp: Boolean = true,
    depth: Int = 0,
    onFocusComment: ((Comment) -> Unit)? = null
) {
    val isReply = comment.parent != null
    if (!isReply) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(300)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                CommentThreadContent(
                    comment = comment,
                    isBanned = isBanned,
                    onReply = onReply,
                    onProfileClick = onProfileClick,
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClick,
                    openLinksInApp = openLinksInApp,
                    depth = depth,
                    onFocusComment = onFocusComment
                )
            }
        }
    } else {
        CommentThreadContent(
            comment = comment,
            isBanned = isBanned,
            onReply = onReply,
            onProfileClick = onProfileClick,
            onMentionClick = onMentionClick,
            onPostClick = onPostClick,
            openLinksInApp = openLinksInApp,
            depth = depth,
            onFocusComment = onFocusComment
        )
    }
}

@Composable
fun CommentThreadContent(
    comment: Comment,
    isBanned: Boolean,
    onReply: (Comment) -> Unit,
    onProfileClick: (String) -> Unit,
    onMentionClick: ((String) -> Unit)?,
    onPostClick: ((String) -> Unit)?,
    openLinksInApp: Boolean,
    depth: Int,
    onFocusComment: ((Comment) -> Unit)?
) {
    val isBlockedPlaceholder = comment.content == "This comment is from a user you blocked"
    val isReply = comment.parent != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left column: Profile picture and thread line
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBlockedPlaceholder) {
                Box(
                    modifier = Modifier.size(if (isReply) 28.dp else 34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Box(modifier = Modifier.clickable { onProfileClick(comment.poster.name) }) {
                    ProfilePicture(
                        username = comment.poster.name, 
                        size = if (isReply) 28.dp else 34.dp
                    )
                }
            }
            
            val safeReplies = comment.replies ?: emptyList()
            if (safeReplies.isNotEmpty() && depth < 2) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }
        
        // Right column: Content + Nested replies
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isBlockedPlaceholder) {
                    Text(
                        text = "Blocked User",
                        style = if (isReply) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = comment.poster.name, 
                        style = if (isReply) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onProfileClick(comment.poster.name) }
                    )
                }
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = formatTime(comment.time), 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (!isBanned && !isBlockedPlaceholder) {
                    IconButton(
                        onClick = { onReply(comment) }, 
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat, 
                            contentDescription = "Reply", 
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Content text
            if (isBlockedPlaceholder) {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            } else {
                val displayContent = remember(comment.content) { autoLinkAndMentions(stripImages(comment.content)) }
                HtmlText(
                    html = displayContent,
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClick,
                    openLinksInApp = openLinksInApp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            
            // Nested replies list
            val safeReplies = comment.replies ?: emptyList()
            if (safeReplies.isNotEmpty()) {
                val maxDepth = 2 // Cap depth at 2 inline levels
                if (depth >= maxDepth) {
                    androidx.compose.material3.TextButton(
                        onClick = { onFocusComment?.invoke(comment) },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "See replies (${safeReplies.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        safeReplies.forEach { reply ->
                            CommentCard(
                                comment = reply,
                                isBanned = isBanned,
                                onReply = onReply,
                                onProfileClick = onProfileClick,
                                onMentionClick = onMentionClick,
                                onPostClick = onPostClick,
                                openLinksInApp = openLinksInApp,
                                depth = depth + 1,
                                onFocusComment = onFocusComment
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    isOverlay: Boolean = false,
    openLinksInApp: Boolean = true,
    onMentionClick: ((String) -> Unit)? = null,
    onPostClick: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
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
            containerColor = if (notification.read) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0f).let {
                // Subtle tinted overlay — 10% primary on top of surface
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    .compositeOver(MaterialTheme.colorScheme.surface)
            },
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = actorName, size = 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        actorName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = notificationLabel(notification.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClick,
                    onClick = onClick,
                    openLinksInApp = openLinksInApp
                )
            } else if (notification.data.post != null && notification.data.post.content != null) {
                val postContent = remember(notification.id) { notification.data.post.content }
                HtmlText(
                    html = postContent,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClick,
                    onClick = onClick,
                    openLinksInApp = openLinksInApp
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
fun ProfilePicture(username: String, size: androidx.compose.ui.unit.Dp, borderColor: Color? = null, cacheBuster: String? = null) {
    val context = LocalContext.current
    val imageRequest = remember(username, cacheBuster) {
        val url = "https://wasteof-image-proxy.tnix.dev/$username?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3"
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .diskCachePolicy(CachePolicy.DISABLED)
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

sealed class WasteofUrl {
    data class Profile(val username: String) : WasteofUrl()
    data class Post(val postId: String) : WasteofUrl()
}

fun parseWasteofUrl(url: String): WasteofUrl? {
    val absoluteUrl = if (url.startsWith("/")) {
        "https://wasteof.money$url"
    } else {
        url
    }
    val uri = try { android.net.Uri.parse(absoluteUrl) } catch (e: Exception) { return null }
    val host = uri.host?.lowercase() ?: ""
    if (host == "wasteof.money" || host == "www.wasteof.money" || host == "beta.wasteof.money") {
        val pathSegments = uri.pathSegments
        if (pathSegments.size == 2) {
            val type = pathSegments[0].lowercase()
            val value = pathSegments[1]
            if (type == "users") {
                return WasteofUrl.Profile(value)
            } else if (type == "posts") {
                return WasteofUrl.Post(value)
            }
        } else if (pathSegments.size == 1) {
            val segment = pathSegments[0]
            if (segment.startsWith("@")) {
                return WasteofUrl.Profile(segment.drop(1))
            }
        }
    }
    return null
}

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    onMentionClick: ((String) -> Unit)? = null,
    onPostClick: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    openLinksInApp: Boolean = true,
    onTruncatedChanged: ((Boolean) -> Unit)? = null
) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val linkColor = MaterialTheme.colorScheme.onBackground.toArgb()

    val spannedText = remember(html, textColor, linkColor, onMentionClick, onPostClick, openLinksInApp) {
        val processedHtml = html.trim()
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>\\s*<p", RegexOption.IGNORE_CASE), "</p>\n\n<p")

        val spanned = HtmlCompat.fromHtml(processedHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableStringBuilder(spanned)

        val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
        for (span in urlSpans) {
            val url = span.url
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            if (url.startsWith("wombat://user/")) {
                if (onMentionClick != null) {
                    spannable.removeSpan(span)
                    val username = url.substringAfter("wombat://user/")
                    val clickable = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onMentionClick.invoke(username)
                        }
                        override fun updateDrawState(ds: TextPaint) {
                            ds.isUnderlineText = false
                            ds.isFakeBoldText = true
                            ds.color = linkColor
                        }
                    }
                    spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                // Non-wombat link: intercept click according to openLinksInApp setting
                spannable.removeSpan(span)
                val clickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        try {
                            val absoluteUrl = if (url.startsWith("/")) {
                                "https://wasteof.money$url"
                            } else {
                                url
                            }
                            val parsed = parseWasteofUrl(absoluteUrl)
                            if (openLinksInApp && parsed != null) {
                                when (parsed) {
                                    is WasteofUrl.Profile -> {
                                        onMentionClick?.invoke(parsed.username)
                                    }
                                    is WasteofUrl.Post -> {
                                        onPostClick?.invoke(parsed.postId)
                                    }
                                }
                            } else {
                                val uri = android.net.Uri.parse(absoluteUrl)
                                val context = widget.context
                                if (openLinksInApp) {
                                    val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder().build()
                                    customTabsIntent.launchUrl(context, uri)
                                } else {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                        ds.color = linkColor
                    }
                }
                spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        // Ensure all links (ClickableSpans) are slightly bold
        spannable.getSpans(0, spannable.length, ClickableSpan::class.java).forEach { span ->
            val s = spannable.getSpanStart(span)
            val e = spannable.getSpanEnd(span)
            if (spannable.getSpans(s, e, StyleSpan::class.java).none { it.style == Typeface.BOLD }) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        // Replace default blockquote spans with branded ones
        val brandColor = 0xFF6366F1.toInt()
        spannable.getSpans(0, spannable.length, android.text.style.QuoteSpan::class.java).forEach { span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val flags = spannable.getSpanFlags(span)
            spannable.removeSpan(span)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                spannable.setSpan(
                    android.text.style.QuoteSpan(brandColor, 6, 24),
                    start, end, flags
                )
            } else {
                spannable.setSpan(
                    android.text.style.QuoteSpan(brandColor),
                    start, end, flags
                )
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

            textView.post {
                val isTruncated = if (maxLines != Int.MAX_VALUE && textView.layout != null) {
                    textView.layout.lineCount > maxLines || 
                    (textView.layout.lineCount == maxLines && textView.layout.getEllipsisCount(maxLines - 1) > 0)
                } else {
                    false
                }
                onTruncatedChanged?.invoke(isTruncated)
            }
        }
    )
}

@Composable
fun PostMetric(
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
fun EmptyStateCard(title: String, message: String) {
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

fun notificationLabel(type: String): String {
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

fun formatTime(time: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(time))
}

fun markdownToHtml(md: String): String {
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

fun isAllowedImageHost(url: String): Boolean {
    return try {
        val host = java.net.URL(url).host.lowercase()
        host == "i.ibb.co" || host == "u.cubeupload.com"
    } catch (e: Exception) {
        false
    }
}

fun extractImages(html: String): List<String> {
    val imgRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    return imgRegex.findAll(html)
        .mapNotNull { it.groupValues.getOrNull(1) }
        .filter { url -> isAllowedImageHost(url) }
        .distinct()
        .toList()
}

fun stripImages(html: String): String {
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

fun autoLinkAndMentions(html: String): String {
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
fun PostImageCarousel(
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
fun FullScreenImageViewer(
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

fun downloadImage(context: Context, url: String, username: String?) {
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
fun ReportDialog(
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
fun UserListBottomSheet(
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
                    items(users.distinctBy { it.id }, key = { it.id }) { user ->
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
