package moe.bunbun.news.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun `strips utm tracking params`() {
        val result = UrlNormalizer.normalize(
            "https://example.com/article?id=123&utm_source=twitter&utm_medium=social"
        )
        assertEquals("https://example.com/article?id=123", result)
    }

    @Test
    fun `forces https from http`() {
        val result = UrlNormalizer.normalize("http://example.com/path")
        assertEquals("https://example.com/path", result)
    }

    @Test
    fun `removes www prefix`() {
        val result = UrlNormalizer.normalize("https://www.example.com/path")
        assertEquals("https://example.com/path", result)
    }

    @Test
    fun `removes trailing slash`() {
        val result = UrlNormalizer.normalize("https://example.com/path/")
        assertEquals("https://example.com/path", result)
    }

    @Test
    fun `removes fragment`() {
        val result = UrlNormalizer.normalize("https://example.com/path#section-1")
        assertEquals("https://example.com/path", result)
    }

    @Test
    fun `strips multiple tracking params`() {
        val result = UrlNormalizer.normalize(
            "https://example.com/article?utm_source=tw&fbclid=abc123&gclid=xyz&id=42"
        )
        assertEquals("https://example.com/article?id=42", result)
    }

    @Test
    fun `sorts query params for stability`() {
        val a = UrlNormalizer.normalize("https://example.com?b=2&a=1&c=3")
        val b = UrlNormalizer.normalize("https://example.com?c=3&a=1&b=2")
        assertEquals(a, b)
        assertEquals("https://example.com?a=1&b=2&c=3", a)
    }

    @Test
    fun `lowercases host`() {
        val result = UrlNormalizer.normalize("https://EXAMPLE.COM/Path")
        assertEquals("https://example.com/Path", result)
    }

    @Test
    fun `strips default ports`() {
        assertEquals(
            "https://example.com/path",
            UrlNormalizer.normalize("https://example.com:443/path"),
        )
        assertEquals(
            "https://example.com/path",
            UrlNormalizer.normalize("http://example.com:80/path"),
        )
    }

    @Test
    fun `keeps non-default ports`() {
        assertEquals(
            "https://example.com:8080/path",
            UrlNormalizer.normalize("https://example.com:8080/path"),
        )
    }

    @Test
    fun `same article with different trackers should normalize to same form`() {
        val a = UrlNormalizer.normalize("https://example.com/news?id=1&utm_source=twitter")
        val b = UrlNormalizer.normalize("https://example.com/news?id=1&utm_campaign=email")
        assertEquals(a, b)
    }

    @Test
    fun `different articles stay different`() {
        val a = UrlNormalizer.normalize("https://example.com/news?id=1")
        val b = UrlNormalizer.normalize("https://example.com/news?id=2")
        assertNotEquals(a, b)
    }

    @Test
    fun `stableHash is deterministic`() {
        val h1 = UrlNormalizer.stableHash("hello")
        val h2 = UrlNormalizer.stableHash("hello")
        assertEquals(h1, h2)
        assertTrue(h1.isNotEmpty())
    }

    @Test
    fun `stableHash differs for different inputs`() {
        assertNotEquals(
            UrlNormalizer.stableHash("hello"),
            UrlNormalizer.stableHash("world"),
        )
    }

    @Test
    fun `empty input returns empty`() {
        assertEquals("", UrlNormalizer.normalize(""))
        assertEquals("   ", UrlNormalizer.normalize("   "))
    }

    @Test
    fun `invalid URL falls back to original`() {
        val garbage = "not a url at all"
        assertEquals(garbage, UrlNormalizer.normalize(garbage))
    }
}