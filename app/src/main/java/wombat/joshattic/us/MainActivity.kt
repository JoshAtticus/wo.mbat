package wombat.joshattic.us

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import wombat.joshattic.us.data.network.RetrofitClient
import wombat.joshattic.us.data.repository.WombatRepository
import wombat.joshattic.us.data.storage.AuthPreferences
import wombat.joshattic.us.ui.screens.HomeScreen
import wombat.joshattic.us.ui.theme.WombatTheme
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = WombatRepository(
            apiService = RetrofitClient.apiService,
            authPreferences = AuthPreferences(applicationContext)
        )
        enableEdgeToEdge()
        setContent {
            WombatTheme {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
                HomeScreen(viewModel = homeViewModel)
            }
        }
    }
}