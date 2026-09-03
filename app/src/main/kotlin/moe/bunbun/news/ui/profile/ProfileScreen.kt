package moe.bunbun.news.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.bunbun.news.R

/**
 * 个人 Tab（M5 占位 + 入口；M8 完整实现历史/收藏/设置等）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToManageFeeds: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.tab_profile)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 入口菜单（M8 会接 HistoryRepo/StarListViewModel 等真实数据）
            MenuSection(
                items = listOf(
                    MenuItem(stringResource(R.string.profile_history), Icons.Filled.History) {},
                    MenuItem(stringResource(R.string.profile_starred), Icons.Filled.Bookmark) {},
                    MenuItem(stringResource(R.string.profile_manage_feeds), Icons.Filled.RssFeed, onClick = onNavigateToManageFeeds),
                    MenuItem(stringResource(R.string.profile_settings), Icons.Filled.Settings, onClick = onNavigateToSettings),
                    MenuItem(stringResource(R.string.profile_about), Icons.Filled.Info) {},
                ),
            )
        }
    }
}

private data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit = {},
)

@Composable
private fun MenuSection(items: List<MenuItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(items, key = { it.title }) { item ->
            MenuRow(item)
            HorizontalDivider()
        }
    }
}

@Composable
private fun MenuRow(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = item.onClick) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}