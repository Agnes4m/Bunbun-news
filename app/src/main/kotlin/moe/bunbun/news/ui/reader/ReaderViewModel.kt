package moe.bunbun.news.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.fulltext.FulltextExtractor
import moe.bunbun.news.data.prefs.UserPreferences
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.HistoryRepository
import moe.bunbun.news.data.repo.SubscriptionRepository
import moe.bunbun.news.data.summarycache.ArticleSummarizer
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.domain.model.SubscriptionType
import timber.log.Timber
import javax.inject.Inject

data class ReaderUiState(
    val article: Article? = null,
    val clusterSize: Int = 0,        // 同 cluster 的文章数
    val isEventSubscribed: Boolean = false,
    /** AI 摘要状态：null=未请求；""=请求中无内容；非空=有摘要 */
    val summary: String? = null,
    val summaryLoading: Boolean = false,
    /** v0.2-Reader-Content：是否正在拉全文 */
    val fulltextLoading: Boolean = false,
    /** 拉全文失败时为 true，UI 显示「重试」按钮 */
    val fulltextFailed: Boolean = false,
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
    private val fulltextExtractor: FulltextExtractor,
    private val userPreferences: UserPreferences,
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
        // 重置全文加载状态（新文章）
        _uiState.value = _uiState.value.copy(fulltextLoading = false, fulltextFailed = false)
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
            // v0.2-Reader-Content：检查是否需要按需拉全文
            ensureFulltext(article)
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

    /**
     * v0.2-Reader-Content：手动重试（UI 上的「加载全文」按钮触发）。
     * 强制重抽，绕过长度阈值。
     */
    fun retryFulltext() {
        val articleId = articleIdFlow.value ?: return
        viewModelScope.launch {
            val article = articleRepository.getById(articleId) ?: return@launch
            _uiState.value = _uiState.value.copy(fulltextLoading = true, fulltextFailed = false)
            val result = fulltextExtractor.fetchAndExtract(article.url)
            if (result?.hasContent == true) {
                articleRepository.updateContentHtml(articleId, result.contentHtml)
                Timber.tag("Reader").i("manual retry fulltext ok: ${result.contentHtml!!.length} chars")
            } else {
                _uiState.value = _uiState.value.copy(fulltextFailed = true)
                Timber.tag("Reader").w("manual retry fulltext failed for ${article.url}")
            }
            _uiState.value = _uiState.value.copy(fulltextLoading = false)
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

    /**
     * v0.2-Reader-Content：按需拉全文的判定与执行。
     *
     * 触发条件（同时满足）：
     * 1. UserPreferences.autoFetchFulltext == true（用户没关）
     * 2. 当前 contentHtml 为空 / 看起来只是摘要（长度 < [SHORT_CONTENT_THRESHOLD]）
     * 3. URL 非空
     *
     * 拉到的全文直接写回 DB（observeById Flow 会自动推送，WebView 立刻重渲染）。
     * 失败不弹错，只在 UI 露出「加载全文」按钮让用户手动重试。
     */
    private suspend fun ensureFulltext(article: Article) {
        val autoFetch = userPreferences.autoFetchFulltext.first()
        if (!autoFetch) return
        if ((article.contentHtml?.length ?: 0) >= SHORT_CONTENT_THRESHOLD) return
        if (article.url.isBlank()) return

        _uiState.value = _uiState.value.copy(fulltextLoading = true, fulltextFailed = false)
        val result = fulltextExtractor.fetchAndExtract(article.url)
        if (result?.hasContent == true) {
            articleRepository.updateContentHtml(article.id, result.contentHtml)
            Timber.tag("Reader").i("auto fulltext ok: ${result.contentHtml!!.length} chars for ${article.url}")
        } else {
            _uiState.value = _uiState.value.copy(fulltextFailed = true)
            Timber.tag("Reader").w("auto fulltext empty/failed for ${article.url}")
        }
        _uiState.value = _uiState.value.copy(fulltextLoading = false)
    }

    companion object {
        /** 视为"只是摘要"的阈值：低于此长度认为 RSS 没给正文，触发拉取 */
        private const val SHORT_CONTENT_THRESHOLD = 800
    }
}
