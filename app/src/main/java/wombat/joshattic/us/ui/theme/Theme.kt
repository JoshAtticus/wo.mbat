package wombat.joshattic.us.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun WombatTheme(
    userColor: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val (brand, brandVariant, brandLight) = getUserColorSchemeColors(userColor)

    val DarkColorScheme = darkColorScheme(
        primary = brand,
        onPrimary = Color.White,
        secondary = brandLight,
        tertiary = brandVariant,
        background = AppBackground,
        surface = CardBackground,
        surfaceVariant = Color(0xFF374151),
        onBackground = SurfaceWhite,
        onSurface = SurfaceWhite,
        onSurfaceVariant = Color(0xFF9CA3AF)
    )

    val LightColorScheme = lightColorScheme(
        primary = brand,
        onPrimary = Color.White,
        secondary = brandLight,
        tertiary = brandVariant,
        background = LightAppBackground,
        surface = LightCardBackground,
        surfaceVariant = LightCardVariant,
        onBackground = Color(0xFF111827),
        onSurface = Color(0xFF111827),
        onSurfaceVariant = Color(0xFF4B5563)
    )

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