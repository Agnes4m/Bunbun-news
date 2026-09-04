package moe.bunbun.news.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import moe.bunbun.news.data.prefs.UserPreferences
import javax.inject.Inject

/**
 * 主导航根 ViewModel。
 *
 * - [firstLaunchDone]：是否已完成首次启动引导；未完成时显示 [OnboardingScreen]
 * - [themeMode]：用户主题偏好（null=跟随系统 / ThemeMode.LIGHT/DARK/EYE_CARE）；
 *   由 [MainActivity] 订阅并传入 [BunbunNewsTheme]，切换立刻生效
 * - [dynamicColor]：是否启用 Material You 动态配色（Android 12+ 有效）
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferences: UserPreferences,
) : ViewModel() {

    val firstLaunchDone: StateFlow<Boolean?> = userPreferences.firstLaunchDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val themeMode: StateFlow<moe.bunbun.news.data.prefs.ThemeMode?> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val dynamicColor: StateFlow<Boolean> = userPreferences.dynamicColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
