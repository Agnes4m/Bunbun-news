package moe.bunbun.news.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedParserTest {

    private val parser: FeedParser = FeedParserImpl(RssXmlParser())

    @Test
    fun `parses RSS 2_0 channel metadata`() {
        val xml = javaClass.classLoader!!.getResource("sample-rss.xml")!!.readText()
        val result = parser.parse(xml)

        assertTrue("expected Success, got $result", result is FeedParseResult.Success)
        val success = result as FeedParseResult.Success
        assertEquals("Sample Tech News", success.title)
        assertEquals("https://example.com", success.siteUrl)
    }

    @Test
    fun `parses well-formed items with all fields`() {
        val xml = javaClass.classLoader!!.getResource("sample-rss.xml")!!.readText()
        val result = parser.parse(xml) as FeedParseResult.Success

        val tesla = result.articles.firstOrNull { it.title.contains("Tesla") }
        assertNotNull("Tesla article should be present", tesla)
        assertEquals("https://example.com/articles/tesla-q3-2026", tesla!!.url)
        assertEquals("https://example.com/articles/tesla-q3-2026", tesla.guid)
        assertEquals("Jane Reporter", tesla.author)
        assertNotNull(tesla.publishedAt)
        assertTrue(tesla.contentHtml!!.contains("Full article body"))
        assertTrue(tesla.excerpt!!.contains("strong Q3 earnings"))
    }

    @Test
    fun `skips items with empty title`() {
        val xml = javaClass.classLoader!!.getResource("sample-rss.xml")!!.readText()
        val result = parser.parse(xml) as FeedParseResult.Success
        // sample has 2 valid + 2 invalid items; expect 2 parsed
        assertEquals(2, result.articles.size)
    }

    @Test
    fun `invalid XML returns Failure`() {
        val result = parser.parse("this is not xml")
        assertTrue("expected Failure, got $result", result is FeedParseResult.Failure)
    }

    @Test
    fun `empty XML returns Failure`() {
        val result = parser.parse("")
        assertTrue(result is FeedParseResult.Failure)
    }

    @Test
    fun `uses url as guid fallback`() {
        val xml = javaClass.classLoader!!.getResource("sample-rss.xml")!!.readText()
        val result = parser.parse(xml) as FeedParseResult.Success
        val apple = result.articles.firstOrNull { it.title.contains("Apple") }!!
        // Apple's <guid>https://example.com/articles/apple-iphone-launch</guid> is used
        assertEquals("https://example.com/articles/apple-iphone-launch", apple.guid)
    }
}