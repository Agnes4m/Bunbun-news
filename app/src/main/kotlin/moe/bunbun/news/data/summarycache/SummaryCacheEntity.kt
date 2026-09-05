package moe.bunbun.news.data.summarycache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 文章摘要缓存表。
 *
 * 与 articles 表分离（避免破坏 articles schema / FTS 同步逻辑）。
 * articleId 同时作为外键概念，但本表不强引用 articles，避免再次触发
 * ON DELETE CASCADE 导致级联问题。
 */
@Entity(
    tableName = "summary_cache",
    indices = [Index(value = ["updatedAt"], orders = [Index.Order.DESC])],
)
data class SummaryCacheEntity(
    @PrimaryKey val articleId: String,
    val summary: String,
    @ColumnInfo(name = "providerLabel") val providerLabel: String,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis(),
)