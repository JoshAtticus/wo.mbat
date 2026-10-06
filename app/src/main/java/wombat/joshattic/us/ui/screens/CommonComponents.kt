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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.DpOffset
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
import androidx.compose.ui.platform.LocalDensity
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
import wombat.joshattic.us.data.LoveCache
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.ui.state.BottomTab
import wombat.joshattic.us.ui.theme.applyGoogleSansFlexTypeface
import wombat.joshattic.us.ui.theme.getUserColorSchemeColors
import wombat.joshattic.us.ui.viewmodel.HomeViewModel
import wombat.joshattic.us.ui.components.BrandedQuoteSpan
import wombat.joshattic.us.ui.components.OpenGraphPreview
import wombat.joshattic.us.ui.components.isOpenGraphPriority
import wombat.joshattic.us.ui.components.QUOTE_GAP_WIDTH_DP
import wombat.joshattic.us.ui.components.QUOTE_STRIPE_WIDTH_DP
import java.text.DateFormat
import java.util.Date
import java.util.Locale

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
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    val selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(BottomTab.Home)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == BottomTab.Home) selectedColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == BottomTab.Home) selectedIconColor else unselectedIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(BottomTab.Explore)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == BottomTab.Explore) selectedColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Tag,
                        contentDescription = "Explore",
                        tint = if (selectedTab == BottomTab.Explore) selectedIconColor else unselectedIconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(BottomTab.Notifications)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == BottomTab.Notifications) selectedColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) { Text(if (unreadCount > 99) "99+" else unreadCount.toString(), fontWeight = FontWeight.Bold) }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = if (selectedTab == BottomTab.Notifications) selectedIconColor else unselectedIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(BottomTab.Account)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == BottomTab.Account) selectedColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
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
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .then(
                                    if (selectedTab == BottomTab.Account)
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                        )
                    } else {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Account",
                            tint = if (selectedTab == BottomTab.Account) selectedIconColor else unselectedIconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserBadges(verified: Boolean, admin: Boolean, beta: Boolean, accentColor: Color? = null) {
    if (!verified && !admin && !beta) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        if (verified) {
            Icon(
                Icons.Filled.Verified,
                contentDescription = "Verified",
                modifier = Modifier.size(18.dp),
                tint = accentColor ?: MaterialTheme.colorScheme.primary
            )
        }
        if (admin) {
            Icon(
                Icons.Filled.AdminPanelSettings,
                contentDescription = "Admin",
                modifier = Modifier.size(18.dp),
                tint = Color(0xFFFFC107)
            )
        }
        if (beta) {
            Icon(
                Icons.Filled.Science,
                contentDescription = "Beta tester",
                modifier = Modifier.size(18.dp),
                tint = accentColor ?: MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProfileStat(label: String, value: Int, accentColor: Color? = null, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        shape = RoundedCornerShape(20.dp), 
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor ?: MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        tonalElevation = 3.dp
    ) {
        DropdownMenuItem(
            text = { Text("Share Profile") },
            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
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
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
        DropdownMenuItem(
            text = { Text("Block", color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Filled.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = onBlock
        )
        DropdownMenuItem(
            text = { Text("Block & Report", color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        tonalElevation = 3.dp
    ) {
        DropdownMenuItem(
            text = { Text("Share Post") },
            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
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
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = onEdit
            )
        }
        if (onBlock != null || onReport != null) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
        if (onBlock != null) {
            DropdownMenuItem(
                text = { Text("Block User", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Filled.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = onBlock
            )
        }
        if (onReport != null) {
            DropdownMenuItem(
                text = { Text("Report Post", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = onReport
            )
        }
        if (onDelete != null) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            DropdownMenuItem(
                text = { Text("Delete Post", color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = onDelete
            )
        }
    }
}

/** Opens the "who reposted this" sheet; provided app-wide so every PostCard can use it. */
val LocalOnViewReposts = compositionLocalOf<((Post) -> Unit)?> { null }

/** When true, blocked quote/comment warnings gain a one-time "Show" button. */
val LocalShowBlockedRevealButton = compositionLocalOf { false }

/** True when [post] is a bare repost wrapper with no added text or images of its own. */
fun isPureRepost(post: Post): Boolean {
    return post.repost != null &&
        post.content.replace(Regex("<.*?>"), "").trim().isBlank() &&
        extractImages(post.content).isEmpty()
}

/**
 * One feed entry: [primary] is the wrapper post that gets rendered, and
 * [reposters] lists every consecutive pure-repost wrapper of the same target
 * (including [primary] itself) to be shown in the combined header.
 */
data class RepostGroup(val primary: Post, val reposters: List<Post>)

/**
 * Merges runs of *consecutive* pure reposts of the same post into a single
 * [RepostGroup], so the feed can show "@a and @b reposted this" instead of
 * repeating the target card once per reposter. Regular posts and quote reposts
 * pass through as single-entry groups.
 */
fun groupConsecutiveReposts(posts: List<Post>): List<RepostGroup> {
    val result = mutableListOf<RepostGroup>()
    for (post in posts) {
        val last = result.lastOrNull()
        if (last != null && isPureRepost(post) && last.primary.repost?.id == post.repost?.id && last.reposters.isNotEmpty()) {
            result[result.lastIndex] = last.copy(reposters = last.reposters + post)
        } else {
            result.add(
                RepostGroup(
                    primary = post,
                    reposters = if (isPureRepost(post)) listOf(post) else emptyList()
                )
            )
        }
    }
    return result
}

/** Combined header label for a group of consecutive reposts of the same post. */
fun repostHeaderText(reposters: List<Post>): String {
    val names = reposters.distinctBy { it.poster.name.lowercase() }
        .map { "@${it.poster.name.lowercase()}" }
    return when (names.size) {
        1 -> "${names[0]} reposted this"
        2 -> "${names[0]} and ${names[1]} reposted this"
        else -> "${names[0]} and ${names.size - 1} others reposted this"
    }
}

/** True while the signed-in user is banned; disables interaction affordances on posts. */
val LocalIsBanned = staticCompositionLocalOf { false }

@Composable
fun PostCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    clickable: Boolean = true,
    truncated: Boolean = false,
    currentUsername: String? = null,
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession> = emptyList(),
    onMentionClick: ((String) -> Unit)? = null,
    onProfileClick: (String) -> Unit = {},
    onLoveClick: ((Post) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    onRepostClick: ((Post) -> Unit)? = null,
    onQuoteClick: ((Post) -> Unit)? = null,
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    onPostClickById: ((String) -> Unit)? = null,
    isFollowing: Boolean? = null,
    followLoading: Boolean = false,
    onFollowClick: (() -> Unit)? = null,
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning",
    groupedReposters: List<Post> = emptyList(),
    ignoreBlockedPoster: Boolean = false
) {
    if (!ignoreBlockedPoster && blockedUsernames.contains(post.poster.name.lowercase())) {
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
    val firstLink = remember(post.content) { extractFirstLink(post.content) }
    var menuExpanded by remember { mutableStateOf(false) }

    val isPureRepost = remember(post) { isPureRepost(post) }
    val isBanned = LocalIsBanned.current

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
                    if (groupedReposters.size > 1) repostHeaderText(groupedReposters)
                    else "@${post.poster.name.lowercase()} reposted this",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PostCard(
                post = post.repost!!,
                onClick = { onPostClick?.invoke(post.repost!!) ?: onClick() },
                modifier = Modifier,
                clickable = clickable,
                truncated = truncated,
                currentUsername = currentUsername,
                savedAccounts = savedAccounts,
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
                linkPreviewPriority = linkPreviewPriority,
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(vertical = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onProfileClick(post.poster.name) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePicture(username = post.poster.name, size = 40.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.poster.name.lowercase(), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isOwnPost = currentUsername?.trim()?.equals(post.poster.name.trim(), ignoreCase = true) == true ||
                                    savedAccounts.any { it.username.trim().equals(post.poster.name.trim(), ignoreCase = true) }
                    if (currentUsername != null && !isOwnPost && onFollowClick != null) {
                        if (followLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            val haptic = LocalHapticFeedback.current
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onFollowClick()
                            }) {
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
                val hasPostImages = showImages && imageUrls.isNotEmpty()
                val openGraphFirst = isOpenGraphPriority(linkPreviewPriority)
                if (openGraphFirst) {
                    firstLink?.let { link ->
                        OpenGraphPreview(
                            url = link,
                            hasPostImages = hasPostImages,
                            openGraphFirst = true,
                            openLinksInApp = openLinksInApp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    if (hasPostImages) {
                        Spacer(modifier = Modifier.height(6.dp))
                        PostImageSquares(
                            images = imageUrls,
                            onImageClick = { images, index -> onImageClick(images, index, post) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    if (hasPostImages) {
                        Spacer(modifier = Modifier.height(6.dp))
                        PostImageCarousel(
                            images = imageUrls,
                            onImageClick = { images, index -> onImageClick(images, index, post) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            isDetailView = !truncated
                        )
                    }
                    firstLink?.let { link ->
                        OpenGraphPreview(
                            url = link,
                            hasPostImages = hasPostImages,
                            openLinksInApp = openLinksInApp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                post.repost?.let { repostPost ->
                    val isRepostPosterBlocked = blockedUsernames.contains(repostPost.poster.name.lowercase())
                    var quoteRevealed by remember(repostPost.id) { mutableStateOf(false) }
                    if (isRepostPosterBlocked && !quoteRevealed) {
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
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (LocalShowBlockedRevealButton.current) {
                                        TextButton(onClick = { quoteRevealed = true }) {
                                            Text("Show")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val repostDisplay = remember(repostPost.content) { autoLinkAndMentions(stripImages(repostPost.content)) }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable(enabled = onPostClick != null) {
                                    onPostClick?.invoke(repostPost)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PostMetric(
                    value = post.loves,
                    label = "loves",
                    icon = if (post.isLoving == true || (post.isLoving == null && LoveCache.get(post.id) == true)) {
                        Icons.Filled.Favorite
                    } else Icons.Filled.FavoriteBorder,
                    isActive = post.isLoving == true || (post.isLoving == null && LoveCache.get(post.id) == true),
                    enabled = post.isLoving != null && !isBanned && !post.loveLoading,
                    pulse = (post.isLoving == null || post.loveLoading) && !isBanned,
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
                        onDismissRequest = { repostMenuExpanded = false },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        tonalElevation = 3.dp
                    ) {
                        DropdownMenuItem(
                            text = { Text("Repost") },
                            leadingIcon = { Icon(Icons.Filled.Repeat, contentDescription = null) },
                            onClick = {
                                repostMenuExpanded = false
                                onRepostClick?.invoke(post)
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        DropdownMenuItem(
                            text = { Text("Quote") },
                            leadingIcon = { Icon(Icons.Filled.FormatQuote, contentDescription = null) },
                            onClick = {
                                repostMenuExpanded = false
                                onQuoteClick?.invoke(post)
                            }
                        )
                        val onViewReposts = LocalOnViewReposts.current.takeIf { post.reposts > 0 }
                        if (onViewReposts != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            DropdownMenuItem(
                                text = { Text("View reposts") },
                                leadingIcon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                                onClick = {
                                    repostMenuExpanded = false
                                    onViewReposts(post)
                                }
                            )
                        }
                    }
                }
                PostShareButton(postId = post.id)
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
    onFocusComment: ((Comment) -> Unit)? = null,
    onLoadReplies: ((Comment) -> Unit)? = null,
    showImages: Boolean = true,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
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
                    onFocusComment = onFocusComment,
                    onLoadReplies = onLoadReplies,
                    showImages = showImages,
                    onImageClick = onImageClick
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
            onFocusComment = onFocusComment,
            onLoadReplies = onLoadReplies,
            showImages = showImages,
            onImageClick = onImageClick
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
    onFocusComment: ((Comment) -> Unit)?,
    onLoadReplies: ((Comment) -> Unit)? = null,
    showImages: Boolean = true,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
) {
    val isBlockedPlaceholder = comment.blocked
    var commentRevealed by remember(comment.id) { mutableStateOf(false) }
    val isReply = comment.parent != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
            
            if (isBlockedPlaceholder && !commentRevealed) {
                Text(
                    text = "This comment is from a user you blocked",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if (LocalShowBlockedRevealButton.current) {
                    TextButton(
                        onClick = { commentRevealed = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Show")
                    }
                }
            } else {
                val commentImageUrls = remember(comment.content) { extractImages(comment.content) }
                val displayContent = remember(comment.content) { autoLinkAndMentions(stripImages(comment.content)) }
                HtmlText(
                    html = displayContent,
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClick,
                    openLinksInApp = openLinksInApp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if (showImages && commentImageUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    PostImageSquares(
                        images = commentImageUrls,
                        onImageClick = onImageClick,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            
            val safeReplies = comment.replies ?: emptyList()
            when {
                safeReplies.isEmpty() && comment.hasReplies && onLoadReplies != null -> {
                    androidx.compose.material3.TextButton(
                        onClick = { onLoadReplies(comment) },
                        enabled = !comment.repliesLoading,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (comment.repliesLoading) "Loading replies..." else "See replies",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                safeReplies.isNotEmpty() -> {
                    val maxDepth = 2
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
                                    onFocusComment = onFocusComment,
                                    onLoadReplies = onLoadReplies,
                                    showImages = showImages,
                                    onImageClick = onImageClick
                                )
                            }
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
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(24.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    ProfilePicture(username = actorName, size = 42.dp)
                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        actorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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
fun ProfilePicture(username: String, size: androidx.compose.ui.unit.Dp, borderColor: Color? = null, borderWidth: androidx.compose.ui.unit.Dp = 3.dp, cacheBuster: String? = null) {
    val context = LocalContext.current
    val cleanUsername = username.trim().lowercase()
    val imageRequest = remember(cleanUsername, cacheBuster) {
        val url = "https://wasteof-image-proxy.tnix.dev/$cleanUsername?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3"
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
                    .border(borderWidth, borderColor, CircleShape)
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
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    onMentionClick: ((String) -> Unit)? = null,
    onPostClick: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    openLinksInApp: Boolean = true,
    onTruncatedChanged: ((Boolean) -> Unit)? = null
) {
    val textColor = (color ?: MaterialTheme.colorScheme.onSurface).toArgb()
    val linkColor = (color ?: MaterialTheme.colorScheme.onBackground).toArgb()
    val density = LocalDensity.current
    val quoteStripeWidthPx = with(density) { QUOTE_STRIPE_WIDTH_DP.dp.roundToPx().coerceAtLeast(1) }
    val quoteGapWidthPx = with(density) { QUOTE_GAP_WIDTH_DP.dp.roundToPx() }

    val spannedText = remember(
        html, textColor, linkColor, onMentionClick, onPostClick, openLinksInApp,
        quoteStripeWidthPx, quoteGapWidthPx
    ) {
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

        spannable.getSpans(0, spannable.length, ClickableSpan::class.java).forEach { span ->
            val s = spannable.getSpanStart(span)
            val e = spannable.getSpanEnd(span)
            if (spannable.getSpans(s, e, StyleSpan::class.java).none { it.style == Typeface.BOLD }) {
                spannable.setSpan(StyleSpan(Typeface.BOLD), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val brandColor = 0xFF6366F1.toInt()
        spannable.getSpans(0, spannable.length, android.text.style.QuoteSpan::class.java).forEach { span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val flags = spannable.getSpanFlags(span)
            spannable.removeSpan(span)
            spannable.setSpan(
                BrandedQuoteSpan(
                    color = brandColor,
                    stripeWidthPx = quoteStripeWidthPx,
                    gapWidthPx = quoteGapWidthPx
                ),
                start, end, flags
            )
        }
        var len = spannable.length
        while (len > 0 && (spannable[len - 1] == '\n' || spannable[len - 1] == '\r' || spannable[len - 1] == ' ')) {
            len--
        }
        if (len < spannable.length) spannable.subSequence(0, len) else spannable
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                applyGoogleSansFlexTypeface(this, roundness = 50f)
                movementMethod = object : LinkMovementMethod() {
                    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: android.view.MotionEvent): Boolean {
                        val scrollX = widget.scrollX
                        val scrollY = widget.scrollY
                        val result = super.onTouchEvent(widget, buffer, event)
                        widget.scrollTo(scrollX, scrollY)
                        return result
                    }
                }
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 16f
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
    enabled: Boolean = true,
    pulse: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val isLoveMetric = label == "loves"
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    val containerColor = if (!enabled && !pulse) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    } else if (isActive) {
        if (isLoveMetric) {
            if (isDarkTheme) Color(0xFF5C1D24) else Color(0xFFFEE2E2)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (!enabled && !pulse) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    } else if (isActive) {
        if (isLoveMetric) {
            if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFEF4444)
        } else {
            MaterialTheme.colorScheme.primary
        }
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    var animMode by remember { mutableStateOf(0) }
    val iconScale by animateFloatAsState(
        targetValue = if (isActive) 1.35f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "MetricIconScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isActive && animMode == 1) 360f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "MetricIconRotation"
    )
    val iconOffsetY by animateFloatAsState(
        targetValue = if (isActive && animMode == 2) -4f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "MetricIconOffsetY"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = if (onClick != null) Modifier.clickable(enabled = enabled) {
            if (!isActive) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                animMode = (0..2).random()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onClick()
        } else Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (pulse) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            rotationZ = iconRotation
                            translationY = iconOffsetY
                        },
                    tint = contentColor
                )
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/** Opens the system share sheet for a post's wasteof.money URL. */
fun sharePostUrl(context: Context, postId: String) {
    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        putExtra(android.content.Intent.EXTRA_TEXT, "https://wasteof.money/posts/$postId")
        type = "text/plain"
    }
    context.startActivity(android.content.Intent.createChooser(sendIntent, null))
}

/**
 * Icon-only share pill styled to match [PostMetric] buttons so it can sit
 * alongside them in the post action row.
 */
@Composable
fun PostShareButton(postId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            sharePostUrl(context, postId)
        }
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 32.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                modifier = Modifier.size(16.dp)
            )
        }
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
    if (time <= 0) return ""
    val now = System.currentTimeMillis()
    val diffMs = now - time
    if (diffMs < 0) return "just now"

    val diffSec = diffMs / 1000
    if (diffSec < 60) {
        return "${diffSec.coerceAtLeast(1)}s ago"
    }

    val diffMin = diffSec / 60
    if (diffMin < 60) {
        return "${diffMin}m ago"
    }

    val diffHours = diffMin / 60
    if (diffHours < 24) {
        return "${diffHours}h ago"
    }

    val diffDays = diffHours / 24
    if (diffDays < 30) {
        return "${diffDays}d ago"
    }

    val diffMonths = diffDays / 30
    if (diffMonths < 12) {
        return "${diffMonths}mo ago"
    }

    val diffYears = diffDays / 365
    return "${diffYears.coerceAtLeast(1)}y ago"
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
        val uri = java.net.URI(url)
        val scheme = uri.scheme?.lowercase()
        (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    } catch (e: Exception) {
        false
    }
}

fun extractImages(html: String): List<String> {
    val imgTagRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    val directUrlRegex = """(?<!["'=/>])(https?://[^\s<>"']+\.(?:jpg|jpeg|png|gif|webp|svg)(?:\?[^\s<>"']*)?)""".toRegex(RegexOption.IGNORE_CASE)

    val fromTags = imgTagRegex.findAll(html).mapNotNull { it.groupValues.getOrNull(1) }
    val fromDirect = directUrlRegex.findAll(html).mapNotNull { it.groupValues.getOrNull(1) }

    return (fromTags + fromDirect)
        .filter { url -> isAllowedImageHost(url) }
        .distinct()
        .toList()
}

fun stripImages(html: String): String {
    val imgTagRegex = """<img[^>]*src=["']([^"']+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    var result = imgTagRegex.replace(html, "")
    val directUrlRegex = """(?<!["'=/>])(https?://[^\s<>"']+\.(?:jpg|jpeg|png|gif|webp|svg)(?:\?[^\s<>"']*)?)""".toRegex(RegexOption.IGNORE_CASE)
    result = directUrlRegex.replace(result) { match ->
        val url = match.value
        if (isAllowedImageHost(url)) "" else url
    }
    return result
}

/** Hosts that are wasteof frontends or otherwise handled by in-app navigation; never link-preview these. */
private val OPEN_GRAPH_SKIPPED_HOSTS = setOf(
    "wasteof.money",
    "beta.wasteof.money",
    "alpha.wasteof.money",
    "worm.eris.cafe",
    "wasteof.eris.cafe"
)

/** Extracts the lowercase host (without "www.") from an absolute http(s) URL, or null. */
fun urlHost(url: String): String? {
    val withoutScheme = url.substringAfter("://", "")
    if (withoutScheme.isEmpty()) return null
    val hostPort = withoutScheme
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
    val host = hostPort.substringAfterLast('@', hostPort).substringBefore(':')
    return host.lowercase(Locale.ROOT).removePrefix("www.").ifEmpty { null }
}

/** True when [url] is an absolute http(s) URL on a previewable (non-wasteof) host. */
fun isOpenGraphEligibleUrl(url: String): Boolean {
    if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) return false
    val host = urlHost(url) ?: return false
    return host !in OPEN_GRAPH_SKIPPED_HOSTS
}

/** Strips trailing sentence punctuation and unbalanced closing brackets from a bare URL. */
private fun trimTrailingUrlPunctuation(url: String): String {
    var result = url
    while (result.isNotEmpty() && result.last() in ".,;:!?…»\"'") {
        result = result.dropLast(1)
    }
    while (result.isNotEmpty() && (result.last() == ')' || result.last() == ']' || result.last() == '}')) {
        val closer = result.last()
        val opener = when (closer) {
            ')' -> '('
            ']' -> '['
            else -> '{'
        }
        if (result.count { it == opener } >= result.count { it == closer }) break
        result = result.dropLast(1)
    }
    return result
}

/**
 * Returns the first previewable link in [html] in document order — either an
 * <a href> target or a bare URL in visible text — skipping wasteof frontends,
 * which are handled by in-app navigation instead.
 */
fun extractFirstLink(html: String): String? {
    val candidates = mutableListOf<Pair<Int, String>>()
    val hrefRegex = Regex("""href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    val bareUrlRegex = Regex("""https?://[^\s<>"']+""", RegexOption.IGNORE_CASE)

    var i = 0
    while (i < html.length) {
        if (html[i] == '<') {
            val tagEnd = html.indexOf('>', i)
            if (tagEnd == -1) break
            val tag = html.substring(i, tagEnd + 1)
            val tagName = tag.substringBefore(' ').substringBefore('>').drop(1).lowercase(Locale.ROOT)
            if (tagName == "a") {
                hrefRegex.find(tag)?.let { match ->
                    candidates.add(i to match.groupValues[1])
                }
            }
            i = tagEnd + 1
        } else {
            val nextTag = html.indexOf('<', i)
            val end = if (nextTag == -1) html.length else nextTag
            bareUrlRegex.findAll(html.substring(i, end)).forEach { match ->
                candidates.add((i + match.range.first) to match.value)
            }
            i = end
        }
    }

    return candidates
        .sortedBy { it.first }
        .map { trimTrailingUrlPunctuation(it.second) }
        .firstOrNull { isOpenGraphEligibleUrl(it) }
}

private const val AUTO_LINK_TRAILING_PUNCTUATION = ".,;:!?…»)]}>\"'"

fun autoLinkAndMentions(html: String): String {
    if (!html.contains('<')) return linkifyPlainSegment(html, insideAnchor = false)

    val sb = StringBuilder(html.length + 64)
    var i = 0
    var anchorDepth = 0
    while (i < html.length) {
        if (html[i] == '<') {
            val tagEnd = html.indexOf('>', i)
            if (tagEnd == -1) {
                sb.append(html, i, html.length)
                break
            }
            val tag = html.substring(i, tagEnd + 1)
            val lower = tag.lowercase(Locale.ROOT)
            val isOpenAnchor = lower.startsWith("<a") &&
                (lower.length == 3 || !lower[2].isLetterOrDigit())
            val isCloseAnchor = lower.startsWith("</a")
            when {
                isOpenAnchor -> anchorDepth++
                isCloseAnchor -> anchorDepth = (anchorDepth - 1).coerceAtLeast(0)
            }
            sb.append(tag)
            i = tagEnd + 1
        } else {
            val nextTag = html.indexOf('<', i)
            val end = if (nextTag == -1) html.length else nextTag
            sb.append(linkifyPlainSegment(html.substring(i, end), insideAnchor = anchorDepth > 0))
            i = end
        }
    }
    return sb.toString()
}

/**
 * Linkifies a single plain-text (between-tags) HTML segment.
 * Never called for tag internals, so attribute values cannot be corrupted here.
 */
private fun linkifyPlainSegment(text: String, insideAnchor: Boolean): String {
    if (insideAnchor) return text
    var result = text

    val mentionRegex = Regex("""(^|[^A-Za-z0-9_.@/\-])@([A-Za-z0-9_]+)""")
    result = mentionRegex.replace(result) { m ->
        val user = m.groupValues[2]
        "${m.groupValues[1]}<a href=\"wombat://user/$user\">@$user</a>"
    }

    val urlRegex = Regex("""(https?://[^\s<>"']+)""", RegexOption.IGNORE_CASE)
    result = urlRegex.replace(result) { m ->
        val full = m.value
        var url = full
        while (url.isNotEmpty() && url.last() in AUTO_LINK_TRAILING_PUNCTUATION) {
            val closer = url.last()
            val opener = when (closer) {
                ')' -> '('
                ']' -> '['
                '}' -> '{'
                else -> null
            }
            if (opener != null && url.count { it == opener } >= url.count { it == closer }) break
            url = url.dropLast(1)
        }
        val trailing = full.substring(url.length)
        """<a href="$url">$url</a>$trailing"""
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

/**
 * Compact tappable square thumbnails for post images, shown when OpenGraph
 * previews are prioritised over the full-size image carousel.
 */
@Composable
fun PostImageSquares(
    images: List<String>,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEachIndexed { index, url ->
            val imageRequest = remember(url) {
                ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .precision(Precision.INEXACT)
                    .build()
            }
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onImageClick(images, index) }
            ) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = "Post image ${index + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun FullScreenImageViewer(
    images: List<String>,
    initialIndex: Int,
    post: Post? = null,
    username: String? = null,
    onDismiss: () -> Unit,
    onLoveClick: ((Post) -> Unit)? = null,
    onCommentClick: ((Post) -> Unit)? = null,
    onRepostClick: ((Post) -> Unit)? = null,
    onProfileClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val safeInitialIndex = initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = safeInitialIndex, pageCount = { images.size })
    val haptic = LocalHapticFeedback.current
    val displayUsername = username ?: post?.poster?.name
    var showUiControls by remember { mutableStateOf(true) }
    var isZoomedIn by remember { mutableStateOf(false) }

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
                    userScrollEnabled = !isZoomedIn
                ) { page ->
                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        val zoomChange = event.calculateZoom()
                                        val panChange = event.calculatePan()
                                        val isPinch = event.changes.size > 1
                                        if (isPinch || scale > 1f) {
                                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                                            if (scale > 1.02f) {
                                                offset += panChange
                                                isZoomedIn = true
                                            } else {
                                                offset = Offset.Zero
                                                scale = 1f
                                                isZoomedIn = false
                                            }
                                            event.changes.forEach { it.consume() }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        showUiControls = !showUiControls
                                    },
                                    onDoubleTap = {
                                        if (scale > 1.05f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                            isZoomedIn = false
                                        } else {
                                            scale = 3f
                                            isZoomedIn = true
                                        }
                                    }
                                )
                            }
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(images[page])
                                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Full screen image ${page + 1}",
                            loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) } },
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

                AnimatedVisibility(
                    visible = showUiControls,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                        }

                        if (images.size > 1) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1} / ${images.size}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(40.dp))
                        }

                        IconButton(
                            onClick = {
                                downloadImage(context, images[pagerState.currentPage], displayUsername)
                            },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Download", tint = Color.White)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showUiControls,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f),
                                        Color.Black.copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            displayUsername?.let { uname ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.clickable(enabled = onProfileClick != null) {
                                        onDismiss()
                                        onProfileClick?.invoke(uname)
                                    }
                                ) {
                                    ProfilePicture(username = uname, size = 32.dp)
                                    Text(
                                        text = "@${uname.lowercase()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            post?.let { p ->
                                val cleanText = remember(p.content) {
                                    autoLinkAndMentions(stripImages(p.content))
                                }
                                if (cleanText.isNotBlank()) {
                                    var isActuallyTruncated by remember { mutableStateOf(false) }
                                    Column {
                                        HtmlText(
                                            html = cleanText,
                                            color = Color.White,
                                            maxLines = 2,
                                            openLinksInApp = true,
                                            onMentionClick = { mention ->
                                                onDismiss()
                                                onProfileClick?.invoke(mention)
                                            },
                                            onTruncatedChanged = { isActuallyTruncated = it }
                                        )
                                        if (isActuallyTruncated) {
                                            Text(
                                                text = "...see more",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier
                                                    .padding(top = 2.dp)
                                                    .clickable {
                                                        onDismiss()
                                                        onCommentClick?.invoke(p)
                                                    }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    val pCachedLoved = p.isLoving == null && LoveCache.get(p.id) == true
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = if (p.isLoving == null && !pCachedLoved) 0.08f else 0.18f))
                                            .clickable(enabled = p.isLoving != null && !p.loveLoading) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onLoveClick?.invoke(p)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        if (p.loveLoading) {
                                            androidx.compose.material3.CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (p.isLoving == true || pCachedLoved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                contentDescription = "Love",
                                                tint = when {
                                                    p.isLoving == true || pCachedLoved -> Color(0xFFFF4081)
                                                    p.isLoving == null -> Color.White.copy(alpha = 0.5f)
                                                    else -> Color.White
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = p.loves.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .clickable {
                                                onDismiss()
                                                onCommentClick?.invoke(p)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Chat,
                                            contentDescription = "Comments",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = p.comments.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .clickable {
                                                onDismiss()
                                                onRepostClick?.invoke(p)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Repeat,
                                            contentDescription = "Repost",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = p.reposts.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    val shareContext = androidx.compose.ui.platform.LocalContext.current
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .clickable { sharePostUrl(shareContext, p.id) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "Share",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
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
