package moe.bunbun.news.data.clustering

/**
 * SimHash 标题指纹（v0.2 主题 B 聚合去重）。
 *
 * 64-bit SimHash：分词 → 哈希 → 加权 → 降维。
 * 相似标题的海明距离 ≤ 3；不相似 > 10。
 *
 * 独立工具类，便于单测。ClusterEngine 集成放到后续 commit 单独做。
 */
object SimHash {

    /**
     * 计算 64-bit SimHash（用 Long 表示）
     */
    fun hash(text: String): Long {
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return 0L

        // 每个特征维度（bit 位）的加权和
        val weights = IntArray(64)

        for (token in tokens) {
            val h = murmur3(token)
            // 每个 token 的哈希位决定 64 个维度的加权方向
            for (i in 0 until 64) {
                if (((h ushr i) and 1L) != 0L) weights[i]++ else weights[i]--
            }
        }

        // 降维：正权重的位置 1，负权重的位置 0
        var simhash = 0L
        for (i in 0 until 64) {
            if (weights[i] > 0) simhash = simhash or (1L shl i)
        }
        return simhash
    }

    /** 计算两个 SimHash 之间的海明距离（不同位数） */
    fun hammingDistance(a: Long, b: Long): Int {
        var xor = a xor b
        var count = 0
        while (xor != 0L) {
            count += xor.toInt() and 1
            xor = xor ushr 1
        }
        return count
    }

    /** 是否相似（海明距离 ≤ [threshold]，默认 3） */
    fun isSimilar(a: Long, b: Long, threshold: Int = 3): Boolean =
        hammingDistance(a, b) <= threshold

    /**
     * 简易分词：转小写，按非字母数字字符切分（保留中文字符）
     * - 英文：按空格和标点切分
     * - 中文：每个汉字作为一个 token（粗粒度；v0.2 暂不引入 jieba 等重型分词器）
     */
    private fun tokenize(text: String): List<String> {
        val lower = text.lowercase()
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        for (ch in lower) {
            when {
                ch.isLetterOrDigit() -> sb.append(ch)
                isCjk(ch) -> {
                    if (sb.isNotEmpty()) { tokens.add(sb.toString()); sb.clear() }
                    tokens.add(ch.toString())
                }
                else -> {
                    if (sb.isNotEmpty()) { tokens.add(sb.toString()); sb.clear() }
                }
            }
        }
        if (sb.isNotEmpty()) tokens.add(sb.toString())
        return tokens.filter { it.isNotEmpty() }
    }

    private fun isCjk(ch: Char): Boolean {
        // Basic CJK Unified Ideographs + 扩展 A 区
        return ch in '\u4E00'..'\u9FFF' || ch in '\u3400'..'\u4DBF'
    }

    /**
     * 64-bit FNV-1a 哈希（确定性、低碰撞，无依赖）。
     * 用作 token 指纹（比 hashCode 跨 JVM 一致）。
     */
    private fun murmur3(token: String): Long {
        // 简化为 FNV-1a 64-bit（标准且跨平台稳定）
        val basis = -3750763034362895579L  // FNV offset basis
        val prime = 1099511628211L
        var hash = basis
        for (byte in token.toByteArray(Charsets.UTF_8)) {
            hash = hash xor (byte.toLong() and 0xFF)
            hash *= prime
        }
        // 混合位（让每个 bit 位置对结果都有贡献）
        hash = hash xor (hash ushr 33)
        hash *= -4658895280553007689L
        hash = hash xor (hash ushr 33)
        return hash
    }
}
