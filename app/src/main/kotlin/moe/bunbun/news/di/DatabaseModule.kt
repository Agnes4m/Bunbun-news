package moe.bunbun.news.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.bunbun.news.data.db.ArticleDao
import moe.bunbun.news.data.db.FeedDao
import moe.bunbun.news.data.db.HistoryDao
import moe.bunbun.news.data.db.MIGRATION_1_2
import moe.bunbun.news.data.db.MIGRATION_2_3
import moe.bunbun.news.data.db.SubscriptionDao
import moe.bunbun.news.data.db.ZixunDatabase
import moe.bunbun.news.data.summarycache.SummaryCacheDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ZixunDatabase =
        Room.databaseBuilder(context, ZixunDatabase::class.java, ZixunDatabase.NAME)
            // v0.2 主题 D 子 X：补正式迁移，用户从 v0.1.x 升级不再清表
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            // 保留 fallbackToDestructiveMigration 作为开发期保险：
            // 如果用户手贱降级 schema（如装了 v0.2 dev 又回退 v0.1），仍走清表
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFeedDao(db: ZixunDatabase): FeedDao = db.feedDao()

    @Provides
    fun provideArticleDao(db: ZixunDatabase): ArticleDao = db.articleDao()

    @Provides
    fun provideSubscriptionDao(db: ZixunDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    fun provideHistoryDao(db: ZixunDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideSummaryCacheDao(db: ZixunDatabase): SummaryCacheDao = db.summaryCacheDao()
}