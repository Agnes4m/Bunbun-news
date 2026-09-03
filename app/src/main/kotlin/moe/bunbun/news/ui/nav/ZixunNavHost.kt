package moe.bunbun.news.ui.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

private enum class TopDestination(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
) {
    Timeline(
        route = "timeline",
        label = "时间线",
        icon = { Icon(Icons.Filled.Star, contentDescription = null) },
    ),
}

@Composable
fun ZixunNavHost(modifier: Modifier = Modifier) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                TopDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* TODO: M6 */ },
                        icon = dest.icon,
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🪶 文闻 Bunbun News", style = MaterialTheme.typography.headlineMedium)
            Text("M1 脚手架就绪", style = MaterialTheme.typography.bodyLarge)
            Text("—— 后续 M2 起逐版本添加功能", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ZixunNavHostPreview() {
    ZixunNavHost()
}
