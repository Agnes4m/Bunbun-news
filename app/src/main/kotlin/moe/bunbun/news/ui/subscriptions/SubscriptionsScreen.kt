package moe.bunbun.news.ui.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import moe.bunbun.news.R

/**
 * 订阅 Tab 屏幕（M5 占位 + 入口，M6 完整实现时间线）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    onNavigateToManageFeeds: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_subscriptions)) },
                actions = {
                    IconButton(onClick = onNavigateToManageFeeds) {
                        Icon(Icons.Filled.Settings, contentDescription = "管理订阅")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "📡 " + stringResource(R.string.tab_subscriptions),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                "—— M6 填充已订内容时间线（M5 占位）",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}