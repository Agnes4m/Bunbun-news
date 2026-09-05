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
            .fallbackToDestructiveMigration() // v0.1 开发期允许删表重建；v0.2+ 加正式迁移
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