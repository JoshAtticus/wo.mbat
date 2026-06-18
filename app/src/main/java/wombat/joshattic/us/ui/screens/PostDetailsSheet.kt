@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Post

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
                        onQuoteClick = onQuoteClick,
                        currentUsername = currentUsername,
                        onDeletePost = onDeletePost
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
