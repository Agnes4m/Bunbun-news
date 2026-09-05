package moe.bunbun.news.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import moe.bunbun.news.data.image.ArticleImageUrlExtractor
import moe.bunbun.news.data.image.ImagePrefetchHelper
import moe.bunbun.news.data.repo.ArticleRepository
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 把最近 24 小时内发布、或最近被刷新的文章封面/正文图片预下载到 Coil 磁盘缓存。
 *
 * 触发时机：SyncWorker 成功后链式 enqueue。读者随后打开 ReaderScreen 时图片直接命中缓存。
 *
 * 节流：同一个 uniqueWork 名（KEEP 策略），多次 enqueue 只保留最新；旧任务会被取消。
 */
@HiltWorker
class ImagePrefetchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val articleRepository: ArticleRepository,
    private val prefetchHelper: ImagePrefetchHelper,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val articles = articleRepository.observeRecent(limit = PREFETCH_LIMIT).first()
            val cutoff = Instant.now().minus(RECENT_WINDOW_HOURS, ChronoUnit.HOURS)
            val recent = articles.filter {
                (it.publishedAt ?: it.fetchedAt) >= cutoff
            }

            val allUrls = recent.flatMap { article ->
                ArticleImageUrlExtractor.extract(
                    html = article.contentHtml,
                    baseUrl = article.url,
                    max = MAX_PER_ARTICLE,
                )
            }.distinct()

            Timber.tag("ImagePrefetch").i(
                "Prefetching ${allUrls.size} images from ${recent.size} recent articles"
            )

            val scheduled = prefetchHelper.prefetch(allUrls)
            Timber.tag("ImagePrefetch").d("Scheduled $scheduled prefetches")
            Result.success()
        } catch (t: Throwable) {
            Timber.tag("ImagePrefetch").w(t, "ImagePrefetchWorker failed")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_NAME = "bunbun-image-prefetch"
        private const val PREFETCH_LIMIT = 80
        private const val MAX_PER_ARTICLE = 6
        private const val RECENT_WINDOW_HOURS = 24L
        private const val MAX_RETRIES = 2
    }
}