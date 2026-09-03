package moe.bunbun.news.data.rss

/**
 * OkHttp 拉取 RSS/Atom feed 的结果。
 * - body == null 表示 304 Not Modified（带 ETag 时）
 * - etag/lastModified 是服务端返回的最新值（写回 FeedEntity）
 */
data class FeedFetchResult(
    val body: String?,
    val etag: String?,
    val lastModified: String?,
    val contentType: String?,
    val notModified: Boolean = false,
)

interface FeedFetcher {
    suspend fun fetch(
        url: String,
        previousEtag: String? = null,
        previousLastModified: String? = null,
    ): FeedFetchResult
}