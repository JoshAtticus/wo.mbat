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
    val p = getWombatColorPalette(userColor)

    val DarkColorScheme = darkColorScheme(
        primary = p.brand,
        onPrimary = Color.White,
        primaryContainer = p.darkContainer,
        onPrimaryContainer = p.brandLight,
        secondary = p.brandLight,
        onSecondary = Color.White,
        secondaryContainer = p.darkContainer,
        onSecondaryContainer = p.brandLight,
        tertiary = p.brandVariant,
        onTertiary = Color.White,
        tertiaryContainer = p.darkContainer,
        onTertiaryContainer = p.brandLight,
        background = p.darkBackground,
        onBackground = SurfaceWhite,
        surface = p.darkSurface,
        onSurface = SurfaceWhite,
        surfaceVariant = p.darkSurfaceVariant,
        onSurfaceVariant = Color(0xFF9CA3AF),
        surfaceContainerLow = p.darkSurfaceContainerLow,
        surfaceContainer = p.darkSurfaceContainer,
        surfaceContainerHigh = p.darkSurfaceContainerHigh,
        surfaceContainerHighest = p.darkSurfaceContainerHighest,
        outline = p.brand.copy(alpha = 0.5f),
        outlineVariant = p.darkSurfaceVariant,
        inversePrimary = p.brand,
        inverseSurface = Color(0xFFE5E7EB),
        inverseOnSurface = Color(0xFF111827),
        error = Color(0xFFEF4444),
        onError = Color.White,
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFCA5A5),
        scrim = Color.Black
    )

    val LightColorScheme = lightColorScheme(
        primary = p.brand,
        onPrimary = Color.White,
        primaryContainer = p.lightContainer,
        onPrimaryContainer = p.brandVariant,
        secondary = p.brand,
        onSecondary = Color.White,
        secondaryContainer = p.lightContainer,
        onSecondaryContainer = p.brandVariant,
        tertiary = p.brandVariant,
        onTertiary = Color.White,
        tertiaryContainer = p.lightContainer,
        onTertiaryContainer = p.brandVariant,
        background = p.lightBackground,
        onBackground = Color(0xFF111827),
        surface = p.lightSurface,
        onSurface = Color(0xFF111827),
        surfaceVariant = p.lightSurfaceVariant,
        onSurfaceVariant = Color(0xFF4B5563),
        surfaceContainerLow = p.lightSurfaceContainerLow,
        surfaceContainer = p.lightSurfaceContainer,
        surfaceContainerHigh = p.lightSurfaceContainerHigh,
        surfaceContainerHighest = p.lightSurfaceContainerHighest,
        outline = p.brand.copy(alpha = 0.4f),
        outlineVariant = Color(0xFFE5E7EB),
        inversePrimary = p.brandLight,
        inverseSurface = Color(0xFF1F2937),
        inverseOnSurface = Color(0xFFF9FAFB),
        error = Color(0xFFDC2626),
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF7F1D1D),
        scrim = Color.Black
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