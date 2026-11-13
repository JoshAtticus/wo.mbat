package wombat.joshattic.us.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import wombat.joshattic.us.AppConfig

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
    fun clientLabel(): String? {
        val name = clientName ?: return null
        return clientVersion?.takeIf { it.isNotBlank() }?.let { "$name $it" } ?: name
    }
}

object Womp {
    const val WOMP_VERSION = "1.0.0"
    const val CLIENT_NAME = "wo.mbat"
    val CLIENT_VERSION: String
        get() = AppConfig.VERSION_NAME.trim()

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
        val obj = runCatching { Json.parseToJsonElement(json).jsonObject }.getOrNull() ?: return null
        val womp = (obj["womp"] as? JsonPrimitive)?.contentOrNull?.trim().orEmpty()
        // womp is the required marker; without it the whole payload is ignored
        if (womp.isEmpty()) return null
        val name = (obj["clientName"] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }?.take(20)
        val version = (obj["clientVersion"] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }?.take(10)
        val size = (obj["linkPreviewSize"] as? JsonPrimitive)?.contentOrNull?.trim()?.lowercase()
            ?.takeIf { it in validPreviewSizes && it != "auto" }
        val og = (obj["enableOpenGraph"] as? JsonPrimitive)?.booleanOrNull ?: true
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
        val obj: JsonObject = buildJsonObject {
            put("womp", meta.womp)
            meta.clientName?.let { put("clientName", it) }
            meta.clientVersion?.let { put("clientVersion", it) }
            // this client supports Open Graph previews, so the toggle is included
            put("enableOpenGraph", meta.enableOpenGraph)
            meta.linkPreviewSize?.let { put("linkPreviewSize", it) }
        }
        return obj.toString()
    }
}
