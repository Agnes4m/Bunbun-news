package moe.bunbun.news.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import moe.bunbun.news.data.clustering.TimeWindowClusterReconciler
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.data.repo.observeAllOnce
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.domain.usecase.SyncFeedsUseCase
import timber.log.Timber
import java.time.Duration
import java.time.Instant

/**
 * 后台同步所有订阅源。
 * 由 WorkManager 定时触发（默认 30 分钟），也支持用户主动下拉刷新时手动触发。
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val feedRepository: FeedRepository,
    private val articleRepository: ArticleRepository,
    private val syncFeedsUseCase: SyncFeedsUseCase,
    private val workScheduler: WorkScheduler,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            val feeds = feedRepository.observeAllOnce()
            Timber.tag("Sync").i("Starting sync for ${feeds.size} feeds")
            val results = feeds.map { feed ->
                syncFeedsUseCase(feed.id)
            }
            val successCount = results.count { !it.skipped && it.error == null }
            val errorCount = results.count { it.error != null }
            Timber.tag("Sync").i("Sync done: $successCount ok, $errorCount errors, total ${results.size}")
            // 同步成功后再触发图片预下载；失败时跳过避免浪费流量
            if (successCount > 0) {
                workScheduler.enqueueImagePrefetch()
                // v0.2 聚合去重 Plan A：基于时间窗 + SimHash 二次聚类
                runClusterReconciliation()
            }
            Result.success()
        } catch (t: Throwable) {
            Timber.tag("Sync").w(t, "SyncWorker failed")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    /**
     * 取最近 7 天文章 → 转 Snapshot → TimeWindowClusterReconciler → 批量写回 clusterId。
     *
     * 注意：聚类本身在 ClusterEngine.computeClusterId 里已经做过（入库前一次性）。
     * 这里做的是"跨文章 / 跨源"的二次合并：
     * - 不同 RSS 源标题措辞不同（"iPhone 16 发布" vs "苹果秋季发布会"）但 SimHash 海明距离 ≤ 3
     * - 同一事件在不同窗口内的报道统一 clusterId，便于 UI 聚合展示
     */
    private suspend fun runClusterReconciliation() {
        try {
            val cutoff = Instant.now().minus(RECONCILE_WINDOW)
            // observeRecent 拿全部；按 publishedAt 过滤最近 7 天。
            // 取最近 limit=1000 覆盖大多数情况，单测过 Reconciler 2100 篇 ~2s。
            val articles: List<Article> = articleRepository.observeRecent(limit = 1000).first()
            val snapshots = articles
                .filter { (it.publishedAt ?: it.fetchedAt) >= cutoff }
                .map {
                    TimeWindowClusterReconciler.Snapshot(
                        id = it.id,
                        title = it.title,
                        publishedAt = it.publishedAt,
                        existingClusterId = it.clusterId,
                    )
                }
            if (snapshots.isEmpty()) {
                Timber.tag("Sync").d("no recent articles to reconcile")
                return
            }
            val plan = TimeWindowClusterReconciler.reconcile(snapshots)
            if (plan.isEmpty) {
                Timber.tag("Sync").d("reconcile produced no updates")
                return
            }
            articleRepository.setClusterIdsBulk(plan.updates)
            Timber.tag("Sync").i("cluster reconcile: merged ${plan.size} articles")
        } catch (t: Throwable) {
            // 聚类失败不阻断 sync —— 主流程已成功，仅聚类没做
            Timber.tag("Sync").w(t, "cluster reconciliation failed (sync still succeeded)")
        }
    }

    companion object {
        const val UNIQUE_NAME = "bunbun-sync"
        const val MAX_RETRIES = 3
        private val RECONCILE_WINDOW: Duration = Duration.ofDays(7)
    }
}