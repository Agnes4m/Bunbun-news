package moe.bunbun.news.data.summary

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import moe.bunbun.news.data.prefs.SummaryProviderType
import moe.bunbun.news.data.prefs.UserPreferences
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SummaryProviderFactoryTest {

    private fun prefs(
        type: SummaryProviderType,
        deepseekKey: String? = null,
    ): UserPreferences = mockk {
        coEvery { summaryProvider } returns flowOf(type)
        coEvery { deepseekApiKey } returns flowOf(deepseekKey)
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder().build()

    /** 工厂实际不读 context（除非 LOCAL provider）；用 null 占位即可 */
    private fun factory(prefs: UserPreferences) = SummaryProviderFactory(
        context = mockk(relaxed = true),
        prefs = prefs,
        okHttpClient = okHttp,
    )

    @Test
    fun `OFF always returns null`() = runTest {
        val out = factory(prefs(SummaryProviderType.OFF)).summarize("t", "b")
        assertNull(out)
    }

    @Test
    fun `DEEPSEEK without api key returns null`() = runTest {
        val out = factory(prefs(SummaryProviderType.DEEPSEEK, deepseekKey = null))
            .summarize("t", "b")
        assertNull(out)
    }

    @Test
    fun `DEEPSEEK with blank api key returns null`() = runTest {
        val out = factory(prefs(SummaryProviderType.DEEPSEEK, deepseekKey = "   "))
            .summarize("t", "b")
        assertNull(out)
    }

    @Test
    fun `DEEPSEEK with api key attempts network and surfaces failures as null`() = runTest {
        // OkHttp 真实发起 HTTP 会失败（无 server），由 DeepSeekSummaryProvider 内部
        // catch IOException 返回 null；这里只确认路径走到 provider 且不抛异常
        val out = factory(prefs(SummaryProviderType.DEEPSEEK, deepseekKey = "sk-fake"))
            .summarize("t", "b")
        assertNull("无服务器应返回 null（不抛）", out)
    }

    @Test
    fun `LOCAL returns null because provider is still stub`() = runTest {
        val out = factory(prefs(SummaryProviderType.LOCAL)).summarize("t", "b")
        // 占位 provider 当前返回 null；v0.2.x 接 MediaPipe 后真实推理
        assertNull(out)
    }

    @Test
    fun `factory produces non-null provider instance via label field`() {
        // label 是 router 的标识，每次读不依赖 prefs
        val factory1 = factory(prefs(SummaryProviderType.OFF))
        assertNotNull(factory1.label)
        assertEquals("router", factory1.label)
    }
}