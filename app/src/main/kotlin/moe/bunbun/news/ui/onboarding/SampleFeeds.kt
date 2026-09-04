package moe.bunbun.news.ui.onboarding

/**
 * v0.1 内置推荐订阅源 OPML。
 *
 * 只收录国内稳定可达的源（在被墙/反爬风险高的网络环境下实测通过）。
 * 覆盖分类：科技。
 * 国际/财经/生活等分类留给 v0.2 由用户自定义 RSS 补充。
 */
val SAMPLE_FEEDS_OPML: String = """
<?xml version="1.0" encoding="UTF-8"?>
<opml version="2.0">
  <head><title>Bunbun News 推荐订阅</title></head>
  <body>
    <outline type="rss" text="少数派" title="少数派" category="科技" xmlUrl="https://sspai.com/feed"/>
    <outline type="rss" text="阮一峰的网络日志" title="阮一峰的网络日志" category="科技" xmlUrl="https://www.ruanyifeng.com/blog/atom.xml"/>
    <outline type="rss" text="IT之家" title="IT之家" category="科技" xmlUrl="https://www.ithome.com/rss/"/>
    <outline type="rss" text="爱范儿" title="爱范儿" category="科技" xmlUrl="https://www.ifanr.com/feed"/>
    <outline type="rss" text="InfoQ 中文" title="InfoQ 中文" category="科技" xmlUrl="https://www.infoq.cn/feed"/>
    <outline type="rss" text="开源中国 OSCHINA" title="开源中国 OSCHINA" category="科技" xmlUrl="https://www.oschina.net/news/rss"/>
  </body>
</opml>
""".trimIndent()

/**
 * 推荐源名称列表（用于 OnboardingScreen 展示）
 */
val SAMPLE_FEED_NAMES: List<String> = listOf(
    "少数派",
    "阮一峰的网络日志",
    "IT之家",
    "爱范儿",
    "InfoQ 中文",
    "开源中国 OSCHINA",
)
