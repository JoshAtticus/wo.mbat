package wombat.joshattic.us.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.preferredLanguages
import platform.Foundation.timeIntervalSince1970
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIPasteboard
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.system.exitProcess

actual object PlatformConfig {
    actual val supportsWearOs: Boolean = false
    actual val supportsDynamicColor: Boolean = false
    actual val supportsAppLinksSettings: Boolean = false

    actual val isTabletDevice: Boolean
        get() = UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad

    @OptIn(ExperimentalForeignApi::class)
    actual val screenResolution: String
        get() {
            val scale = UIScreen.mainScreen.scale
            return UIScreen.mainScreen.bounds.useContents {
                val w = (size.width * scale).toInt()
                val h = (size.height * scale).toInt()
                "${w}x${h}"
            }
        }

    actual val userAgent: String
        get() {
            val systemVersion = UIDevice.currentDevice.systemVersion.replace('.', '_')
            return "Mozilla/5.0 (iPhone; CPU iPhone OS $systemVersion like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148"
        }

    actual val systemLanguageTag: String
        get() = (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
}

actual object PlatformActions {
    private var toastHandler: ((String) -> Unit)? = null

    fun registerToastHandler(handler: (String) -> Unit) {
        toastHandler = handler
    }

    actual fun showToast(message: String) {
        toastHandler?.invoke(message)
    }

    actual fun copyToClipboard(label: String, text: String) {
        if (text.isBlank()) return
        UIPasteboard.generalPasteboard.string = text
        showToast("$label copied")
    }

    actual fun downloadImage(url: String) {
        openUrl(url, inApp = false)
    }

    actual fun shareText(text: String, title: String?) {
        val rootVc = topViewController() ?: return
        val vc = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        rootVc.presentViewController(vc, animated = true, completion = null)
    }

    actual fun openUrl(url: String, inApp: Boolean) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    actual fun openAppLinksSettings() {}

    actual fun isAppLinksApproved(): Boolean? = null

    actual fun syncWearAuth(token: String?, username: String?) {}

    @OptIn(ExperimentalForeignApi::class)
    actual fun dataStorePath(name: String): Path {
        val dir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        val basePath = requireNotNull(dir?.path) { "Could not resolve iOS Documents directory" }
        return "$basePath/$name.preferences_pb".toPath()
    }

    actual fun randomUuid(): String = NSUUID().UUIDString()

    actual fun exitApp() {
        exitProcess(0)
    }
}

private fun topViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? platform.UIKit.UIWindow
    var vc = keyWindow?.rootViewController
    while (vc?.presentedViewController != null) {
        vc = vc.presentedViewController
    }
    return vc
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val len = length.toInt()
    if (len == 0) return ByteArray(0)
    val result = ByteArray(len)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return result
}

private class MultiPickerDelegate(
    private val onResult: (List<PickedImage>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) return

        val picked = mutableListOf<PickedImage>()
        var remaining = results.size
        for ((index, result) in results.withIndex()) {
            val provider = result.itemProvider
            if (provider.canLoadObjectOfClass(UIImage.`class`()!!)) {
                provider.loadObjectOfClass(UIImage.`class`()!!) { obj, _ ->
                    val image = obj as? UIImage
                    val data = image?.let { UIImageJPEGRepresentation(it, 0.9) }
                    if (data != null) {
                        picked.add(
                            PickedImage(
                                bytes = data.toByteArray(),
                                filename = "image_${index + 1}.jpg",
                                mimeType = "image/jpeg"
                            )
                        )
                    }
                    remaining--
                    if (remaining == 0 && picked.isNotEmpty()) {
                        platform.darwin.dispatch_async(dispatch_get_main_queue()) {
                            onResult(picked.toList())
                        }
                    }
                }
            } else {
                remaining--
                if (remaining == 0 && picked.isNotEmpty()) {
                    platform.darwin.dispatch_async(dispatch_get_main_queue()) {
                        onResult(picked.toList())
                    }
                }
            }
        }
    }
}

private var activePickerDelegate: MultiPickerDelegate? = null

@Composable
actual fun rememberMultiImagePicker(
    maxItems: Int,
    onPicked: (List<PickedImage>) -> Unit
): () -> Unit {
    return remember(maxItems, onPicked) {
        {
            val rootVc = topViewController()
            if (rootVc != null) {
                val config = PHPickerConfiguration().apply {
                    selectionLimit = maxItems.toLong()
                    filter = PHPickerFilter.imagesFilter
                }
                val picker = PHPickerViewController(configuration = config)
                val delegate = MultiPickerDelegate { images ->
                    activePickerDelegate = null
                    onPicked(images)
                }
                activePickerDelegate = delegate
                picker.delegate = delegate
                rootVc.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@Composable
actual fun rememberSingleImagePicker(
    onPicked: (PickedImage?) -> Unit
): () -> Unit {
    return rememberMultiImagePicker(maxItems = 1) { list ->
        onPicked(list.firstOrNull())
    }
}

@Composable
actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

actual class NetworkConnectivityObserver {
    actual val isConnected: Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(monitor) { path ->
            val satisfied = nw_path_get_status(path) == nw_path_status_satisfied
            trySend(satisfied)
        }
        nw_path_monitor_start(monitor)
        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged()
}

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
