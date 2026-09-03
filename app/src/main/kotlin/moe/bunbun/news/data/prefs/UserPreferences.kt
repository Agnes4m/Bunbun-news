package moe.bunbun.news.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val SYNC_INTERVAL = intPreferencesKey("sync_interval_minutes")
    private val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }

    val syncInterval: Flow<SyncInterval> = context.dataStore.data.map {
        SyncInterval.fromMinutes((it[SYNC_INTERVAL] ?: 30).toLong())
    }

    val firstLaunchDone: Flow<Boolean> = context.dataStore.data.map { it[FIRST_LAUNCH_DONE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setSyncInterval(interval: SyncInterval) {
        context.dataStore.edit { it[SYNC_INTERVAL] = interval.minutes.toInt() }
    }

    suspend fun markFirstLaunchDone() {
        context.dataStore.edit { it[FIRST_LAUNCH_DONE] = true }
    }
}