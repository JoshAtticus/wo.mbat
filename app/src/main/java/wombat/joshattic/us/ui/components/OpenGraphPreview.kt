package wombat.joshattic.us.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import wombat.joshattic.us.data.model.OpenGraphResponse
import wombat.joshattic.us.data.network.RetrofitClient
import wombat.joshattic.us.ui.screens.urlHost

/**
 * Process-wide memory cache for link previews so recomposed feed cards and
 * repeated links never refetch. Failures are negatively cached too, so
 * unreachable sites are not hammered on every scroll pass.
 */
object OpenGraphPreviewCache {
    private const val MAX_ENTRIES = 128
    private val cache = LinkedHashMap<String, OpenGraphResponse>()
    private val failures = LinkedHashSet<String>()

    @Synchronized
    fun get(url: String): OpenGraphResponse? = cache[url]

    @Synchronized
    fun remember(url: String, response: OpenGraphResponse) {
        cache.remove(url)
        cache[url] = response
        failures.remove(url)
        while (cache.size > MAX_ENTRIES) {
            cache.remove(cache.keys.first())
        }
    }

    @Synchronized
    fun rememberFailure(url: String) {
        failures.remove(url)
        failures.add(url)
        while (failures.size > MAX_ENTRIES * 2) {
            failures.remove(failures.first())
        }
    }

    @Synchronized
    fun isKnownFailure(url: String): Boolean = url in failures
}

/**
 * Link preview card for the first external URL in a post, fetched from
 * og.joshattic.us. Two layouts:
 *  - Large banner (image with overlaid title and "From <domain>" caption) when
 *    the post has no images of its own and the preview provides one.
 *  - Compact card (domain / title / description, with a thumbnail when the
 *    preview has any image) in all other cases.
 * Renders nothing while loading or when no usable metadata is available.
 */
@Composable
fun OpenGraphPreview(
    url: String,
    hasPostImages: Boolean,
    openLinksInApp: Boolean = true,
    modifier: Modifier = Modifier
) {
    var response by remember(url) { mutableStateOf(OpenGraphPreviewCache.get(url)) }
    var unavailable by remember(url) { mutableStateOf(OpenGraphPreviewCache.isKnownFailure(url)) }
    val context = LocalContext.current

    LaunchedEffect(url) {
        if (response == null && !unavailable) {
            val result = withContext(Dispatchers.IO) {
                runCatching { RetrofitClient.openGraphApiService.fetchOpenGraph(url) }
            }
            result.onSuccess { fetched ->
                val metadata = fetched.metadata
                val usable = metadata != null &&
                    (fetched.statusCode == null || fetched.statusCode in 200..299) &&
                    (!metadata.title.isNullOrBlank() ||
                        !metadata.description.isNullOrBlank() ||
                        !metadata.image.isNullOrBlank())
                if (usable && metadata != null) {
                    OpenGraphPreviewCache.remember(url, fetched)
                    response = fetched
                } else {
                    OpenGraphPreviewCache.rememberFailure(url)
                    unavailable = true
                }
            }.onFailure {
                OpenGraphPreviewCache.rememberFailure(url)
                unavailable = true
            }
        }
    }

    if (unavailable) return
    val metadata = response?.metadata ?: return
    val domain = remember(url) { urlHost(url) }
    val title = metadata.title?.trim()?.takeIf { it.isNotEmpty() }
    val description = metadata.description?.trim()?.takeIf { it.isNotEmpty() }
    val imageUrl = metadata.image?.trim()?.takeIf { it.isNotEmpty() }
    if (title == null && description == null && imageUrl == null) return
    renderPreview(url, domain, title, description, imageUrl, hasPostImages, openLinksInApp, context, modifier)
}

@Composable
private fun renderPreview(
    url: String,
    domain: String?,
    title: String?,
    description: String?,
    imageUrl: String?,
    hasPostImages: Boolean,
    openLinksInApp: Boolean,
    context: android.content.Context,
    modifier: Modifier
) {
    val openLink: () -> Unit = {
        try {
            val uri = android.net.Uri.parse(url)
            if (openLinksInApp) {
                androidx.browser.customtabs.CustomTabsIntent.Builder().build().launchUrl(context, uri)
            } else {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (!hasPostImages && imageUrl != null) {
        // Large banner style: full-width preview image with the title overlaid at the bottom.
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = openLink)
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = title ?: "Link preview from $domain",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
                if (title != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            if (domain != null) {
                Text(
                    text = "From $domain",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }
    } else {
        // Compact style: thumbnail (when available) beside domain / title / description.
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = openLink)
        ) {
            Row {
                if (imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(width = 92.dp, height = 92.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (domain != null) {
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}