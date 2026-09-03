package moe.bunbun.news.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.bunbun.news.domain.model.Article
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 文章列表卡片（M6 通用组件）。
 * - 点击 → 打开阅读器（M7 接入）
 * - 星标按钮 → 切换收藏
 */
@Composable
fun ArticleCard(
    article: Article,
    onClick: (Article) -> Unit = {},
    onToggleStar: (Article) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onClick(article) },
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (article.isRead) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (article.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!article.excerpt.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            article.excerpt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(
                    onClick = { onToggleStar(article) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        if (article.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (article.isStarred) "取消收藏" else "收藏",
                        tint = if (article.isStarred) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    article.author ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    formatRelativeTime(article.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatRelativeTime(instant: Instant?): String {
    if (instant == null) return ""
    val now = Instant.now()
    val duration = Duration.between(instant, now)
    return when {
        duration.isNegative -> "刚刚"
        duration.toMinutes() < 1 -> "刚刚"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} 分钟前"
        duration.toHours() < 24 -> "${duration.toHours()} 小时前"
        duration.toDays() < 7 -> "${duration.toDays()} 天前"
        else -> {
            val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            "${dateTime.year}-${dateTime.monthValue.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')}"
        }
    }
}