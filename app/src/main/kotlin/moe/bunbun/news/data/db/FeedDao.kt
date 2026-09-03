package moe.bunbun.news.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    // 注意：不能用 REPLACE —— 它会删除旧行再插入，触发 articles 的 ON DELETE CASCADE 清空文章
    @Upsert
    suspend fun upsert(feed: FeedEntity)

    @Upsert
    suspend fun upsertAll(feeds: List<FeedEntity>)

    @Update
    suspend fun update(feed: FeedEntity)

    @Query("DELETE FROM feeds WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM feeds WHERE id = :id")
    suspend fun getById(id: String): FeedEntity?

    @Query("SELECT * FROM feeds WHERE id = :id")
    fun observeById(id: String): Flow<FeedEntity?>

    @Query("SELECT * FROM feeds ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feeds")
    suspend fun getAll(): List<FeedEntity>

    @Query("SELECT COUNT(*) FROM feeds")
    suspend fun count(): Int
}