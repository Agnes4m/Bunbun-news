package moe.bunbun.news.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClusterEngineTest {

    private fun id(title: String, url: String = "https://example.com/$title") =
        ClusterEngine.computeClusterId(title, url)

    @Test
    fun `same title produces same clusterId`() {
        val a = id("Tesla announces Q3 earnings")
        val b = id("Tesla announces Q3 earnings")
        assertEquals(a, b)
    }

@Test
    fun `same wording order variations cluster identically`() {
        // v0.1 简化版：不做词干提取，"announces" 和 "announced" 算不同 token
        // 这里测的是词序重排后核心词相同（同词形）的聚类
        val a = id("earnings beat tesla q3", "https://a.com")
        val b = id("tesla q3 earnings beat", "https://b.com")
        assertEquals(a, b)
    }

    @Test
    fun `different topics get different clusterIds`() {
        val a = id("Tesla Q3 earnings beat", "https://a.com/1")
        val b = id("Apple iPhone launch event", "https://b.com/1")
        assertNotEquals(a, b)
    }

    @Test
    fun `stopwords are filtered out`() {
        // The / of / and 等停用词不应影响聚类
        val a = id("The Tesla earnings of Q3")
        val b = id("Tesla earnings Q3")
        assertEquals(a, b)
    }

    @Test
    fun `chinese titles with identical content cluster`() {
        // 字符顺序不影响（top 6 sorted 后相同）
        val a = id("特斯拉 Q3 财报", "https://a.cn/1")
        val b = id("Q3 财报 特斯拉", "https://b.cn/2")
        assertEquals(a, b)
    }

    @Test
    fun `different chinese topics get different clusterIds`() {
        val a = id("特斯拉 Q3 财报超预期", "https://a.cn/1")
        val b = id("苹果发布会秋季新品", "https://b.cn/2")
        assertNotEquals(a, b)
    }

    @Test
    fun `empty title falls back to URL hash`() {
        val a = ClusterEngine.computeClusterId("", "https://example.com/article/123")
        val b = ClusterEngine.computeClusterId("", "https://example.com/article/123")
        assertEquals(a, b)
    }

    @Test
    fun `empty title with different URLs get different clusterIds`() {
        val a = ClusterEngine.computeClusterId("", "https://example.com/article/1")
        val b = ClusterEngine.computeClusterId("", "https://example.com/article/2")
        assertNotEquals(a, b)
    }

    @Test
    fun `clusterId is stable across runs`() {
        // 多次调用产生相同 ID（无随机状态）
        val ids = List(10) { id("Tesla earnings Q3 2026", "https://x.com/1") }
        assertEquals(1, ids.toSet().size)
    }

    @Test
    fun `identical core titles always cluster`() {
        // 两条标题只有尾部一字之差，但排序后取 top 6 就会不同——v0.1 不做模糊匹配
        // 这里测的是：完全相同的核心词 → 必须相同 clusterId
        val a = id("Tesla Q3 earnings beat", "https://a.com")
        val b = id("Tesla Q3 earnings beat", "https://b.com")
        assertEquals(a, b)
    }

    @Test
    fun `jaccard similarity of identical sets is 1`() {
        val a = listOf("tesla", "earnings", "q3")
        assertEquals(1.0, ClusterEngine.jaccardSimilarity(a, a), 0.0001)
    }

    @Test
    fun `jaccard similarity of disjoint sets is 0`() {
        val a = listOf("tesla", "earnings")
        val b = listOf("apple", "iphone")
        assertEquals(0.0, ClusterEngine.jaccardSimilarity(a, b), 0.0001)
    }

    @Test
    fun `jaccard similarity of partial overlap`() {
        val a = listOf("tesla", "earnings", "q3")
        val b = listOf("tesla", "earnings", "growth")
        // 交集 2，并集 4 → 0.5
        assertEquals(0.5, ClusterEngine.jaccardSimilarity(a, b), 0.0001)
    }
}