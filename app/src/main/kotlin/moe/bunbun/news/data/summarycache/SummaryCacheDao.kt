package moe.bunbun.news.data.summarycache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryCacheDao {

    @Query("SELECT * FROM summary_cache WHERE articleId = :id")
    suspend fun getById(id: String): SummaryCacheEntity?

    @Query("SELECT * FROM summary_cache WHERE articleId = :id")
    fun observeById(id: String): Flow<SummaryCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SummaryCacheEntity)

    @Query("DELETE FROM summary_cache WHERE articleId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM summary_cache WHERE updatedAt < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("SELECT COUNT(*) FROM summary_cache")
    suspend fun countAll(): Int
}