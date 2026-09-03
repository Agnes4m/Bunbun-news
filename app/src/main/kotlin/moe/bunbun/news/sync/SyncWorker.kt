package moe.bunbun.news.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.data.repo.observeAllOnce
import moe.bunbun.news.domain.usecase.SyncFeedsUseCase
import timber.log.Timber

/**
 * 后台同步所有订阅源。
 * 由 WorkManager 定时触发（默认 30 分钟），也支持用户主动下拉刷新时手动触发。
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val feedRepository: FeedRepository,
    private val syncFeedsUseCase: SyncFeedsUseCase,
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
            Result.success()
        } catch (t: Throwable) {
            Timber.tag("Sync").w(t, "SyncWorker failed")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_NAME = "bunbun-sync"
        const val MAX_RETRIES = 3
    }
}