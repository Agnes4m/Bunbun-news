package moe.bunbun.news.data.rss

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 把来源 URL 规整成稳定形式，用于事件聚类（v0.1 简化版 + v0.2 国内平台扩展）。
 *
 * 处理：
 * - 小写 scheme + host
 * - 移除 "www." 前缀
 * - 强制 https（如原为 http）
 * - 删除默认端口（80/443）
 * - 移除 fragment
 * - 移除常见追踪参数（utm_*, fbclid, gclid, mc_cid, mc_eid, spm, share_* 等；含精确匹配 + 前缀匹配）
 * - 去除尾部斜杠
 * - query 参数按键名排序
 */
object UrlNormalizer {

    /** 精确匹配的追踪参数名 */
    private val TRACKING_PARAMS = setOf(
        // utm_*（GA 通用）
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "utm_id", "utm_name", "utm_brand", "utm_social", "utm_social-type",
        // 海外广告/邮件追踪
        "fbclid", "gclid", "gclsrc", "msclkid", "mc_cid", "mc_eid",
        "ref", "ref_src", "source", "src", "_hsenc", "_hsmi",
        "vero_conv", "_hsfp", "oly_anon_id", "oly_enc_id",
        // 国内电商/社交追踪（v0.2 新增）
        "spm", "spu", "scm",          // 阿里 SPM 矩阵
        "from", "chksm", "scene", "clicktime", "wid",  // 微信分享/今日头条
        "seid", "sid", "wm",          // B 站/新浪
        "share_from", "share_source", "share_id", "share_medium", "share_to",
        "track_id", "algo",            // 淘宝/京东
    )

    /**
     * 前缀匹配的追踪参数（参数名以这些前缀开头视为追踪参数）
     * - utm_ 已通过 TRACKING_PARAMS 精确匹配，但这里也覆盖（兼容未来扩展）
     * - _hs HubSpot 系列（_hsenc/_hsmi/_hsfp 已精确匹配）
     * - spm_ 阿里系子追踪
     * - share_ 分享追踪
     */
    private val TRACKING_PREFIXES = listOf("utm_", "spm_", "share_", "_hs")

    /** 判断参数 key 是否为追踪参数（精确匹配或前缀匹配） */
    private fun isTracking(key: String): Boolean =
        key in TRACKING_PARAMS || TRACKING_PREFIXES.any { key.startsWith(it) }

    fun normalize(rawUrl: String): String {
        // 空白或空：原样返回
        if (rawUrl.isBlank()) return rawUrl

        val trimmed = rawUrl.trim()
        val parsed = runCatching { URI(trimmed) }.getOrNull() ?: return trimmed

        val originalScheme = parsed.scheme?.lowercase()
        val scheme = if (originalScheme == "http") "https" else (originalScheme ?: "https")
        val host = parsed.host?.lowercase()?.removePrefix("www.") ?: return trimmed
        val port = parsed.port.let { p ->
            // 用原始 scheme 判断默认端口（避免 http → https 转换后漏判 80）
            if ((originalScheme == "https" && p == 443) || (originalScheme == "http" && p == 80)) -1 else p
        }

        val rawPath = parsed.rawPath.orEmpty().trimEnd('/').ifEmpty { "" }
        val cleanedQuery = parsed.rawQuery
            ?.split('&')
            ?.mapNotNull { kv ->
                val eq = kv.indexOf('=')
                if (eq < 0) kv to "" else kv.substring(0, eq) to kv.substring(eq + 1)
            }
            ?.filter { (k, _) -> !isTracking(k) }
            ?.sortedBy { it.first }
            ?.joinToString("&") { (k, v) -> if (v.isEmpty()) k else "$k=$v" }
            ?.takeIf { it.isNotEmpty() }

        val builder = StringBuilder()
            .append(scheme).append("://").append(host)
        if (port != -1) builder.append(':').append(port)
        builder.append(rawPath)
        if (cleanedQuery != null) builder.append('?').append(cleanedQuery)

        return builder.toString()
    }

    /** 用于 clusterId 的稳定 hash（v0.1 简化为对 normalized URL + title 取 hash） */
    fun stableHash(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        var h = 1125899906842597L // 大质数种子
        for (b in bytes) {
            h = 31 * h + b
        }
        return h.toString(Character.MAX_RADIX)
    }

    @Suppress("unused")
    private fun decode(s: String): String = URLDecoder.decode(s, Charsets.UTF_8)

    @Suppress("unused")
    private fun encode(s: String): String = URLEncoder.encode(s, Charsets.UTF_8)
}