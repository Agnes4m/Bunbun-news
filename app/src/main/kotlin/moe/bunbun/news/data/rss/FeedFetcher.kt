package moe.bunbun.news.data.rss

/**
 * OkHttp 拉取 RSS/Atom feed 的结果。
 * - notModified = true 表示 304 Not Modified
 * - error != null 表示拉取失败（4xx/5xx/网络错误）
 * - body 仅在 2xx 时非空
 */
data class FeedFetchResult(
    val body: String?,
    val etag: String?,
    val lastModified: String?,
    val contentType: String?,
    val notModified: Boolean = false,
    val error: String? = null,
)

interface FeedFetcher {
    suspend fun fetch(
        url: String,
        previousEtag: String? = null,
        previousLastModified: String? = null,
    ): FeedFetchResult
}