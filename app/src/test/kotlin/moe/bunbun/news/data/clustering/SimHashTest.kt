package moe.bunbun.news.data.clustering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class SimHashTest {

    @Test
    fun `empty text returns zero hash`() {
        assertEquals(0L, SimHash.hash(""))
        assertEquals(0L, SimHash.hash("   "))
    }

    @Test
    fun `same text produces same hash`() {
        assertEquals(SimHash.hash("hello world"), SimHash.hash("hello world"))
        assertEquals(SimHash.hash("华为发布新手机"), SimHash.hash("华为发布新手机"))
    }

    @Test
    fun `different text produces different hash`() {
        assertNotEquals(SimHash.hash("hello world"), SimHash.hash("hello earth"))
        assertNotEquals(SimHash.hash("华为手机"), SimHash.hash("苹果电脑"))
    }

    @Test
    fun `chinese similar titles have moderate hamming distance`() {
        // 中文短标题每个字一个 token，1 字差异就翻 ~32 位
        // v0.2 SimHash 在中文上只能区分"明显不同"，精细区分等 jieba 分词
        val h1 = SimHash.hash("华为发布新手机")
        val h2 = SimHash.hash("华为发布新款手机")
        val d = SimHash.hammingDistance(h1, h2)
        assertTrue("chinese similar distance should be less than 64 (not random), was $d", d < 50)
    }

    @Test
    fun `english similar titles have small hamming distance`() {
        val h1 = SimHash.hash("Tesla launches new car model")
        val h2 = SimHash.hash("Tesla launches new car vehicle")
        val d = SimHash.hammingDistance(h1, h2)
        assertTrue("english similar distance should be small, was $d", d <= 10)
    }

    @Test
    fun `different events have large hamming distance`() {
        val h1 = SimHash.hash("华为发布新手机 Mate 60 Pro")
        val h2 = SimHash.hash("苹果发布新款 MacBook 笔记本电脑")
        val d = SimHash.hammingDistance(h1, h2)
        assertTrue("hamming distance should be large for different topics, was $d", d >= 20)
    }

    @Test
    fun `hamming distance is symmetric`() {
        val h1 = SimHash.hash("新闻 A 报道事件 X")
        val h2 = SimHash.hash("新闻 B 报道事件 Y")
        assertEquals(
            SimHash.hammingDistance(h1, h2),
            SimHash.hammingDistance(h2, h1),
        )
    }

    @Test
    fun `isSimilar respects threshold`() {
        val h1 = SimHash.hash("事件一")
        val h2 = SimHash.hash("事件二")
        val d = SimHash.hammingDistance(h1, h2)
        // 不同事件，d 较大
        assertTrue("expected large distance, was $d", d > 3)
        assertFalse(SimHash.isSimilar(h1, h2, threshold = 3))
        // 用更大阈值则可能为 true
        assertTrue(SimHash.isSimilar(h1, h2, threshold = 64))
    }

    @Test
    fun `case insensitive`() {
        assertEquals(SimHash.hash("HELLO WORLD"), SimHash.hash("hello world"))
        assertEquals(SimHash.hash("Hello World"), SimHash.hash("hello world"))
    }

    @Test
    fun `chinese and english mix`() {
        // 中英混合分词（每个汉字 + 英文单词都是独立 token）
        val h1 = SimHash.hash("Apple 发布 iPhone 15")
        val h2 = SimHash.hash("Apple 推出 iPhone 15")  // "发布" vs "推出" 不同 → 不完全相似
        val d = SimHash.hammingDistance(h1, h2)
        // "发布" vs "推出" 是 1 token 差异，64-bit 中应该有少量差异
        assertTrue("distance $d should be reasonable", d in 1..30)
    }
}
