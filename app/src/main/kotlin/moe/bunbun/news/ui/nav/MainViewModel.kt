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
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferences: UserPreferences,
) : ViewModel() {

    val firstLaunchDone: StateFlow<Boolean?> = userPreferences.firstLaunchDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
