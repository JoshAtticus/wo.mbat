package wombat.joshattic.us.ui.theme

import androidx.compose.ui.graphics.Color

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
    val base = when (userColor?.lowercase()) {
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
        "gray" -> Color(0xFF1F2937)
        else -> Color(0xFF6366F1) // default indigo
    }
    val variant = base.copy(
        red = (base.red * 0.8f).coerceIn(0f, 1f),
        green = (base.green * 0.8f).coerceIn(0f, 1f),
        blue = (base.blue * 0.8f).coerceIn(0f, 1f)
    )
    val light = base.copy(
        red = ((base.red + 1f) / 2).coerceIn(0f, 1f),
        green = ((base.green + 1f) / 2).coerceIn(0f, 1f),
        blue = ((base.blue + 1f) / 2).coerceIn(0f, 1f)
    )
    return Triple(base, variant, light)
}