package wombat.joshattic.us.wear.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Typography

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import wombat.joshattic.us.wear.R

@OptIn(ExperimentalTextApi::class)
private fun googleSansFlexFont(weight: FontWeight, roundness: Float): Font = Font(
    resId = R.font.google_sans_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        weight,
        FontStyle.Normal,
        FontVariation.Setting("ROND", roundness)
    )
)

@OptIn(ExperimentalTextApi::class)
private fun googleSansFlexFamily(roundness: Float): FontFamily = FontFamily(
    googleSansFlexFont(FontWeight.Thin, roundness),
    googleSansFlexFont(FontWeight.ExtraLight, roundness),
    googleSansFlexFont(FontWeight.Light, roundness),
    googleSansFlexFont(FontWeight.Normal, roundness),
    googleSansFlexFont(FontWeight.Medium, roundness),
    googleSansFlexFont(FontWeight.SemiBold, roundness),
    googleSansFlexFont(FontWeight.Bold, roundness),
    googleSansFlexFont(FontWeight.ExtraBold, roundness),
    googleSansFlexFont(FontWeight.Black, roundness)
)

private val GoogleSansFlexHeaders = googleSansFlexFamily(roundness = 100f)
private val GoogleSansFlexBody = googleSansFlexFamily(roundness = 50f)

private fun androidx.compose.ui.text.TextStyle.withFontFamily(fontFamily: FontFamily) =
    copy(fontFamily = fontFamily)

private val DefaultWearTypography = Typography()
private val WearTypography = DefaultWearTypography.copy(
    display1 = DefaultWearTypography.display1.withFontFamily(GoogleSansFlexHeaders),
    display2 = DefaultWearTypography.display2.withFontFamily(GoogleSansFlexHeaders),
    display3 = DefaultWearTypography.display3.withFontFamily(GoogleSansFlexHeaders),
    title1 = DefaultWearTypography.title1.withFontFamily(GoogleSansFlexHeaders),
    title2 = DefaultWearTypography.title2.withFontFamily(GoogleSansFlexHeaders),
    title3 = DefaultWearTypography.title3.withFontFamily(GoogleSansFlexHeaders),
    body1 = DefaultWearTypography.body1.withFontFamily(GoogleSansFlexBody),
    body2 = DefaultWearTypography.body2.withFontFamily(GoogleSansFlexBody),
    button = DefaultWearTypography.button.withFontFamily(GoogleSansFlexBody),
    caption1 = DefaultWearTypography.caption1.withFontFamily(GoogleSansFlexBody),
    caption2 = DefaultWearTypography.caption2.withFontFamily(GoogleSansFlexBody),
    caption3 = DefaultWearTypography.caption3.withFontFamily(GoogleSansFlexBody)
)

val BrandIndigo = Color(0xFF6366F1)
val BrandIndigoVariant = Color(0xFF4F46E5)
val BrandTeal = Color(0xFF0D9488)
val DarkCardSurface = Color(0xFF1E293B)
val DarkCardVariant = Color(0xFF111827)
val DarkCardBorder = Color(0xFF334155)

val WasteofColors = Colors(
    primary = BrandIndigo,
    primaryVariant = BrandIndigoVariant,
    secondary = BrandTeal,
    secondaryVariant = Color(0xFF0F766E),
    background = Color.Black,
    surface = DarkCardSurface,
    error = Color(0xFFEF4444),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    onError = Color.White
)

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WasteofColors,
        typography = WearTypography,
        content = content
    )
}
