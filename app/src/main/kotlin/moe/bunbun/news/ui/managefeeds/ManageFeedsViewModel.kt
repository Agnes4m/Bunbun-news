package moe.bunbun.news.ui.managefeeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.rss.OpmlExporter
import moe.bunbun.news.data.rss.OpmlFeed
import moe.bunbun.news.data.rss.OpmlImporter
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.sync.WorkScheduler
import javax.inject.Inject
import moe.bunbun.news.domain.model.Feed

data class ManageFeedsUiState(
    val feeds: List<Feed> = emptyList(),
    val isAdding: Boolean = false,
)

@HiltViewModel
class ManageFeedsViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val opmlImporter: OpmlImporter,
    private val opmlExporter: OpmlExporter,
    private val workScheduler: WorkScheduler,
) : ViewModel() {

    val feeds: StateFlow<List<Feed>> = feedRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    fun showAddDialog() {
        _isAdding.value = true
    }

    fun dismissAddDialog() {
        _isAdding.value = false
    }

    /** 用户输入 URL 和标题（标题可空，自动从 URL 推断） */
    fun addFeed(url: String, title: String?) {
        val finalTitle = title?.takeIf { it.isNotBlank() } ?: url
        val id = "feed-${url.hashCode()}"
        viewModelScope.launch {
            feedRepository.upsert(
                Feed(
                    id = id,
                    url = url,
                    title = finalTitle,
                    siteUrl = null,
                    iconUrl = null,
                    category = null,
                    lastSyncAt = null,
                    etag = null,
                    lastModified = null,
                    createdAt = java.time.Instant.now(),
                ),
            )
            _isAdding.value = false
            // 立即触发一次同步
            workScheduler.requestImmediateSync()
        }
    }

    fun deleteFeed(feedId: String) {
        viewModelScope.launch {
            feedRepository.delete(feedId)
        }
    }

    /** 解析 OPML 内容（从 URI 读取流后调用） */
    fun importOpml(content: String) {
        val feeds = opmlImporter.extractFeedUrls(content.byteInputStream())
        viewModelScope.launch {
            feeds.forEach { feed ->
                addFeed(feed.url, feed.title)
            }
        }
    }

    /** 导出当前所有 feed 为 OPML */
    fun exportOpml(): String = opmlExporter.export(feeds.value)

    fun syncNow() {
        workScheduler.requestImmediateSync()
    }
}