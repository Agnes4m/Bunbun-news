package moe.bunbun.news.data.readability

import net.dankito.readability4j.Readability4J

/**
 * 包装 Readability4J，把任意 HTML 抽取成结构化结果（标题 / 摘要 / 正文 HTML / 纯文本）。
 *
 * 设计要点：
 * - 同步阻塞调用；调用方应在 IO 线程执行（WorkManager / Dispatchers.IO）。
 * - 任何解析失败（HTML 过短 / 无 <article> / 抛出异常）一律返回 null，调用方按原文 RSS 的 excerpt 兜底。
 * - URL 可空；空 URL 时跳过 URL 相关的归一化逻辑，Readability4J 内部对此安全。
 */
object ArticleContentExtractor {

    data class Result(
        val title: String?,
        val excerpt: String?,
        val contentHtml: String?,
        val plainText: String?,
    ) {
        val hasContent: Boolean
            get() = !contentHtml.isNullOrBlank()
    }

    fun extract(html: String?, url: String?): Result? {
        if (html.isNullOrBlank()) return null
        return try {
            // Readability4J 的 uri 构造参数非空；空 URL 时传空串即可
            val parser = Readability4J(url ?: "", html)
            val article = parser.parse() ?: return null
            Result(
                title = article.title,
                excerpt = article.excerpt,
                contentHtml = article.content,
                plainText = article.textContent,
            )
        } catch (t: Throwable) {
            null
        }
    }
}