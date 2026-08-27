package wombat.joshattic.us

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import wombat.joshattic.us.data.model.OpenGraphResponse
import wombat.joshattic.us.data.model.Poster
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.ui.screens.autoLinkAndMentions
import wombat.joshattic.us.ui.screens.groupConsecutiveReposts
import wombat.joshattic.us.ui.screens.repostHeaderText

/**
 * Tests for merging consecutive pure reposts of the same post into a single
 * feed entry with a combined "@a and @b reposted this" header.
 */
class RepostGroupingTest {

    private fun poster(name: String) = Poster(id = "id-$name", name = name, color = "indigo")

    private fun regularPost(id: String, name: String) =
        Post(id = id, poster = poster(name), content = "<p>hello</p>", time = 0L, comments = 0, loves = 0, reposts = 0)

    private fun pureRepost(id: String, name: String, target: Post) =
        Post(id = id, poster = poster(name), content = "", repost = target, time = 0L, comments = 0, loves = 0, reposts = 0)

    private val target = regularPost("target", "engineerrunner")

    @Test
    fun consecutiveRepostsOfSamePost_areMerged() {
        val feed = listOf(
            pureRepost("r1", "joshatticus", target),
            pureRepost("r2", "ethernet", target)
        )
        val groups = groupConsecutiveReposts(feed)
        assertEquals(1, groups.size)
        assertEquals(listOf("joshatticus", "ethernet"), groups[0].reposters.map { it.poster.name })
        assertEquals("r1", groups[0].primary.id)
    }

    @Test
    fun nonAdjacentRepostsOfSamePost_areNotMerged() {
        val feed = listOf(
            pureRepost("r1", "joshatticus", target),
            regularPost("other", "someone"),
            pureRepost("r2", "ethernet", target)
        )
        val groups = groupConsecutiveReposts(feed)
        assertEquals(3, groups.size)
        assertEquals(1, groups[0].reposters.size)
        assertEquals(0, groups[1].reposters.size)
        assertEquals(1, groups[2].reposters.size)
    }

    @Test
    fun repostsOfDifferentPosts_areNotMerged() {
        val otherTarget = regularPost("target2", "jeffalo")
        val feed = listOf(
            pureRepost("r1", "joshatticus", target),
            pureRepost("r2", "ethernet", otherTarget)
        )
        val groups = groupConsecutiveReposts(feed)
        assertEquals(2, groups.size)
    }

    @Test
    fun quoteReposts_areNeverGrouped() {
        val quoteRepost = Post(
            id = "q1", poster = poster("joshatticus"),
            content = "<p>look at this</p>", repost = target,
            time = 0L, comments = 0, loves = 0, reposts = 0
        )
        val feed = listOf(pureRepost("r1", "ethernet", target), quoteRepost)
        val groups = groupConsecutiveReposts(feed)
        assertEquals(2, groups.size)
        assertEquals(1, groups[0].reposters.size)
        assertEquals(0, groups[1].reposters.size)
    }

    @Test
    fun regularFeedPassesThroughUnchanged() {
        val feed = listOf(regularPost("a", "u1"), regularPost("b", "u2"), regularPost("c", "u3"))
        val groups = groupConsecutiveReposts(feed)
        assertEquals(feed.map { it.id }, groups.map { it.primary.id })
        assertTrue(groups.all { it.reposters.isEmpty() })
    }

    @Test
    fun headerText_singleReposter() {
        val text = repostHeaderText(listOf(pureRepost("r1", "joshatticus", target)))
        assertEquals("@joshatticus reposted this", text)
    }

    @Test
    fun headerText_twoReposters() {
        val text = repostHeaderText(
            listOf(pureRepost("r1", "joshatticus", target), pureRepost("r2", "ethernet", target))
        )
        assertEquals("@joshatticus and @ethernet reposted this", text)
    }

    @Test
    fun headerText_duplicateNamesAreDeduplicated() {
        // e.g. the same wrapper post appearing twice in the feed
        val text = repostHeaderText(
            listOf(pureRepost("r1", "ethernet", target), pureRepost("r2", "Ethernet", target))
        )
        assertEquals("@ethernet reposted this", text)
    }

    @Test
    fun headerText_threePlusUsesOthers() {
        val text = repostHeaderText(
            listOf(
                pureRepost("r1", "joshatticus", target),
                pureRepost("r2", "ethernet", target),
                pureRepost("r3", "jeffalo", target)
            )
        )
        assertEquals("@joshatticus and 2 others reposted this", text)
    }
}

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

/**
 * Tests for first-link extraction used by OpenGraph previews: the earliest
 * external link in a post wins, wasteof frontends are skipped.
 */
class OpenGraphLinkTest {

    @Test
    fun firstAnchorHrefWins() {
        val html = "<p><a href=\"https://b.site\">x</a> and https://a.site later</p>"
        assertEquals("https://b.site", wombat.joshattic.us.ui.screens.extractFirstLink(html))
    }

    @Test
    fun bareUrlInTextIsFound() {
        val html = "<p><u>https://blog.joshattic.us/posts/2026-08-19-post</u></p>"
        assertEquals("https://blog.joshattic.us/posts/2026-08-19-post", wombat.joshattic.us.ui.screens.extractFirstLink(html))
    }

    @Test
    fun skipsWasteofMoney() {
        val html = "<p><a href=\"https://wasteof.money/posts/123\">post</a> see https://real.site/page</p>"
        assertEquals("https://real.site/page", wombat.joshattic.us.ui.screens.extractFirstLink(html))
    }

    @Test
    fun skipsAllWasteofFrontends() {
        for (host in listOf("alpha.wasteof.money", "worm.eris.cafe", "wasteof.eris.cafe", "www.wasteof.money", "beta.wasteof.money")) {
            val html = "<p><a href=\"https://$host/x\">y</a> https://ok.site</p>"
            assertEquals(host, "https://ok.site", wombat.joshattic.us.ui.screens.extractFirstLink(html))
        }
    }

    @Test
    fun returnsNullWhenOnlySkippedHosts() {
        assertNull(wombat.joshattic.us.ui.screens.extractFirstLink("<p><a href=\"https://wasteof.money/posts/1\">a</a></p>"))
    }

    @Test
    fun returnsNullWhenNoLinks() {
        assertNull(wombat.joshattic.us.ui.screens.extractFirstLink("<p>just text, no links</p>"))
    }

    @Test
    fun trailingPunctuationIsTrimmed() {
        assertEquals("https://example.com", wombat.joshattic.us.ui.screens.extractFirstLink("<p>see https://example.com.</p>"))
    }

    @Test
    fun wombatSchemeIsIgnored() {
        assertNull(wombat.joshattic.us.ui.screens.extractFirstLink("<p><a href=\"wombat://user/josh\">@josh</a></p>"))
    }

    @Test
    fun ogApiResponseParses() {
        val json = """
            {"requested_url":"https://github.com","status_code":200,"fetched_at_unix":1797772800,
             "metadata":{"title":"GitHub","description":"desc","image":"https://img","url":"https://github.com"},
             "cached":false}
        """.trimIndent()
        val parsed = Gson().fromJson(json, OpenGraphResponse::class.java)
        assertEquals("GitHub", parsed.metadata?.title)
        assertEquals("desc", parsed.metadata?.description)
        assertEquals("https://img", parsed.metadata?.image)
        assertEquals(200, parsed.statusCode)
        assertEquals(false, parsed.cached)
    }
}

/**
 * Tests for the link preview priority helpers that decide whether OpenGraph
 * previews render large above post images.
 */
class LinkPreviewPriorityTest {

    @Test
    fun openGraphPriorityOnlyForOpengraphValue() {
        assertTrue(wombat.joshattic.us.ui.components.isOpenGraphPriority("opengraph"))
        assertFalse(wombat.joshattic.us.ui.components.isOpenGraphPriority("images"))
        assertFalse(wombat.joshattic.us.ui.components.isOpenGraphPriority(""))
        assertFalse(wombat.joshattic.us.ui.components.isOpenGraphPriority(null))
    }

    @Test
    fun openGraphPriorityIsCaseInsensitive() {
        assertTrue(wombat.joshattic.us.ui.components.isOpenGraphPriority("OpenGraph"))
        assertTrue(wombat.joshattic.us.ui.components.isOpenGraphPriority("OPENGRAPH"))
    }

    @Test
    fun largePreviewNeedsPreviewImage() {
        assertFalse(
            wombat.joshattic.us.ui.components.shouldUseLargePreview(
                hasPreviewImage = false, hasPostImages = false, openGraphFirst = true
            )
        )
        assertFalse(
            wombat.joshattic.us.ui.components.shouldUseLargePreview(
                hasPreviewImage = false, hasPostImages = true, openGraphFirst = true
            )
        )
    }

    @Test
    fun largePreviewWithoutPostImagesIsDefault() {
        // A lone link preview is always rendered large, regardless of setting.
        assertTrue(
            wombat.joshattic.us.ui.components.shouldUseLargePreview(
                hasPreviewImage = true, hasPostImages = false, openGraphFirst = false
            )
        )
        assertTrue(
            wombat.joshattic.us.ui.components.shouldUseLargePreview(
                hasPreviewImage = true, hasPostImages = false, openGraphFirst = true
            )
        )
    }

    @Test
    fun postImagesBeatPreviewUnlessOpengraphFirst() {
        // Default "images" priority keeps post images dominant.
        assertFalse(
            wombat.joshattic.us.ui.components.shouldUseLargePreview(
                hasPreviewImage = true, hasPostImages = true, openGraphFirst = false
            )
        )
        // "opengraph" priority promotes the preview over post images.
        assertTrue(
            wombat.joshattic.us.ui.components.shouldUseLargePreview(
                hasPreviewImage = true, hasPostImages = true, openGraphFirst = true
            )
        )
    }
}
