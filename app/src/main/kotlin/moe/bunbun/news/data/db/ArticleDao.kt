package moe.bunbun.news.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(article: ArticleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(articles: List<ArticleEntity>): List<Long>

    @Query("UPDATE articles SET isRead = :isRead WHERE id = :id")
    suspend fun setRead(id: String, isRead: Boolean)

    @Query("UPDATE articles SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: String, starred: Boolean)

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE id = :id")
    fun observeById(id: String): Flow<ArticleEntity?>

    /** 全量时间线（按 publishedAt 倒序，未读优先）—— HomeScreen 用 */
    @Query(
        """
        SELECT * FROM articles
        WHERE publishedAt IS NOT NULL
        ORDER BY publishedAt DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int = 200): Flow<List<ArticleEntity>>

    /** 订阅源时间线 —— SubscriptionsScreen 用 */
    @Query(
        """
        SELECT a.* FROM articles a
        INNER JOIN subscriptions s ON (
            (s.type = 'feed' AND s.targetId = a.feedId)
            OR
            (s.type = 'event' AND s.targetId = a.clusterId)
        )
        WHERE a.publishedAt IS NOT NULL
        ORDER BY a.publishedAt DESC
        LIMIT :limit
        """
    )
    fun observeSubscriptionTimeline(limit: Int = 500): Flow<List<ArticleEntity>>

    /** 收藏列表 —— ProfileScreen 用 */
    @Query(
        """
        SELECT * FROM articles
        WHERE isStarred = 1
        ORDER BY publishedAt DESC
        """
    )
    fun observeStarred(): Flow<List<ArticleEntity>>

    /** 同 clusterId 的所有文章 —— ReaderScreen "订阅此事件" / 多源展示用 */
    @Query(
        """
        SELECT * FROM articles
        WHERE clusterId = :clusterId
        ORDER BY publishedAt DESC
        """
    )
    fun observeByCluster(clusterId: String): Flow<List<ArticleEntity>>

    /** FTS 搜索（M2 用 LIKE 实现，FTS4 留 v0.2）*/
    @Query(
        """
        SELECT * FROM articles
        WHERE title LIKE '%' || :query || '%'
           OR excerpt LIKE '%' || :query || '%'
        ORDER BY publishedAt DESC
        LIMIT :limit
        """
    )
    fun search(query: String, limit: Int = 100): Flow<List<ArticleEntity>>

    @Query("UPDATE articles SET clusterId = :clusterId WHERE id = :id")
    suspend fun setClusterId(id: String, clusterId: String)

    @Query("SELECT COUNT(*) FROM articles WHERE clusterId IS NULL")
    suspend fun countWithoutCluster(): Int

    @Transaction
    @Query("SELECT * FROM articles WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ArticleEntity>
}