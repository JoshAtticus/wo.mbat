package wombat.joshattic.us.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
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
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
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

    // Shared IO scope so in-flight fetches complete and cache their result even
    // when the card that started them leaves composition during a scroll.
    private val fetchScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cache = LinkedHashMap<String, OpenGraphResponse>()
    private val failures = LinkedHashSet<String>()
    private val inFlight = mutableMapOf<String, Deferred<Result<OpenGraphResponse>>>()

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

    /**
     * Returns cached metadata, or fetches it on the shared IO scope. Concurrent
     * requests for the same URL share one in-flight call instead of doubling up,
     * and every caller observes the outcome once the request settles.
     */
    suspend fun getOrFetch(url: String): OpenGraphResponse? {
        cache[url]?.let { return it }
        if (isKnownFailure(url)) return null
        val deferred: Deferred<Result<OpenGraphResponse>> = synchronized(this) {
            inFlight.getOrPut(url) {
                fetchScope.async {
                    runCatching { RetrofitClient.openGraphApiService.fetchOpenGraph(url) }
                }
            }
        }
        val result = deferred.await()
        synchronized(this) { inFlight.remove(url) }
        return result.getOrNull()?.also { fetched ->
            if (isUsable(fetched)) remember(url, fetched) else rememberFailure(url)
        }
    }

    /** A response is usable when the fetch succeeded and any metadata came back. */
    fun isUsable(fetched: OpenGraphResponse): Boolean {
        val metadata = fetched.metadata ?: return false
        val statusOk = fetched.statusCode == null || fetched.statusCode in 200..299
        return statusOk &&
            (!metadata.title.isNullOrBlank() ||
                !metadata.description.isNullOrBlank() ||
                !metadata.image.isNullOrBlank())
    }
}

/**
 * True when the preview should render as the large banner: requires a preview
 * image, and either the post has no images of its own or OpenGraph is
 * prioritised over post images.
 */
fun shouldUseLargePreview(hasPreviewImage: Boolean, hasPostImages: Boolean, openGraphFirst: Boolean): Boolean =
    hasPreviewImage && (!hasPostImages || openGraphFirst)

/** True when the "prioritise OpenGraph" link preview setting is active. */
fun isOpenGraphPriority(linkPreviewPriority: String?): Boolean =
    linkPreviewPriority.equals("opengraph", ignoreCase = true)


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
    openGraphFirst: Boolean = false,
    modifier: Modifier = Modifier
) {
    var response by remember(url) { mutableStateOf(OpenGraphPreviewCache.get(url)) }
    var unavailable by remember(url) { mutableStateOf(OpenGraphPreviewCache.isKnownFailure(url)) }
    val context = LocalContext.current

    // Fully asynchronous: the request runs on the cache's IO scope (never the
    // UI thread and never blocking composition); the card simply appears when
    // the result lands.
    LaunchedEffect(url) {
        if (response == null && !unavailable) {
            val fetched = OpenGraphPreviewCache.getOrFetch(url)
            if (fetched != null) response = fetched else unavailable = true
        }
    }

    if (unavailable) return
    val metadata = response?.metadata ?: return
    val domain = remember(url) { urlHost(url) }
    val title = metadata.title?.trim()?.takeIf { it.isNotEmpty() }
    val description = metadata.description?.trim()?.takeIf { it.isNotEmpty() }
    val imageUrl = metadata.image?.trim()?.takeIf { it.isNotEmpty() }
    if (title == null && description == null && imageUrl == null) return
    renderPreview(url, domain, title, description, imageUrl, hasPostImages, openGraphFirst, openLinksInApp, context, modifier)
}

@Composable
private fun renderPreview(
    url: String,
    domain: String?,
    title: String?,
    description: String?,
    imageUrl: String?,
    hasPostImages: Boolean,
    openGraphFirst: Boolean,
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

    if (shouldUseLargePreview(imageUrl != null, hasPostImages, openGraphFirst)) {
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
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (imageUrl != null) {
                    // Full-height flush thumbnail: no gutter on any side, so
                    // tall text blocks can't pool empty space around it.
                    // AsyncImage (not Subcompose) because IntrinsicSize.Min
                    // queries crash subcompose-based layouts at measure time.
                    Box(
                        modifier = Modifier
                            .width(92.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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