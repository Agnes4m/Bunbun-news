package moe.bunbun.news.data.rss

import moe.bunbun.news.domain.model.Feed
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OPML 导出器。生成标准 OPML 2.0 格式。
 */
@Singleton
class OpmlExporter @Inject constructor() {

    fun export(feeds: List<Feed>, title: String = "Bunbun News Subscriptions"): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<opml version="2.0">""")
        sb.appendLine("  <head>")
        sb.appendLine("    <title>$title</title>")
        sb.appendLine("    <dateCreated>${java.time.Instant.now()}</dateCreated>")
        sb.appendLine("  </head>")
        sb.appendLine("  <body>")
        feeds.forEach { feed ->
            sb.appendLine(
                """    <outline type="rss" text="${escape(feed.title)}" title="${escape(feed.title)}" xmlUrl="${escape(feed.url)}" htmlUrl="${escape(feed.siteUrl ?: "")}"/>"""
            )
        }
        sb.appendLine("  </body>")
        sb.appendLine("</opml>")
        return sb.toString()
    }

    private fun escape(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}