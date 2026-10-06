package wombat.joshattic.us.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan

const val QUOTE_STRIPE_WIDTH_DP = 2
const val QUOTE_GAP_WIDTH_DP = 9

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