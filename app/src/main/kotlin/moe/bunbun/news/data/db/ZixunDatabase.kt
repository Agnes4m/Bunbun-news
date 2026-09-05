package moe.bunbun.news.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import moe.bunbun.news.data.summarycache.SummaryCacheDao
import moe.bunbun.news.data.summarycache.SummaryCacheEntity

@Database(
    entities = [
        FeedEntity::class,
        ArticleEntity::class,
        ArticleFtsEntity::class,
        SubscriptionEntity::class,
        HistoryEntity::class,
        SummaryCacheEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class ZixunDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun historyDao(): HistoryDao
    abstract fun summaryCacheDao(): SummaryCacheDao

    companion object {
        const val NAME = "zixun.db"
    }
}