package moe.bunbun.news.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.domain.model.Article
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<Article>> = _query
        .debounce(300)  // 300ms 防抖
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else articleRepository.search(q, limit = 100)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) {
        _query.value = q
    }

    fun toggleStar(articleId: String) {
        viewModelScope.launch {
            articleRepository.toggleStar(articleId)
        }
    }
}