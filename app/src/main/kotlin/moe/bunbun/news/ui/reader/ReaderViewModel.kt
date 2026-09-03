package moe.bunbun.news.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.HistoryRepository
import moe.bunbun.news.data.repo.SubscriptionRepository
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.domain.model.SubscriptionType
import javax.inject.Inject

data class ReaderUiState(
    val article: Article? = null,
    val clusterSize: Int = 0,        // 同 cluster 的文章数
    val isEventSubscribed: Boolean = false,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val historyRepository: HistoryRepository,
    private val subscriptionRepository: SubscriptionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val articleId: String = checkNotNull(savedStateHandle["articleId"]) {
        "ReaderViewModel requires 'articleId' navigation arg"
    }

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    val articleState: StateFlow<Article?> = articleRepository.observeById(articleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
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
    }

    fun toggleEventSubscription() {
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
        viewModelScope.launch {
            articleRepository.toggleStar(articleId)
        }
    }
}