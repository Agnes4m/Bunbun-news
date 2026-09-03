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
import moe.bunbun.news.ui.reader.ReaderScreen
import moe.bunbun.news.ui.search.SearchScreen
import moe.bunbun.news.ui.subscriptions.SubscriptionsScreen

private enum class TopDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.tab_home, Icons.Filled.Home),
    Search("search", R.string.tab_search, Icons.Filled.Search),
    Subscriptions("subscriptions", R.string.tab_subscriptions, Icons.Filled.RssFeed),
    Profile("profile", R.string.tab_profile, Icons.Filled.Person),
}

private enum class SubScreen { ManageFeeds, None }

@Composable
fun ZixunNavHost(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(TopDestination.Home) }
    var subScreen by remember { mutableStateOf(SubScreen.None) }
    var readingArticleId by remember { mutableStateOf<String?>(null) }

    val onArticleClick: (String) -> Unit = { id -> readingArticleId = id }

    Scaffold(
        bottomBar = {
            if (subScreen == SubScreen.None && readingArticleId == null) {
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
        when {
            readingArticleId != null -> ReaderScreen(
                articleId = readingArticleId!!,
                onBack = { readingArticleId = null },
                modifier = modifier.fillMaxSize().padding(innerPadding),
            )
            subScreen == SubScreen.ManageFeeds -> ManageFeedsScreen(
                onBack = { subScreen = SubScreen.None },
                modifier = modifier.fillMaxSize().padding(innerPadding),
            )
            else -> when (selected) {
                TopDestination.Home -> HomeScreen(
                    onArticleClick = onArticleClick,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                TopDestination.Search -> SearchScreen(
                    onArticleClick = onArticleClick,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                TopDestination.Subscriptions -> SubscriptionsScreen(
                    onNavigateToManageFeeds = { subScreen = SubScreen.ManageFeeds },
                    onArticleClick = onArticleClick,
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