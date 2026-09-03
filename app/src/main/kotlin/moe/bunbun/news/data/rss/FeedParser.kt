package moe.bunbun.news.data.rss

import javax.inject.Inject
import javax.inject.Singleton

/**
 * RSS / Atom feed 解析后的单篇文章。
 * 这是抓取层和持久化层之间的中间形态，还没带 clusterId。
 */
data class ParsedArticle(
    val guid: String,
    val title: String,
    val url: String,
    val author: String?,
    val contentHtml: String?,
    val excerpt: String?,
    val publishedAt: java.time.Instant?,
)

sealed class FeedParseResult {
    data class Success(val title: String, val siteUrl: String?, val articles: List<ParsedArticle>) : FeedParseResult()
    data class Failure(val message: String, val cause: Throwable? = null) : FeedParseResult()
}

interface FeedParser {
    fun parse(xml: String): FeedParseResult
}

/**
 * FeedParser 的 XmlPullParser 实现 —— 委托给 RssXmlParser（见同包）。
 * 取代之前的 prof18/rssparser-android 实现（6.x 强制 HTTP fetch，不符合需求）。
 */
@Singleton
class FeedParserImpl @Inject constructor(
    private val delegate: RssXmlParser,
) : FeedParser {
    override fun parse(xml: String): FeedParseResult = delegate.parse(xml)
}