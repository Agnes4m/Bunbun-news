package moe.bunbun.news.data.summary

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import moe.bunbun.news.data.prefs.SummaryProviderType
import moe.bunbun.news.data.prefs.UserPreferences
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 摘要 provider 工厂 + 路由器（v0.2 主题 D 子 4）。
 *
 * 实现 SummaryProvider 接口本身，每次 summarize 都重新读 prefs：
 * - OFF → NoOpSummaryProvider（始终返回 null，缓存也不会写）
 * - DEEPSEEK + 有 apiKey → DeepSeekSummaryProvider(每次都按 key 重建 Api 实例)
 * - DEEPSEEK + 无 apiKey → NoOpSummaryProvider（让用户先去设置填 key）
 * - LOCAL → LocalSummaryProvider（占位，v0.2.x 接 MediaPipe）
 *
 * 性能：每次调用 DeepSeekSummaryProvider 都会 new 一个 Retrofit。DeepSeek 调用
 * 是低频（用户打开 ReaderScreen），可接受。等 v0.2.x 高频调用时改成缓存 DeepSeekApi。
 */
@Singleton
class SummaryProviderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: UserPreferences,
    private val okHttpClient: OkHttpClient,
) : SummaryProvider {

    override val label: String
        get() = "router" // label 由具体 provider 提供；router 不暴露

    override suspend fun summarize(title: String, body: String): String? {
        val provider = current()
        return provider.summarize(title, body)
    }

    private suspend fun current(): SummaryProvider {
        val type = prefs.summaryProvider.first()
        return when (type) {
            SummaryProviderType.OFF -> NoOpSummaryProvider
            SummaryProviderType.DEEPSEEK -> {
                val key = prefs.deepseekApiKey.first()
                if (!key.isNullOrBlank()) {
                    val api = DeepSeekClient.create(apiKeyProvider = { key }, okHttpClient = okHttpClient)
                    DeepSeekSummaryProvider(api = api, apiKeyProvider = { key })
                } else {
                    NoOpSummaryProvider
                }
            }
            SummaryProviderType.LOCAL -> LocalSummaryProvider(context)
        }
    }
}

/** Provider 关闭 / 未配置：永远返回 null，让 ArticleSummarizer 写"无摘要"路径 */
object NoOpSummaryProvider : SummaryProvider {
    override val label: String = "关闭"
    override suspend fun summarize(title: String, body: String): String? = null
}