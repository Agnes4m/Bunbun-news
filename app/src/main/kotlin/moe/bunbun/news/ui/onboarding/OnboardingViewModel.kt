package moe.bunbun.news.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import moe.bunbun.news.data.prefs.UserPreferences
import moe.bunbun.news.data.rss.OpmlImporter
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.sync.WorkScheduler
import javax.inject.Inject

/**
 * 引导页 ViewModel。
 *
 * 提供两个动作：
 * - [importSampleFeeds]：解析内置的 11 个推荐源并写入数据库，触发同步，标记首启完成
 * - [skip]：直接标记首启完成（用户暂不订阅任何源）
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val opmlImporter: OpmlImporter,
    private val feedRepository: FeedRepository,
    private val workScheduler: WorkScheduler,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    private val _importedCount = MutableStateFlow(0)
    val importedCount: StateFlow<Int> = _importedCount.asStateFlow()

    /**
     * 导入内置推荐源列表（[SAMPLE_OPML]）。
     * 入库成功后立即触发一次同步，标记首启完成。
     */
    fun importSampleFeeds(sampleOpml: String) {
        if (_importing.value) return
        viewModelScope.launch {
            _importing.value = true
            try {
                val feeds = opmlImporter.extractFeedUrls(sampleOpml.byteInputStream())
                feeds.forEach { feed ->
                    feedRepository.upsert(
                        moe.bunbun.news.domain.model.Feed(
                            id = "feed-${feed.url.hashCode()}",
                            url = feed.url,
                            title = feed.title,
                            siteUrl = null,
                            iconUrl = null,
                            category = feed.category,
                            lastSyncAt = null,
                            etag = null,
                            lastModified = null,
                            createdAt = java.time.Instant.now(),
                        ),
                    )
                }
                _importedCount.value = feeds.size
                workScheduler.requestImmediateSync()
            } finally {
                userPreferences.markFirstLaunchDone()
                _importing.value = false
            }
        }
    }

    fun skip() {
        viewModelScope.launch {
            userPreferences.markFirstLaunchDone()
        }
    }
}
