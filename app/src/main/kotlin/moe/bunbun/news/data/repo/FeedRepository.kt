package moe.bunbun.news.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.bunbun.news.data.db.FeedDao
import moe.bunbun.news.data.toDomain
import moe.bunbun.news.data.toEntity
import moe.bunbun.news.domain.model.Feed
import javax.inject.Inject
import javax.inject.Singleton

interface FeedRepository {
    fun observeAll(): Flow<List<Feed>>
    fun observeById(id: String): Flow<Feed?>
    suspend fun getById(id: String): Feed?
    suspend fun upsert(feed: Feed)
    suspend fun delete(id: String)
    suspend fun count(): Int
}

@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val dao: FeedDao,
) : FeedRepository {

    override fun observeAll(): Flow<List<Feed>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Feed?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Feed? =
        dao.getById(id)?.toDomain()

    override suspend fun upsert(feed: Feed) {
        dao.upsert(feed.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun count(): Int = dao.count()
}