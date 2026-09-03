package moe.bunbun.news.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.ArticleRepositoryImpl
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.data.repo.FeedRepositoryImpl
import moe.bunbun.news.data.repo.HistoryRepository
import moe.bunbun.news.data.repo.HistoryRepositoryImpl
import moe.bunbun.news.data.repo.SubscriptionRepository
import moe.bunbun.news.data.repo.SubscriptionRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository

    @Binds
    @Singleton
    abstract fun bindArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}