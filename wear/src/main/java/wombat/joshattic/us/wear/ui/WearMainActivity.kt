package wombat.joshattic.us.wear.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import wombat.joshattic.us.wear.WearViewModel
import wombat.joshattic.us.wear.ui.theme.WearTheme

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: WearViewModel = viewModel(factory = WearViewModel.factory(applicationContext))
            WearTheme {
                WearApp(viewModel = vm)
            }
        }
    }
}
