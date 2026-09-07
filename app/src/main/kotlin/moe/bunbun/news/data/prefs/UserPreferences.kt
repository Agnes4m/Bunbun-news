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

/**
 * 云端后端类型（v0.2 主题 C 子 4 配置持久化）。
 * 用 string 存 DataStore，启动时映射回 [SyncBackend]。
 */
enum class BackendType(val key: String) {
    LOCAL("local"),
    MINIFLUX("miniflux"),
    FEVER("fever"),
    ;

    companion object {
        fun fromKey(key: String?): BackendType = entries.firstOrNull { it.key == key } ?: LOCAL
    }
}

/**
 * AI 摘要 provider 类型（v0.2 主题 D 子 4）。
 * OFF / DEEPSEEK / LOCAL。
 */
enum class SummaryProviderType(val key: String) {
    OFF("off"),
    DEEPSEEK("deepseek"),
    LOCAL("local"),
    ;

    companion object {
        fun fromKey(key: String?): SummaryProviderType = entries.firstOrNull { it.key == key } ?: OFF
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

    // v0.2 主题 C 子 4：后端同步配置
    private val BACKEND_TYPE = stringPreferencesKey("backend_type")
    private val BACKEND_URL = stringPreferencesKey("backend_url")
    private val BACKEND_USERNAME = stringPreferencesKey("backend_username")
    private val BACKEND_API_KEY = stringPreferencesKey("backend_api_key")
    // 同步方向：PULL_ONLY / PUSH_ONLY / BIDIRECTIONAL / DISABLED
    private val SYNC_DIRECTION = stringPreferencesKey("sync_direction")

    // v0.2 主题 D 子 4：AI 摘要 provider 配置
    private val SUMMARY_PROVIDER = stringPreferencesKey("summary_provider")
    private val DEEPSEEK_API_KEY = stringPreferencesKey("deepseek_api_key")

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

    /** 后端类型（默认 LOCAL） */
    val backendType: Flow<BackendType> = context.dataStore.data.map {
        BackendType.fromKey(it[BACKEND_TYPE])
    }

    /** 后端配置快照（url / username / apiKey 任一字段缺失时返回 null，由调用方回退到 LOCAL） */
    val backendUrl: Flow<String?> = context.dataStore.data.map { it[BACKEND_URL] }
    val backendUsername: Flow<String?> = context.dataStore.data.map { it[BACKEND_USERNAME] }
    val backendApiKey: Flow<String?> = context.dataStore.data.map { it[BACKEND_API_KEY] }

    /** 同步方向（默认 BIDIRECTIONAL；LOCAL_ONLY 后端强制禁用 push） */
    val syncDirection: Flow<String> = context.dataStore.data.map { it[SYNC_DIRECTION] ?: "BIDIRECTIONAL" }

    /** AI 摘要 provider（默认 OFF） */
    val summaryProvider: Flow<SummaryProviderType> = context.dataStore.data.map {
        SummaryProviderType.fromKey(it[SUMMARY_PROVIDER])
    }

    /** DeepSeek API key */
    val deepseekApiKey: Flow<String?> = context.dataStore.data.map { it[DEEPSEEK_API_KEY] }

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

    suspend fun setBackend(
        type: BackendType,
        url: String? = null,
        username: String? = null,
        apiKey: String? = null,
    ) {
        context.dataStore.edit { prefs ->
            prefs[BACKEND_TYPE] = type.key
            if (url != null) prefs[BACKEND_URL] = url else prefs.remove(BACKEND_URL)
            if (username != null) prefs[BACKEND_USERNAME] = username else prefs.remove(BACKEND_USERNAME)
            if (apiKey != null) prefs[BACKEND_API_KEY] = apiKey else prefs.remove(BACKEND_API_KEY)
        }
    }

    suspend fun setSyncDirectionPref(direction: String) {
        context.dataStore.edit { it[SYNC_DIRECTION] = direction }
    }

    suspend fun setSummaryProvider(type: SummaryProviderType, deepseekApiKey: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[SUMMARY_PROVIDER] = type.key
            if (deepseekApiKey != null) prefs[DEEPSEEK_API_KEY] = deepseekApiKey
        }
    }
}