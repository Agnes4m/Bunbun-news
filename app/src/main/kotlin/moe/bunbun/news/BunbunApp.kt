package moe.bunbun.news

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import moe.bunbun.news.sync.WorkScheduler
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BunbunApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var workScheduler: WorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (isDebugBuild) android.util.Log.DEBUG else android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (isDebugBuild) {
            Timber.plant(Timber.DebugTree())
        }
        // 注册周期同步任务（首次启动后开始，30 分钟一次）
        workScheduler.schedulePeriodicSync()
        // 启动时立刻拉一次：用户开 app 就能看到最新内容
        // （OneTime work REPLACE 策略，多次启动不会堆积）
        workScheduler.requestImmediateSync()
    }

    private val isDebugBuild: Boolean
        get() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}