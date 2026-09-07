package moe.bunbun.news.i18n

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import moe.bunbun.news.data.prefs.AppLocale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步缓存当前 AppLocale（v0.2 i18n）。
 *
 * 必要性：MainActivity.attachBaseContext 是同步调用，**不能** await DataStore
 * （DataStore 是 suspend API）。本 cache 是个同步读写的 SharedPreferences 镜像，
 * 由 [moe.bunbun.news.data.prefs.UserPreferences] 在 setAppLocale 时同步更新，
 * 启动时由 BunbunApp 读 DataStore 后填入。
 *
 * 读：MainActivity.attachBaseContext → 取 cache 立即返回
 * 写：UserPreferences.setAppLocale → 同时写 DataStore + cache（保持同步）
 */
@Singleton
class LocaleCache @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val sp = context.applicationContext
        .getSharedPreferences("bunbun_locale_cache", Context.MODE_PRIVATE)

    @Volatile
    var locale: AppLocale = AppLocale.fromKey(sp.getString(KEY, null))
        set(value) {
            field = value
            sp.edit().putString(KEY, value.key).apply()
        }

    companion object {
        private const val KEY = "app_locale"
    }
}