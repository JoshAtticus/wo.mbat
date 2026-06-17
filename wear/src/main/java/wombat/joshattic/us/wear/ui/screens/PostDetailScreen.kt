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
import coil.compose.AsyncImage
import wombat.joshattic.us.wear.WearUiState

@Composable
fun PostDetailScreen(
    uiState: WearUiState,
    onLoveClick: () -> Unit,
    onSubmitComment: (String) -> Unit,
    onBack: () -> Unit
) {
    val post = uiState.selectedPost ?: return

    // RemoteInput launcher for on-watch text entry (keyboard or voice)
    val KEY_COMMENT = "comment_text"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val bundle: Bundle? = RemoteInput.getResultsFromIntent(result.data ?: Intent())
        bundle?.getCharSequence(KEY_COMMENT)?.toString()?.takeIf { it.isNotBlank() }
            ?.let { onSubmitComment(it) }
    }

    fun launchCommentInput() {
        val remoteInput = RemoteInput.Builder(KEY_COMMENT)
            .setLabel("Write a reply…")
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

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()

        ScalingLazyColumn(
            state = rememberScalingLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
        ) {
            // Post content card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Author
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://wasteof-image-proxy.tnix.dev/${post.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                            contentDescription = post.poster.name,
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = post.poster.name,
                            style = MaterialTheme.typography.caption1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Full post text
                    Text(
                        text = postPlainText,
                        style = MaterialTheme.typography.body2
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
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = "https://wasteof-image-proxy.tnix.dev/${post.repost.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp).clip(CircleShape)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(post.repost.poster.name, style = MaterialTheme.typography.caption2, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(repostText, style = MaterialTheme.typography.caption2)
                                
                                if (repostImages.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    repostImages.forEach { url ->
                                        val fullUrl = if (url.startsWith("/")) "https://api.wasteof.money$url" else url
                                        AsyncImage(
                                            model = fullUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp, max = 150.dp).padding(vertical = 2.dp),
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
                            AsyncImage(
                                model = fullUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp, max = 150.dp).padding(vertical = 4.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.caption3,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    // Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onLoveClick,
                            modifier = Modifier.size(32.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
                        ) {
                            Icon(
                                imageVector = if (post.isLoving == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Love",
                                modifier = Modifier.size(18.dp),
                                tint = if (post.isLoving == true) Color(0xFFEF4444) else MaterialTheme.colors.onSurface
                            )
                        }
                        Text("${post.loves}", style = MaterialTheme.typography.caption2)
                        Spacer(Modifier.width(12.dp))
                        Chip(
                            onClick = { launchCommentInput() },
                            label = { Text("Reply", style = MaterialTheme.typography.caption2) },
                            icon = {
                                Icon(
                                    Icons.Filled.ModeComment,
                                    contentDescription = "Reply",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = ChipDefaults.primaryChipColors(),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            // Divider label
            item {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            if (uiState.commentsLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            } else if (uiState.comments.isEmpty()) {
                item {
                    Text(
                        "No comments yet",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            items(uiState.comments, key = { it.id }) { comment ->
                val commentPlainText = androidx.compose.runtime.remember(comment.content) {
                    comment.content.stripHtml()
                }

                Chip(
                    onClick = { launchCommentInput() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    colors = ChipDefaults.secondaryChipColors(),
                    label = {
                        Column(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://wasteof-image-proxy.tnix.dev/${comment.poster.name}?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3",
                                    contentDescription = comment.poster.name,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    comment.poster.name,
                                    style = MaterialTheme.typography.caption2,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = commentPlainText,
                                style = MaterialTheme.typography.caption2,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }
        }
    }
}
