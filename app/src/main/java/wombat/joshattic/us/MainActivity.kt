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
            blockedUsersDatabase = BlockedUsersDatabase(applicationContext)
        )
        enableEdgeToEdge()
        setContent {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

            // Push auth session to the watch whenever it changes (login/logout/switch)
            val session = uiState.session
            LaunchedEffect(session) {
                WearSyncService.pushSession(
                    dataClient = Wearable.getDataClient(this@MainActivity),
                    token = (session as? wombat.joshattic.us.data.model.AuthSession)?.token,
                    username = (session as? wombat.joshattic.us.data.model.AuthSession)?.username
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
