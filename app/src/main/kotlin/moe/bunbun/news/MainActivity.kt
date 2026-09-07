package moe.bunbun.news

import android.content.Context
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
import moe.bunbun.news.data.prefs.AppLocale
import moe.bunbun.news.i18n.LocaleHelper
import moe.bunbun.news.ui.nav.MainViewModel
import moe.bunbun.news.ui.nav.ZixunNavHost
import moe.bunbun.news.ui.theme.BunbunNewsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        // attachBaseContext 在 Hilt 注入之前调用，**不能**依赖 @Inject lateinit var。
        // 直接同步读 SharedPreferences（LocaleCache 用的就是同一份 sp 文件），
        // 这是 attachBaseContext 唯一可用的同步路径。
        val locale = readCachedLocale(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, locale))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 订阅 UserPreferences 主题相关字段，喂给 BunbunNewsTheme，
            // 让用户在 Settings 切换主题时 UI 立刻变色（v0.1.1 修复 → v0.2 扩展为 4 态）
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()
            BunbunNewsTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ZixunNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    companion object {
        private const val LOCALE_SP = "bunbun_locale_cache"
        private const val LOCALE_KEY = "app_locale"
        private fun readCachedLocale(ctx: Context): AppLocale {
            return try {
                val key = ctx.applicationContext
                    .getSharedPreferences(LOCALE_SP, Context.MODE_PRIVATE)
                    .getString(LOCALE_KEY, null)
                AppLocale.fromKey(key)
            } catch (_: Throwable) {
                AppLocale.SYSTEM
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
