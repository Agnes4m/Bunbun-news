package moe.bunbun.news.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    /** 阅读时记录或更新最近时间 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY readAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE articleId = :articleId")
    suspend fun getByArticle(articleId: String): HistoryEntity?

    @Query("DELETE FROM history WHERE articleId = :articleId")
    suspend fun deleteByArticle(articleId: String)

    @Query("DELETE FROM history WHERE readAt < :before")
    suspend fun deleteOlderThan(before: Long)

    /** v0.2 Settings：清空阅读历史（保留 feeds / articles / subscriptions） */
    @Query("DELETE FROM history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM history")
    suspend fun count(): Int
}