package moe.bunbun.news.data.syncbridge

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import moe.bunbun.news.data.fever.FeverApi
import moe.bunbun.news.data.fever.FeverClient
import moe.bunbun.news.data.miniflux.MinifluxApi
import moe.bunbun.news.data.miniflux.MinifluxClient
import moe.bunbun.news.data.prefs.UserPreferences
import okhttp3.OkHttpClient
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/**
 * 后端同步 Worker（v0.2 主题 C 子 4）。
 *
 * 执行流程：
 * 1. 读 UserPreferences → 构造 SyncBackend
 * 2. 读 SyncDirection 偏好（字符串）
 * 3. SyncPlanner.plan → 步骤列表
 * 4. 按 backend 类型构造对应 Api 客户端，调 /me 验证凭据
 * 5. 当前版本（v0.2 主题 C 子 4）：仅做"连接可达 + 凭据有效"的烟测
 *    —— 真正拉取 feeds/entries 留给 v0.2.x 增量
 *
 * 失败重试：3 次后退避（指数）
 */
@HiltWorker
class BackendSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: UserPreferences,
    private val okHttpClient: OkHttpClient,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val backend = BackendConfigResolver.resolve(prefs)
            val directionPref = prefs.syncDirection.first()
            val direction = parseDirection(directionPref)
            val plan = SyncPlanner.plan(backend, direction)

            Timber.tag("BackendSync").i(
                "Backend=${backend.displayName()} direction=$direction steps=${plan.size}"
            )

            if (plan.isEmpty()) {
                Timber.tag("BackendSync").d("无同步步骤，跳过")
                return Result.success()
            }

            val ok = when (backend) {
                is SyncBackend.LocalOnly -> true
                is SyncBackend.Miniflux -> runMinifluxHealthCheck(backend)
                is SyncBackend.Fever -> runFeverHealthCheck(backend)
            }
            if (ok) Result.success() else if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        } catch (t: Throwable) {
            Timber.tag("BackendSync").w(t, "BackendSyncWorker failed")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private suspend fun runMinifluxHealthCheck(b: SyncBackend.Miniflux): Boolean {
        val api = MinifluxClient.create(
            baseUrl = b.baseUrl,
            tokenProvider = { b.apiKey },
            okHttpClient = okHttpClient,
        )
        return try {
            val user = api.me()
            Timber.tag("BackendSync").i("Miniflux /me ok: ${user.username}")
            true
        } catch (e: HttpException) {
            Timber.tag("BackendSync").w(e, "Miniflux /me HTTP ${e.code()}")
            false
        } catch (e: IOException) {
            Timber.tag("BackendSync").w(e, "Miniflux /me network")
            false
        }
    }

    private suspend fun runFeverHealthCheck(b: SyncBackend.Fever): Boolean {
        val api = FeverClient.create(
            baseUrl = b.baseUrl,
            apiKeyProvider = { b.apiKey },
            okHttpClient = okHttpClient,
        )
        return try {
            val resp = api.call(
                FeverClient.signedParams(mapOf("groups" to "")),
            )
            val ok = resp.auth == 1
            Timber.tag("BackendSync").i("Fever /api auth=${resp.auth}")
            ok
        } catch (e: IOException) {
            Timber.tag("BackendSync").w(e, "Fever /api network")
            false
        } catch (t: Throwable) {
            Timber.tag("BackendSync").w(t, "Fever /api unknown")
            false
        }
    }

    private fun parseDirection(s: String): SyncDirection = when (s) {
        "PULL_ONLY" -> SyncDirection.PULL_ONLY
        "PUSH_ONLY" -> SyncDirection.PUSH_ONLY
        "DISABLED" -> SyncDirection.DISABLED
        else -> SyncDirection.BIDIRECTIONAL
    }

    companion object {
        const val UNIQUE_NAME = "bunbun-backend-sync"
        private const val MAX_RETRIES = 3
    }
}