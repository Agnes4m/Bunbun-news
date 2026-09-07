package moe.bunbun.news.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.HistoryRepository
import moe.bunbun.news.data.repo.SubscriptionRepository
import moe.bunbun.news.data.summarycache.ArticleSummarizer
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.domain.model.SubscriptionType
import javax.inject.Inject

data class ReaderUiState(
    val article: Article? = null,
    val clusterSize: Int = 0,        // 同 cluster 的文章数
    val isEventSubscribed: Boolean = false,
    /** AI 摘要状态：null=未请求；""=请求中无内容；非空=有摘要 */
    val summary: String? = null,
    val summaryLoading: Boolean = false,
)

/**
 * 阅读器 ViewModel。
 * 注意：本项目是自定义导航（没有 Navigation-compose），
 * articleId 由 ReaderScreen 通过 [setArticleId] 显式传入，
 * 不能用 SavedStateHandle["articleId"]（自定义导航没有这个参数）。
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val historyRepository: HistoryRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val summarizer: ArticleSummarizer,
) : ViewModel() {

    private val articleIdFlow = MutableStateFlow<String?>(null)

    val articleState: StateFlow<Article?> = articleIdFlow
        .flatMapLatest { id -> id?.let { articleRepository.observeById(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun setArticleId(articleId: String) {
        if (articleIdFlow.value == articleId) return
        articleIdFlow.value = articleId
        // 打开即标记已读 + 写历史
        viewModelScope.launch {
            articleRepository.markRead(articleId, true)
            historyRepository.recordRead(articleId)
        }
        // 加载 cluster 状态
        viewModelScope.launch {
            val article = articleRepository.getById(articleId) ?: return@launch
            if (article.clusterId != null) {
                val isSubscribed = subscriptionRepository.isSubscribed(
                    SubscriptionType.EVENT, article.clusterId
                )
                _uiState.value = _uiState.value.copy(
                    isEventSubscribed = isSubscribed,
                )
            }
        }
        // 加载 AI 摘要（ArticleSummarizer 内部走缓存优先）
        loadSummary(articleId)
    }

    fun toggleEventSubscription() {
        val articleId = articleIdFlow.value ?: return
        viewModelScope.launch {
            val article = articleRepository.getById(articleId) ?: return@launch
            val clusterId = article.clusterId ?: return@launch
            val nowSubscribed = subscriptionRepository.toggle(
                type = SubscriptionType.EVENT,
                targetId = clusterId,
                title = article.title,
            )
            _uiState.value = _uiState.value.copy(isEventSubscribed = nowSubscribed)
        }
    }

    fun toggleStar() {
        val articleId = articleIdFlow.value ?: return
        viewModelScope.launch {
            articleRepository.toggleStar(articleId)
        }
    }

    fun resummarize() {
        val articleId = articleIdFlow.value ?: return
        _uiState.value = _uiState.value.copy(summaryLoading = true)
        viewModelScope.launch {
            val article = articleRepository.getById(articleId) ?: return@launch
            val out = summarizer.resummarize(articleId, article.title, article.contentHtml.orEmpty())
            _uiState.value = _uiState.value.copy(summary = out, summaryLoading = false)
        }
    }

    private fun loadSummary(articleId: String) {
        _uiState.value = _uiState.value.copy(summaryLoading = true, summary = "")
        viewModelScope.launch {
            val article = articleRepository.getById(articleId) ?: return@launch
            val out = summarizer.summarize(articleId, article.title, article.contentHtml.orEmpty())
            _uiState.value = _uiState.value.copy(summary = out, summaryLoading = false)
        }
    }
}
