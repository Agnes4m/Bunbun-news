package moe.bunbun.news.ui.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import moe.bunbun.news.R

private enum class TopDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home(
        route = "home",
        labelRes = R.string.tab_home,
        icon = Icons.Filled.Home,
    ),
    Search(
        route = "search",
        labelRes = R.string.tab_search,
        icon = Icons.Filled.Search,
    ),
    Subscriptions(
        route = "subscriptions",
        labelRes = R.string.tab_subscriptions,
        icon = Icons.Filled.RssFeed,
    ),
    Profile(
        route = "profile",
        labelRes = R.string.tab_profile,
        icon = Icons.Filled.Person,
    ),
}

@Composable
fun ZixunNavHost(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(TopDestination.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = selected == dest,
                        onClick = { selected = dest },
                        icon = { Icon(dest.icon, contentDescription = null) },
                        label = { Text(stringResource(dest.labelRes)) },
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
            val currentLabel = stringResource(selected.labelRes)
            Text(
                "🪶 文闻 Bunbun News",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text("当前 Tab: $currentLabel", style = MaterialTheme.typography.bodyLarge)
            Text("—— M1 占位，M2 起填功能", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ZixunNavHostPreview() {
    ZixunNavHost()
}