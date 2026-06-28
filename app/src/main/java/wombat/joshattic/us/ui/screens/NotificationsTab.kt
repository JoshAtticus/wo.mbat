@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import wombat.joshattic.us.data.model.Notification

@Composable
fun NotificationsTab(
    session: Any?,
    unreadNotifications: List<Notification>,
    readNotifications: List<Notification>,
    loading: Boolean,
    loadingMore: Boolean = false,
    isLastPage: Boolean = false,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (Notification) -> Unit,
    onLoadNextPage: () -> Unit = {},
    openLinksInApp: Boolean = true,
    onMentionClick: ((String) -> Unit)? = null,
    onPostClickById: ((String) -> Unit)? = null
) {
    val refreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    // Trigger load-more when near end of list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                if (visibleItems.isEmpty()) false
                else {
                    val lastVisible = visibleItems.last()
                    lastVisible.index >= listState.layoutInfo.totalItemsCount - 3
                }
            }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadNextPage() }
    }

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = loading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (session == null || unreadNotifications.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (session == null) {
                                Text(
                                    text = "Sign in to receive alerts about loves, comments, reposts, and follows.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (unreadNotifications.isNotEmpty()) {
                                Button(onClick = onMarkAllRead) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark all read")
                                }
                            }
                        }
                    }
                }
            }

            if (unreadNotifications.isEmpty() && !loading) {
                item { EmptyStateCard("All clear", "No unread notifications right now.") }
            }

            items(unreadNotifications.distinctBy { it.id }, key = { it.id }, contentType = { "notification" }) { notification ->
                NotificationCard(
                    notification = notification,
                    openLinksInApp = openLinksInApp,
                    onMentionClick = onMentionClick,
                    onPostClick = onPostClickById,
                    onClick = { onNotificationClick(notification) }
                )
            }

            if (readNotifications.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "Read notifications",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                // Filter out any notifications that might also be in the unread list
                val filteredRead = readNotifications.filter { read ->
                    unreadNotifications.none { unread -> unread.id == read.id }
                }.distinctBy { it.id }

                items(filteredRead, key = { it.id }, contentType = { "notification" }) { notification ->
                    NotificationCard(
                        notification = notification,
                        openLinksInApp = openLinksInApp,
                        onMentionClick = onMentionClick,
                        onPostClick = onPostClickById,
                        onClick = { onNotificationClick(notification) }
                    )
                }
            }

            // Load more indicator
            if (loadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
