package moe.bunbun.news.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.ui.common.ArticleCard
import moe.bunbun.news.ui.common.EmptyFeedScreen

/**
 * 首页（v0.2 主题 A 子 X + 子 9：非规整瀑布流布局）
 *
 * 数据为空时显示 EmptyFeedScreen，用户可一键导入推荐源或跳到订阅管理页。
 * 非空时用 LazyVerticalStaggeredGrid 做瀑布流：
 * - 大图/含图的 article 渲染成大卡（占两行高度）
 * - 纯文本短 article 渲染成小卡（占一行高度）
 *
 * 用户首次进入 app 没有 feed/articles → 立即看到 EmptyFeedScreen（不依赖 firstLaunchDone），
 * 这也是 v0.2 修复的"空状态提示"语义。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (String) -> Unit = {},
    onNavigateToManageFeeds: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val articles by viewModel.hotArticles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_home)) },
                actions = {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = "热度（v0.2）",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (articles.isEmpty()) {
                EmptyFeedScreen(
                    onNavigateToManageFeeds = onNavigateToManageFeeds,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // v0.2 子 9：瀑布流（非规整方框）。StaggeredGrid 让每行高度由内容决定，
                // 标题 + 摘要长的 article 自然换行换页，短 article 占用较少纵向空间，
                // 形成类小红书的双列交错效果。
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(articles, key = { it.article.id }) { hot ->
                        ArticleCard(
                            article = hot.article,
                            onClick = { onArticleClick(hot.article.id) },
                            onToggleStar = { viewModel.toggleStar(hot.article.id) },
                            clusterSize = hot.clusterSize,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}