package moe.bunbun.news.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bunbun_prefs")

enum class SyncInterval(val minutes: Long) {
    FAST(15),
    NORMAL(30),
    RELAXED(60),
    MANUAL(120);

    companion object {
        fun fromMinutes(m: Long): SyncInterval = entries.firstOrNull { it.minutes == m } ?: NORMAL
    }
}

/** 主题模式（v0.2 扩展为四态，含护眼） */
enum class ThemeMode(val key: String) {
    LIGHT("light"),     // 浅色
    DARK("dark"),       // 深色
    EYE_CARE("eye"),    // 护眼（米黄底深棕字）

    ;

    companion object {
        fun fromKey(key: String?): ThemeMode? = entries.firstOrNull { it.key == key }
    }
}

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val SYNC_INTERVAL = intPreferencesKey("sync_interval_minutes")
    private val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
    // v0.1.1 旧字段保留读取兼容（v0.2 已切到 theme_mode）
    private val DARK_MODE_LEGACY = booleanPreferencesKey("dark_mode")
    private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")

    /** 主题偏好：null = 跟随系统 */
    val themeMode: Flow<ThemeMode?> = context.dataStore.data.map { prefs ->
        // 优先读新字段；旧用户从 darkMode 字段迁移
        ThemeMode.fromKey(prefs[THEME_MODE])
            ?: prefs[DARK_MODE_LEGACY]?.let { legacy ->
                if (legacy) ThemeMode.DARK else ThemeMode.LIGHT
            }
    }

    /** 是否启用 Material You 动态配色（Android 12+ 有效） */
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR] ?: true }

    val syncInterval: Flow<SyncInterval> = context.dataStore.data.map {
        SyncInterval.fromMinutes((it[SYNC_INTERVAL] ?: 30).toLong())
    }

    val firstLaunchDone: Flow<Boolean> = context.dataStore.data.map { it[FIRST_LAUNCH_DONE] ?: false }

    /** 设置主题偏好（null = 跟随系统） */
    suspend fun setThemeMode(mode: ThemeMode?) {
        context.dataStore.edit { prefs ->
            if (mode == null) {
                prefs.remove(THEME_MODE)
                // 同步清掉 legacy darkMode 字段，避免下次启动被旧值复活
                prefs.remove(DARK_MODE_LEGACY)
            } else {
                prefs[THEME_MODE] = mode.key
            }
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setSyncInterval(interval: SyncInterval) {
        context.dataStore.edit { it[SYNC_INTERVAL] = interval.minutes.toInt() }
    }

    suspend fun markFirstLaunchDone() {
        context.dataStore.edit { it[FIRST_LAUNCH_DONE] = true }
    }
}