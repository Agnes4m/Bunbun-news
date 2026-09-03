package moe.bunbun.news.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(feed: FeedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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