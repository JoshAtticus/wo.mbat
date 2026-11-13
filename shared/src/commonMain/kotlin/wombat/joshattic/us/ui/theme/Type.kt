package wombat.joshattic.us.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import wombat.shared.generated.resources.Res
import wombat.shared.generated.resources.google_sans_flex

@Composable
private fun googleSansFlexFamily(): FontFamily {
    val weights = listOf(
        FontWeight.Thin,
        FontWeight.ExtraLight,
        FontWeight.Light,
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
        FontWeight.ExtraBold,
        FontWeight.Black
    )
    val fonts = weights.map { weight ->
        Font(
            resource = Res.font.google_sans_flex,
            weight = weight,
            style = FontStyle.Normal
        )
    }
    return remember(fonts) { FontFamily(fonts) }
}

private val DefaultTypography = Typography()

@Composable
fun rememberWombatTypography(): Typography {
    val family = googleSansFlexFamily()
    return remember(family) {
        Typography(
            bodyLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp
            ),
            labelLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp
            ),
            displayLarge = DefaultTypography.displayLarge.copy(fontFamily = family),
            displayMedium = DefaultTypography.displayMedium.copy(fontFamily = family),
            displaySmall = DefaultTypography.displaySmall.copy(fontFamily = family),
            headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = family),
            headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = family),
            headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = family),
            titleSmall = DefaultTypography.titleSmall.copy(fontFamily = family),
            bodySmall = DefaultTypography.bodySmall.copy(fontFamily = family),
            labelMedium = DefaultTypography.labelMedium.copy(fontFamily = family),
            labelSmall = DefaultTypography.labelSmall.copy(fontFamily = family)
        )
    }
}
