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
    // Kept between onCreate and setContent so the deep link can be handed to the ViewModel
    // once it exists; cleared once consumed so it is only handled a single time.
    private var pendingDeepLink: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Only pick up the link on a fresh launch, not on recreation after rotation,
        // otherwise the same URL would re-open its view every configuration change
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

            // Push auth session to the watch based on settings whenever it changes
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

            // Push watch settings whenever they change
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

            // Handle a wasteof.money link opened from another app once the ViewModel is ready.
            // Auth may still be loading here, but every routed action fetches its data itself,
            // so this is safe to fire immediately.
            val deepLink = pendingDeepLink
            pendingDeepLink = null
            if (deepLink != null) {
                homeViewModel.handleDeepLink(deepLink)
            }

            // Theme: custom theme overrides the account colour; optionally use system Material You (12L+)
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
        // singleTop: the activity already exists, so the ViewModel is live and can
        // handle the link immediately
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
