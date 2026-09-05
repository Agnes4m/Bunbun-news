package moe.bunbun.news.data.summary

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地摘要（基于 MediaPipe LLM Inference + Gemma 2B 模型，v0.2 主题 D 子 2）。
 *
 * 当前实现是 **占位**：返回 null 并记日志，等 v0.2.x 阶段：
 * 1. 接入 com.google.mediapipe:tasks-genai:0.x
 * 3. 在 Application 首次启动时把 .task 文件从 assets/ 拷到 cacheDir
 * 4. 实现真正的 generateResponse(prompt) 调用
 *
 * 之所以先提交 stub：让 SummaryProvider 抽象有第二种实现可注入，
 * 切换逻辑（云端 vs 本地）可以在 SettingsScreen 上做。
 */
@Singleton
class LocalSummaryProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : SummaryProvider {

    override val label: String = "本地 Gemma 2B（占位）"

    override suspend fun summarize(title: String, body: String): String? {
        Timber.tag("Summary").d("LocalSummaryProvider 占位实现，未实际推理")
        // v0.2.x 实现：
        // 1. 用 SummaryPromptBuilder.build(title, body) 构造 prompt
        // 2. mediaPipeLlm.generateResponse(prompt.system + "\n" + prompt.user)
        // 3. return 结果.trim()
        // 当前直接返回 null，由调用方按"无摘要"渲染
        return null
    }
}