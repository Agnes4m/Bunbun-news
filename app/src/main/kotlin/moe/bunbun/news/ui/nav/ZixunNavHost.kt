package moe.bunbun.news.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import moe.bunbun.news.R
import moe.bunbun.news.ui.home.HomeScreen
import moe.bunbun.news.ui.managefeeds.ManageFeedsScreen
import moe.bunbun.news.ui.profile.ProfileScreen
import moe.bunbun.news.ui.search.SearchScreen
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

private enum class SubScreen { ManageFeeds, None }

@Composable
fun ZixunNavHost(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(TopDestination.Home) }
    var subScreen by remember { mutableStateOf(SubScreen.None) }

    Scaffold(
        bottomBar = {
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
                modifier = modifier.fillMaxSize().padding(innerPadding),
            )
            SubScreen.None -> when (selected) {
                TopDestination.Home -> HomeScreen(
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                TopDestination.Search -> SearchScreen(
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                TopDestination.Subscriptions -> SubscriptionsScreen(
                    onNavigateToManageFeeds = { subScreen = SubScreen.ManageFeeds },
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                TopDestination.Profile -> ProfileScreen(
                    onNavigateToManageFeeds = { subScreen = SubScreen.ManageFeeds },
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
            }
        }
    }
}