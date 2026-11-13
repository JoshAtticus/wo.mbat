package wombat.joshattic.us

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIViewController
import wombat.joshattic.us.data.network.RetrofitClient
import wombat.joshattic.us.data.repository.WombatRepository
import wombat.joshattic.us.data.storage.AuthPreferences
import wombat.joshattic.us.data.storage.BlockedUsersDatabase
import wombat.joshattic.us.data.storage.SettingsPreferences
import wombat.joshattic.us.platform.PlatformActions
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

private var activeHomeViewModel: HomeViewModel? = null
private var exceptionHookInstalled = false

@OptIn(ExperimentalForeignApi::class)
private fun crashLogFilePath(): String? {
    val dir = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return dir?.path?.let { "$it/last_crash.txt" }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeCrashLog(report: String) {
    val path = crashLogFilePath() ?: return
    (NSString.create(string = report) as NSString).writeToFile(
        path = path,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun readCrashLog(): String? {
    val path = crashLogFilePath() ?: return null
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
    return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun clearCrashLog() {
    val path = crashLogFilePath() ?: return
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

@OptIn(ExperimentalNativeApi::class)
private fun installCrashHandler() {
    if (exceptionHookInstalled) return
    exceptionHookInstalled = true
    setUnhandledExceptionHook { throwable ->
        val report = buildString {
            appendLine("=== UNCAUGHT KOTLIN EXCEPTION ===")
            appendLine("Type: ${throwable::class.qualifiedName ?: throwable::class.simpleName}")
            appendLine("Message: ${throwable.message}")
            appendLine()
            appendLine("Stack trace:")
            appendLine(throwable.stackTraceToString())
            var cause = throwable.cause
            while (cause != null) {
                appendLine()
                appendLine("Caused by: ${cause::class.qualifiedName}: ${cause.message}")
                appendLine(cause.stackTraceToString())
                cause = cause.cause
            }
        }
        println(report)
        writeCrashLog(report)
        terminateWithUnhandledException(throwable)
    }
}

fun handleWombatDeepLink(url: String) {
    activeHomeViewModel?.handleDeepLink(url)
}

fun MainViewController(): UIViewController {
    installCrashHandler()
    return ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        }
    ) {
        var previousCrash by remember { mutableStateOf(readCrashLog()) }
        var startupError by remember { mutableStateOf<String?>(null) }

        val activeCrashReport = startupError ?: previousCrash
        if (activeCrashReport != null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF121212),
                contentColor = Color.White
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Crash Report",
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.material3.Button(
                            onClick = {
                                PlatformActions.shareText(activeCrashReport, "wo.mbat Crash Log")
                            }
                        ) {
                            Text("AirDrop / Share")
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                PlatformActions.copyToClipboard("Crash log", activeCrashReport)
                            }
                        ) {
                            Text("Copy")
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                clearCrashLog()
                                previousCrash = null
                                startupError = null
                            }
                        ) {
                            Text("Clear & Continue")
                        }
                    }
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = activeCrashReport,
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }
            return@ComposeUIViewController
        }

        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                    add(SvgDecoder.Factory())
                }
                .build()
        }

        val homeViewModel = remember {
            runCatching {
                val repository = WombatRepository(
                    apiService = RetrofitClient.apiService,
                    authPreferences = AuthPreferences(),
                    blockedUsersDatabase = BlockedUsersDatabase(),
                    settingsPreferences = SettingsPreferences()
                )
                HomeViewModel.create(repository).also { activeHomeViewModel = it }
            }.getOrElse { throwable ->
                val report = "=== STARTUP EXCEPTION ===\n${throwable.stackTraceToString()}"
                writeCrashLog(report)
                startupError = report
                null
            }
        } ?: return@ComposeUIViewController

    var toastMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        PlatformActions.registerToastHandler { message ->
            toastMessage = message
        }
        onDispose { }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WombatApp(homeViewModel = homeViewModel)

        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 84.dp, start = 24.dp, end = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.85f),
                contentColor = Color.White,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = toastMessage ?: "",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
    }
}
