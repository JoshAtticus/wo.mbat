@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package wombat.joshattic.us.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Post

@Composable
fun PostDetailsContent(
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
    onMentionClick: ((String) -> Unit)? = null,
    replyingTo: Comment? = null,
    onCancelReply: () -> Unit = {},
    onReplyToComment: (Comment) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onLoveClick: ((Post) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    scrollToCommentId: String? = null,
    onScrollToCommentComplete: () -> Unit = {},
    onRepostClick: ((Post) -> Unit)? = null,
    onQuoteClick: ((Post) -> Unit)? = null,
    currentUsername: String? = null,
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession> = emptyList(),
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    onPostClickById: ((String) -> Unit)? = null,
    showCloseButton: Boolean = false,
    focusedComment: Comment? = null,
    onFocusComment: (Comment) -> Unit = {},
    onClearFocusComment: () -> Unit = {},
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning",
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val handleProfileClick = remember(onProfileClick, onDismiss) {
        { username: String ->
            onDismiss()
            onProfileClick(username)
        }
    }
    val handleMentionClick = remember(onMentionClick, onDismiss) {
        onMentionClick?.let { orig ->
            { username: String ->
                onDismiss()
                orig(username)
            }
        }
    }

    val currentOnExpand by rememberUpdatedState(onExpandComments)

    LaunchedEffect(post.id) {
        currentOnExpand()
    }

    LaunchedEffect(scrollToCommentId, loading, comments) {
        if (scrollToCommentId != null && !loading && comments.isNotEmpty()) {
            fun hasCommentId(c: Comment, id: String): Boolean {
                if (c.id == id) return true
                c.replies?.forEach { r ->
                    if (hasCommentId(r, id)) return true
                }
                return false
            }
            val index = comments.indexOfFirst { hasCommentId(it, scrollToCommentId) }
            if (index != -1) {
                listState.animateScrollToItem(index + 1) // +1 for post header
                onScrollToCommentComplete()
            }
        }
    }

    Column(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (showCloseButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close post details")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }

        val isImeVisible = WindowInsets.isImeVisible
        val isReplyBoxVisible by remember(isImeVisible) {
            derivedStateOf {
                isImeVisible || listState.layoutInfo.visibleItemsInfo.any { it.index > 0 }
            }
        }
        val displayComments = if (focusedComment != null) listOf(focusedComment) else comments

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
                    onMentionClick = handleMentionClick,
                    onProfileClick = handleProfileClick,
                    onLoveClick = onLoveClick,
                    onPostClick = onPostClick,
                    onImageClick = onImageClick,
                    onBlockUser = onBlockUser,
                    onReportPost = onReportPost,
                    onRepostClick = onRepostClick,
                    onQuoteClick = onQuoteClick,
                    currentUsername = currentUsername,
                    savedAccounts = savedAccounts,
                    onDeletePost = onDeletePost,
                    onEditPost = onEditPost,
                    showImages = showImages,
                    openLinksInApp = openLinksInApp,
                    linkPreviewPriority = linkPreviewPriority,
                    onPostClickById = onPostClickById,
                    blockedUsernames = blockedUsernames,
                    blockedQuoteHandling = blockedQuoteHandling,
                    // The user explicitly bypassed the block warning to open this post
                    ignoreBlockedPoster = true
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnimatedContent(
                        targetState = focusedComment != null,
                        transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
                        label = "threadHeaderTransition"
                    ) { isFocused ->
                        if (isFocused) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onClearFocusComment() }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to post thread",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Back to post thread",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text("Comments", style = MaterialTheme.typography.titleMedium)
                        }
                    }
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

            if (!loading && displayComments.isNotEmpty()) {
                items(displayComments.distinctBy { it.id }, key = { it.id }) { comment ->
                    CommentCard(
                        comment = comment,
                        isBanned = isBanned,
                        onReply = onReplyToComment,
                        onProfileClick = handleProfileClick,
                        onMentionClick = handleMentionClick,
                        onPostClick = onPostClickById,
                        openLinksInApp = openLinksInApp,
                        depth = 0,
                        onFocusComment = onFocusComment
                    )
                }
            }
        }

        // Input area (TextField + Reply button) — imePadding lifts it above the software keyboard
        if (!isBanned) {
            AnimatedVisibility(
                visible = isReplyBoxVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
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
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        AnimatedVisibility(visible = replyingTo != null) {
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
                                Spacer(modifier = Modifier.width(6.dp))
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
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            placeholder = {
                                Text(
                                    if (replyingTo != null) "Reply to @${replyingTo.poster.name}..."
                                    else "Write a reply..."
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = onSubmit,
                                    enabled = draft.isNotBlank()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send reply",
                                        tint = if (draft.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostDetailsSheet(
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
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    onBlockUser: ((String) -> Unit)? = null,
    onReportPost: ((Post) -> Unit)? = null,
    scrollToCommentId: String? = null,
    onScrollToCommentComplete: () -> Unit = {},
    onRepostClick: (Post) -> Unit = {},
    onQuoteClick: (Post) -> Unit = {},
    currentUsername: String? = null,
    savedAccounts: List<wombat.joshattic.us.data.model.AuthSession> = emptyList(),
    onDeletePost: ((Post) -> Unit)? = null,
    onEditPost: ((Post) -> Unit)? = null,
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    onPostClickById: ((String) -> Unit)? = null,
    focusedComment: Comment? = null,
    onFocusComment: (Comment) -> Unit = {},
    onClearFocusComment: () -> Unit = {},
    blockedUsernames: Set<String> = emptySet(),
    blockedQuoteHandling: String = "warning",
    errorMessage: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(scrollToCommentId) {
        if (scrollToCommentId != null) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        PostDetailsContent(
            post = post,
            comments = comments,
            loading = loading,
            draft = draft,
            isBanned = isBanned,
            onDraftChange = onDraftChange,
            onSubmit = onSubmit,
            onDismiss = onDismiss,
            onExpandComments = onExpandComments,
            onCollapseComments = onCollapseComments,
            onMentionClick = onMentionClick,
            replyingTo = replyingTo,
            onCancelReply = onCancelReply,
            onReplyToComment = onReplyToComment,
            onProfileClick = onProfileClick,
            onLoveClick = onLoveClick,
            onPostClick = onPostClick,
            onImageClick = onImageClick,
            onBlockUser = onBlockUser,
            onReportPost = onReportPost,
            scrollToCommentId = scrollToCommentId,
            onScrollToCommentComplete = onScrollToCommentComplete,
            onRepostClick = onRepostClick,
            onQuoteClick = onQuoteClick,
            currentUsername = currentUsername,
            savedAccounts = savedAccounts,
            onDeletePost = onDeletePost,
            onEditPost = onEditPost,
            showImages = showImages,
            openLinksInApp = openLinksInApp,
            linkPreviewPriority = linkPreviewPriority,
            onPostClickById = onPostClickById,
            showCloseButton = false,
            focusedComment = focusedComment,
            onFocusComment = onFocusComment,
            onClearFocusComment = onClearFocusComment,
            blockedUsernames = blockedUsernames,
            blockedQuoteHandling = blockedQuoteHandling,
            errorMessage = errorMessage
        )
    }
}

