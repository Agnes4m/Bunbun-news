package moe.bunbun.news.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkManager 调度门面。封装周期性 + 一次性 work 的注册逻辑。
 */
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    /**
     * 注册或更新周期同步任务。
     * 默认 30 分钟一次（WorkManager 最小间隔），仅在联网时触发。
     */
    fun schedulePeriodicSync(
        intervalMinutes: Long = 30L,
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(intervalMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /**
     * 用户主动触发的立即同步（如下拉刷新）。
     * 用 unique work REPLACE 策略：如果已有正在跑的实例会被替换。
     */
    fun requestImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "${SyncWorker.UNIQUE_NAME}-immediate",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.UNIQUE_NAME)
    }

    /**
     * 链式触发 ImagePrefetchWorker：在 SyncWorker 成功后把最近 24h 文章的图片
     * 预下载到 Coil 磁盘缓存。
     *
     * 使用 KEEP 策略——多次入队只保留最新版本，旧任务被新任务覆盖前会被取消。
     */
    fun enqueueImagePrefetch() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ImagePrefetchWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(
            ImagePrefetchWorker.UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}