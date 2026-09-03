package moe.bunbun.news.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 订阅目标支持两种类型：
 * - type = "feed"   targetId = feedId
 * - type = "event"  targetId = clusterId
 *
 * title 字段冗余存储避免 JOIN 渲染列表。
 */
@Entity(
    tableName = "subscriptions",
    indices = [
        Index(value = ["type", "targetId"], unique = true),
        Index("type"),
    ],
)
data class SubscriptionEntity(
    @PrimaryKey val id: String, // "${type}:${targetId}"
    val type: String,
    @ColumnInfo(name = "targetId") val targetId: String,
    val title: String,
    @ColumnInfo(name = "notifyEnabled") val notifyEnabled: Boolean = false,
    @ColumnInfo(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE_FEED = "feed"
        const val TYPE_EVENT = "event"

        fun forFeed(feedId: String) = "feed:$feedId"
        fun forEvent(clusterId: String) = "event:$clusterId"
    }
}