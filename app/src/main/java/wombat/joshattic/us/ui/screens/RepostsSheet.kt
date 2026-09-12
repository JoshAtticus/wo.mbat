@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Post

/**
 * Bottom sheet listing everyone who reposted [post]. The original post sits at
 * the top for context; tapping any entry opens its post details.
 */
@Composable
fun RepostsSheet(
    post: Post,
    reposts: List<Post>,
    loading: Boolean,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onOpenPost: (Post) -> Unit,
    onProfileClick: (String) -> Unit = {},
    onMentionClick: ((String) -> Unit)? = null,
    onLoveClick: ((Post) -> Unit)? = null,
    onRepostClick: ((Post) -> Unit)? = null,
    onQuoteClick: ((Post) -> Unit)? = null,
    currentUsername: String? = null,
    savedAccounts: List<AuthSession> = emptyList(),
    onImageClick: (List<String>, Int, Post?) -> Unit = { _, _, _ -> },
    showImages: Boolean = true,
    openLinksInApp: Boolean = true,
    linkPreviewPriority: String = "images",
    blockedUsernames: Set<String> = emptySet()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // "1 Repost and 2 Quotes", "2 Quotes", "1 Repost" …
    val repostCount = reposts.count { isPureRepost(it) }
    val quoteCount = reposts.size - repostCount
    val headerTitle = when {
        reposts.isEmpty() -> "Reposts"
        else -> listOf(
            repostCount.takeIf { it > 0 }?.let { "$it Repost" + if (it == 1) "" else "s" },
            quoteCount.takeIf { it > 0 }?.let { "$it Quote" + if (it == 1) "" else "s" }
        ).filterNotNull().joinToString(" and ")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // Default sheet color matches PostCard's surfaceContainerLow, which
        // would make the post cards invisible — use the background instead.
        containerColor = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                Text(
                    headerTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            item(key = "original") {
                PostCard(
                    post = post,
                    onClick = { onOpenPost(post) },
                    truncated = true,
                    currentUsername = currentUsername,
                    savedAccounts = savedAccounts,
                    onMentionClick = onMentionClick,
                    onProfileClick = onProfileClick,
                    onLoveClick = onLoveClick,
                    onRepostClick = onRepostClick,
                    onQuoteClick = onQuoteClick,
                    onImageClick = onImageClick,
                    showImages = showImages,
                    openLinksInApp = openLinksInApp,
                    linkPreviewPriority = linkPreviewPriority,
                    blockedUsernames = blockedUsernames
                )
            }
            // Separator between the original post and the reposts list
            item(key = "separator") {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            if (loading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (errorMessage != null) {
                item(key = "error") {
                    Text(
                        errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            } else if (reposts.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "No reposts yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            } else {
                items(reposts, key = { it.id }) { repost ->
                    PostCard(
                        post = repost,
                        onClick = { onOpenPost(repost) },
                        truncated = true,
                        currentUsername = currentUsername,
                        savedAccounts = savedAccounts,
                        onMentionClick = onMentionClick,
                        onProfileClick = onProfileClick,
                        onLoveClick = onLoveClick,
                        onRepostClick = onRepostClick,
                        onQuoteClick = onQuoteClick,
                        onImageClick = onImageClick,
                        showImages = showImages,
                        openLinksInApp = openLinksInApp,
                        linkPreviewPriority = linkPreviewPriority,
                        blockedUsernames = blockedUsernames
                    )
                }
            }
        }
    }
}
