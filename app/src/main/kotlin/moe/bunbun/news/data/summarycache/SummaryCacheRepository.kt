package moe.bunbun.news.data.summarycache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SummaryCacheRepository {
    suspend fun get(articleId: String): String?
    fun observe(articleId: String): Flow<String?>
    suspend fun put(articleId: String, summary: String, providerLabel: String)
    suspend fun evict(articleId: String)
    suspend fun evictOlderThan(before: Long): Int
}

@Singleton
class SummaryCacheRepositoryImpl @Inject constructor(
    private val dao: SummaryCacheDao,
) : SummaryCacheRepository {

    override suspend fun get(articleId: String): String? =
        dao.getById(articleId)?.summary

    override fun observe(articleId: String): Flow<String?> =
        dao.observeById(articleId).map { it?.summary }

    override suspend fun put(articleId: String, summary: String, providerLabel: String) {
        dao.upsert(
            SummaryCacheEntity(
                articleId = articleId,
                summary = summary,
                providerLabel = providerLabel,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun evict(articleId: String) {
        dao.deleteById(articleId)
    }

    override suspend fun evictOlderThan(before: Long): Int =
        dao.deleteOlderThan(before)
}