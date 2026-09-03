package moe.bunbun.news.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.data.repo.FeedRepository
import moe.bunbun.news.data.repo.HistoryRepository
import moe.bunbun.news.data.repo.SubscriptionRepository
import javax.inject.Inject

data class ProfileStats(
    val feedCount: Int = 0,
    val subscriptionCount: Int = 0,
    val starredCount: Int = 0,
    val historyCount: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    feedRepository: FeedRepository,
    subscriptionRepository: SubscriptionRepository,
    articleRepository: ArticleRepository,
    historyRepository: HistoryRepository,
) : ViewModel() {

    val stats: StateFlow<ProfileStats> = combine(
        feedRepository.observeAll(),
        subscriptionRepository.observeAll(),
        articleRepository.observeStarred(),
        historyRepository.observeRecent(limit = 1000),
    ) { feeds, subs, starred, history ->
        ProfileStats(
            feedCount = feeds.size,
            subscriptionCount = subs.size,
            starredCount = starred.size,
            historyCount = history.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileStats())
}