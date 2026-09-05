package moe.bunbun.news.data.summary

/**
 * 把文章标题 + 正文文本组装成给云端 LLM 的 prompt。
 *
 * 设计原则：
 * - 显式长度上限（截断正文）以节省 token 和避开 4k 输入限制
 * - 多语言倾向：正文是中文就用中文 prompt，纯英文就用英文 prompt，混合时用中文
 * - 提示词带"用 1-2 句话总结事实"约束，避免输出"这段文字讨论了..."等废话
 */
object SummaryPromptBuilder {

    /** 中文 prompt 模板：system + user */
    const val ZH_SYSTEM = "你是一个简洁的中文新闻摘要助手。用一句话总结文章的核心事实，不超过 60 字。"

    /** 英文 prompt 模板 */
    const val EN_SYSTEM = "You are a concise news summarizer. Reply with one sentence under 40 words."

    /** 正文截断字符数（中文 1 字 = 1 字符） */
    const val MAX_BODY_CHARS = 2_000

    data class Prompt(val system: String, val user: String)

    fun build(title: String, body: String, maxBodyChars: Int = MAX_BODY_CHARS): Prompt {
        val trimmedBody = body.take(maxBodyChars)
        val lang = detectLanguage(title + trimmedBody)
        val (system, header) = if (lang == Lang.ENGLISH) {
            EN_SYSTEM to "Summarize the following news article in 1-2 sentence(s):"
        } else {
            ZH_SYSTEM to "请用 1-2 句话总结以下新闻的核心事实："
        }
        val user = buildString {
            appendLine("标题：$title")
            if (trimmedBody.isNotBlank()) {
                appendLine()
                append(trimmedBody)
            }
        }
        return Prompt(system = system, user = "$header\n\n$user")
    }

    enum class Lang { CHINESE, ENGLISH, MIXED }

    /** 简易语言识别：按 Unicode 范围统计 CJK 字符占比 */
    fun detectLanguage(text: String): Lang {
        if (text.isEmpty()) return Lang.CHINESE
        var cjk = 0
        var ascii = 0
        for (ch in text) {
            when {
                ch.code in 0x4E00..0x9FFF || ch.code in 0x3400..0x4DBF -> cjk++
                ch.code in 0x20..0x7E -> ascii++
            }
        }
        if (cjk == 0 && ascii == 0) return Lang.MIXED
        if (cjk == 0) return Lang.ENGLISH
        if (ascii == 0) return Lang.CHINESE
        // 两者都非零 → MIXED（混合文章用中文 prompt 更友好）
        return Lang.MIXED
    }
}