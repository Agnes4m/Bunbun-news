package moe.bunbun.news.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.domain.model.Article
import java.time.Instant
import javax.inject.Inject

/**
 * 首页单个条目：把 article + 它所在 cluster 的大小一起带出来。
 * clusterSize = 1 表示该文章单独成 cluster；> 1 表示有 N 个源在报道同一事件。
 */
data class HotArticle(
    val article: Article,
    val clusterSize: Int,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    /** 保留旧字段供兼容（按时间倒序），新 UI 推荐用 [hotArticles] */
    val recentArticles: StateFlow<List<Article>> = articleRepository.observeRecent(limit = 200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * v0.2 首页热度算法：按"被多少个不同源报道同一事件（clusterId）"排序，
     * 多源报道的事件优先，然后按发布时间倒序。无 clusterId 的文章排最后。
     *
     * 每条 hot article 携带 clusterSize，让 UI 决定是否显示"📰 N 源都在报道"徽标。
     */
    val hotArticles: StateFlow<List<HotArticle>> = articleRepository.observeRecent(limit = 200)
        .map { articles ->
            val clusterCounts: Map<String, Int> =
                articles.mapNotNull { it.clusterId }.groupingBy { it }.eachCount()
            val sorted = articles.sortedWith(
                compareByDescending<Article> { a ->
                    // 无 clusterId 的视为 -1，永远排最后；否则按 cluster 大小
                    if (a.clusterId != null) clusterCounts[a.clusterId] ?: 0 else -1
                }.thenByDescending { it.publishedAt ?: Instant.EPOCH },
            )
            // 同 cluster 重复项只展示第一条（避免多源聚合卡被刷屏）
            val result = mutableListOf<HotArticle>()
            val seenClusters = mutableSetOf<String?>()
            for (article in sorted) {
                val key = article.clusterId
                if (seenClusters.add(key)) {
                    val size = if (key != null) clusterCounts[key] ?: 1 else 1
                    result += HotArticle(article, size)
                }
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markRead(articleId: String) {
        viewModelScope.launch {
            articleRepository.markRead(articleId, true)
        }
    }

    fun toggleStar(articleId: String) {
        viewModelScope.launch {
            articleRepository.toggleStar(articleId)
        }
    }
}