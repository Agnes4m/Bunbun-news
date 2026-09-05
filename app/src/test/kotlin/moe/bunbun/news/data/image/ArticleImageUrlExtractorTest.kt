package moe.bunbun.news.data.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleImageUrlExtractorTest {

    @Test
    fun `extracts absolute src images`() {
        val html = """<img src="https://cdn.example.com/a.jpg"><img src="https://cdn.example.com/b.png">"""
        val urls = ArticleImageUrlExtractor.extract(html)
        assertEquals(2, urls.size)
        assertTrue("https://cdn.example.com/a.jpg" in urls)
        assertTrue("https://cdn.example.com/b.png" in urls)
    }

    @Test
    fun `extracts lazy data-src when src missing`() {
        val html = """<img data-src="https://cdn.example.com/lazy.jpg" src="">"""
        val urls = ArticleImageUrlExtractor.extract(html)
        assertEquals(listOf("https://cdn.example.com/lazy.jpg"), urls)
    }

    @Test
    fun `resolves relative URLs against baseUrl`() {
        val html = """<img src="/uploads/2026/a.jpg">"""
        val urls = ArticleImageUrlExtractor.extract(html, baseUrl = "https://news.example.com/post/123")
        assertEquals(listOf("https://news.example.com/uploads/2026/a.jpg"), urls)
    }

    @Test
    fun `skips data and javascript URIs`() {
        val html = """
                <img src="data:image/png;base64,iVBORw0KGgo=">
                <img src="javascript:void(0)">
                <img src="https://cdn.example.com/real.jpg">
            """
        val urls = ArticleImageUrlExtractor.extract(html)
        assertEquals(listOf("https://cdn.example.com/real.jpg"), urls)
    }

    @Test
    fun `deduplicates identical URLs`() {
        val html = """
                <img src="https://cdn.example.com/a.jpg">
                <img src="https://cdn.example.com/a.jpg">
                <img data-src="https://cdn.example.com/a.jpg">
            """
        val urls = ArticleImageUrlExtractor.extract(html)
        assertEquals(1, urls.size)
    }

    @Test
    fun `respects max parameter`() {
        val html = (1..20).joinToString("") { "<img src=\"https://cdn.example.com/$it.jpg\">" }
        val urls = ArticleImageUrlExtractor.extract(html, max = 5)
        assertEquals(5, urls.size)
    }

    @Test
    fun `returns empty on null or blank html`() {
        assertTrue(ArticleImageUrlExtractor.extract(null).isEmpty())
        assertTrue(ArticleImageUrlExtractor.extract("").isEmpty())
        assertTrue(ArticleImageUrlExtractor.extract("   ").isEmpty())
    }

    @Test
    fun `returns empty on html without img tags`() {
        val urls = ArticleImageUrlExtractor.extract("<p>纯文本段落</p>")
        assertTrue(urls.isEmpty())
    }

    @Test
    fun `uses srcset first candidate when src missing`() {
        val html = """<img srcset="https://cdn.example.com/m.jpg 1x, https://cdn.example.com/h.jpg 2x">"""
        val urls = ArticleImageUrlExtractor.extract(html)
        assertEquals(listOf("https://cdn.example.com/m.jpg"), urls)
    }

    @Test
    fun `ignores relative urls when baseUrl is missing`() {
        val html = """<img src="/local/photo.jpg">"""
        // 没有 baseUrl，相对路径无法解析，应被忽略
        val urls = ArticleImageUrlExtractor.extract(html)
        assertTrue(urls.isEmpty())
    }
}