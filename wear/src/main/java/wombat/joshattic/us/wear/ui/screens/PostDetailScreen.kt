package wombat.joshattic.us.wear.ui.screens

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import java.text.DateFormat
import java.util.Date
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import coil.compose.SubcomposeAsyncImage
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Comment
import wombat.joshattic.us.wear.data.model.Post

@Composable
fun PostDetailScreen(
    uiState: WearUiState,
    onLoveClick: () -> Unit,
    onSubmitComment: (text: String, parentId: String?) -> Unit,
    onRepostClick: (Post) -> Unit,
    onQuoteClick: (Post) -> Unit,
    onBack: () -> Unit
) {
    val post = uiState.selectedPost ?: return

    // Track target parent comment ID for replies
    var replyTargetCommentId by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var replyTargetUsername by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    // RemoteInput launcher for on-watch text entry (keyboard or voice)
    val KEY_COMMENT = "comment_text"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val bundle: Bundle? = RemoteInput.getResultsFromIntent(result.data ?: Intent())
        bundle?.getCharSequence(KEY_COMMENT)?.toString()?.takeIf { it.isNotBlank() }
            ?.let { text -> onSubmitComment(text, replyTargetCommentId) }
    }

    fun launchCommentInput(parentCommentId: String? = null, parentUsername: String? = null) {
        replyTargetCommentId = parentCommentId
        replyTargetUsername = parentUsername
        val label = if (parentUsername != null) "Reply to @$parentUsername…" else "Write a reply…"
        val remoteInput = RemoteInput.Builder(KEY_COMMENT)
            .setLabel(label)
            .build()
        val intent = androidx.wear.input.RemoteInputIntentHelper.createActionRemoteInputIntent()
        androidx.wear.input.RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        launcher.launch(intent)
    }

    val postPlainText = androidx.compose.runtime.remember(post.content) {
        post.content?.stripHtml() ?: ""
    }
    val images = androidx.compose.runtime.remember(post.content) {
        val regex = Regex("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>")
        post.content?.let { regex.findAll(it).map { m -> m.groupValues[1] }.toList() } ?: emptyList()
    }
    val formattedTime = androidx.compose.runtime.remember(post.time) {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(post.time))
    }

    val flattenedComments = androidx.compose.runtime.remember(uiState.comments) {
        flattenCommentsWithLevel(uiState.comments)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()

        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            // Start at the top instead of centering the post card mid-screen,
            // which made the screen look pre-scrolled on open
            autoCentering = null,
            // 40dp top padding: clears the TimeText clock and the sharp part of the
            // circular bezel curve so the first card isn't clipped at scroll position 0
            contentPadding = PaddingValues(top = 40.dp, bottom = 28.dp),
            scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.75f
            )
        ) {
            // Post content card — 20dp side padding keeps it inside the circular
            // bezel while it sits in the narrower top region of the screen
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    // Author
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SubcomposeAsyncImage(
                            model = "https://wasteof-image-proxy.tnix.dev/${post.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                            contentDescription = post.poster.name,
                            loading = {
                                CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 1.5.dp)
                            },
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = post.poster.name,
                            style = MaterialTheme.typography.caption1,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Full post text
                    Text(
                        text = postPlainText,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    // Repost and Images
                    if (post.repost != null) {
                        val repostText = androidx.compose.runtime.remember(post.repost.content) {
                            post.repost.content?.stripHtml() ?: ""
                        }
                        val repostImages = androidx.compose.runtime.remember(post.repost.content) {
                            val regex = Regex("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>")
                            post.repost.content?.let { regex.findAll(it).map { m -> m.groupValues[1] }.toList() } ?: emptyList()
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SubcomposeAsyncImage(
                                        model = "https://wasteof-image-proxy.tnix.dev/${post.repost.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                        contentDescription = null,
                                        loading = {
                                            CircularProgressIndicator(modifier = Modifier.padding(1.dp), strokeWidth = 1.dp)
                                        },
                                        modifier = Modifier.size(18.dp).clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        post.repost.poster.name,
                                        style = MaterialTheme.typography.caption2,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(repostText, style = MaterialTheme.typography.caption2, color = MaterialTheme.colors.onSurfaceVariant)
                                
                                if (repostImages.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    repostImages.forEach { url ->
                                        val fullUrl = if (url.startsWith("/")) "https://api.wasteof.money$url" else url
                                        SubcomposeAsyncImage(
                                            model = fullUrl,
                                            contentDescription = null,
                                            loading = {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().height(80.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 40.dp, max = 150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .padding(vertical = 2.dp),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (images.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        images.forEach { url ->
                            val fullUrl = if (url.startsWith("/")) "https://api.wasteof.money$url" else url
                            SubcomposeAsyncImage(
                                model = fullUrl,
                                contentDescription = null,
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 40.dp, max = 150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.caption3,
                        color = MaterialTheme.colors.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    // Actions
                    var showRepostDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Chip(
                            onClick = onLoveClick,
                            label = { Text("${post.loves}", style = MaterialTheme.typography.caption2) },
                            icon = {
                                Icon(
                                    imageVector = if (post.isLoving == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Love",
                                    tint = if (post.isLoving == true) Color(0xFFEF4444) else MaterialTheme.colors.onSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
                            modifier = Modifier.weight(1f).height(34.dp)
                        )
                        Chip(
                            onClick = { showRepostDialog = true },
                            label = { Text("${post.reposts}", style = MaterialTheme.typography.caption2) },
                            icon = {
                                Icon(Icons.Filled.Repeat, "Repost", modifier = Modifier.size(15.dp))
                            },
                            colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
                            modifier = Modifier.weight(1f).height(34.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Chip(
                        onClick = { launchCommentInput() },
                        label = { Text("Reply to Post (${post.comments})", style = MaterialTheme.typography.caption2) },
                        icon = {
                            Icon(
                                Icons.Filled.ModeComment,
                                contentDescription = "Reply",
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    )

                    androidx.wear.compose.material.dialog.Dialog(
                        showDialog = showRepostDialog,
                        onDismissRequest = { showRepostDialog = false },
                    ) {
                        androidx.wear.compose.material.dialog.Alert(
                            title = { Text("Repost", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                            negativeButton = {
                                Button(
                                    onClick = { showRepostDialog = false; onRepostClick(post) },
                                    colors = ButtonDefaults.secondaryButtonColors()
                                ) {
                                    Icon(Icons.Filled.Repeat, "Repost")
                                }
                            },
                            positiveButton = {
                                Button(
                                    onClick = { showRepostDialog = false; onQuoteClick(post) }
                                ) {
                                    Icon(Icons.Filled.FormatQuote, "Quote")
                                }
                            }
                        )
                    }
                }
            }

            // Divider label
            item {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (uiState.commentsLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            } else if (uiState.comments.isEmpty()) {
                item {
                    Text(
                        "No comments yet",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            items(flattenedComments, key = { (comment, _) -> comment.id }) { (comment, level) ->
                val commentPlainText = androidx.compose.runtime.remember(comment.content) {
                    comment.content.stripHtml()
                }

                val indentDp = (level.coerceAtMost(3) * 10).dp

                Chip(
                    onClick = { launchCommentInput(parentCommentId = comment.id, parentUsername = comment.poster.name) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = indentDp + 2.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (level > 0) MaterialTheme.colors.surface.copy(alpha = 0.7f) else MaterialTheme.colors.surface
                    ),
                    label = {
                        Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SubcomposeAsyncImage(
                                    model = "https://wasteof-image-proxy.tnix.dev/${comment.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                    contentDescription = comment.poster.name,
                                    loading = {
                                        CircularProgressIndicator(modifier = Modifier.padding(1.dp), strokeWidth = 1.dp)
                                    },
                                    modifier = Modifier.size(16.dp).clip(CircleShape)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    comment.poster.name,
                                    style = MaterialTheme.typography.caption2,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colors.onSurface
                                )
                                if (level > 0) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "reply",
                                        style = MaterialTheme.typography.caption3,
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = commentPlainText,
                                style = MaterialTheme.typography.caption2,
                                color = MaterialTheme.colors.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }
        }
    }
}

private fun flattenCommentsWithLevel(comments: List<Comment>, level: Int = 0): List<Pair<Comment, Int>> {
    val result = mutableListOf<Pair<Comment, Int>>()
    for (comment in comments) {
        result.add(comment to level)
        val replies = comment.replies ?: emptyList()
        if (replies.isNotEmpty()) {
            result.addAll(flattenCommentsWithLevel(replies, level + 1))
        }
    }
    return result
}
