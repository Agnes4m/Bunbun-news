package moe.bunbun.news.ui.categories

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.ui.common.ArticleCard
import java.time.Instant
import java.time.ZoneId

/**
 * 分类浏览页（v0.1 新增 Tab）：
 * 顶部一排分类 chips（综合/科技/财经…），点击切换过滤文章。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onArticleClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_categories)) })
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 分类 chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = category == uiState.selectedCategory,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category) },
                    )
                }
            }

            // 文章列表
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.articles.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("🏷️", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "该分类下暂无文章",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "去订阅页添加对应分类的 RSS 源吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    val todayLabel = stringResource(R.string.categories_group_today)
                    val yesterdayLabel = stringResource(R.string.categories_group_yesterday)
                    val weekLabel = stringResource(R.string.categories_group_this_week)
                    val earlierLabel = stringResource(R.string.categories_group_earlier)
                    val groups = remember(uiState.articles, todayLabel, yesterdayLabel, weekLabel, earlierLabel) {
                        groupArticlesByDate(
                            uiState.articles,
                            todayLabel,
                            yesterdayLabel,
                            weekLabel,
                            earlierLabel,
                        )
                    }
                    LazyColumn {
                        groups.forEach { group ->
                            item(key = "header-${group.label}") {
                                DateGroupHeader(group.label, group.articles.size)
                            }
                            items(group.articles, key = { it.id }) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = { onArticleClick(article.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 日期分组（v0.2 体验增强）：把分类下的文章按发布时间分到 今天 / 昨天 / 本周 / 更早
 */
private data class DateGroup(val label: String, val articles: List<Article>)

/**
 * 按 publishedAt 把文章分到 4 段。无 publishedAt 的文章归入"更早"。
 *
 * @param todayLabel/yesterdayLabel/thisWeekLabel/earlierLabel 传入本地化字符串
 */
private fun groupArticlesByDate(
    articles: List<Article>,
    todayLabel: String,
    yesterdayLabel: String,
    thisWeekLabel: String,
    earlierLabel: String,
    zone: ZoneId = ZoneId.systemDefault(),
    now: Instant = Instant.now(),
): List<DateGroup> {
    val today = now.atZone(zone).toLocalDate()
    val todayStart = today.atStartOfDay(zone).toInstant()
    val yesterdayStart = today.minusDays(1).atStartOfDay(zone).toInstant()
    val weekStart = today.minusDays(7).atStartOfDay(zone).toInstant()

    val todayArts = mutableListOf<Article>()
    val yesterdayArts = mutableListOf<Article>()
    val weekArts = mutableListOf<Article>()
    val earlierArts = mutableListOf<Article>()
    for (article in articles) {
        val ts = article.publishedAt ?: continue
        when {
            ts >= todayStart -> todayArts.add(article)
            ts >= yesterdayStart -> yesterdayArts.add(article)
            ts >= weekStart -> weekArts.add(article)
            else -> earlierArts.add(article)
        }
    }

    return buildList {
        if (todayArts.isNotEmpty()) add(DateGroup(todayLabel, todayArts.toList()))
        if (yesterdayArts.isNotEmpty()) add(DateGroup(yesterdayLabel, yesterdayArts.toList()))
        if (weekArts.isNotEmpty()) add(DateGroup(thisWeekLabel, weekArts.toList()))
        if (earlierArts.isNotEmpty()) add(DateGroup(earlierLabel, earlierArts.toList()))
    }
}

@Composable
private fun DateGroupHeader(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.padding(start = 8.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
