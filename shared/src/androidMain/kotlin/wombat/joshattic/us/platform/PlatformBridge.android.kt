package wombat.joshattic.us.platform

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import okio.Path
import okio.Path.Companion.toPath
import java.util.Locale
import java.util.UUID

object AndroidPlatformContext {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

actual object PlatformConfig {
    actual val supportsWearOs: Boolean = true
    actual val supportsDynamicColor: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    actual val supportsAppLinksSettings: Boolean = true
    actual val isTabletDevice: Boolean
        get() = AndroidPlatformContext.appContext.resources.configuration.smallestScreenWidthDp >= 600
    actual val screenResolution: String
        get() {
            val metrics = AndroidPlatformContext.appContext.resources.displayMetrics
            return "${metrics.widthPixels}x${metrics.heightPixels}"
        }
    actual val userAgent: String
        get() {
            val mobileToken = if (isTabletDevice) "" else "Mobile "
            return "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 ${mobileToken}Safari/537.36"
        }
    actual val systemLanguageTag: String
        get() = Locale.getDefault().toLanguageTag()
}

actual object PlatformActions {
    private val ctx: Context
        get() = AndroidPlatformContext.appContext

    actual fun showToast(message: String) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
    }

    actual fun copyToClipboard(label: String, text: String) {
        if (text.isBlank()) return
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        showToast("$label copied")
    }

    actual fun downloadImage(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading image")
                .setDescription(url.substringAfterLast('/').ifBlank { "image.jpg" })
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    "wombat_${System.currentTimeMillis()}.jpg"
                )
            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            showToast("Downloading image…")
        } catch (e: Exception) {
            showToast("Download failed: ${e.message}")
        }
    }

    actual fun shareText(text: String, title: String?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(chooser)
    }

    actual fun openUrl(url: String, inApp: Boolean) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun openAppLinksSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${ctx.packageName}"))
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }

    actual fun isAppLinksApproved(): Boolean? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return try {
            val manager = ctx.getSystemService(android.content.pm.verify.domain.DomainVerificationManager::class.java)
            val state = manager?.getDomainVerificationUserState(ctx.packageName)
            state?.hostToStateMap?.get("wasteof.money") ==
                android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_VERIFIED ||
                state?.hostToStateMap?.get("wasteof.money") ==
                android.content.pm.verify.domain.DomainVerificationUserState.DOMAIN_STATE_SELECTED
        } catch (_: Exception) {
            null
        }
    }

    actual fun syncWearAuth(token: String?, username: String?) {
        try {
            val dataClient = Wearable.getDataClient(ctx)
            val putDataReq = PutDataMapRequest.create("/auth").apply {
                dataMap.putString("token", token ?: "")
                dataMap.putString("username", username ?: "")
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataReq)
        } catch (_: Exception) {
        }
    }

    actual fun dataStorePath(name: String): Path {
        return ctx.filesDir.resolve("datastore/$name.preferences_pb").absolutePath.toPath()
    }

    actual fun randomUuid(): String = UUID.randomUUID().toString()

    actual fun exitApp() {
        Process.killProcess(Process.myPid())
    }
}

private fun readPickedImage(context: Context, uri: Uri): PickedImage? {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
    val filename = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "image.jpg" } ?: "image.jpg"
    return PickedImage(bytes = bytes, filename = filename, mimeType = mimeType)
}

@Composable
actual fun rememberMultiImagePicker(
    maxItems: Int,
    onPicked: (List<PickedImage>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxItems)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val images = uris.mapNotNull { readPickedImage(context, it) }
            onPicked(images)
        }
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

@Composable
actual fun rememberSingleImagePicker(
    onPicked: (PickedImage?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onPicked(uri?.let { readPickedImage(context, it) })
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

@Composable
actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

actual class NetworkConnectivityObserver {
    private val connectivityManager =
        AndroidPlatformContext.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    actual val isConnected: Flow<Boolean> = callbackFlow {
        val initial = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
        trySend(initial)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                val active = connectivityManager.activeNetwork
                val hasInternet = active?.let {
                    connectivityManager.getNetworkCapabilities(it)
                        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                } ?: false
                trySend(hasInternet)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
