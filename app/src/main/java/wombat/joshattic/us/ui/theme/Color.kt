package wombat.joshattic.us.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val LightAppBackground = Color(0xFFF1F5F9)
val LightCardBackground = Color(0xFFFFFFFF)
val LightCardVariant = Color(0xFFF8FAFC)

val SurfaceWhite = Color(0xFFFFFFFF)

data class WombatColorPalette(
    val brand: Color,
    val brandVariant: Color,
    val brandLight: Color,
    // Dark theme backgrounds — hue-tinted dark
    val darkBackground: Color,
    val darkSurface: Color,
    val darkSurfaceVariant: Color,
    val darkSurfaceContainerLow: Color,
    val darkSurfaceContainer: Color,
    val darkSurfaceContainerHigh: Color,
    val darkSurfaceContainerHighest: Color,
    // Light theme backgrounds — hue-tinted light
    val lightBackground: Color,
    val lightSurface: Color,
    val lightSurfaceVariant: Color,
    val lightSurfaceContainerLow: Color,
    val lightSurfaceContainer: Color,
    val lightSurfaceContainerHigh: Color,
    val lightSurfaceContainerHighest: Color,
    // Container colors
    val darkContainer: Color,
    val lightContainer: Color,
)

fun getUserColorSchemeColors(userColor: String?): Triple<Color, Color, Color> {
    val palette = getWombatColorPalette(userColor)
    return Triple(palette.brand, palette.brandVariant, palette.brandLight)
}

fun getWombatColorPalette(userColor: String?): WombatColorPalette {
    val baseColor = when (userColor?.lowercase()) {
        "red"     -> Color(0xFFDC2626)
        "orange"  -> Color(0xFFEA580C)
        "yellow"  -> Color(0xFFCA8A04)
        "green"   -> Color(0xFF16A34A)
        "teal"    -> Color(0xFF0D9488)
        "cyan"    -> Color(0xFF0891B2)
        "blue"    -> Color(0xFF2563EB)
        "indigo"  -> Color(0xFF6366F1)
        "violet"  -> Color(0xFF7C3AED)
        "purple"  -> Color(0xFF9333EA)
        "fuchsia" -> Color(0xFFC026FF)
        "pink"    -> Color(0xFFDB2777)
        "gray"    -> Color(0xFF6B7280)
        else      -> Color(0xFF6366F1)
    }

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
    val hue = hsl[0]
    val sat = hsl[1]

    // brand: base as-is
    val brand = baseColor

    // brandVariant: darker (-15% lightness)
    val variantHsl = hsl.copyOf()
    variantHsl[2] = (hsl[2] - 0.15f).coerceIn(0f, 1f)
    val brandVariant = Color(ColorUtils.HSLToColor(variantHsl))

    // brandLight: lighter (+35% lightness, half saturation)
    val lightHsl = hsl.copyOf()
    lightHsl[1] = (sat * 0.5f).coerceIn(0f, 1f)
    lightHsl[2] = (hsl[2] + 0.35f).coerceIn(0f, 1f)
    val brandLight = Color(ColorUtils.HSLToColor(lightHsl))

    // Dark backgrounds — hue-tinted dark
    val darkBg = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.25f).coerceIn(0f, 1f), 0.07f)))
    val darkSurface = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.22f).coerceIn(0f, 1f), 0.11f)))
    val darkSurfaceVariant = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.18f).coerceIn(0f, 1f), 0.17f)))
    val darkSurfaceContainerLow = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.22f).coerceIn(0f, 1f), 0.09f)))
    val darkSurfaceContainer = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.20f).coerceIn(0f, 1f), 0.12f)))
    val darkSurfaceContainerHigh = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.18f).coerceIn(0f, 1f), 0.15f)))
    val darkSurfaceContainerHighest = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.16f).coerceIn(0f, 1f), 0.19f)))

    // Light backgrounds — slate light background with pure white card containers for high contrast
    val lightBg = Color(0xFFF1F5F9)
    val lightSurface = Color(0xFFFFFFFF)
    val lightSurfaceVariant = Color(0xFFE2E8F0)
    val lightSurfaceContainerLow = Color(0xFFFFFFFF)
    val lightSurfaceContainer = Color(0xFFF8FAFC)
    val lightSurfaceContainerHigh = Color(0xFFE2E8F0)
    val lightSurfaceContainerHighest = Color(0xFFCBD5E1)

    // Container colors
    val darkContainer = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.6f).coerceIn(0f, 1f), 0.22f)))
    val lightContainer = Color(ColorUtils.HSLToColor(floatArrayOf(hue, (sat * 0.5f).coerceIn(0f, 1f), 0.88f)))

    return WombatColorPalette(
        brand = brand,
        brandVariant = brandVariant,
        brandLight = brandLight,
        darkBackground = darkBg,
        darkSurface = darkSurface,
        darkSurfaceVariant = darkSurfaceVariant,
        darkSurfaceContainerLow = darkSurfaceContainerLow,
        darkSurfaceContainer = darkSurfaceContainer,
        darkSurfaceContainerHigh = darkSurfaceContainerHigh,
        darkSurfaceContainerHighest = darkSurfaceContainerHighest,
        lightBackground = lightBg,
        lightSurface = lightSurface,
        lightSurfaceVariant = lightSurfaceVariant,
        lightSurfaceContainerLow = lightSurfaceContainerLow,
        lightSurfaceContainer = lightSurfaceContainer,
        lightSurfaceContainerHigh = lightSurfaceContainerHigh,
        lightSurfaceContainerHighest = lightSurfaceContainerHighest,
        darkContainer = darkContainer,
        lightContainer = lightContainer
    )
}