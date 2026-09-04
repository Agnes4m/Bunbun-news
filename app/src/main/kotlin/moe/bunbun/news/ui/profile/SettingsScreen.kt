package moe.bunbun.news.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.bunbun.news.R
import moe.bunbun.news.data.prefs.SyncInterval
import moe.bunbun.news.data.prefs.ThemeMode
import moe.bunbun.news.data.prefs.UserPreferences
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
) : ViewModel() {
    /** null = 跟随系统；LIGHT/DARK/EYE_CARE 三态 */
    val themeMode: StateFlow<ThemeMode?> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val dynamicColor: StateFlow<Boolean> = prefs.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val syncInterval: StateFlow<SyncInterval> = prefs.syncInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncInterval.NORMAL)

    fun setThemeMode(mode: ThemeMode?) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefs.setDynamicColor(enabled) }
    }

    fun setSyncInterval(interval: SyncInterval) {
        viewModelScope.launch { prefs.setSyncInterval(interval) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val syncInterval by viewModel.syncInterval.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader("外观")
            // 四态主题：跟随系统 / 浅色 / 深色 / 护眼（v0.2）
            ThemeOption.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = themeMode == option.themeMode,
                        onClick = { viewModel.setThemeMode(option.themeMode) },
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(option.label, fontWeight = FontWeight.Medium)
                        Text(
                            option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            // Material You 动态配色开关
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Material You 动态配色", fontWeight = FontWeight.Medium)
                    Text(
                        "Android 12+ 取壁纸主色；护眼主题下忽略",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }
            HorizontalDivider()

            SectionHeader("同步")
            SyncInterval.entries.forEach { interval ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = interval == syncInterval,
                        onClick = { viewModel.setSyncInterval(interval) },
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(interval.label(), fontWeight = FontWeight.Medium)
                        Text(
                            interval.description(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            HorizontalDivider()

            SectionHeader("缓存")
            SettingRow(
                title = "清空阅读历史",
                subtitle = "设置项 v0.2 实装",
                trailing = {},
            )
            SettingRow(
                title = "重置数据库",
                subtitle = "设置项 v0.2 实装",
                trailing = {},
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing()
    }
}

private fun SyncInterval.label(): String = when (this) {
    SyncInterval.FAST -> "频繁"
    SyncInterval.NORMAL -> "普通（推荐）"
    SyncInterval.RELAXED -> "省流"
    SyncInterval.MANUAL -> "仅手动"
}

private fun SyncInterval.description(): String = "${minutes} 分钟一次"

/** 主题选项（v0.2 四态：跟随/浅/深/护眼） */
private enum class ThemeOption(
    val label: String,
    val description: String,
    /** 写入 UserPreferences.themeMode 的值（null = 跟随系统） */
    val themeMode: ThemeMode?,
) {
    FOLLOW_SYSTEM("跟随系统", "跟随 Android 系统深色设置", null),
    LIGHT("浅色", "始终使用浅色主题", ThemeMode.LIGHT),
    DARK("深色", "始终使用深色主题（省电）", ThemeMode.DARK),
    EYE_CARE("护眼", "米黄底深棕字，夜间阅读舒适", ThemeMode.EYE_CARE),
}