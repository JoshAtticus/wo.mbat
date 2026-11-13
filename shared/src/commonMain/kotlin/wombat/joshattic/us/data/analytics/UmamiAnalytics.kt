package wombat.joshattic.us.data.analytics

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.concurrent.Volatile
import wombat.joshattic.us.AppConfig
import wombat.joshattic.us.data.storage.SettingsPreferences
import wombat.joshattic.us.platform.PlatformConfig

class UmamiAnalytics(
    private val settingsPreferences: SettingsPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sendMutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    private val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 5_000
            requestTimeoutMillis = 8_000
            socketTimeoutMillis = 5_000
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    val isTablet: Boolean
        get() = PlatformConfig.isTabletDevice

    private val screenResolution: String
        get() = PlatformConfig.screenResolution

    private val userAgent: String
        get() = PlatformConfig.userAgent

    private val appVersionTag: String = "v${AppConfig.VERSION_NAME}"

    @Volatile
    private var cachedDeviceId: String? = null

    @Volatile
    private var cacheToken: String? = null

    @Volatile
    private var disabled: Boolean = false

    @Volatile
    private var currentUrl: String = "/home"

    @Volatile
    private var currentTitle: String = "Home Feed"

    @Volatile
    private var previousUrl: String = ""

    @Volatile
    private var pendingExternalReferrer: String? = null

    fun setExternalReferrer(referrer: String?) {
        pendingExternalReferrer = referrer
    }

    fun startNewVisit(externalReferrer: String? = null) {
        cacheToken = null
        previousUrl = ""
        pendingExternalReferrer = externalReferrer
    }

    fun trackPageView(path: String, title: String) {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        val referrer = pendingExternalReferrer?.also { pendingExternalReferrer = null }
            ?: if (previousUrl.isNotEmpty() && previousUrl != normalizedPath) {
                "https://$HOSTNAME$previousUrl"
            } else {
                ""
            }
        previousUrl = currentUrl
        currentUrl = normalizedPath
        currentTitle = title

        enqueueSend(
            url = normalizedPath,
            title = title,
            referrer = referrer,
            name = null,
            data = null
        )
    }

    fun trackCurrentPageView() {
        trackPageView(currentUrl, currentTitle)
    }

    fun trackEvent(
        name: String,
        data: Map<String, Any?>? = null,
        url: String? = null,
        title: String? = null
    ) {
        val targetUrl = url ?: currentUrl
        val targetTitle = title ?: currentTitle
        val referrer = pendingExternalReferrer?.also { pendingExternalReferrer = null }
            ?: if (previousUrl.isNotEmpty()) "https://$HOSTNAME$previousUrl" else ""

        enqueueSend(
            url = targetUrl,
            title = targetTitle,
            referrer = referrer,
            name = name,
            data = data?.takeIf { it.isNotEmpty() }
        )
    }

    private fun enqueueSend(
        url: String,
        title: String,
        referrer: String,
        name: String?,
        data: Map<String, Any?>?
    ) {
        if (disabled) return
        scope.launch {
            sendMutex.withLock {
                if (disabled) return@withLock
                runCatching {
                    val deviceId = cachedDeviceId ?: settingsPreferences.getOrCreateAnalyticsDeviceId().also {
                        cachedDeviceId = it
                    }
                    val bodyObj = UmamiRequest(
                        type = "event",
                        payload = UmamiPayload(
                            website = WEBSITE_ID,
                            screen = screenResolution,
                            language = PlatformConfig.systemLanguageTag,
                            title = title,
                            hostname = HOSTNAME,
                            url = url,
                            referrer = referrer,
                            tag = appVersionTag,
                            id = deviceId,
                            name = name,
                            data = data?.toJsonObject()
                        )
                    )
                    val response = client.post(ENDPOINT) {
                        contentType(ContentType.Application.Json)
                        header("User-Agent", userAgent)
                        header("x-umami-website-id", WEBSITE_ID)
                        header("x-umami-hostname", HOSTNAME)
                        cacheToken?.let { token ->
                            header("x-umami-cache", token)
                        }
                        setBody(bodyObj)
                    }
                    if (response.status.isSuccess()) {
                        val responseBody = response.bodyAsText()
                        if (responseBody.isNotBlank()) {
                            val parsed = runCatching {
                                json.decodeFromString<UmamiResponse>(responseBody)
                            }.getOrNull()
                            if (parsed?.disabled == true) {
                                disabled = true
                            }
                            parsed?.cache?.takeIf { it.isNotBlank() }?.let { newToken ->
                                cacheToken = newToken
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Map<String, Any?>.toJsonObject(): JsonObject =
        JsonObject(mapValues { (_, v) -> v.toJsonElement() })

    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Map<*, *> -> JsonObject(
            entries.mapNotNull { (k, v) -> (k as? String)?.let { it to v.toJsonElement() } }.toMap()
        )
        else -> JsonPrimitive(toString())
    }

    @Serializable
    private data class UmamiRequest(
        val type: String,
        val payload: UmamiPayload
    )

    @Serializable
    private data class UmamiPayload(
        val website: String,
        val screen: String,
        val language: String,
        val title: String,
        val hostname: String,
        val url: String,
        val referrer: String,
        val tag: String,
        val id: String,
        val name: String? = null,
        val data: JsonObject? = null
    )

    @Serializable
    private data class UmamiResponse(
        val cache: String? = null,
        val disabled: Boolean? = null
    )

    companion object {
        private const val ENDPOINT = "https://wombat.joshattic.us/assets/api/send"
        private const val WEBSITE_ID = "fdd1e8af-06b6-4a35-bab4-be691346b11e"
        private const val HOSTNAME = "wombat.joshattic.us"
        const val VISIT_TIMEOUT_MS = 30 * 60 * 1000L
    }
}
