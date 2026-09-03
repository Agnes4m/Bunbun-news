package moe.bunbun.news.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(subscription: SubscriptionEntity): Long

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM subscriptions WHERE type = :type AND targetId = :targetId")
    suspend fun deleteByTarget(type: String, targetId: String)

    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE type = :type ORDER BY createdAt DESC")
    fun observeByType(type: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE type = :type AND targetId = :targetId)")
    suspend fun exists(type: String, targetId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE type = :type AND targetId = :targetId)")
    fun observeExists(type: String, targetId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun count(): Int
}