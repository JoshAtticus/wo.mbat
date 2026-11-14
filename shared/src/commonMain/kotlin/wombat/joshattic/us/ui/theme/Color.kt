package wombat.joshattic.us.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

val LightAppBackground = Color(0xFFF1F5F9)
val LightCardBackground = Color(0xFFFFFFFF)
val LightCardVariant = Color(0xFFF8FAFC)

val SurfaceWhite = Color(0xFFFFFFFF)

val WombatThemeColorNames = listOf(
    "red", "orange", "yellow", "green", "teal", "cyan",
    "blue", "indigo", "violet", "purple", "fuchsia", "pink", "gray"
)

data class WombatColorPalette(
    val brand: Color,
    val brandVariant: Color,
    val brandLight: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkSurfaceVariant: Color,
    val darkSurfaceContainerLow: Color,
    val darkSurfaceContainer: Color,
    val darkSurfaceContainerHigh: Color,
    val darkSurfaceContainerHighest: Color,
    val lightBackground: Color,
    val lightSurface: Color,
    val lightSurfaceVariant: Color,
    val lightSurfaceContainerLow: Color,
    val lightSurfaceContainer: Color,
    val lightSurfaceContainerHigh: Color,
    val lightSurfaceContainerHighest: Color,
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
    colorToHsl(baseColor, hsl)
    val hue = hsl[0]
    val sat = hsl[1]

    val brand = baseColor

    val variantHsl = hsl.copyOf()
    variantHsl[2] = (hsl[2] - 0.15f).coerceIn(0f, 1f)
    val brandVariant = hslToColor(variantHsl)

    val lightHsl = hsl.copyOf()
    lightHsl[1] = (sat * 0.5f).coerceIn(0f, 1f)
    lightHsl[2] = (hsl[2] + 0.35f).coerceIn(0f, 1f)
    val brandLight = hslToColor(lightHsl)

    val darkBg = hslToColor(floatArrayOf(hue, (sat * 0.25f).coerceIn(0f, 1f), 0.07f))
    val darkSurface = hslToColor(floatArrayOf(hue, (sat * 0.22f).coerceIn(0f, 1f), 0.11f))
    val darkSurfaceVariant = hslToColor(floatArrayOf(hue, (sat * 0.18f).coerceIn(0f, 1f), 0.17f))
    val darkSurfaceContainerLow = hslToColor(floatArrayOf(hue, (sat * 0.22f).coerceIn(0f, 1f), 0.09f))
    val darkSurfaceContainer = hslToColor(floatArrayOf(hue, (sat * 0.20f).coerceIn(0f, 1f), 0.12f))
    val darkSurfaceContainerHigh = hslToColor(floatArrayOf(hue, (sat * 0.18f).coerceIn(0f, 1f), 0.15f))
    val darkSurfaceContainerHighest = hslToColor(floatArrayOf(hue, (sat * 0.16f).coerceIn(0f, 1f), 0.19f))

    val lightBg = Color(0xFFF1F5F9)
    val lightSurface = Color(0xFFFFFFFF)
    val lightSurfaceVariant = Color(0xFFE2E8F0)
    val lightSurfaceContainerLow = Color(0xFFFFFFFF)
    val lightSurfaceContainer = Color(0xFFF8FAFC)
    val lightSurfaceContainerHigh = Color(0xFFE2E8F0)
    val lightSurfaceContainerHighest = Color(0xFFCBD5E1)

    val darkContainer = hslToColor(floatArrayOf(hue, (sat * 0.6f).coerceIn(0f, 1f), 0.22f))
    val lightContainer = hslToColor(floatArrayOf(hue, (sat * 0.5f).coerceIn(0f, 1f), 0.88f))

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

private fun colorToHsl(color: Color, outHsl: FloatArray) {
    val r = color.red
    val g = color.green
    val b = color.blue
    val maxVal = max(r, max(g, b))
    val minVal = min(r, min(g, b))
    val delta = maxVal - minVal
    val l = (maxVal + minVal) / 2f
    val s: Float
    var h: Float
    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = delta / (1f - abs(2f * l - 1f))
        h = when (maxVal) {
            r -> ((g - b) / delta) % 6f
            g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        }
        h *= 60f
        if (h < 0f) h += 360f
    }
    outHsl[0] = h.coerceIn(0f, 360f)
    outHsl[1] = s.coerceIn(0f, 1f)
    outHsl[2] = l.coerceIn(0f, 1f)
}

private fun hslToColor(hsl: FloatArray): Color {
    val h = hsl[0]
    val s = hsl[1].coerceIn(0f, 1f)
    val l = hsl[2].coerceIn(0f, 1f)
    val c = (1f - abs(2f * l - 1f)) * s
    val m = l - 0.5f * c
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f)
    )
}