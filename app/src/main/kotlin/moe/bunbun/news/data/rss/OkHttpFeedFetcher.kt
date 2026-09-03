package moe.bunbun.news.data.rss

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp 实现的 FeedFetcher，支持 ETag / Last-Modified 条件请求。
 * 304 Not Modified 时返回 notModified=true 且 body=null。
 */
@Singleton
class OkHttpFeedFetcher @Inject constructor(
    private val client: OkHttpClient,
) : FeedFetcher {

    override suspend fun fetch(
        url: String,
        previousEtag: String?,
        previousLastModified: String?,
    ): FeedFetchResult = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/rss+xml, application/atom+xml, application/xml;q=0.9, text/xml;q=0.8, */*;q=0.5")

        if (!previousEtag.isNullOrBlank()) {
            requestBuilder.header("If-None-Match", previousEtag)
        }
        if (!previousLastModified.isNullOrBlank()) {
            requestBuilder.header("If-Modified-Since", previousLastModified)
        }

        val request = requestBuilder.build()

        try {
            client.newCall(request).execute().use { response ->
                val notModified = response.code == 304
                if (notModified) {
                    return@withContext FeedFetchResult(
                        body = null,
                        etag = response.header("ETag"),
                        lastModified = response.header("Last-Modified"),
                        contentType = response.header("Content-Type"),
                        notModified = true,
                    )
                }
                if (!response.isSuccessful) {
                    return@withContext FeedFetchResult(
                        body = null,
                        etag = response.header("ETag"),
                        lastModified = response.header("Last-Modified"),
                        contentType = response.header("Content-Type"),
                        error = "HTTP ${response.code} ${response.message}",
                    )
                }
                FeedFetchResult(
                    body = response.body?.string(),
                    etag = response.header("ETag"),
                    lastModified = response.header("Last-Modified"),
                    contentType = response.header("Content-Type"),
                )
            }
        } catch (t: Throwable) {
            FeedFetchResult(
                body = null,
                etag = null,
                lastModified = null,
                contentType = null,
                error = t.message ?: t::class.java.simpleName,
            )
        }
    }

    companion object {
        private const val USER_AGENT = "BunbunNews/0.1 (Android; RSS reader)"
    }
}