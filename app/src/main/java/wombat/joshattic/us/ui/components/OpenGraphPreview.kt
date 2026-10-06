package wombat.joshattic.us.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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

object OpenGraphPreviewCache {
    private const val MAX_ENTRIES = 128

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

    fun isUsable(fetched: OpenGraphResponse): Boolean {
        val metadata = fetched.metadata ?: return false
        val statusOk = fetched.statusCode == null || fetched.statusCode in 200..299
        return statusOk &&
            (!metadata.title.isNullOrBlank() ||
                !metadata.description.isNullOrBlank() ||
                !metadata.image.isNullOrBlank())
    }
}

fun shouldUseLargePreview(hasPreviewImage: Boolean, hasPostImages: Boolean, openGraphFirst: Boolean): Boolean =
    hasPreviewImage && (!hasPostImages || openGraphFirst)

fun isOpenGraphPriority(linkPreviewPriority: String?): Boolean =
    linkPreviewPriority.equals("opengraph", ignoreCase = true)


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
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
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
                        .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
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