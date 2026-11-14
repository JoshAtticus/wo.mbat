package wombat.joshattic.us

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wombat.joshattic.us.platform.PlatformConfig
import wombat.joshattic.us.ui.screens.HomeScreen
import wombat.joshattic.us.ui.screens.LocalOnViewReposts
import wombat.joshattic.us.ui.screens.LocalShowBlockedCommentRevealButton
import wombat.joshattic.us.ui.screens.LocalShowBlockedQuoteRevealButton
import wombat.joshattic.us.ui.screens.SplashOverlay
import wombat.joshattic.us.ui.theme.WombatTheme
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

@Composable
fun WombatApp(homeViewModel: HomeViewModel) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val useCustomTheme = uiState.themeSource == "custom"
    val useDynamic = useCustomTheme && uiState.customThemeDynamic && PlatformConfig.supportsDynamicColor
    val themeUserColor = if (useCustomTheme && !useDynamic) uiState.customThemeColor else uiState.accountProfile?.color

    WombatTheme(userColor = themeUserColor, dynamicColor = useDynamic) {
        CompositionLocalProvider(
            LocalOnViewReposts provides { post ->
                homeViewModel.openReposts(post)
            },
            LocalShowBlockedQuoteRevealButton provides uiState.showBlockedQuoteRevealButton,
            LocalShowBlockedCommentRevealButton provides uiState.showBlockedCommentRevealButton
        ) {
            SplashOverlay {
                HomeScreen(viewModel = homeViewModel)
            }
        }
    }
}
