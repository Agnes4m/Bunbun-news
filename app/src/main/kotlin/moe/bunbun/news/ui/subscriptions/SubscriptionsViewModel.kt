package moe.bunbun.news.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.domain.model.Article
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    /** 订阅混合时间线（源订阅 OR 事件订阅），按 publishedAt 倒序 */
    val timeline: StateFlow<List<Article>> = articleRepository.observeSubscriptionTimeline(limit = 500)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleStar(articleId: String) {
        viewModelScope.launch {
            articleRepository.toggleStar(articleId)
        }
    }
}