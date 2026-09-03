package moe.bunbun.news.domain.usecase

import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.data.rss.ClusterEngine
import moe.bunbun.news.data.rss.FeedFetcher
import moe.bunbun.news.data.rss.FeedParseResult
import moe.bunbun.news.data.rss.FeedParser
import moe.bunbun.news.domain.model.Feed
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * 同步单个 Feed：拉取 → 解析 → 算 clusterId → 写库 → 更新 Feed 元数据。
 * 任何步骤失败都会通过 Timber 记录日志并跳过该 feed，不影响其他。
 */
@Singleton
class SyncFeedsUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val articleRepository: ArticleRepository,
    private val fetcher: FeedFetcher,
    private val parser: FeedParser,
) {
    data class SyncStats(
        val feedId: String,
        val fetched: Boolean,
        val newArticles: Int,
        val skipped: Boolean,
        val error: String? = null,
    )

    suspend operator fun invoke(feedId: String): SyncStats {
        val feed = feedRepository.getById(feedId)
            ?: return SyncStats(feedId, fetched = false, newArticles = 0, skipped = true, error = "feed not found")

        return try {
            syncOne(feed)
        } catch (t: Throwable) {
            Timber.tag("Sync").w(t, "sync failed for $feedId")
            SyncStats(feedId, fetched = false, newArticles = 0, skipped = false, error = t.message)
        }
    }

    private suspend fun syncOne(feed: Feed): SyncStats {
        val result = fetcher.fetch(
            url = feed.url,
            previousEtag = feed.etag,
            previousLastModified = feed.lastModified,
        )

        // 拉取失败（4xx/5xx/网络）
        if (result.error != null) {
            Timber.tag("Sync").w("fetch failed for ${feed.id}: ${result.error}")
            return SyncStats(feed.id, fetched = false, newArticles = 0, skipped = false, error = result.error)
        }

        // 304 Not Modified — 跳过
        if (result.notModified || result.body == null) {
            feedRepository.upsert(feed.copy(lastSyncAt = Instant.now()))
            return SyncStats(feed.id, fetched = true, newArticles = 0, skipped = true)
        }

        // 解析失败 — 仍记录 lastSyncAt，下次重试
        val parseResult = parser.parse(result.body)
        if (parseResult is FeedParseResult.Failure) {
            Timber.tag("Sync").w("parse failed for ${feed.id}: ${parseResult.message}")
            feedRepository.upsert(feed.copy(lastSyncAt = Instant.now()))
            return SyncStats(feed.id, fetched = true, newArticles = 0, skipped = true, error = parseResult.message)
        }

        val success = parseResult as FeedParseResult.Success

        Timber.tag("Sync").d("Parsed ${success.articles.size} articles from ${feed.title}")

        val feedIdForArticles = feed.id
        val domainArticles = success.articles.map { parsed ->
            val clusterId = ClusterEngine.computeClusterId(parsed.title, parsed.url)
            moe.bunbun.news.domain.model.Article(
                id = "art-${parsed.url.hashCode()}",
                feedId = feedIdForArticles,
                guid = parsed.guid,
                title = parsed.title,
                author = parsed.author,
                url = parsed.url,
                contentHtml = parsed.contentHtml,
                excerpt = parsed.excerpt,
                publishedAt = parsed.publishedAt,
                fetchedAt = Instant.now(),
                isRead = false,
                isStarred = false,
                clusterId = clusterId,
            )
        }

        Timber.tag("Sync").d("Mapped ${domainArticles.size} domain articles, IDs: ${domainArticles.take(3).map { it.id }}")

        val inserted = articleRepository.upsertAll(domainArticles)
        Timber.tag("Sync").i("Inserted $inserted/${domainArticles.size} articles for ${feed.title} (sample IDs: ${domainArticles.take(2).map { it.id }})")

        // 更新 Feed 元数据（etag / lastModified / lastSyncAt）
        feedRepository.upsert(
            feed.copy(
                etag = result.etag ?: feed.etag,
                lastModified = result.lastModified ?: feed.lastModified,
                lastSyncAt = Instant.now(),
                title = success.title.ifBlank { feed.title },
                siteUrl = success.siteUrl ?: feed.siteUrl,
            ),
        )

        return SyncStats(feed.id, fetched = true, newArticles = inserted, skipped = false)
    }

    /** 同步所有 Feed（M4 的 WorkManager 会调） */
    suspend fun syncAll(): List<SyncStats> {
        val feeds = feedRepository.observeAll() // 实际场景应该用 suspend getAll
        // 这里简单迭代；M4 在 WorkManager 里协程中调用
        return emptyList() // 占位 —— 真正实现走 FeedRepository.observeAll().first()
    }
}