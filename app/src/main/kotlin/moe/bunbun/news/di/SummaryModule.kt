package moe.bunbun.news.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import moe.bunbun.news.data.summary.LocalSummaryProvider
import moe.bunbun.news.data.summary.SummaryProvider
import javax.inject.Singleton

/**
 * SummaryProvider 的默认绑定：当前是 LocalSummaryProvider（占位）。
 *
 * 后续 v0.2 主题 D 子 4（云端/本地选择器）会改为：
 * - @Provides 根据 UserPreferences.summaryProvider 字段返回不同实现
 * - OFF → 返回一个返回 null 的 NoOpProvider
 * - DEEPSEEK → 返回 DeepSeekSummaryProvider
 * - LOCAL → 返回 LocalSummaryProvider
 *
 * 现在 D 子 4 未做完，先用 LocalSummaryProvider 占位，ReaderScreen 会拿到 null
 * 摘要并显示"暂无摘要"。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SummaryModule {

    @Binds
    @Singleton
    abstract fun bindSummaryProvider(impl: LocalSummaryProvider): SummaryProvider
}