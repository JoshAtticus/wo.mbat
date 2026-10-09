package wombat.joshattic.us.data.network

import org.json.JSONObject

/**
 * womp 1.0.0 metadata (https://github.com/JoshAtticus/womp-spec).
 *
 * Metadata travels in the `alt` attribute of the first `<p>` element of a post's
 * HTML body — the server never shows it and never strips it.
 */
data class WompMetadata(
    val womp: String,
    val clientName: String? = null,
    val clientVersion: String? = null,
    val enableOpenGraph: Boolean = true,
    val linkPreviewSize: String? = null
) {
    /** "wo.mbat 2.4.1" style label, or null when the client is unnamed. */
    fun clientLabel(): String? {
        val name = clientName ?: return null
        return clientVersion?.takeIf { it.isNotBlank() }?.let { "$name $it" } ?: name
    }
}

object Womp {
    const val WOMP_VERSION = "1.0.0"
    const val CLIENT_NAME = "wo.mbat"
    const val CLIENT_VERSION = "2.4.1"

    // \b after "p" avoids matching <picture>/<pre>/<param> etc.
    private val firstPTag = Regex("<p\\b[^>]*>", RegexOption.IGNORE_CASE)
    private val altAttribute = Regex("\\s+alt\\s*=\\s*(\"[^\"]*\"|'[^']*')", RegexOption.IGNORE_CASE)
    private val validPreviewSizes = setOf("large", "small", "auto")

    /** Parses womp metadata from post HTML; returns null when absent or invalid. */
    fun parse(html: String?): WompMetadata? {
        if (html.isNullOrBlank()) return null
        val tag = firstPTag.find(html) ?: return null
        val alt = altAttribute.find(tag.value) ?: return null
        // the server HTML-escapes attribute values (&quot; etc), so decode before decoding JSON
        return parseJson(unescapeHtml(alt.groupValues[1].trim('\'', '"')))
    }

    private fun unescapeHtml(value: String): String = value
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        // &amp; last so it isn't decoded from an already-decoded entity
        .replace("&amp;", "&")

    /**
     * Removes the womp metadata `alt` attribute from the first `<p>`, for
     * scanning raw post bodies without matching against metadata (e.g. the
     * like easter egg keywords).
     */
    fun stripMetadata(html: String?): String {
        if (html.isNullOrBlank()) return html.orEmpty()
        val firstP = firstPTag.find(html) ?: return html
        return html.replaceRange(firstP.range, firstP.value.replace(altAttribute, ""))
    }

    private fun parseJson(json: String): WompMetadata? {
        val obj = runCatching { JSONObject(json) }.getOrNull() ?: return null
        val womp = obj.optString("womp").trim()
        // womp is the required marker; without it the whole payload is ignored
        if (womp.isEmpty()) return null
        val name = obj.optString("clientName").trim().takeIf { it.isNotEmpty() }?.take(20)
        val version = obj.optString("clientVersion").trim().takeIf { it.isNotEmpty() }?.take(10)
        val size = obj.optString("linkPreviewSize").trim().lowercase()
            .takeIf { it in validPreviewSizes && it != "auto" }
        val og = if (obj.has("enableOpenGraph")) obj.optBoolean("enableOpenGraph", true) else true
        return WompMetadata(womp, name, version, og, size)
    }

    /**
     * Returns the metadata to attach when this client creates/edits a post.
     * Spec: never modify metadata placed by another client — preserve theirs;
     * ours (or missing metadata) is refreshed to the current client/version.
     */
    fun metadataForPost(originalHtml: String? = null): WompMetadata {
        val original = parse(originalHtml)
        val foreign = original?.clientName != null && !original.clientName.equals(CLIENT_NAME, ignoreCase = true)
        return if (foreign) original!! else WompMetadata(WOMP_VERSION, CLIENT_NAME, CLIENT_VERSION)
    }

    /**
     * Attaches womp metadata to [html]'s first `<p>` (wrapping the content in a
     * fresh `<p>` when there is none). Blank content (pure reposts) is untouched.
     */
    fun attach(html: String, originalHtml: String? = null): String {
        if (html.isBlank()) return html
        val alt = "alt='${jsonFor(metadataForPost(originalHtml))}'"
        val firstP = firstPTag.find(html) ?: return "<p $alt>$html</p>"
        val cleaned = firstP.value.replace(altAttribute, "")
        // drop the trailing '>' of the opening tag and re-add with our alt attribute
        return html.replaceRange(firstP.range, cleaned.dropLast(1) + " $alt>")
    }

    private fun jsonFor(meta: WompMetadata): String {
        val obj = JSONObject()
        obj.put("womp", meta.womp)
        meta.clientName?.let { obj.put("clientName", it) }
        meta.clientVersion?.let { obj.put("clientVersion", it) }
        // this client supports Open Graph previews, so the toggle is included
        obj.put("enableOpenGraph", meta.enableOpenGraph)
        meta.linkPreviewSize?.let { obj.put("linkPreviewSize", it) }
        return obj.toString()
    }
}
