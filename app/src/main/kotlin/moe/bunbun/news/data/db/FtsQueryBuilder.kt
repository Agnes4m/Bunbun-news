package moe.bunbun.news.data.db

/**
 * 把用户输入的搜索串转成 SQLite FTS4 MATCH 子句。
 *
 * 策略：
 * - 空串 → 永远返回空字符串（让上层 DAO 直接拿空结果）
 * - 含 ASCII 字母数字的 token → 加 `*` 后缀做前缀匹配（"rss" → "rss*"）
 * - 纯中文/全角字符 → 保持原样（unicode61 分词按字切，前缀匹配对中文意义不大）
 * - 引号短语（"ip ad*"）原样透传
 *
 * 多个 token 用空格 AND；最后返回的串不带外引号，让上层直接拼到 SQL 模板里。
 */
object FtsQueryBuilder {

    fun build(rawQuery: String): String {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return ""

        // 整段是引号短语 → 原样（split 不会拆开它）
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) return trimmed

        val tokens = trimmed.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map(::tokenToFts)

        return tokens.joinToString(" ")
    }

    private fun tokenToFts(token: String): String {
        // 用户主动用通配符 → 原样（不再自动加 *）
        if ('*' in token || '?' in token) return token
        // 纯中文 / 全角字符 → 原样（unicode61 已按字分词，前缀通配对中文意义不大）
        if (token.none { it.isLetterOrDigit() && it.code < 128 }) return token
        // 拉丁 / 数字 token → 加前缀通配符
        return "$token*"
    }
}