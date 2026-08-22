package wombat.joshattic.us.wear.ui.screens

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.data.model.Post

/**
 * Explore screen.
 *
 * A full-width Search chip sits at the top. Tapping it uses [RemoteInput] (same
 * mechanism as [ComposeScreen]) to open the Wear OS keyboard/voice input. Once the
 * user submits a query, [onSearch] is called and the results replace the trending feed.
 *
 * When no search is active, the standard trending feed is shown.
 */
@Composable
fun ExploreScreen(
    uiState: WearUiState,
    onPostClick: (Post) -> Unit,
    onLoveClick: (Post) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    val KEY_SEARCH = "search_query"
    val searchLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val bundle: Bundle? = RemoteInput.getResultsFromIntent(result.data ?: Intent())
            val query = bundle?.getCharSequence(KEY_SEARCH)?.toString()?.trim()
            if (!query.isNullOrBlank()) {
                onSearch(query)
            }
        }
    }

    fun launchSearch() {
        val remoteInput = RemoteInput.Builder(KEY_SEARCH)
            .setLabel("Search posts…")
            .build()
        val intent = androidx.wear.input.RemoteInputIntentHelper.createActionRemoteInputIntent()
        androidx.wear.input.RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        searchLauncher.launch(intent)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TimeText()

        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            autoCentering = null,
            contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
            scalingParams = ScalingLazyColumnDefaults.scalingParams(edgeScale = 0.75f)
        ) {
            // ── Search chip (always visible, 2-wide / full-width) ────────────
            item {
                val activeQuery = uiState.searchQuery
                Chip(
                    onClick = { launchSearch() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 4.dp),
                    colors = if (activeQuery != null)
                        ChipDefaults.primaryChipColors()
                    else
                        ChipDefaults.secondaryChipColors(),
                    icon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (activeQuery != null) "\"$activeQuery\"" else "Search posts",
                            style = MaterialTheme.typography.caption1,
                            fontWeight = if (activeQuery != null) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                )
            }

            // "Clear search" chip when a search is active
            if (uiState.searchQuery != null) {
                item {
                    Chip(
                        onClick = onClearSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .padding(horizontal = 4.dp),
                        colors = ChipDefaults.secondaryChipColors(),
                        label = {
                            Text(
                                "Clear · show trending",
                                style = MaterialTheme.typography.caption2
                            )
                        }
                    )
                }
            } else {
                // Section label
                item {
                    Text(
                        text = "Trending",
                        style = MaterialTheme.typography.caption1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                    )
                }
            }

            // ── Feed / search results ────────────────────────────────────────
            if (uiState.exploreLoading && uiState.exploreFeed.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    }
                }
            } else if (uiState.exploreFeed.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery != null) "No results found" else "Nothing here yet",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            items(uiState.exploreFeed.distinctBy { it.id }, key = { it.id }) { post ->
                WearPostCard(
                    post = post,
                    showImages = uiState.showImages,
                    showPfp = uiState.showPfp,
                    onClick = { onPostClick(post) },
                    onLoveClick = { onLoveClick(post) }
                )
            }

            if (uiState.exploreHasMore && uiState.exploreFeed.isNotEmpty()) {
                item {
                    Chip(
                        onClick = onLoadMore,
                        label = { Text("Load more", style = MaterialTheme.typography.body2) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.padding(top = 4.dp).height(32.dp)
                    )
                }
            }
        }
    }
}
