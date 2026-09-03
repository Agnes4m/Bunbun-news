package moe.bunbun.news.data.rss

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用 Android 内置 XmlPullParser 实现的 RSS 2.0 / Atom 解析器。
 * RSS-Parser 6.x 强制 HTTP fetch（parse(input) 也会当作 URL 处理），不适合
 * "自己 fetch + 自己解析" 的架构，所以这里自己实现。
 *
 * 支持：
 * - RSS 2.0（item + content:encoded + dc:creator）
 * - Atom（entry + content + author）
 * - 频道元数据（title / link / description）
 *
 * FeedParser 接口与 ParsedArticle/FeedParseResult 数据类定义在 FeedParser.kt 中。
 */
@Singleton
class RssXmlParser @Inject constructor() {

    fun parse(xml: String): FeedParseResult {
        return try {
            doParse(xml)
        } catch (t: Throwable) {
            FeedParseResult.Failure(t.message ?: "parse failed", cause = t)
        }
    }

    private fun doParse(xml: String): FeedParseResult {
        val parser = XmlPullParserFactory.newInstance().newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(xml))
        }

        // 跳到第一个 START_TAG
        var event = parser.next()
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
        if (event != XmlPullParser.START_TAG) {
            return FeedParseResult.Failure("No root element found")
        }

        return when (parser.name.lowercase()) {
            "rss" -> parseRss(parser)
            "feed" -> parseAtom(parser)
            else -> FeedParseResult.Failure("Unsupported root element: ${parser.name}")
        }
    }

    // ---------------- RSS 2.0 ----------------

    private fun parseRss(parser: XmlPullParser): FeedParseResult {
        var title = ""
        var link: String? = null
        val articles = mutableListOf<ParsedArticle>()

        var event = parser.next()
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "channel" -> {
                        // 进入 channel 子树
                        var chEvent = parser.next()
                        while (chEvent != XmlPullParser.END_TAG || parser.name.lowercase() != "channel") {
                            if (chEvent == XmlPullParser.START_TAG) {
                                when (parser.name.lowercase()) {
                                    "title" -> title = readTextSafe(parser)
                                    "link" -> link = readTextSafe(parser)
                                    "item" -> parseRssItem(parser)?.let { articles.add(it) }
                                    else -> skip(parser)
                                }
                            }
                            chEvent = parser.next()
                            if (chEvent == XmlPullParser.END_DOCUMENT) break
                        }
                    }
                    else -> skip(parser)
                }
            }
            event = parser.next()
        }

        return FeedParseResult.Success(title, link, articles)
    }

    private fun parseRssItem(parser: XmlPullParser): ParsedArticle? {
        var title: String? = null
        var link: String? = null
        var guid: String? = null
        var author: String? = null
        var dcCreator: String? = null
        var pubDate: String? = null
        var description: String? = null
        var contentEncoded: String? = null

        var event = parser.next()
        while (!(event == XmlPullParser.END_TAG && parser.name.equals("item", ignoreCase = true))) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "title" -> title = readTextSafe(parser)
                    "link" -> link = readTextSafe(parser)
                    "guid" -> guid = readTextSafe(parser)
                    "author" -> author = readTextSafe(parser)
                    "pubdate" -> pubDate = readTextSafe(parser)
                    "description" -> description = readTextSafe(parser)
                    "encoded", "content:encoded" -> contentEncoded = readTextSafe(parser) // content:encoded
                    "creator", "dc:creator" -> dcCreator = readTextSafe(parser) // dc:creator
                    else -> skip(parser)
                }
            }
            event = parser.next()
            if (event == XmlPullParser.END_DOCUMENT) break
        }

        val titleText = title?.takeIf { it.isNotBlank() } ?: return null
        val linkText = link?.takeIf { it.isNotBlank() } ?: return null

        val html = contentEncoded ?: description
        val excerpt = description?.let { stripHtml(it).take(280) }

        return ParsedArticle(
            guid = guid ?: linkText,
            title = titleText,
            url = linkText,
            author = (author ?: dcCreator)?.takeIf { it.isNotBlank() },
            contentHtml = html,
            excerpt = excerpt,
            publishedAt = pubDate?.let { parseRssDate(it) },
        )
    }

    // ---------------- Atom ----------------

    private fun parseAtom(parser: XmlPullParser): FeedParseResult {
        var title = ""
        var link: String? = null
        val articles = mutableListOf<ParsedArticle>()

        var event = parser.next()
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "title" -> title = readTextSafe(parser)
                    "link" -> {
                        val href = parser.getAttributeValue(null, "href")
                        val rel = parser.getAttributeValue(null, "rel")
                        if (link == null || rel == "alternate") link = href
                        skip(parser) // link 是空元素
                    }
                    "entry" -> parseAtomEntry(parser)?.let { articles.add(it) }
                    else -> skip(parser)
                }
            }
            event = parser.next()
        }

        return FeedParseResult.Success(title, link, articles)
    }

    private fun parseAtomEntry(parser: XmlPullParser): ParsedArticle? {
        var title: String? = null
        var link: String? = null
        var id: String? = null
        var author: String? = null
        var published: String? = null
        var updated: String? = null
        var content: String? = null
        var summary: String? = null

        var event = parser.next()
        while (!(event == XmlPullParser.END_TAG && parser.name.equals("entry", ignoreCase = true))) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "title" -> title = readTextSafe(parser)
                    "link" -> {
                        val href = parser.getAttributeValue(null, "href")
                        if (link == null) link = href
                        skip(parser)
                    }
                    "id" -> id = readTextSafe(parser)
                    "author" -> author = parseAtomAuthor(parser)
                    "published" -> published = readTextSafe(parser)
                    "updated" -> updated = readTextSafe(parser)
                    "content" -> content = readTextSafe(parser)
                    "summary" -> summary = readTextSafe(parser)
                    else -> skip(parser)
                }
            }
            event = parser.next()
            if (event == XmlPullParser.END_DOCUMENT) break
        }

        val titleText = title?.takeIf { it.isNotBlank() } ?: return null
        val linkText = link?.takeIf { it.isNotBlank() } ?: return null

        val html = content ?: summary
        val excerpt = summary?.let { stripHtml(it).take(280) }

        return ParsedArticle(
            guid = id ?: linkText,
            title = titleText,
            url = linkText,
            author = author?.takeIf { it.isNotBlank() },
            contentHtml = html,
            excerpt = excerpt,
            publishedAt = published?.let { parseAtomDate(it) }
                ?: updated?.let { parseAtomDate(it) },
        )
    }

    private fun parseAtomAuthor(parser: XmlPullParser): String? {
        // author > name 嵌套结构
        var author: String? = null
        var event = parser.next()
        while (!(event == XmlPullParser.END_TAG && parser.name.equals("author", ignoreCase = true))) {
            if (event == XmlPullParser.START_TAG && parser.name.equals("name", ignoreCase = true)) {
                author = readTextSafe(parser)
            } else if (event == XmlPullParser.START_TAG) {
                skip(parser)
            }
            event = parser.next()
            if (event == XmlPullParser.END_DOCUMENT) break
        }
        return author
    }

    // ---------------- helpers ----------------

    private fun readTextSafe(parser: XmlPullParser): String {
        val text = StringBuilder()
        var event = parser.next()
        while (event == XmlPullParser.TEXT || event == XmlPullParser.ENTITY_REF || event == XmlPullParser.CDSECT) {
            text.append(parser.text)
            event = parser.next()
        }
        return text.toString().trim()
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    private fun stripHtml(html: String): String =
        html.replace(Regex("<[^>]+>"), "").replace("&nbsp;", " ").trim()

    private fun parseRssDate(raw: String): Instant? = try {
        ZonedDateTime.parse(raw.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
    } catch (e: DateTimeParseException) {
        try { Instant.parse(raw.trim()) } catch (_: Throwable) { null }
    } catch (e: Throwable) { null }

    private fun parseAtomDate(raw: String): Instant? = try {
        Instant.parse(raw.trim())
    } catch (e: DateTimeParseException) {
        null
    } catch (e: Throwable) {
        null
    }
}