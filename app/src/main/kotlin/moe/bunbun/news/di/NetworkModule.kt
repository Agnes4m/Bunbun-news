package moe.bunbun.news.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        // 部分源（如 Solidot、阮一峰、纽约时报中文等）会拒绝默认 UA，
        // 用浏览器 UA 通过反爬首道筛选
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
                )
                .header("Accept", "application/rss+xml, application/atom+xml, application/xml;q=0.9, */*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideFeedFetcher(impl: OkHttpFeedFetcher): FeedFetcher = impl

    /**
     * Coil 的 ImageLoader。预下载（ImagePrefetchWorker）和 UI（AsyncImage）共用一个实例，
     * 因此磁盘缓存路径一致，预下载的图片打开文章时能直接命中。
     */
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .respectCacheHeaders(false) // 预下载内容不应受服务器 Cache-Control 约束
            .build()
}

/** 接口绑定模块（@Binds 必须在 abstract class 里） */
@Module
@InstallIn(SingletonComponent::class)
abstract class ParserBindingModule {
    @Binds
    @Singleton
    abstract fun bindFeedParser(impl: FeedParserImpl): FeedParser
}