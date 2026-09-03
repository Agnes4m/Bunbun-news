package moe.bunbun.news.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.bunbun.news.data.db.HistoryDao
import moe.bunbun.news.data.db.HistoryEntity
import moe.bunbun.news.data.toDomain
import moe.bunbun.news.domain.model.History
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface HistoryRepository {
    fun observeRecent(limit: Int = 200): Flow<List<History>>
    suspend fun recordRead(articleId: String, scrollPercent: Float = 0f)
    suspend fun delete(articleId: String)
    suspend fun deleteOlderThan(before: Instant)
    suspend fun count(): Int
}

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
) : HistoryRepository {

    override fun observeRecent(limit: Int): Flow<List<History>> =
        dao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun recordRead(articleId: String, scrollPercent: Float) {
        dao.upsert(
            HistoryEntity(
                articleId = articleId,
                readAt = Instant.now().toEpochMilli(),
                scrollPercent = scrollPercent,
            ),
        )
    }

    override suspend fun delete(articleId: String) {
        dao.deleteByArticle(articleId)
    }

    override suspend fun deleteOlderThan(before: Instant) {
        dao.deleteOlderThan(before.toEpochMilli())
    }

    override suspend fun count(): Int = dao.count()
}