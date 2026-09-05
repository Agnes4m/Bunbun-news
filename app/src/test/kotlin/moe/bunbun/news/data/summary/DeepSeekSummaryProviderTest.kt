package moe.bunbun.news.data.summary

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class DeepSeekSummaryProviderTest {

    private val api = mockk<DeepSeekChatApi>()
    private var currentKey: String? = "test-key"
    private val provider = DeepSeekSummaryProvider(
        api = api,
        apiKeyProvider = { currentKey },
    )

    @Test
    fun `summarize returns first choice content on success`() = runTest {
        val resp = DeepSeekChatResponse(
            choices = listOf(
                DeepSeekChoice(message = DeepSeekMessage("assistant", "  这是摘要  ")),
            ),
        )
        coEvery {
            api.chat(authorization = "Bearer test-key", request = any())
        } returns resp

        val out = provider.summarize("标题", "正文")
        assertEquals("这是摘要", out)
    }

    @Test
    fun `summarize returns null when no api key`() = runTest {
        currentKey = null
        assertNull(provider.summarize("标题", "正文"))
        // 没调 API
        coVerify(exactly = 0) { api.chat(any(), any()) }
        currentKey = "test-key" // 恢复
    }

    @Test
    fun `summarize returns null on blank api key`() = runTest {
        currentKey = "   "
        assertNull(provider.summarize("标题", "正文"))
        coVerify(exactly = 0) { api.chat(any(), any()) }
        currentKey = "test-key"
    }

    @Test
    fun `summarize returns null on empty response content`() = runTest {
        val resp = DeepSeekChatResponse(
            choices = listOf(DeepSeekChoice(message = DeepSeekMessage("assistant", ""))),
        )
        coEvery { api.chat(any(), any()) } returns resp
        assertNull(provider.summarize("标题", "正文"))
    }

    @Test
    fun `summarize returns null on HttpException`() = runTest {
        coEvery { api.chat(any(), any()) } throws HttpException(
            Response.error<Any>(500, "boom".toResponseBody()),
        )
        assertNull(provider.summarize("标题", "正文"))
    }

    @Test
    fun `summarize returns null on IOException`() = runTest {
        coEvery { api.chat(any(), any()) } throws IOException("network down")
        assertNull(provider.summarize("标题", "正文"))
    }

    @Test
    fun `summarize sends system + user messages with correct roles`() = runTest {
        val resp = DeepSeekChatResponse(
            choices = listOf(DeepSeekChoice(message = DeepSeekMessage("assistant", "OK"))),
        )
        coEvery { api.chat(any(), any()) } returns resp
        provider.summarize("标题", "正文")

        coVerify {
            api.chat(
                authorization = "Bearer test-key",
                request = match { req ->
                    req.model == "deepseek-chat" &&
                        req.messages.size == 2 &&
                        req.messages[0].role == "system" &&
                        req.messages[1].role == "user" &&
                        req.messages[1].content.contains("标题")
                },
            )
        }
    }

    @Test
    fun `summarize calls api for each invocation`() = runTest {
        val resp = DeepSeekChatResponse(
            choices = listOf(DeepSeekChoice(message = DeepSeekMessage("assistant", "ok"))),
        )
        coEvery { api.chat(any(), any()) } returns resp
        provider.summarize("a", "b")
        provider.summarize("c", "d")
        coVerify(exactly = 2) { api.chat(any(), any()) }
    }

    @Test
    fun `label includes model name`() {
        assertTrue(provider.label.contains("DeepSeek"))
    }

    @Test
    fun `current apiKey is read on every call`() = runTest {
        val resp = DeepSeekChatResponse(
            choices = listOf(DeepSeekChoice(message = DeepSeekMessage("assistant", "ok"))),
        )
        coEvery { api.chat(any(), any()) } returns resp
        currentKey = "k1"
        provider.summarize("a", "b")
        currentKey = "k2"
        provider.summarize("c", "d")
        coVerify { api.chat(authorization = "Bearer k1", request = any()) }
        coVerify { api.chat(authorization = "Bearer k2", request = any()) }
    }
}

private fun String.toResponseBody() = okhttp3.ResponseBody.create(null, this)