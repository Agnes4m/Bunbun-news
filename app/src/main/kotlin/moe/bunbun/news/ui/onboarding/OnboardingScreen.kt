package moe.bunbun.news.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 首次启动引导页：
 * - 展示文闻介绍
 * - 列出内置推荐源（前 6 项 + "…+ N 更多"）
 * - 两个动作："一键导入"（主按钮） / "跳过"（文字按钮）
 *
 * 通过 UserPreferences.firstLaunchDone 决定是否展示；
 * 任一动作完成后写入 firstLaunchDone=true，主界面自动接管。
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val importing by viewModel.importing.collectAsState()
    val importedCount by viewModel.importedCount.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 顶部 Logo + 标题（紧凑）
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("🪶", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "文闻 Bunbun News",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "一个轻量级 RSS 新闻聚合器",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            // 推荐源卡片（紧凑 + LazyColumn 可滚动）
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "v0.1 精选 ${SAMPLE_FEED_NAMES.size} 个推荐订阅源",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "覆盖科技 / 国际 / 财经 / 社区",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    val previewCount = 6
                    val previewNames = SAMPLE_FEED_NAMES.take(previewCount)
                    val moreCount = SAMPLE_FEED_NAMES.size - previewCount
                    LazyColumn(modifier = Modifier.height(132.dp)) {
                        items(previewNames) { name ->
                            Text(
                                "• $name",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (moreCount > 0) {
                            item {
                                Text(
                                    "… +$moreCount 更多",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 操作区
            if (importing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        if (importedCount > 0) "已添加 $importedCount 个，准备同步..." else "正在添加推荐源...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.importSampleFeeds(SAMPLE_FEEDS_OPML) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("一键导入 ${SAMPLE_FEED_NAMES.size} 个推荐源")
                }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { viewModel.skip() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("跳过，稍后自己添加")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
