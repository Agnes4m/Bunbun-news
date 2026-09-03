package moe.bunbun.news.data.rss

import java.io.InputStream

/**
 * OPML 导入（M5 用，先 stub）。
 * OPML 是 RSS 阅读器之间交换订阅列表的标准格式（XML）。
 */
interface OpmlImporter {
    /** 从 OPML 文件解析出 feed URL 列表 */
    fun extractFeedUrls(input: InputStream): List<String>
}