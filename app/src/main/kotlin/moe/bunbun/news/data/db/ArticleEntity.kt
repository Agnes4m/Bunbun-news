package moe.bunbun.news.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    foreignKeys = [
        ForeignKey(
            entity = FeedEntity::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("feedId"),
        Index("publishedAt"),
        Index("clusterId"),
        Index(value = ["feedId", "guid"], unique = true),
    ],
)
data class ArticleEntity(
    @PrimaryKey val id: String, // guid 或 url 的 hash
    @ColumnInfo(name = "feedId") val feedId: String,
    val guid: String,
    val title: String,
    val author: String?,
    val url: String,
    @ColumnInfo(name = "contentHtml") val contentHtml: String?,
    @ColumnInfo(name = "excerpt") val excerpt: String?,
    @ColumnInfo(name = "publishedAt") val publishedAt: Long?,
    @ColumnInfo(name = "fetchedAt") val fetchedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "isRead") val isRead: Boolean = false,
    @ColumnInfo(name = "isStarred") val isStarred: Boolean = false,
    @ColumnInfo(name = "clusterId") val clusterId: String? = null, // M3 计算，用于事件订阅
)