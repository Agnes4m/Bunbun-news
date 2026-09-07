package moe.bunbun.news.ui.common

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
import moe.bunbun.news.ui.onboarding.OnboardingViewModel
import moe.bunbun.news.ui.onboarding.SAMPLE_FEED_NAMES
import moe.bunbun.news.ui.onboarding.SAMPLE_FEEDS_OPML

/**
 * "暂无订阅源 / 文章" 空状态页（v0.2 主题 A 子 X）。
 *
 * 显示场景：
 * - HomeScreen 热文章列表为空（首次启动 / 用户清空了所有源 / sync 全失败）
 * - SubscriptionsScreen 订阅列表为空
 *
 * 提供两个动作：
 * - "一键导入 6 个推荐源"：复用 OnboardingViewModel.importSampleFeeds 写库 + 触发同步
 * - "手动添加 RSS 订阅"：回调到 [onNavigateToManageFeeds]（由宿主决定跳转哪个 Tab）
 *
 * 设计要点：
 * - 不写入 firstLaunchDone（真正的"已完成首启"语义不存在）；
 *   只要用户没订阅源，无论是否点过这个页，下次还会显示。
 * - 比 OnboardingScreen 少一个"跳过"按钮：
 *   空状态下用户必须做出选择（导入 / 手动添加），不能"跳过"。
 */
@Composable
fun EmptyFeedScreen(
    onNavigateToManageFeeds: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
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
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("🪶", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "还没有文章",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "先添加 RSS 订阅源，开始你的新闻流",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // 推荐源卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "v0.2 精选 ${SAMPLE_FEED_NAMES.size} 个推荐订阅源",
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
                    LazyColumn(modifier = Modifier.height(140.dp)) {
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
                    onClick = onNavigateToManageFeeds,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("手动添加 RSS 订阅")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}