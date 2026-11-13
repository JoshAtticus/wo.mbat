package wombat.joshattic.us.data.analytics

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import wombat.joshattic.us.BuildConfig
import wombat.joshattic.us.data.storage.SettingsPreferences
import java.util.Locale
import java.util.concurrent.TimeUnit

class UmamiAnalytics(
    private val context: Context,
    private val settingsPreferences: SettingsPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sendMutex = Mutex()
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    val isTablet: Boolean =
        context.resources.configuration.smallestScreenWidthDp >= 600

    private val screenResolution: String by lazy {
        val metrics = context.resources.displayMetrics
        "${metrics.widthPixels}x${metrics.heightPixels}"
    }

    private val userAgent: String by lazy {
        val mobileToken = if (isTablet) "" else "Mobile "
        "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 ${mobileToken}Safari/537.36"
    }

    private val appVersionTag: String = "v${BuildConfig.VERSION_NAME}"

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
                            language = Locale.getDefault().toLanguageTag(),
                            title = title,
                            hostname = HOSTNAME,
                            url = url,
                            referrer = referrer,
                            tag = appVersionTag,
                            id = deviceId,
                            name = name,
                            data = data
                        )
                    )
                    val requestBuilder = Request.Builder()
                        .url(ENDPOINT)
                        .post(gson.toJson(bodyObj).toRequestBody(jsonMediaType))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", userAgent)
                        .header("x-umami-website-id", WEBSITE_ID)
                        .header("x-umami-hostname", HOSTNAME)

                    cacheToken?.let { token ->
                        requestBuilder.header("x-umami-cache", token)
                    }

                    client.newCall(requestBuilder.build()).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            if (!responseBody.isNullOrBlank()) {
                                val parsed = gson.fromJson(responseBody, UmamiResponse::class.java)
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
    }

    private data class UmamiRequest(
        val type: String,
        val payload: UmamiPayload
    )

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
        val data: Map<String, Any?>? = null
    )

    private data class UmamiResponse(
        @SerializedName("cache") val cache: String? = null,
        @SerializedName("disabled") val disabled: Boolean? = null
    )

    companion object {
        private const val ENDPOINT = "https://wombat.joshattic.us/assets/api/send"
        private const val WEBSITE_ID = "fdd1e8af-06b6-4a35-bab4-be691346b11e"
        private const val HOSTNAME = "wombat.joshattic.us"
        const val VISIT_TIMEOUT_MS = 30 * 60 * 1000L
    }
}
