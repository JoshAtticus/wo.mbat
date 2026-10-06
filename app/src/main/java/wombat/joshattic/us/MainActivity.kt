package wombat.joshattic.us

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wearable.Wearable
import wombat.joshattic.us.data.network.RetrofitClient
import wombat.joshattic.us.data.repository.WombatRepository
import wombat.joshattic.us.data.storage.AuthPreferences
import wombat.joshattic.us.data.storage.BlockedUsersDatabase
import wombat.joshattic.us.data.storage.SettingsPreferences
import wombat.joshattic.us.ui.screens.HomeScreen
import wombat.joshattic.us.ui.screens.SplashOverlay
import wombat.joshattic.us.ui.theme.WombatTheme
import wombat.joshattic.us.ui.viewmodel.HomeViewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import wombat.joshattic.us.wear.WearSyncService

class MainActivity : ComponentActivity() {
    private var pendingDeepLink: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            pendingDeepLink = intent?.extractDeepLink()
        }
        val repository = WombatRepository(
            apiService = RetrofitClient.apiService,
            authPreferences = AuthPreferences(applicationContext),
            blockedUsersDatabase = BlockedUsersDatabase(applicationContext),
            settingsPreferences = SettingsPreferences(applicationContext)
        )
        enableEdgeToEdge()
        setContent {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository, applicationContext))
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

            val session = uiState.session
            val wearAccount = uiState.wearAccount
            val savedAccounts = uiState.savedAccounts
            LaunchedEffect(session, wearAccount, savedAccounts) {
                val targetSession = if (wearAccount == "Last used on phone") {
                    session as? wombat.joshattic.us.data.model.AuthSession
                } else {
                    savedAccounts.find { it.username.equals(wearAccount, ignoreCase = true) }
                }
                WearSyncService.pushSession(
                    dataClient = Wearable.getDataClient(this@MainActivity),
                    token = targetSession?.token,
                    username = targetSession?.username
                )
            }

            val wearShowImages = uiState.wearShowImages
            val wearShowProfilePictures = uiState.wearShowProfilePictures
            val wearFeedType = uiState.wearFeedType
            LaunchedEffect(wearShowImages, wearShowProfilePictures, wearFeedType) {
                WearSyncService.pushSettings(
                    dataClient = Wearable.getDataClient(this@MainActivity),
                    showImages = wearShowImages,
                    showPfp = wearShowProfilePictures,
                    feedType = wearFeedType
                )
            }

            val deepLink = pendingDeepLink
            pendingDeepLink = null
            if (deepLink != null) {
                homeViewModel.handleDeepLink(deepLink)
            }

            val useCustomTheme = uiState.themeSource == "custom"
            val useDynamic = useCustomTheme && uiState.customThemeDynamic &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S_V2
            val themeUserColor = if (useCustomTheme && !useDynamic) uiState.customThemeColor else uiState.accountProfile?.color

            WombatTheme(userColor = themeUserColor, dynamicColor = useDynamic) {
                androidx.compose.runtime.CompositionLocalProvider(
                    wombat.joshattic.us.ui.screens.LocalOnViewReposts provides { post ->
                        homeViewModel.openReposts(post)
                    },
                    wombat.joshattic.us.ui.screens.LocalShowBlockedRevealButton provides uiState.showBlockedRevealButton
                ) {
                    SplashOverlay {
                        HomeScreen(viewModel = homeViewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.extractDeepLink()?.let { deepLink ->
            androidx.lifecycle.ViewModelProvider(this)[HomeViewModel::class.java]
                .handleDeepLink(deepLink)
        }
    }

    private fun Intent.extractDeepLink(): Uri? {
        if (action != Intent.ACTION_VIEW) return null
        val uri = data ?: return null
        return if (uri.host == "wasteof.money" &&
            (uri.path?.startsWith("/posts/") == true || uri.path?.startsWith("/users/") == true)
        ) uri else null
    }
}
