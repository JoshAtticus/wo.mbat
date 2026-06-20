package wombat.joshattic.us

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
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
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

            WombatTheme(userColor = uiState.accountProfile?.color) {
                SplashOverlay {
                    HomeScreen(viewModel = homeViewModel)
                }
            }
        }
    }
}
