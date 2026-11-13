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
import kotlinx.coroutines.delay
import platform.UIKit.UIViewController
import wombat.joshattic.us.data.network.RetrofitClient
import wombat.joshattic.us.data.repository.WombatRepository
import wombat.joshattic.us.data.storage.AuthPreferences
import wombat.joshattic.us.data.storage.BlockedUsersDatabase
import wombat.joshattic.us.data.storage.SettingsPreferences
import wombat.joshattic.us.platform.PlatformActions
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

private var activeHomeViewModel: HomeViewModel? = null

fun handleWombatDeepLink(url: String) {
    activeHomeViewModel?.handleDeepLink(url)
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .build()
    }

    val homeViewModel = remember {
        val repository = WombatRepository(
            apiService = RetrofitClient.apiService,
            authPreferences = AuthPreferences(),
            blockedUsersDatabase = BlockedUsersDatabase(),
            settingsPreferences = SettingsPreferences()
        )
        HomeViewModel.create(repository).also { activeHomeViewModel = it }
    }

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
