package moe.bunbun.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import moe.bunbun.news.ui.nav.MainViewModel
import moe.bunbun.news.ui.nav.ZixunNavHost
import moe.bunbun.news.ui.theme.BunbunNewsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 订阅 UserPreferences.darkMode 并喂给 BunbunNewsTheme，
            // 让用户在 Settings 切深色时 UI 立刻变色（M8 遗留 bug 修复）
            val darkMode by viewModel.darkMode.collectAsState()
            BunbunNewsTheme(darkTheme = darkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ZixunNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityPreview() {
    BunbunNewsTheme {
        ZixunNavHost()
    }
}
