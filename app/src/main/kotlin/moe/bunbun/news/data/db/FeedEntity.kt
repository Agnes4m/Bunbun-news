package moe.bunbun.news.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feeds",
    indices = [Index(value = ["url"], unique = true)],
)
data class FeedEntity(
    @PrimaryKey val id: String, // url 的 hash（稳定 ID）
    val url: String,
    val title: String,
    val siteUrl: String?,
    val iconUrl: String?,
    val category: String? = null,
    @ColumnInfo(name = "lastSyncAt") val lastSyncAt: Long? = null,
    val etag: String? = null,
    val lastModified: String? = null,
    @ColumnInfo(name = "createdAt") val createdAt: Long = System.currentTimeMillis(),
)