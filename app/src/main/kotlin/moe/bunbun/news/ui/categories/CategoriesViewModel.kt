package moe.bunbun.news.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import moe.bunbun.news.data.repo.ArticleRepository
import moe.bunbun.news.domain.model.Article
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<String> = listOf("综合", "科技", "财经", "生活", "国际"),
    val selectedCategory: String = "综合",
    val articles: List<Article> = emptyList(),
)

/**
 * 分类浏览：按 feed.category 过滤文章。
 * 切换分类用 flatMapLatest 重新查询。
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow("综合")

    val uiState: StateFlow<CategoriesUiState> =
        selectedCategory
            .flatMapLatest { category ->
                combine(
                    articleRepository.observeByCategory(category, limit = 300),
                    kotlinx.coroutines.flow.flowOf(loadCategoriesFromDb()),
                ) { articles, categories ->
                    CategoriesUiState(
                        categories = categories,
                        selectedCategory = category,
                        articles = articles,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    private suspend fun loadCategoriesFromDb(): List<String> {
        val db = articleRepository.getCategories()
        val defaults = listOf("综合", "科技", "财经", "生活", "国际")
        return (defaults + db).distinct()
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }
}
