package moe.bunbun.news.data.rss

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OPML 导入器（v0.1）。标准 OPML 2.0 格式：
 *
 * ```xml
 * <opml version="2.0">
 *   <head><title>...</title></head>
 *   <body>
 *     <outline type="rss" xmlUrl="https://..." title="..." />
 *     <outline title="Tech">
 *       <outline type="rss" xmlUrl="..." />
 *     </outline>
 *   </body>
 * </opml>
 * ```
 */
@Singleton
class OpmlImporter @Inject constructor() {

    fun extractFeedUrls(input: InputStream): List<OpmlFeed> {
        val feeds = mutableListOf<OpmlFeed>()
        try {
            input.use { stream ->
                val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
                    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                    setInput(stream, Charsets.UTF_8.name())
                }

                var event = parser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG && parser.name.equals("outline", ignoreCase = true)) {
                        val url = parser.getAttributeValue(null, "xmlUrl")
                            ?: parser.getAttributeValue(null, "url")
                        val title = parser.getAttributeValue(null, "title")
                            ?: parser.getAttributeValue(null, "text")
                        val type = parser.getAttributeValue(null, "type")

                        if (!url.isNullOrBlank() && (type == null || type.equals("rss", ignoreCase = true) || type.equals("atom", ignoreCase = true))) {
                            val category = parser.getAttributeValue(null, "category")
                            feeds.add(OpmlFeed(title = title ?: url, url = url, category = category))
                        }
                        // 跳过嵌套 outline（分类节点没有 xmlUrl）
                    }
                    event = parser.next()
                }
            }
        } catch (t: Throwable) {
            // 解析失败返回已收集的部分
        }
        return feeds
    }
}

data class OpmlFeed(val title: String, val url: String, val category: String? = null)