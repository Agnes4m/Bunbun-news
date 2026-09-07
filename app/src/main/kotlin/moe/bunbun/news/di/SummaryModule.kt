package moe.bunbun.news.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moe.bunbun.news.data.summary.SummaryProvider
import moe.bunbun.news.data.summary.SummaryProviderFactory
import javax.inject.Singleton

/**
 * SummaryProvider 绑定：当前指向 SummaryProviderFactory（路由器），
 * 路由器根据 UserPreferences.summaryProvider 字段在每次调用时选择实际 provider。
 *
 * 历史：
 * - v0.2 主题 D 子 3：曾用 @Binds 直接绑 LocalSummaryProvider（占位）
 * - v0.2 主题 D 子 4（本 commit）：改为工厂模式，支持 OFF / DEEPSEEK / LOCAL 切换
 */
@Module
@InstallIn(SingletonComponent::class)
object SummaryModule {

    @Provides
    @Singleton
    fun provideSummaryProvider(factory: SummaryProviderFactory): SummaryProvider = factory
}