package wombat.joshattic.us.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SurfaceWhite,
    secondary = SurfaceTint,
    tertiary = Neutral90,
    background = Neutral10,
    surface = Neutral20,
    onPrimary = Neutral10,
    onSecondary = Neutral10,
    onTertiary = Neutral10,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Neutral10,
    secondary = Neutral20,
    tertiary = SurfaceTint,
    background = Neutral95,
    surface = SurfaceWhite,
    surfaceVariant = Neutral90,
    outline = SurfaceTint,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceWhite,
    onTertiary = Neutral10,
    onBackground = Neutral10,
    onSurface = Neutral10,
    onSurfaceVariant = Neutral20
)

@Composable
fun WombatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}