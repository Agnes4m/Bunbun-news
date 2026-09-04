package moe.bunbun.news.ui.managefeeds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.domain.model.Feed
import moe.bunbun.news.ui.onboarding.SAMPLE_FEEDS_OPML

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFeedsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageFeedsViewModel = hiltViewModel(),
) {
    val feeds by viewModel.feeds.collectAsState()
    val isAdding by viewModel.isAdding.collectAsState()
    var pendingOpml by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.profile_manage_feeds)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.syncNow() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "立即同步")
                        }
                        IconButton(onClick = { pendingOpml = sampleOpmlContent() }) {
                            Icon(Icons.Filled.FileUpload, contentDescription = "导入示例 OPML")
                        }
                        IconButton(onClick = { viewModel.showAddDialog() }) {
                            Icon(Icons.Filled.Add, contentDescription = "添加订阅")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (feeds.isEmpty()) {
                    EmptyHint()
                } else {
                    Text(
                        "${feeds.size} 个订阅源",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(feeds, key = { it.id }) { feed ->
                            FeedRow(feed = feed, onDelete = { viewModel.deleteFeed(feed.id) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

    if (isAdding) {
        AddFeedDialog(
            onDismiss = viewModel::dismissAddDialog,
            onConfirm = { url, title -> viewModel.addFeed(url, title) },
        )
    }

    pendingOpml?.let { opml ->
        viewModel.importOpml(opml)
        pendingOpml = null
    }
}

@Composable
private fun FeedRow(feed: Feed, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.RssFeed,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                feed.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                feed.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun EmptyHint() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.RssFeed,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.empty_placeholder),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            "点击右上角 + 添加，或导入示例 OPML",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AddFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String?) -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_feed)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it.trim() },
                    label = { Text("RSS / Atom URL") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题（可选）") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (url.isNotBlank()) onConfirm(url, title.takeIf { it.isNotBlank() })
                },
                enabled = url.isNotBlank(),
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

/** v0.1 内置推荐订阅源 —— 复用 OnboardingScreen 的共享常量，保持单一数据源 */
private fun sampleOpmlContent(): String = SAMPLE_FEEDS_OPML

@Preview
@Composable
private fun ManageFeedsScreenPreview() {
    // 预览只展示静态 UI；真实 ViewModel 由 Hilt 注入
    MaterialTheme {
        AddFeedDialog(onDismiss = {}, onConfirm = { _, _ -> })
    }
}