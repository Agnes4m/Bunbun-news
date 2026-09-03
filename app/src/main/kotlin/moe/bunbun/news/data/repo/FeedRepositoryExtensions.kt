package moe.bunbun.news.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Flow 扩展：取一次最新值后立即返回。
 * 用于 SyncWorker 等不需要持续订阅的场景。
 */
suspend fun <T> Flow<T>.firstOrThrow(): T = first()

/**
 * FeedRepository 的 suspend 取一次方法。
 */
suspend fun FeedRepository.observeAllOnce(): List<moe.bunbun.news.domain.model.Feed> =
    observeAll().firstOrThrow()