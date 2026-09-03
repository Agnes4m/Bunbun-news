package moe.bunbun.news.ui.home

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
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    val recentArticles: StateFlow<List<Article>> = articleRepository.observeRecent(limit = 200)
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