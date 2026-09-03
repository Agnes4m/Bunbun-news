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
import androidx.compose.ui.unit.dp
import moe.bunbun.news.R
import moe.bunbun.news.ui.managefeeds.ManageFeedsScreen
import moe.bunbun.news.ui.profile.ProfileScreen
import moe.bunbun.news.ui.subscriptions.SubscriptionsScreen

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

/** 屏幕外的二级导航：管理订阅页（从订阅 Tab 和个人 Tab 都可进入） */
private enum class SubScreen { ManageFeeds, None }

@Composable
fun ZixunNavHost(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(TopDestination.Home) }
    var subScreen by remember { mutableStateOf(SubScreen.None) }

    Scaffold(
        bottomBar = {
            // 处于二级页面时仍可看到底部 Tab，但不可切换
            if (subScreen == SubScreen.None) {
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
            }
        },
    ) { innerPadding ->
        when (subScreen) {
            SubScreen.ManageFeeds -> ManageFeedsScreen(
                onBack = { subScreen = SubScreen.None },
                modifier = modifier.padding(innerPadding),
            )
            SubScreen.None -> when (selected) {
                TopDestination.Home -> HomePlaceholder(modifier, innerPadding)
                TopDestination.Search -> SearchPlaceholder(modifier, innerPadding)
                TopDestination.Subscriptions -> SubscriptionsScreen(
                    onNavigateToManageFeeds = { subScreen = SubScreen.ManageFeeds },
                )
                TopDestination.Profile -> ProfileScreen(
                    onNavigateToManageFeeds = { subScreen = SubScreen.ManageFeeds },
                )
            }
        }
    }
}

@Composable
private fun HomePlaceholder(modifier: Modifier = Modifier, innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    PlaceholderContent(
        title = "🏠 首页",
        subtitle = "今日文章时间线（M6 填）",
        modifier = modifier,
        innerPadding = innerPadding,
    )
}

@Composable
private fun SearchPlaceholder(modifier: Modifier = Modifier, innerPadding: androidx.compose.foundation.layout.PaddingValues) {
    PlaceholderContent(
        title = "🔍 搜索",
        subtitle = "FTS 全文搜索（M6 填）",
        modifier = modifier,
        innerPadding = innerPadding,
    )
}

@Composable
private fun PlaceholderContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
    }
}