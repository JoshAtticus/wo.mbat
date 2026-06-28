package wombat.joshattic.us.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val AppBackground = Color(0xFF111827)
val CardBackground = Color(0xFF1F2937)
val LightAppBackground = Color(0xFFF1F5F9)
val LightCardBackground = Color(0xFFFFFFFF)
val LightCardVariant = Color(0xFFF8FAFC)
val Brand = Color(0xFF6366F1)
val BrandVariant = Color(0xFF4F46E5)
val BrandLight = Color(0xFF818CF8)

val Neutral10 = Color(0xFF111827)
val Neutral20 = Color(0xFF1F2937)
val Neutral90 = Color(0xFFF5F3EF)
val Neutral95 = Color(0xFFFAF9F7)

val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceTint = Color(0xFFE7E1D8)

fun getUserColorSchemeColors(userColor: String?): Triple<Color, Color, Color> {
    val baseColor = when (userColor?.lowercase()) {
        "red" -> Color(0xFFDC2626)
        "orange" -> Color(0xFFEA580C)
        "yellow" -> Color(0xFFCA8A04)
        "green" -> Color(0xFF16A34A)
        "teal" -> Color(0xFF0D9488)
        "cyan" -> Color(0xFF0891B2)
        "blue" -> Color(0xFF2563EB)
        "indigo" -> Color(0xFF6366F1)
        "violet" -> Color(0xFF7C3AED)
        "purple" -> Color(0xFF9333EA)
        "fuchsia" -> Color(0xFFC026FF)
        "pink" -> Color(0xFFDB2777)
        "gray" -> Color(0xFF6B7280)
        else -> Color(0xFF6366F1)
    }

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(baseColor.toArgb(), hsl)

    // Variant: Darker (lower lightness)
    val variantHsl = hsl.copyOf()
    variantHsl[2] = (hsl[2] - 0.15f).coerceIn(0f, 1f)
    val variant = Color(ColorUtils.HSLToColor(variantHsl))

    // Light: Lighter (higher lightness, lower saturation)
    val lightHsl = hsl.copyOf()
    lightHsl[1] = (hsl[1] * 0.5f).coerceIn(0f, 1f)
    lightHsl[2] = (hsl[2] + 0.35f).coerceIn(0f, 1f)
    val light = Color(ColorUtils.HSLToColor(lightHsl))

    return Triple(baseColor, variant, light)
}