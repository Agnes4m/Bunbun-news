package moe.bunbun.news.ui.subscriptions

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
import javax.inject.Inject

/**
 * v0.2 Plan A：把 clusterSize（distinct 源数）算到每个 article 上，
 * 让 UI 卡片显示 "📰 N 源都在报道" 徽标。
 */
data class SubscriptionTimelineItem(
    val article: Article,
    val clusterSize: Int,
)

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    /** 订阅混合时间线（源订阅 OR 事件订阅），按 publishedAt 倒序 */
    val timeline: StateFlow<List<Article>> = articleRepository.observeSubscriptionTimeline(limit = 500)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** v0.2 Plan A：带 clusterSize 的版本（distinct feedId 数） */
    val timelineWithCluster: StateFlow<List<SubscriptionTimelineItem>> = timeline
        .map { articles ->
            val sourceCounts: Map<String, Int> = articles
                .mapNotNull { it.clusterId }
                .distinct()
                .associateWith { cid ->
                    articles.asSequence()
                        .filter { it.clusterId == cid }
                        .map { it.feedId }
                        .distinct()
                        .count()
                }
            articles.map { a ->
                SubscriptionTimelineItem(
                    article = a,
                    clusterSize = if (a.clusterId != null) sourceCounts[a.clusterId] ?: 1 else 1,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleStar(articleId: String) {
        viewModelScope.launch {
            articleRepository.toggleStar(articleId)
        }
    }
}