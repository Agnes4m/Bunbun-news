package moe.bunbun.news.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index(value = ["readAt"], orders = [Index.Order.DESC])],
)
data class HistoryEntity(
    @PrimaryKey val articleId: String,
    @ColumnInfo(name = "readAt") val readAt: Long,
    @ColumnInfo(name = "scrollPercent") val scrollPercent: Float = 0f,
)