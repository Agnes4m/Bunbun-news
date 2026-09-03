package moe.bunbun.news.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.bunbun.news.data.db.SubscriptionDao
import moe.bunbun.news.data.db.SubscriptionEntity
import moe.bunbun.news.data.toDomain
import moe.bunbun.news.domain.model.Subscription
import moe.bunbun.news.domain.model.SubscriptionType
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface SubscriptionRepository {
    fun observeAll(): Flow<List<Subscription>>
    fun observeByType(type: SubscriptionType): Flow<List<Subscription>>
    fun observeExists(type: SubscriptionType, targetId: String): Flow<Boolean>
    suspend fun isSubscribed(type: SubscriptionType, targetId: String): Boolean

    /** 已订阅则取消；未订阅则添加（toggle 语义） */
    suspend fun toggle(type: SubscriptionType, targetId: String, title: String): Boolean
    suspend fun deleteByTarget(type: SubscriptionType, targetId: String)
    suspend fun count(): Int
}

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val dao: SubscriptionDao,
) : SubscriptionRepository {

    override fun observeAll(): Flow<List<Subscription>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeByType(type: SubscriptionType): Flow<List<Subscription>> =
        dao.observeByType(type.toDbValue()).map { entities -> entities.map { it.toDomain() } }

    override fun observeExists(type: SubscriptionType, targetId: String): Flow<Boolean> =
        dao.observeExists(type.toDbValue(), targetId)

    override suspend fun isSubscribed(type: SubscriptionType, targetId: String): Boolean =
        dao.exists(type.toDbValue(), targetId)

    override suspend fun toggle(
        type: SubscriptionType,
        targetId: String,
        title: String,
    ): Boolean {
        val dbType = type.toDbValue()
        return if (dao.exists(dbType, targetId)) {
            dao.deleteByTarget(dbType, targetId)
            false
        } else {
            dao.insertIfAbsent(
                SubscriptionEntity(
                    id = "$dbType:$targetId",
                    type = dbType,
                    targetId = targetId,
                    title = title,
                    notifyEnabled = false,
                    createdAt = Instant.now().toEpochMilli(),
                ),
            )
            true
        }
    }

    override suspend fun deleteByTarget(type: SubscriptionType, targetId: String) {
        dao.deleteByTarget(type.toDbValue(), targetId)
    }

    override suspend fun count(): Int = dao.count()
}

private fun SubscriptionType.toDbValue(): String = when (this) {
    SubscriptionType.FEED -> SubscriptionEntity.TYPE_FEED
    SubscriptionType.EVENT -> SubscriptionEntity.TYPE_EVENT
}