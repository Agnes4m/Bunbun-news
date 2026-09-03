package moe.bunbun.news.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpmlImporterTest {

    private val importer = OpmlImporter()

    @Test
    fun `extracts feed URLs from OPML`() {
        val stream = javaClass.classLoader!!.getResource("sample.opml")!!.openStream()
        val feeds = importer.extractFeedUrls(stream)
        assertEquals(3, feeds.size)
        assertTrue(feeds.any { it.url == "https://techcrunch.com/feed/" && it.title == "TechCrunch" })
        assertTrue(feeds.any { it.url == "https://hnrss.org/frontpage" && it.title == "Hacker News" })
        assertTrue(feeds.any { it.url == "https://www.theverge.com/rss/index.xml" && it.title == "The Verge" })
    }

    @Test
    fun `skips outline without xmlUrl`() {
        // "Empty Folder" outline should be excluded
        val stream = javaClass.classLoader!!.getResource("sample.opml")!!.openStream()
        val feeds = importer.extractFeedUrls(stream)
        assertTrue(feeds.none { it.title == "Empty Folder" })
    }

    @Test
    fun `handles empty stream gracefully`() {
        val feeds = importer.extractFeedUrls("".byteInputStream())
        assertEquals(0, feeds.size)
    }

    @Test
    fun `handles malformed XML gracefully`() {
        val feeds = importer.extractFeedUrls("not valid xml".byteInputStream())
        assertEquals(0, feeds.size)
    }

    @Test
    fun `accepts both rss and atom types`() {
        val stream = javaClass.classLoader!!.getResource("sample.opml")!!.openStream()
        val feeds = importer.extractFeedUrls(stream)
        assertTrue(feeds.any { it.url.contains("techcrunch") }) // type=rss
        assertTrue(feeds.any { it.url.contains("hnrss") })    // type=atom
    }
}