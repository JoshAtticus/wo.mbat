package wombat.joshattic.us.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan

/** Blockquote stripe width in dp (density-scaled equivalent of the old 6px constant). */
const val QUOTE_STRIPE_WIDTH_DP = 2

/** Gap between blockquote stripe and text in dp (equivalent of the old 24px constant). */
const val QUOTE_GAP_WIDTH_DP = 9

/**
 * Brand-styled blockquote span that renders identically on all supported API levels.
 *
 * The platform [android.text.style.QuoteSpan] only gained configurable stripe/gap
 * widths in API 28; on older versions the single-color constructor draws a hairline
 * stripe with virtually no gap between it and the text. This span fixes that and is
 * used purely for display (see HtmlText) — the composer keeps platform QuoteSpans so
 * they survive HtmlCompat.toHtml round-trips.
 */
class BrandedQuoteSpan(
    private val color: Int,
    stripeWidthPx: Int,
    private val gapWidthPx: Int
) : LeadingMarginSpan {

    private val stripeWidthPx = stripeWidthPx.coerceAtLeast(1)

    override fun getLeadingMargin(first: Boolean): Int = stripeWidthPx + gapWidthPx

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout
    ) {
        val previousStyle = p.style
        val previousColor = p.color
        p.style = Paint.Style.FILL
        p.color = color
        c.drawRect(x.toFloat(), top.toFloat(), (x + dir * stripeWidthPx).toFloat(), bottom.toFloat(), p)
        p.style = previousStyle
        p.color = previousColor
    }
}