package moe.bunbun.news.ui.reader

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.domain.model.Article
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    articleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenInBrowser: (String) -> Unit = {},
    /** v0.2 Plan A：点 timeline 上的 chip 切换到同事件的其他文章。 */
    onSwitchArticle: (String) -> Unit = {},
) {
    val viewModel: ReaderViewModel = hiltViewModel(key = "reader-$articleId")
    val article by viewModel.articleState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(articleId) { viewModel.setArticleId(articleId) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(article?.title ?: "", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    val isStarred = article?.isStarred == true
                    IconButton(onClick = { viewModel.toggleStar() }) {
                        Icon(
                            if (isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = stringResource(
                                if (isStarred) R.string.cd_unstar else R.string.cd_star
                            ),
                            tint = if (isStarred) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val current = article
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (current != null) {
                if (current.clusterId != null) {
                    EventSubscriptionBar(
                        isSubscribed = uiState.isEventSubscribed,
                        onToggle = viewModel::toggleEventSubscription,
                    )
                    // v0.2 Plan A：事件时间线（同 cluster 多于 1 篇才显示）
                    EventTimelineStrip(
                        siblings = uiState.clusterSiblings,
                        currentArticleId = current.id,
                        feedTitlesById = uiState.feedTitlesById,
                        onSwitchArticle = onSwitchArticle,
                    )
                }
                SummaryCard(
                    summary = uiState.summary,
                    loading = uiState.summaryLoading,
                    onResummarize = viewModel::resummarize,
                )
                // v0.2-Reader-Content：拉全文进度条 / 失败提示
                FulltextStatusBar(
                    loading = uiState.fulltextLoading,
                    failed = uiState.fulltextFailed,
                    onRetry = viewModel::retryFulltext,
                )
                ArticleWebView(
                    html = current.contentHtml ?: current.excerpt ?: "<p>${current.url}</p>",
                    title = current.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.reader_loading_article),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * v0.2-Reader-Content：全文加载状态条。
 *
 * - loading：进度环 + 提示文字
 * - failed：警示文字 + "重试" 按钮（用户主动触发再试一次）
 * - 都 false：不渲染（透明）
 */
@Composable
private fun FulltextStatusBar(
    loading: Boolean,
    failed: Boolean,
    onRetry: () -> Unit,
) {
    if (!loading && !failed) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.height(16.dp),
                strokeWidth = 2.dp,
            )
            Text(
                stringResource(R.string.fulltext_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (failed) {
            Text(
                stringResource(R.string.fulltext_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.fulltext_retry), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * AI 摘要卡片（v0.2 主题 D 子 7 — ReaderScreen 接入）。
 *
 * 渲染规则：
 * - summaryLoading=true 且 summary=""：显示进度环 + "生成中…"
 * - summary 非空：显示摘要 + 重新生成按钮
 * - summary 加载完毕但为 null：显示"暂无摘要"提示
 * - summary 为空字符串（初始 "" 状态）：折叠整个卡片（不显示）
 */
@Composable
private fun SummaryCard(
    summary: String?,
    loading: Boolean,
    onResummarize: () -> Unit,
) {
    // 完全没请求过：summary==null 时不显示任何东西
    if (summary == null && !loading) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.summary_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            } else if (summary != null) {
                TextButton(onClick = onResummarize) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.height(16.dp),
                    )
                    Spacer(Modifier.height(0.dp))
                    Text(
                        stringResource(R.string.summary_resummarize),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        when {
            loading && summary.isNullOrBlank() -> {
                Text(
                    stringResource(R.string.summary_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            summary.isNullOrBlank() -> {
                Text(
                    stringResource(R.string.summary_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            else -> {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun EventSubscriptionBar(isSubscribed: Boolean, onToggle: () -> Unit) {
    FilledTonalButton(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(
            if (isSubscribed) Icons.Filled.Bookmark else Icons.Filled.OpenInBrowser,
            contentDescription = null,
        )
        Text(
            // v0.2 i18n 收尾：用 stringResource 替掉之前的硬编码中文
            text = "  " + stringResource(
                if (isSubscribed) R.string.subscribed_event else R.string.unsubscribed_event
            ),
            fontWeight = if (isSubscribed) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

/**
 * v0.2 Plan A 事件时间线（同 cluster 的所有文章，按时间排）。
 *
 * 渲染规则：
 * - siblings.size <= 1：不渲染（单源或唯一报道）
 * - 当前文章的 chip 用 primaryContainer 高亮
 * - 首报 chip 标 "● 首报"；其他标 "后续" / "回应"（按 publishedAt 是否晚于集群中位数）
 *
 * 点 chip → onSwitchArticle(articleId) → ReaderViewModel.setArticleId 切过去
 */
@Composable
private fun EventTimelineStrip(
    siblings: List<Article>,
    currentArticleId: String,
    feedTitlesById: Map<String, String>,
    onSwitchArticle: (String) -> Unit,
) {
    if (siblings.size <= 1) return

    val firstReport = siblings.minByOrNull { it.publishedAt ?: it.fetchedAt } ?: siblings.first()
    val medianTime = siblings
        .mapNotNull { it.publishedAt }
        .sorted()
        .let { ts -> if (ts.isEmpty()) null else ts[ts.size / 2] }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = stringResource(R.string.event_timeline_title) + " · " +
                    stringResource(R.string.event_timeline_summary, siblings.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.size(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(siblings, key = { it.id }) { sibling ->
                val isCurrent = sibling.id == currentArticleId
                val isFirst = sibling.id == firstReport.id
                val roleLabel = when {
                    isFirst -> stringResource(R.string.event_first_report)
                    medianTime != null && (sibling.publishedAt ?: sibling.fetchedAt).isAfter(medianTime) ->
                        stringResource(R.string.event_response)
                    else -> stringResource(R.string.event_followup)
                }
                val feedName = feedTitlesById[sibling.feedId] ?: ""
                AssistChip(
                    onClick = { if (!isCurrent) onSwitchArticle(sibling.id) },
                    label = {
                        Text(
                            text = "$roleLabel · $feedName · ${relativeShort(sibling.publishedAt ?: sibling.fetchedAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    colors = if (isCurrent) {
                        AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        AssistChipDefaults.assistChipColors()
                    },
                )
            }
        }
    }
}

/** 短相对时间（用于 timeline chip）：如 "2h"、"30m"、"3d" */
private fun relativeShort(instant: Instant): String {
    val now = Instant.now()
    val d = Duration.between(instant, now)
    return when {
        d.isNegative -> "now"
        d.toMinutes() < 1 -> "now"
        d.toMinutes() < 60 -> "${d.toMinutes()}m"
        d.toHours() < 24 -> "${d.toHours()}h"
        d.toDays() < 7 -> "${d.toDays()}d"
        else -> {
            val dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            "${dt.monthValue}/${dt.dayOfMonth}"
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ArticleWebView(html: String, title: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val styledHtml = remember(html) { wrapInArticleTemplate(html, title) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.apply {
                    javaScriptEnabled = false
                    domStorageEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                isVerticalScrollBarEnabled = true
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        },
    )
}

private fun wrapInArticleTemplate(content: String, title: String): String {
    val safe = content
        .replace("<script", "&lt;script")
        .replace("</script", "&lt;/script")
    return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
           line-height: 1.6; padding: 16px; color: #222; max-width: 720px; margin: 0 auto; }
    h1 { font-size: 1.5em; line-height: 1.3; margin-bottom: 0.5em; }
    img { max-width: 100%; height: auto; }
    pre, code { background: #f5f5f5; padding: 8px; border-radius: 4px; overflow-x: auto; }
    blockquote { border-left: 3px solid #ccc; margin-left: 0; padding-left: 12px; color: #555; }
  </style>
</head>
<body>
  <h1>${title.replace("<", "&lt;")}</h1>
  $safe
</body>
</html>
    """.trimIndent()
}