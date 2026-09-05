package moe.bunbun.news.data.image

import org.jsoup.Jsoup

/**
 * 从文章 HTML 中抽取可用的图片 URL（用于 Coil 预下载）。
 *
 * 抽取规则：
 * - 跳过 `data:` URI（base64 内联图）和 `javascript:` 链接
 * - 跳过 srcset 多分辨率中的第一个候选项（避免下载过大的图）
 * - 处理相对路径：基于 baseUrl 拼接
 * - 处理懒加载属性：data-src / data-original / data-lazy-src
 * - 去重（同一 URL 不重复入预下载列表）
 */
object ArticleImageUrlExtractor {

    private val LAZY_ATTRS = listOf("data-src", "data-original", "data-lazy-src")

    fun extract(html: String?, baseUrl: String? = null, max: Int = 16): List<String> {
        if (html.isNullOrBlank()) return emptyList()
        return try {
            val doc = if (baseUrl.isNullOrBlank()) {
                Jsoup.parseBodyFragment(html)
            } else {
                Jsoup.parseBodyFragment(html, baseUrl)
            }
            val urls = LinkedHashSet<String>()
            doc.select("img").forEach { img ->
                val raw = pickBestSrc(img) ?: return@forEach
                val absolute = absolutize(raw, baseUrl) ?: return@forEach
                if (isHttpUrl(absolute) && urls.size < max) urls.add(absolute)
            }
            urls.toList()
        } catch (t: Throwable) {
            emptyList()
        }
    }

    private fun pickBestSrc(img: org.jsoup.nodes.Element): String? {
        // 1) 显式懒加载属性优先
        for (attr in LAZY_ATTRS) {
            img.attr(attr).takeIf { it.isNotBlank() }?.let { return it }
        }
        // 2) srcset 第一个候选
        img.attr("srcset").splitToSequence(",")
            .map { it.trim().substringBefore(' ') }
            .firstOrNull { it.isNotBlank() }
            ?.let { return it }
        // 3) 普通 src
        return img.attr("src").takeIf { it.isNotBlank() }
    }

    private fun absolutize(raw: String, baseUrl: String?): String? {
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
        if (raw.startsWith("data:") || raw.startsWith("javascript:")) return null
        if (baseUrl.isNullOrBlank()) return null
        return try {
            val base = java.net.URI(baseUrl)
            base.resolve(raw)?.toString()
        } catch (_: Throwable) {
            null
        }
    }

    private fun isHttpUrl(s: String): Boolean =
        s.startsWith("http://") || s.startsWith("https://")
}