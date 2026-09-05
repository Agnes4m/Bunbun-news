package moe.bunbun.news.data.summarycache

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import moe.bunbun.news.data.summary.SummaryProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleSummarizerTest {

    private val provider = mockk<SummaryProvider>()
    private val cache = mockk<SummaryCacheRepository>(relaxUnitFun = true)
    private val summarizer = ArticleSummarizer(provider, cache)

    @Test
    fun `cache hit returns cached summary without calling provider`() = runTest {
        coEvery { cache.get("a1") } returns "已缓存摘要"
        val out = summarizer.summarize("a1", "title", "body")
        assertEquals("已缓存摘要", out)
        coVerify(exactly = 0) { provider.summarize(any(), any()) }
        coVerify(exactly = 0) { cache.put(any(), any(), any()) }
    }

    @Test
    fun `cache miss calls provider and stores result`() = runTest {
        coEvery { cache.get("a1") } returns null
        coEvery { provider.summarize("title", "body") } returns "新摘要"
        coEvery { provider.label } returns "DeepSeek"

        val out = summarizer.summarize("a1", "title", "body")

        assertEquals("新摘要", out)
        coVerifyOrder {
            cache.get("a1")
            provider.summarize("title", "body")
            cache.put("a1", "新摘要", "DeepSeek")
        }
    }

    @Test
    fun `provider returns null does not write cache`() = runTest {
        coEvery { cache.get("a1") } returns null
        coEvery { provider.summarize(any(), any()) } returns null

        val out = summarizer.summarize("a1", "title", "body")

        assertNull(out)
        coVerify(exactly = 0) { cache.put(any(), any(), any()) }
    }

    @Test
    fun `resummarize evicts cache first then re-fetches`() = runTest {
        coEvery { cache.get("a1") } returns null
        coEvery { provider.summarize("title", "body") } returns "v2"
        coEvery { provider.label } returns "Local"

        val out = summarizer.resummarize("a1", "title", "body")
        assertEquals("v2", out)

        coVerifyOrder {
            cache.evict("a1")
            cache.get("a1")
            provider.summarize("title", "body")
            cache.put("a1", "v2", "Local")
        }
    }

    @Test
    fun `resummarize always evicts cache`() = runTest {
        // 不管 cache.get 返回什么，resummarize 必须先 evict
        coEvery { cache.get("a1") } returns "旧值"
        coEvery { provider.summarize(any(), any()) } returns "新值"
        coEvery { provider.label } returns "DeepSeek"

        summarizer.resummarize("a1", "title", "body")

        // cache.evict 在 summarize 之前被调用（顺序由 mockk 验证）
        coVerifyOrder {
            cache.evict("a1")
            cache.get("a1")
        }
    }
}