package moe.bunbun.news.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.bunbun.news.data.db.ArticleDao
import moe.bunbun.news.data.db.ArticleEntity
import moe.bunbun.news.data.toDomain
import moe.bunbun.news.data.toEntity
import moe.bunbun.news.domain.model.Article
import javax.inject.Inject
import javax.inject.Singleton

interface ArticleRepository {
    fun observeRecent(limit: Int = 200): Flow<List<Article>>
    fun observeSubscriptionTimeline(limit: Int = 500): Flow<List<Article>>
    fun observeStarred(): Flow<List<Article>>
    fun observeByCluster(clusterId: String): Flow<List<Article>>
    fun observeById(id: String): Flow<Article?>
    fun search(query: String, limit: Int = 100): Flow<List<Article>>

    suspend fun getById(id: String): Article?
    suspend fun upsert(article: Article): Boolean
    suspend fun upsertAll(articles: List<Article>): Int
    suspend fun markRead(id: String, isRead: Boolean)
    suspend fun toggleStar(id: String)
    suspend fun setClusterId(id: String, clusterId: String)
}

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val dao: ArticleDao,
) : ArticleRepository {

    override fun observeRecent(limit: Int): Flow<List<Article>> =
        dao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeSubscriptionTimeline(limit: Int): Flow<List<Article>> =
        dao.observeSubscriptionTimeline(limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeStarred(): Flow<List<Article>> =
        dao.observeStarred().map { entities -> entities.map { it.toDomain() } }

    override fun observeByCluster(clusterId: String): Flow<List<Article>> =
        dao.observeByCluster(clusterId).map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Article?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun search(query: String, limit: Int): Flow<List<Article>> =
        dao.search(query, limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): Article? =
        dao.getById(id)?.toDomain()

    override suspend fun upsert(article: Article): Boolean =
        dao.insertIfAbsent(article.toEntity()) != -1L

    override suspend fun upsertAll(articles: List<Article>): Int =
        dao.insertAllIfAbsent(articles.map(Article::toEntity)).count { it != -1L }

    override suspend fun markRead(id: String, isRead: Boolean) {
        dao.setRead(id, isRead)
    }

    override suspend fun toggleStar(id: String) {
        val current = dao.getById(id) ?: return
        dao.setStarred(id, !current.isStarred)
    }

    override suspend fun setClusterId(id: String, clusterId: String) {
        dao.setClusterId(id, clusterId)
    }
}