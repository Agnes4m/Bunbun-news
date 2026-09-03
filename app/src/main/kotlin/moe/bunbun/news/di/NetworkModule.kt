package moe.bunbun.news.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moe.bunbun.news.data.rss.FeedFetcher
import moe.bunbun.news.data.rss.FeedParser
import moe.bunbun.news.data.rss.FeedParserImpl
import moe.bunbun.news.data.rss.OkHttpFeedFetcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideFeedFetcher(impl: OkHttpFeedFetcher): FeedFetcher = impl
}

/** 接口绑定模块（@Binds 必须在 abstract class 里） */
@Module
@InstallIn(SingletonComponent::class)
abstract class ParserBindingModule {
    @Binds
    @Singleton
    abstract fun bindFeedParser(impl: FeedParserImpl): FeedParser
}