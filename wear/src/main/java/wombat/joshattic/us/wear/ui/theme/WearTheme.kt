package wombat.joshattic.us.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

val WasteofColors = Colors(
    primary = Color(0xFF8B5CF6),
    primaryVariant = Color(0xFF7C3AED),
    secondary = Color(0xFF10B981),
    secondaryVariant = Color(0xFF059669),
    error = Color(0xFFEF4444),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onError = Color.White
)

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WasteofColors,
        content = content
    )
}
