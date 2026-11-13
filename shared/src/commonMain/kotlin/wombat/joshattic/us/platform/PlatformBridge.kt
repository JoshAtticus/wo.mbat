package wombat.joshattic.us.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import okio.Path

data class PickedImage(
    val bytes: ByteArray,
    val filename: String,
    val mimeType: String
)

expect object PlatformConfig {
    val supportsWearOs: Boolean
    val supportsDynamicColor: Boolean
    val supportsAppLinksSettings: Boolean
    val isTabletDevice: Boolean
    val screenResolution: String
    val userAgent: String
    val systemLanguageTag: String
}

expect object PlatformActions {
    fun showToast(message: String)
    fun copyToClipboard(label: String, text: String)
    fun downloadImage(url: String)
    fun shareText(text: String, title: String? = null)
    fun openUrl(url: String, inApp: Boolean = false)
    fun openAppLinksSettings()
    fun isAppLinksApproved(): Boolean?
    fun syncWearAuth(token: String?, username: String?)
    fun dataStorePath(name: String): Path
    fun randomUuid(): String
    fun exitApp()
}

@Composable
expect fun rememberMultiImagePicker(
    maxItems: Int = 8,
    onPicked: (List<PickedImage>) -> Unit
): () -> Unit

@Composable
expect fun rememberSingleImagePicker(
    onPicked: (PickedImage?) -> Unit
): () -> Unit

@Composable
expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

expect class NetworkConnectivityObserver() {
    val isConnected: Flow<Boolean>
}

expect class PlatformLock() {
    fun lock()
    fun unlock()
}

inline fun <T> PlatformLock.withLock(block: () -> T): T {
    lock()
    return try {
        block()
    } finally {
        unlock()
    }
}

expect fun currentTimeMillis(): Long
