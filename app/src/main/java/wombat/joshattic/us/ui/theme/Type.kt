package wombat.joshattic.us.ui.theme

import android.graphics.Typeface
import android.os.Build
import android.widget.TextView
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import wombat.joshattic.us.R

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

private fun TextStyle.withFontFamily(fontFamily: FontFamily) = copy(fontFamily = fontFamily)

private val DefaultTypography = Typography()

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFlexBody,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSansFlexBody,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlexHeaders,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlexHeaders,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFlexBody,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    displayLarge = DefaultTypography.displayLarge.withFontFamily(GoogleSansFlexHeaders),
    displayMedium = DefaultTypography.displayMedium.withFontFamily(GoogleSansFlexHeaders),
    displaySmall = DefaultTypography.displaySmall.withFontFamily(GoogleSansFlexHeaders),
    headlineLarge = DefaultTypography.headlineLarge.withFontFamily(GoogleSansFlexHeaders),
    headlineMedium = DefaultTypography.headlineMedium.withFontFamily(GoogleSansFlexHeaders),
    headlineSmall = DefaultTypography.headlineSmall.withFontFamily(GoogleSansFlexHeaders),
    titleSmall = DefaultTypography.titleSmall.withFontFamily(GoogleSansFlexHeaders),
    bodySmall = DefaultTypography.bodySmall.withFontFamily(GoogleSansFlexBody),
    labelMedium = DefaultTypography.labelMedium.withFontFamily(GoogleSansFlexBody),
    labelSmall = DefaultTypography.labelSmall.withFontFamily(GoogleSansFlexBody)
)

internal fun applyGoogleSansFlexTypeface(textView: TextView, roundness: Float) {
    textView.typeface = ResourcesCompat.getFont(textView.context, R.font.google_sans_flex) ?: Typeface.DEFAULT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        textView.paint.fontVariationSettings = "'ROND' $roundness"
    }
}
