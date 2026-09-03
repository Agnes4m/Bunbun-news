package moe.bunbun.news.data.rss

/**
 * 事件聚类引擎（v0.1 简化版）。
 *
 * 算法：
 * 1. 把标题归一化（小写、去标点、英文按词、中文按字符）
 * 2. 去掉停用词
 * 3. 取最具代表性的前 N 个 token（按字母序排序保证稳定）
 * 4. clusterId = stableHash("t:" + sortedTokens.joinToString("|"))
 *
 * 同一事件的报道（标题核心词相同）会得到相同 clusterId。
 *
 * 局限：仅基于标题词，v0.3 升级 SimHash + 时间窗 + URL 加权。
 */
object ClusterEngine {

    private const val MAX_TOKENS = 6

    // 极简停用词表（仅中英文高频词）
    private val STOPWORDS: Set<String> = setOf(
        "a", "an", "the", "and", "or", "of", "to", "in", "on", "at", "by", "for",
        "is", "are", "was", "were", "be", "been", "being",
        "with", "from", "as", "this", "that", "it", "its",
        "的", "了", "在", "是", "和", "与", "或", "也", "都", "就", "对",
        "这", "那", "我", "你", "他", "她", "它", "们",
    )

    /** 计算 clusterId。空标题或纯停用词时退化为 URL hash */
    fun computeClusterId(title: String, url: String): String {
        val tokens = tokenize(title)
            .filter { it !in STOPWORDS }
            .distinct()

        val representative = if (tokens.isEmpty()) {
            // 标题没法用 → 退化为 URL 签名（只能处理完全同 URL 的聚合）
            listOf("u:" + UrlNormalizer.normalize(url))
        } else {
            // 取前 N 个 token，按字母序排序后拼接（保证稳定）
            tokens.sorted().take(MAX_TOKENS)
        }

        return UrlNormalizer.stableHash("t:" + representative.joinToString("|"))
    }

    /** 计算两个 clusterId 之间的相似度（用于 v0.3 升级 SimHash 后做近邻搜索） */
    @Suppress("unused")
    fun jaccardSimilarity(a: List<String>, b: List<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val sa = a.toSet()
        val sb = b.toSet()
        val intersect = (sa intersect sb).size
        val union = (sa union sb).size
        return if (union == 0) 0.0 else intersect.toDouble() / union
    }

    private fun tokenize(title: String): List<String> {
        if (title.isBlank()) return emptyList()
        val cleaned = title.lowercase()

        // 英文/数字词
        val words = cleaned.split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 2 }

        // 中文字符（CJK 1-gram —— 单字粒度，确保中文标题也能聚类）
        val cjk = cleaned.filter { it.code in 0x4E00..0x9FFF }
            .map { it.toString() }

        return (words + cjk)
    }
}