package moe.bunbun.news.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FeedEntity::class,
        ArticleEntity::class,
        ArticleFtsEntity::class,
        SubscriptionEntity::class,
        HistoryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class ZixunDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun historyDao(): HistoryDao

    companion object {
        const val NAME = "zixun.db"
    }
}