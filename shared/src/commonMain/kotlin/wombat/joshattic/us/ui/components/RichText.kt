package wombat.joshattic.us.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

enum class RichFormat {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    QUOTE
}

data class RichSpan(
    val start: Int,
    val end: Int,
    val format: RichFormat
)

const val QUOTE_ANNOTATION_TAG = "wombat_quote"

val BrandQuoteColor = Color(0xFF6366F1)

/**
 * Draws the branded indigo vertical quote bar for any character ranges tagged with [QUOTE_ANNOTATION_TAG].
 */
fun DrawScope.drawQuoteBars(
    textLayoutResult: TextLayoutResult?,
    annotatedString: AnnotatedString,
    stripeWidthPx: Float = 2f * density,
    color: Color = BrandQuoteColor
) {
    val layout = textLayoutResult ?: return
    val quoteAnnotations = annotatedString.getStringAnnotations(QUOTE_ANNOTATION_TAG, 0, annotatedString.length)
    for (ann in quoteAnnotations) {
        val start = ann.start.coerceIn(0, layout.layoutInput.text.length)
        val end = (ann.end - 1).coerceIn(start, (layout.layoutInput.text.length - 1).coerceAtLeast(0))
        if (layout.layoutInput.text.isEmpty()) continue
        val startLine = layout.getLineForOffset(start)
        val endLine = layout.getLineForOffset(end)
        val top = layout.getLineTop(startLine)
        val bottom = layout.getLineBottom(endLine)
        drawRect(
            color = color,
            topLeft = Offset(0f, top),
            size = Size(stripeWidthPx.coerceAtLeast(1f), (bottom - top).coerceAtLeast(1f))
        )
    }
}

/**
 * Decodes HTML entities and converts wasteof.money HTML into a Compose [AnnotatedString].
 */
fun htmlToAnnotatedString(
    html: String,
    linkColor: Color,
    onMentionClick: ((String) -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null
): AnnotatedString {
    val tokens = tokenizeHtml(html.trim())
    val builder = AnnotatedString.Builder()

    data class ActiveTag(
        val name: String,
        val start: Int,
        val href: String? = null
    )

    val stack = mutableListOf<ActiveTag>()
    var listIndex = 0
    var inOrderedList = false

    fun appendNewlines(count: Int) {
        val current = builder.toString()
        var existing = 0
        var i = current.length - 1
        while (i >= 0 && current[i] == '\n') {
            existing++
            i--
        }
        val needed = (count - existing).coerceAtLeast(0)
        repeat(needed) { builder.append('\n') }
    }

    for (token in tokens) {
        when (token) {
            is HtmlToken.Text -> {
                builder.append(decodeHtmlEntities(token.text))
            }
            is HtmlToken.OpenTag -> {
                val tag = token.name.lowercase()
                when (tag) {
                    "br" -> builder.append('\n')
                    "p", "div" -> {
                        if (builder.length > 0) appendNewlines(2)
                        stack.add(ActiveTag(tag, builder.length))
                    }
                    "blockquote" -> {
                        if (builder.length > 0) appendNewlines(1)
                        stack.add(ActiveTag(tag, builder.length))
                    }
                    "ul" -> {
                        if (builder.length > 0) appendNewlines(1)
                        inOrderedList = false
                        stack.add(ActiveTag(tag, builder.length))
                    }
                    "ol" -> {
                        if (builder.length > 0) appendNewlines(1)
                        inOrderedList = true
                        listIndex = 0
                        stack.add(ActiveTag(tag, builder.length))
                    }
                    "li" -> {
                        if (builder.length > 0) appendNewlines(1)
                        if (inOrderedList) {
                            listIndex++
                            builder.append("$listIndex. ")
                        } else {
                            builder.append("• ")
                        }
                        stack.add(ActiveTag(tag, builder.length))
                    }
                    "h1", "h2", "h3", "h4", "h5", "h6",
                    "b", "strong", "i", "em", "u", "s", "del", "strike",
                    "code", "pre", "a" -> {
                        if (tag == "pre" && builder.length > 0) appendNewlines(1)
                        stack.add(ActiveTag(tag, builder.length, token.attrs["href"]))
                    }
                }
            }
            is HtmlToken.CloseTag -> {
                val tag = token.name.lowercase()
                val idx = stack.indexOfLast { it.name == tag }
                if (idx >= 0) {
                    val active = stack.removeAt(idx)
                    val start = active.start
                    val end = builder.length
                    if (end > start) {
                        when (tag) {
                            "b", "strong" -> builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                            "i", "em" -> builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                            "u" -> builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                            "s", "del", "strike" -> builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                            "code", "pre" -> builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, end)
                            "h1" -> builder.addStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold), start, end)
                            "h2" -> builder.addStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), start, end)
                            "h3" -> builder.addStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold), start, end)
                            "h4", "h5", "h6" -> builder.addStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold), start, end)
                            "blockquote" -> {
                                builder.addStyle(
                                    ParagraphStyle(textIndent = TextIndent(firstLine = 11.sp, restLine = 11.sp)),
                                    start,
                                    end
                                )
                                builder.addStringAnnotation(QUOTE_ANNOTATION_TAG, "quote", start, end)
                            }
                            "a" -> {
                                val href = active.href
                                if (!href.isNullOrBlank()) {
                                    val linkStyle = TextLinkStyles(
                                        style = SpanStyle(
                                            color = linkColor,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.None
                                        )
                                    )
                                    val annotation = LinkAnnotation.Clickable(
                                        tag = href,
                                        styles = linkStyle
                                    ) {
                                        if (href.startsWith("wombat://user/")) {
                                            val username = href.substringAfter("wombat://user/")
                                            onMentionClick?.invoke(username)
                                        } else {
                                            onUrlClick?.invoke(href)
                                        }
                                    }
                                    builder.addLink(annotation, start, end)
                                }
                            }
                        }
                    }
                    when (tag) {
                        "p", "div" -> appendNewlines(2)
                        "blockquote", "pre", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol" -> appendNewlines(1)
                    }
                }
            }
        }
    }

    val raw = builder.toAnnotatedString()
    var trimmedLen = raw.length
    while (trimmedLen > 0 && (raw[trimmedLen - 1] == '\n' || raw[trimmedLen - 1] == '\r' || raw[trimmedLen - 1] == ' ')) {
        trimmedLen--
    }
    return if (trimmedLen < raw.length) raw.subSequence(0, trimmedLen) else raw
}

private sealed class HtmlToken {
    data class Text(val text: String) : HtmlToken()
    data class OpenTag(val name: String, val attrs: Map<String, String>) : HtmlToken()
    data class CloseTag(val name: String) : HtmlToken()
}

private val TAG_REGEX = Regex("<(/?)([a-zA-Z0-9]+)([^>]*)>")
private val ATTR_REGEX = Regex("""([a-zA-Z0-9_-]+)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""")

private fun tokenizeHtml(html: String): List<HtmlToken> {
    val tokens = mutableListOf<HtmlToken>()
    var lastIndex = 0
    for (match in TAG_REGEX.findAll(html)) {
        if (match.range.first > lastIndex) {
            tokens.add(HtmlToken.Text(html.substring(lastIndex, match.range.first)))
        }
        val isClosing = match.groupValues[1] == "/"
        val tagName = match.groupValues[2]
        val attrStr = match.groupValues[3]
        if (isClosing) {
            tokens.add(HtmlToken.CloseTag(tagName))
        } else {
            val attrs = mutableMapOf<String, String>()
            for (attrMatch in ATTR_REGEX.findAll(attrStr)) {
                val key = attrMatch.groupValues[1].lowercase()
                val value = attrMatch.groupValues[2].ifEmpty {
                    attrMatch.groupValues[3].ifEmpty { attrMatch.groupValues[4] }
                }
                attrs[key] = decodeHtmlEntities(value)
            }
            tokens.add(HtmlToken.OpenTag(tagName, attrs))
            if (attrStr.trimEnd().endsWith("/")) {
                tokens.add(HtmlToken.CloseTag(tagName))
            }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < html.length) {
        tokens.add(HtmlToken.Text(html.substring(lastIndex)))
    }
    return tokens
}

fun decodeHtmlEntities(input: String): String {
    if (!input.contains('&')) return input
    return input
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace(Regex("&#x([0-9a-fA-F]+);")) { match ->
            match.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: match.value
        }
        .replace(Regex("&#([0-9]+);")) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
}

fun escapeHtmlText(input: String): String = buildString(input.length) {
    for (ch in input) {
        when (ch) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            else -> append(ch)
        }
    }
}

/**
 * Pure Compose rich-text editor state supporting Bold, Italic, Underline, Strikethrough, and Quote.
 */
class RichTextEditorState(initialHtml: String = "") {
    private val inlineSpans = mutableListOf<RichSpan>()
    private val quotedLines = mutableSetOf<Int>()
    private val activeTypingFormats = mutableSetOf<RichFormat>()

    var value: TextFieldValue = TextFieldValue("")
        private set

    init {
        setFromHtml(initialHtml)
    }

    fun setFromHtml(html: String) {
        inlineSpans.clear()
        quotedLines.clear()
        activeTypingFormats.clear()

        val textBuilder = StringBuilder()
        val parsed = parseHtmlForEditor(html.trim())
        textBuilder.append(parsed.text)
        inlineSpans.addAll(parsed.spans)
        quotedLines.addAll(parsed.quotedLines)

        val plain = textBuilder.toString()
        val annotated = buildStyledAnnotatedString(plain)
        value = TextFieldValue(
            annotatedString = annotated,
            selection = TextRange(plain.length)
        )
    }

    fun onValueChange(newValue: TextFieldValue) {
        val oldText = value.text
        val newText = newValue.text

        if (oldText != newText) {
            applyTextDiff(oldText, newText)
        } else {
            syncActiveTypingFormats(newValue.selection)
        }

        val annotated = buildStyledAnnotatedString(newText)
        value = newValue.copy(annotatedString = annotated)
    }

    fun toggleFormat(format: RichFormat) {
        val text = value.text
        if (format == RichFormat.QUOTE) {
            val selStart = min(value.selection.start, value.selection.end).coerceIn(0, text.length)
            val selEnd = max(value.selection.start, value.selection.end).coerceIn(0, text.length)
            val startLine = lineIndexAt(text, selStart)
            val endLine = lineIndexAt(text, selEnd)
            val linesRange = startLine..endLine
            val allQuoted = linesRange.all { it in quotedLines }
            if (allQuoted) {
                quotedLines.removeAll(linesRange.toSet())
            } else {
                quotedLines.addAll(linesRange)
            }
            value = value.copy(annotatedString = buildStyledAnnotatedString(text))
            return
        }

        val start = min(value.selection.start, value.selection.end).coerceIn(0, text.length)
        val end = max(value.selection.start, value.selection.end).coerceIn(0, text.length)

        if (start == end) {
            if (format in activeTypingFormats) {
                activeTypingFormats.remove(format)
            } else {
                activeTypingFormats.add(format)
            }
            return
        }

        val fullyCovered = isRangeCovered(start, end, format)
        if (fullyCovered) {
            removeFormatInRange(start, end, format)
        } else {
            addFormatInRange(start, end, format)
        }
        value = value.copy(annotatedString = buildStyledAnnotatedString(text))
    }

    fun isFormatActive(format: RichFormat): Boolean {
        val text = value.text
        if (format == RichFormat.QUOTE) {
            val line = lineIndexAt(text, value.selection.start.coerceIn(0, text.length))
            return line in quotedLines
        }
        val start = min(value.selection.start, value.selection.end).coerceIn(0, text.length)
        val end = max(value.selection.start, value.selection.end).coerceIn(0, text.length)
        return if (start == end) {
            format in activeTypingFormats
        } else {
            isRangeCovered(start, end, format)
        }
    }

    fun clear() {
        inlineSpans.clear()
        quotedLines.clear()
        activeTypingFormats.clear()
        value = TextFieldValue("")
    }

    fun toHtml(): String {
        val text = value.text
        if (text.isBlank()) return ""
        val lines = text.split('\n')
        val out = StringBuilder()
        var offset = 0
        lines.forEachIndexed { lineIdx, lineText ->
            val lineStart = offset
            val lineEnd = offset + lineText.length
            val innerHtml = renderLineInlineHtml(lineText, lineStart, lineEnd)
            if (lineIdx in quotedLines) {
                out.append("<blockquote><p dir=\"ltr\">").append(innerHtml).append("</p></blockquote>")
            } else if (innerHtml.isNotEmpty() || lineIdx < lines.lastIndex) {
                out.append("<p dir=\"ltr\">").append(innerHtml).append("</p>")
            }
            offset = lineEnd + 1
        }
        return out.toString()
    }

    private fun renderLineInlineHtml(lineText: String, lineStart: Int, lineEnd: Int): String {
        if (lineText.isEmpty()) return ""
        val relevant = inlineSpans.filter { it.end > lineStart && it.start < lineEnd }
        if (relevant.isEmpty()) return escapeHtmlText(lineText)

        val sb = StringBuilder()
        var activeBold = false
        var activeItalic = false
        var activeUnderline = false
        var activeStrike = false

        fun updateTags(pos: Int) {
            val wantBold = relevant.any { it.format == RichFormat.BOLD && pos in it.start until it.end }
            val wantItalic = relevant.any { it.format == RichFormat.ITALIC && pos in it.start until it.end }
            val wantUnderline = relevant.any { it.format == RichFormat.UNDERLINE && pos in it.start until it.end }
            val wantStrike = relevant.any { it.format == RichFormat.STRIKETHROUGH && pos in it.start until it.end }

            if (activeStrike && !wantStrike) { sb.append("</s>"); activeStrike = false }
            if (activeUnderline && !wantUnderline) { sb.append("</u>"); activeUnderline = false }
            if (activeItalic && !wantItalic) { sb.append("</i>"); activeItalic = false }
            if (activeBold && !wantBold) { sb.append("</b>"); activeBold = false }

            if (!activeBold && wantBold) { sb.append("<b>"); activeBold = true }
            if (!activeItalic && wantItalic) { sb.append("<i>"); activeItalic = true }
            if (!activeUnderline && wantUnderline) { sb.append("<u>"); activeUnderline = true }
            if (!activeStrike && wantStrike) { sb.append("<s>"); activeStrike = true }
        }

        for (i in lineText.indices) {
            updateTags(lineStart + i)
            sb.append(escapeHtmlText(lineText[i].toString()))
        }
        if (activeStrike) sb.append("</s>")
        if (activeUnderline) sb.append("</u>")
        if (activeItalic) sb.append("</i>")
        if (activeBold) sb.append("</b>")
        return sb.toString()
    }

    private fun applyTextDiff(oldText: String, newText: String) {
        var prefix = 0
        val minLen = min(oldText.length, newText.length)
        while (prefix < minLen && oldText[prefix] == newText[prefix]) {
            prefix++
        }
        var oldSuffix = oldText.length
        var newSuffix = newText.length
        while (oldSuffix > prefix && newSuffix > prefix && oldText[oldSuffix - 1] == newText[newSuffix - 1]) {
            oldSuffix--
            newSuffix--
        }

        val removedLen = oldSuffix - prefix
        val insertedLen = newSuffix - prefix
        val delta = insertedLen - removedLen

        // Update inline spans
        val updated = mutableListOf<RichSpan>()
        for (span in inlineSpans) {
            when {
                span.end <= prefix -> updated.add(span)
                span.start >= oldSuffix -> updated.add(span.copy(start = span.start + delta, end = span.end + delta))
                else -> {
                    val newStart = if (span.start < prefix) span.start else prefix + insertedLen
                    val newEnd = if (span.end > oldSuffix) span.end + delta else prefix
                    if (newEnd > newStart) {
                        updated.add(span.copy(start = newStart, end = newEnd))
                    }
                }
            }
        }
        inlineSpans.clear()
        inlineSpans.addAll(updated)

        if (insertedLen > 0) {
            for (fmt in activeTypingFormats) {
                if (fmt != RichFormat.QUOTE) {
                    addFormatInRange(prefix, prefix + insertedLen, fmt)
                }
            }
        }

        // Update quoted lines if newlines were inserted or removed
        val oldLines = oldText.count { it == '\n' }
        val newLines = newText.count { it == '\n' }
        if (oldLines != newLines) {
            val changeLine = lineIndexAt(oldText, prefix)
            val lineDelta = newLines - oldLines
            val shifted = mutableSetOf<Int>()
            for (line in quotedLines) {
                when {
                    line < changeLine -> shifted.add(line)
                    line == changeLine -> {
                        shifted.add(line)
                        if (lineDelta > 0) {
                            for (i in 1..lineDelta) shifted.add(line + i)
                        }
                    }
                    else -> {
                        val target = line + lineDelta
                        if (target >= changeLine) shifted.add(target)
                    }
                }
            }
            quotedLines.clear()
            quotedLines.addAll(shifted)
        }
    }

    private fun syncActiveTypingFormats(selection: TextRange) {
        if (selection.start != selection.end) return
        val pos = selection.start
        activeTypingFormats.clear()
        if (pos > 0) {
            for (fmt in listOf(RichFormat.BOLD, RichFormat.ITALIC, RichFormat.UNDERLINE, RichFormat.STRIKETHROUGH)) {
                if (inlineSpans.any { it.format == fmt && pos in (it.start + 1)..it.end }) {
                    activeTypingFormats.add(fmt)
                }
            }
        }
    }

    private fun isRangeCovered(start: Int, end: Int, format: RichFormat): Boolean {
        if (start >= end) return false
        for (i in start until end) {
            if (inlineSpans.none { it.format == format && i in it.start until it.end }) {
                return false
            }
        }
        return true
    }

    private fun addFormatInRange(start: Int, end: Int, format: RichFormat) {
        if (start >= end) return
        removeFormatInRange(start, end, format)
        inlineSpans.add(RichSpan(start, end, format))
        mergeAdjacentSpans(format)
    }

    private fun removeFormatInRange(start: Int, end: Int, format: RichFormat) {
        val result = mutableListOf<RichSpan>()
        for (span in inlineSpans) {
            if (span.format != format || span.end <= start || span.start >= end) {
                result.add(span)
            } else {
                if (span.start < start) result.add(span.copy(end = start))
                if (span.end > end) result.add(span.copy(start = end))
            }
        }
        inlineSpans.clear()
        inlineSpans.addAll(result)
    }

    private fun mergeAdjacentSpans(format: RichFormat) {
        val matching = inlineSpans.filter { it.format == format }.sortedBy { it.start }
        val others = inlineSpans.filter { it.format != format }
        val merged = mutableListOf<RichSpan>()
        for (span in matching) {
            val last = merged.lastOrNull()
            if (last != null && last.end >= span.start) {
                merged[merged.lastIndex] = last.copy(end = max(last.end, span.end))
            } else {
                merged.add(span)
            }
        }
        inlineSpans.clear()
        inlineSpans.addAll(others)
        inlineSpans.addAll(merged)
    }

    private fun buildStyledAnnotatedString(text: String): AnnotatedString {
        return AnnotatedString.Builder(text).apply {
            for (span in inlineSpans) {
                val s = span.start.coerceIn(0, text.length)
                val e = span.end.coerceIn(s, text.length)
                if (e > s) {
                    val style = when (span.format) {
                        RichFormat.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                        RichFormat.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                        RichFormat.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                        RichFormat.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                        RichFormat.QUOTE -> continue
                    }
                    addStyle(style, s, e)
                }
            }
            if (quotedLines.isNotEmpty()) {
                val lines = text.split('\n')
                var offset = 0
                lines.forEachIndexed { idx, line ->
                    val end = offset + line.length
                    if (idx in quotedLines && end > offset) {
                        addStyle(
                            ParagraphStyle(textIndent = TextIndent(firstLine = 11.sp, restLine = 11.sp)),
                            offset,
                            end
                        )
                        addStringAnnotation(QUOTE_ANNOTATION_TAG, "quote", offset, end)
                    }
                    offset = end + 1
                }
            }
        }.toAnnotatedString()
    }

    private fun lineIndexAt(text: String, offset: Int): Int {
        var count = 0
        val limit = offset.coerceIn(0, text.length)
        for (i in 0 until limit) {
            if (text[i] == '\n') count++
        }
        return count
    }

    private data class ParsedEditorContent(
        val text: String,
        val spans: List<RichSpan>,
        val quotedLines: Set<Int>
    )

    private fun parseHtmlForEditor(html: String): ParsedEditorContent {
        if (html.isEmpty()) return ParsedEditorContent("", emptyList(), emptySet())
        val tokens = tokenizeHtml(html)
        val text = StringBuilder()
        val spans = mutableListOf<RichSpan>()
        val quotes = mutableSetOf<Int>()
        val stack = mutableListOf<Pair<String, Int>>()
        var inQuote = false

        fun appendNewlineIfNeeded(minNewlines: Int) {
            if (text.isEmpty()) return
            var existing = 0
            var i = text.length - 1
            while (i >= 0 && text[i] == '\n') {
                existing++
                i--
            }
            repeat((minNewlines - existing).coerceAtLeast(0)) { text.append('\n') }
        }

        for (token in tokens) {
            when (token) {
                is HtmlToken.Text -> {
                    val decoded = decodeHtmlEntities(token.text)
                    val startLine = text.count { it == '\n' }
                    text.append(decoded)
                    if (inQuote) {
                        val endLine = text.count { it == '\n' }
                        for (l in startLine..endLine) quotes.add(l)
                    }
                }
                is HtmlToken.OpenTag -> {
                    val tag = token.name.lowercase()
                    when (tag) {
                        "br" -> {
                            text.append('\n')
                            if (inQuote) quotes.add(text.count { it == '\n' })
                        }
                        "p", "div" -> {
                            appendNewlineIfNeeded(1)
                            stack.add(tag to text.length)
                        }
                        "blockquote" -> {
                            appendNewlineIfNeeded(1)
                            inQuote = true
                            stack.add(tag to text.length)
                        }
                        "b", "strong", "i", "em", "u", "s", "del", "strike" -> {
                            stack.add(tag to text.length)
                        }
                    }
                }
                is HtmlToken.CloseTag -> {
                    val tag = token.name.lowercase()
                    val idx = stack.indexOfLast { it.first == tag }
                    if (idx >= 0) {
                        val (_, start) = stack.removeAt(idx)
                        val end = text.length
                        if (end > start) {
                            when (tag) {
                                "b", "strong" -> spans.add(RichSpan(start, end, RichFormat.BOLD))
                                "i", "em" -> spans.add(RichSpan(start, end, RichFormat.ITALIC))
                                "u" -> spans.add(RichSpan(start, end, RichFormat.UNDERLINE))
                                "s", "del", "strike" -> spans.add(RichSpan(start, end, RichFormat.STRIKETHROUGH))
                            }
                        }
                        if (tag == "blockquote") {
                            inQuote = false
                            appendNewlineIfNeeded(1)
                        } else if (tag == "p" || tag == "div") {
                            appendNewlineIfNeeded(1)
                        }
                    }
                }
            }
        }

        while (text.isNotEmpty() && text.last() == '\n') {
            text.deleteAt(text.length - 1)
        }
        return ParsedEditorContent(
            text = text.toString(),
            spans = spans.filter { it.end <= text.length },
            quotedLines = quotes.filter { it <= text.count { c -> c == '\n' } }.toSet()
        )
    }
}
