package moe.bunbun.news.data.summary

/**
 * 摘要提供方抽象（云端 / 本地）。
 *
 * 实现：
 * - DeepSeekSummaryProvider：云端 DeepSeek API（v0.2 主题 D 子 1 默认）
 * - LocalSummaryProvider：本地 MediaPipe Gemma 2B（v0.2 主题 D 子 2 占位）
 *
 * 调用方（ReaderScreen / SummaryWorker）通过 provider.summarize(title, body) 拿到
 * 摘要字符串；任何失败（网络、超时、解析）一律返回 null，让调用方按"无摘要"渲染。
 */
interface SummaryProvider {
    /**
     * 给文章生成一句话摘要。
     * @return 摘要字符串；null 表示生成失败/不可用
     */
    suspend fun summarize(title: String, body: String): String?

    /** 标签，用于日志和设置面板显示当前 provider 类型 */
    val label: String
}