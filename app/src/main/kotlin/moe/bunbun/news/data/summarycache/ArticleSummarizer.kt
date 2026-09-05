package moe.bunbun.news.data.summarycache

import moe.bunbun.news.data.summary.SummaryProvider
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 带缓存的文章摘要器。
 *
 * 调用流程：
 * 1. 查缓存：命中直接返回
 * 2. 未命中 → 调用 provider.summarize()
 * 3. provider 返回非空 → 写入缓存
 * 4. 返回摘要（或 null 表示本次失败）
 *
 * 缓存语义：
 * - 同一篇文章的摘要会被永久缓存（除非显式 evict）
 * - ArticleId 是稳定 ID（来自 ArticleEntity.id = "art-<url.hashCode>"）
 * - 文章正文变化时 clusterId / id 不变，但内容变了 → 旧摘要会过期。
 *   实际生产应加 hash(body) 字段；v0.2 先按 id 缓存
 *
 * 并发：
 * - 同一篇文章多次并发调用 → cache miss 都调 provider，可能浪费一次 token。
 *   v0.2.x 可加内存层（Mutex + pendingFuture）去重；本期简化
 */
@Singleton
class ArticleSummarizer @Inject constructor(
    private val provider: SummaryProvider,
    private val cache: SummaryCacheRepository,
) {

    /**
     * 取摘要（缓存优先）。返回 null 表示无摘要（缓存空 + provider 失败）。
     */
    suspend fun summarize(
        articleId: String,
        title: String,
        body: String,
    ): String? {
        cache.get(articleId)?.let {
            Timber.tag("Summary").d("hit cache for $articleId")
            return it
        }
        val fresh = provider.summarize(title, body)
        if (fresh != null) {
            cache.put(articleId, fresh, provider.label)
            Timber.tag("Summary").d("cached fresh summary for $articleId")
        }
        return fresh
    }

    /** 强制重新生成（忽略缓存）。例如文章正文更新后调用 */
    suspend fun resummarize(
        articleId: String,
        title: String,
        body: String,
    ): String? {
        cache.evict(articleId)
        return summarize(articleId, title, body)
    }
}