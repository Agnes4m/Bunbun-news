package moe.bunbun.news.ui.reader

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.domain.model.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    articleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenInBrowser: (String) -> Unit = {},
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    val isStarred = article?.isStarred == true
                    IconButton(onClick = { viewModel.toggleStar() }) {
                        Icon(
                            if (isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "收藏",
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
                }
                SummaryCard(
                    summary = uiState.summary,
                    loading = uiState.summaryLoading,
                    onResummarize = viewModel::resummarize,
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
                    Text("加载中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
            if (isSubscribed) "  已订阅此事件（点此取消）" else "  订阅此事件后续报道",
            fontWeight = if (isSubscribed) FontWeight.SemiBold else FontWeight.Normal,
        )
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