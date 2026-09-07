package moe.bunbun.news.data.fulltext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.bunbun.news.data.readability.ArticleContentExtractor
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全文获取 + 抽取（v0.2-Reader-Content）。
 *
 * 解决痛点：很多 RSS 源 `<description>` 只给首段摘要（280 字左右），
 * 用户在 Reader 里看不到完整正文，只能跳浏览器。
 * 本组件在 Reader 打开文章时按需拉原文 HTML → 跑 Readability4J → 回写 article.contentHtml。
 *
 * 设计要点：
 * - 同步阻塞 IO（[Dispatchers.IO]），调用方通常在 viewModelScope 里 launch
 * - 失败一律返回 null，绝不抛异常（用户已在 app 内，不应该被错误打断阅读）
 * - HTML 短于 [MIN_HTML_LENGTH] 直接放弃抽取（噪声页 / 反爬占位）
 * - 与 RSS 抓取共用 [OkHttpClient]（复用连接池）
 * - User-Agent 与 FeedFetcher 区分（浏览器身份，降低被拦概率）
 */
@Singleton
class FulltextExtractor @Inject constructor(
    private val client: OkHttpClient,
) {

    /**
     * 拉 [url] 的原文 HTML 并抽取正文。
     * @return 抽取后的 [ArticleContentExtractor.Result]；失败 / 内容过短 / 抽取不出正文时返回 null
     */
    suspend fun fetchAndExtract(url: String): ArticleContentExtractor.Result? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null

        val request = try {
            Request.Builder()
                .url(url)
                .header("User-Agent", BROWSER_UA)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .build()
        } catch (t: Throwable) {
            Timber.tag(TAG).w(t, "build request failed: $url")
            return@withContext null
        }

        val html = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.tag(TAG).d("non-2xx ${response.code} for $url")
                    return@withContext null
                }
                val body = response.body?.string().orEmpty()
                if (body.length < MIN_HTML_LENGTH) {
                    Timber.tag(TAG).d("body too short (${body.length}) for $url")
                    return@withContext null
                }
                body
            }
        } catch (t: Throwable) {
            Timber.tag(TAG).w(t, "fetch failed: $url")
            return@withContext null
        }

        ArticleContentExtractor.extract(html, url).also { result ->
            if (result?.hasContent == true) {
                Timber.tag(TAG).i("extracted ${result.contentHtml!!.length} chars from $url")
            } else {
                Timber.tag(TAG).d("readability produced no content for $url")
            }
        }
    }

    companion object {
        private const val TAG = "Fulltext"
        private const val MIN_HTML_LENGTH = 512
        private const val BROWSER_UA =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36 BunbunNews/0.2"
    }
}
