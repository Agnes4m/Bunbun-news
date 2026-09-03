package moe.bunbun.news.ui.reader

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import moe.bunbun.news.domain.model.Article

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    articleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenInBrowser: (String) -> Unit = {},
) {
    val viewModel: ReaderViewModel = hiltViewModel()
    val article by viewModel.articleState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Text("加载中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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