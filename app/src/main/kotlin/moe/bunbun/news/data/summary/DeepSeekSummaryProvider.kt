package moe.bunbun.news.data.summary

import moe.bunbun.news.data.summary.SummaryPromptBuilder.build
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * 基于 DeepSeek Chat Completions 的云端摘要器。
 *
 * 设计要点：
 * - 每个请求都从 apiKeyProvider() 取最新 key（用户改设置后下次即生效）
 * - 不抛异常：HttpException / IOException / 解析失败 → 返回 null
 * - 把 prompt 拼接交给 SummaryPromptBuilder，便于多语言扩展
 */
class DeepSeekSummaryProvider(
    private val api: DeepSeekChatApi,
    private val apiKeyProvider: () -> String?,
    private val model: String = DeepSeekClient.DEFAULT_MODEL,
) : SummaryProvider {

    override val label: String = "DeepSeek ($model)"

    override suspend fun summarize(title: String, body: String): String? {
        val key = apiKeyProvider()?.takeIf { it.isNotBlank() } ?: run {
            Timber.tag("Summary").w("DeepSeek 未配置 apiKey")
            return null
        }
        val prompt = build(title, body)
        val request = DeepSeekChatRequest(
            model = model,
            messages = listOf(
                DeepSeekMessage(role = "system", content = prompt.system),
                DeepSeekMessage(role = "user", content = prompt.user),
            ),
            temperature = 0.3,
            max_tokens = 200,
            stream = false,
        )
        return try {
            val resp = api.chat(authorization = "Bearer $key", request = request)
            val content = resp.firstContent()
            if (content.isBlank()) null else content
        } catch (e: HttpException) {
            Timber.tag("Summary").w(e, "DeepSeek HTTP ${e.code()}")
            null
        } catch (e: IOException) {
            Timber.tag("Summary").w(e, "DeepSeek network error")
            null
        } catch (t: Throwable) {
            Timber.tag("Summary").w(t, "DeepSeek unknown error")
            null
        }
    }
}