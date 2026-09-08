package moe.bunbun.news.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.ui.common.ArticleCard
import moe.bunbun.news.ui.common.EmptyFeedScreen

/**
 * 首页（v0.2 主题 A 子 X + 子 9 + 下拉随机推荐）
 *
 * 数据为空时显示 EmptyFeedScreen，用户可一键导入推荐源或跳到订阅管理页。
 * 非空时用 LazyVerticalStaggeredGrid 做瀑布流：
 * - 大图/含图的 article 渲染成大卡（占两行高度）
 * - 纯文本短 article 渲染成小卡（占一行高度）
 *
 * v0.2 新增：下拉随机推荐
 * - 默认显示热度排序（hotArticles）
 * - 下拉刷新：从最近 200 篇里随机抽 N=20 篇打乱顺序
 * - 右上角 🔀 按钮也能触发随机推荐
 * - 保留 LazyStaggeredGrid 滚动位置（用户看到的是「内容变了但我没跳走」）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (String) -> Unit = {},
    onNavigateToManageFeeds: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // v0.2 下拉随机推荐：用 shuffledArticles 替换 hotArticles
    // 首次进入 seed=0 → 直接返回 hotArticles 顺序（保留热度）
    // 下拉刷新 → seed 自增 → 触发洗牌 → 展示 N=20 随机
    val articles by viewModel.shuffledArticles.collectAsState()
    var isRefreshing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val gridState = rememberLazyStaggeredGridState()

    // 模拟刷新结束（短延迟给 spinner 一个展示窗口）
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(450)
            viewModel.shuffle()
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_home)) },
                actions = {
                    // 🔀 按钮也能触发随机推荐
                    IconButton(onClick = { isRefreshing = true }) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = stringResource(R.string.home_shuffle_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                // v0.2 子 9：瀑布流 + 下拉随机推荐
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { isRefreshing = true },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyVerticalStaggeredGrid(
                        state = gridState,
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
}