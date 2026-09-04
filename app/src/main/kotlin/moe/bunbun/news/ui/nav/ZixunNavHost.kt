package moe.bunbun.news.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import moe.bunbun.news.R
import moe.bunbun.news.ui.categories.CategoriesScreen
import moe.bunbun.news.ui.home.HomeScreen
import moe.bunbun.news.ui.managefeeds.ManageFeedsScreen
import moe.bunbun.news.ui.onboarding.OnboardingScreen
import moe.bunbun.news.ui.profile.AboutScreen
import moe.bunbun.news.ui.profile.HistoryListScreen
import moe.bunbun.news.ui.profile.ProfileScreen
import moe.bunbun.news.ui.profile.SettingsScreen
import moe.bunbun.news.ui.profile.StarredListScreen
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
    Categories("categories", R.string.tab_categories, Icons.Filled.Category),
    Subscriptions("subscriptions", R.string.tab_subscriptions, Icons.Filled.RssFeed),
    Profile("profile", R.string.tab_profile, Icons.Filled.Person),
}

private enum class SubScreen { None, ManageFeeds, History, Starred, Settings, About }

@Composable
fun ZixunNavHost(modifier: Modifier = Modifier) {
    val rootViewModel: MainViewModel = hiltViewModel()
    val firstLaunchDone by rootViewModel.firstLaunchDone.collectAsState()

    // 首次启动（firstLaunchDone 还没读到或为 false）显示 OnboardingScreen，
    // 主界面在引导页导入完成 / 跳过后才接管。
    if (firstLaunchDone != true) {
        OnboardingScreen(modifier = modifier)
        return
    }

    var selected by remember { mutableStateOf(TopDestination.Home) }
    var subScreen by remember { mutableStateOf(SubScreen.None) }
    var readingArticleId by remember { mutableStateOf<String?>(null) }

    val onArticleClick: (String) -> Unit = { id -> readingArticleId = id }
    val onBackToTab: () -> Unit = { subScreen = SubScreen.None }

    // 返回键：阅读器 → 列表 → Tab（子页面）→ 退出（仅在可返回时拦截）
    BackHandler(enabled = readingArticleId != null || subScreen != SubScreen.None) {
        when {
            readingArticleId != null -> readingArticleId = null
            subScreen != SubScreen.None -> subScreen = SubScreen.None
        }
    }

    val showBottomBar = subScreen == SubScreen.None && readingArticleId == null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
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
            else -> when (subScreen) {
                SubScreen.ManageFeeds -> ManageFeedsScreen(
                    onBack = onBackToTab,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                SubScreen.History -> HistoryListScreen(
                    onBack = onBackToTab,
                    onArticleClick = onArticleClick,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                SubScreen.Starred -> StarredListScreen(
                    onBack = onBackToTab,
                    onArticleClick = onArticleClick,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                SubScreen.Settings -> SettingsScreen(
                    onBack = onBackToTab,
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                )
                SubScreen.About -> AboutScreen(
                    onBack = onBackToTab,
                )
                SubScreen.None -> when (selected) {
                    TopDestination.Home -> HomeScreen(
                        onArticleClick = onArticleClick,
                        modifier = modifier.fillMaxSize().padding(innerPadding),
                    )
                    TopDestination.Search -> SearchScreen(
                        onArticleClick = onArticleClick,
                        modifier = modifier.fillMaxSize().padding(innerPadding),
                    )
                    TopDestination.Categories -> CategoriesScreen(
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
                        onNavigateToHistory = { subScreen = SubScreen.History },
                        onNavigateToStarred = { subScreen = SubScreen.Starred },
                        onNavigateToSettings = { subScreen = SubScreen.Settings },
                        onNavigateToAbout = { subScreen = SubScreen.About },
                        modifier = modifier.fillMaxSize().padding(innerPadding),
                    )
                }
            }
        }
    }
}