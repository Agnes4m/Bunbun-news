package moe.bunbun.news.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.HistoryRepository
import moe.bunbun.news.domain.model.Article
import javax.inject.Inject

data class HistoryItem(
    val article: Article,
    val readAt: java.time.Instant,
)

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    /**
     * 显示历史记录（文章 + 阅读时间），最新在前。
     * 通过 articleIds 列表 + 批量查询拿到文章对象，避免 Room JOIN 的 typeconverter 复杂度。
     */
    val items: StateFlow<List<HistoryItem>> = historyRepository.observeRecent(limit = 200)
        .let { historyFlow ->
            kotlinx.coroutines.flow.combine(historyFlow, MutableStateFlow(Unit)) { history, _ ->
                val articleIds = history.map { it.articleId }
                val articles = articleRepository.getByIds(articleIds).associateBy { it.id }
                history.mapNotNull { h -> articles[h.articleId]?.let { HistoryItem(it, h.readAt) } }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}