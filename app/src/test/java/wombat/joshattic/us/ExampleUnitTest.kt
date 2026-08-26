package wombat.joshattic.us

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import wombat.joshattic.us.ui.screens.autoLinkAndMentions

/**
 * Regression tests for autoLinkAndMentions — links must be clickable on ALL posts,
 * including URLs wrapped in formatting tags (<u>, <strong>, <em>, ...) and URLs
 * containing '@' (e.g. mastodon links).
 */
class AutoLinkTest {

    @Test
    fun urlInsideUnderlineTag_isLinked() {
        // Exact structure of https://wasteof.money/posts/6a858e5b2f994596b28ad4e0
        val html = "<p><u>https://blog.joshattic.us/posts/2026-08-19-actually-i-dont-hate-smartwatches-i-hate-google</u></p>"
        val result = autoLinkAndMentions(html)
        assertTrue(
            "URL wrapped in <u> should be linkified",
            result.contains("<a href=\"https://blog.joshattic.us/posts/2026-08-19-actually-i-dont-hate-smartwatches-i-hate-google\">")
        )
    }

    @Test
    fun urlsAfterOtherFormattingTags_areLinked() {
        for (tag in listOf("strong", "em", "i", "b", "code", "s")) {
            val result = autoLinkAndMentions("<p><$tag>https://example.com/page</$tag></p>")
            assertTrue(
                "URL after <$tag> should be linkified",
                result.contains("<a href=\"https://example.com/page\">")
            )
        }
    }

    @Test
    fun plainUrlIsStillLinked() {
        val result = autoLinkAndMentions("<p>check this out https://example.com cool</p>")
        assertTrue(result.contains("<a href=\"https://example.com\">https://example.com</a>"))
    }

    @Test
    fun urlContainingAtSign_isNotCorruptedByMentionRegex() {
        val html = "<p>https://mastodon.social/@someuser/123456</p>"
        val result = autoLinkAndMentions(html)
        assertTrue(
            "Full URL with @path should be the href",
            result.contains("<a href=\"https://mastodon.social/@someuser/123456\">")
        )
        assertFalse("No wombat mention link may leak into a real URL", result.contains("wombat://"))
    }

    @Test
    fun existingAnchorContentIsNotDoubleWrapped() {
        val html = "<p><a href=\"https://example.com\">visit https://example.com now</a></p>"
        val result = autoLinkAndMentions(html)
        assertEquals(html, result)
    }

    @Test
    fun attributeValuesAreNeverTouched() {
        val html = "<img src=\"https://i.ibb.co/1tQvGjwj/hero.png\" alt=\"@notamention\">"
        val result = autoLinkAndMentions(html)
        assertEquals(html, result)
    }

    @Test
    fun mentionsAreStillConverted() {
        val result = autoLinkAndMentions("<p>hey @joshatticus look!</p>")
        assertTrue(result.contains("<a href=\"wombat://user/joshatticus\">@joshatticus</a>"))
    }

    @Test
    fun emailIsNotTreatedAsMention() {
        val result = autoLinkAndMentions("<p>mail me at foo@bar.com</p>")
        assertFalse(result.contains("wombat://"))
    }

    @Test
    fun trailingSentencePunctuationIsNotPartOfUrl() {
        val result = autoLinkAndMentions("<p>see https://example.com.</p>")
        assertTrue(result.contains("<a href=\"https://example.com\">https://example.com</a>."))
    }

    @Test
    fun balancedParenthesesStayInUrl() {
        val result = autoLinkAndMentions("<p>(see https://en.wikipedia.org/wiki/Frog_(genus))</p>")
        assertTrue(result.contains("<a href=\"https://en.wikipedia.org/wiki/Frog_(genus)\">"))
    }

    @Test
    fun urlInsideParentheses_isLinkedWithoutClosingParen() {
        val result = autoLinkAndMentions("<p>(https://blog.joshattic.us/posts/some-post)</p>")
        assertTrue(
            "URL inside () should get a clean href",
            result.contains("<a href=\"https://blog.joshattic.us/posts/some-post\">")
        )
        assertFalse("Unbalanced ')' must not end up in the href", result.contains("some-post)\""))
        assertTrue(result.contains("</a>)"))
    }

    @Test
    fun urlInsideSquareBrackets_isLinkedWithoutClosingBracket() {
        val result = autoLinkAndMentions("<p>[https://example.com/page]</p>")
        assertTrue(result.contains("<a href=\"https://example.com/page\">"))
        assertFalse(result.contains("page]\""))
        assertTrue(result.contains("</a>]"))
    }

    @Test
    fun fullRealPostPayload_linksCorrectly() {
        // Mirrors the actual API response for post 6a858e5b2f994596b28ad4e0
        val html = "<p>New Blog Post — <strong>Actually, I don't hate smartwatches, I just hate Google, and I wish I didn't</strong></p>" +
            "<p><u>https://blog.joshattic.us/posts/2026-08-19-actually-i-dont-hate-smartwatches-i-hate-google</u></p>" +
            "<blockquote><p><em>Comments on this post are mirrored to my blog.</em></p></blockquote>" +
            "<img src=\"https://i.ibb.co/1tQvGjwj/hero.png\">"
        val result = autoLinkAndMentions(html)
        assertTrue(
            result.contains(
                "<u><a href=\"https://blog.joshattic.us/posts/2026-08-19-actually-i-dont-hate-smartwatches-i-hate-google\">" +
                    "https://blog.joshattic.us/posts/2026-08-19-actually-i-dont-hate-smartwatches-i-hate-google</a></u>"
            )
        )
        assertFalse(result.contains("wombat://"))
    }
}