package moe.bunbun.news.i18n

import android.content.Context
import moe.bunbun.news.data.prefs.AppLocale
import java.util.Locale

/**
 * 解决 v0.2 i18n：根据 [AppLocale] 把 app locale 切换到对应语言。
 *
 * 关键点：必须在 [android.app.Activity.attachBaseContext] 阶段应用，
 * 这样后续 inflate / setContent 读 stringResource 时拿到的是目标语言资源。
 */
object LocaleHelper {

    /**
     * 用 [appLocale] 包装 [base]，返回 locale 已被覆盖的 Context。
     * 实际 attach 到 Activity 时用这个 Context 调用 super.attachBaseContext。
     */
    fun wrap(base: Context, appLocale: AppLocale): Context {
        val locale = resolveLocale(appLocale, base)
        if (locale == null) return base  // SYSTEM + base 已是系统 locale
        return updateContextLocale(base, locale)
    }

    /**
     * 把 [AppLocale] 映射到 java.util.Locale。
     * SYSTEM 时返回 null（调用方用 base 自己的 locale）。
     */
    fun resolveLocale(appLocale: AppLocale, base: Context): Locale? = when (appLocale) {
        AppLocale.SYSTEM -> null
        AppLocale.ENGLISH -> Locale.ENGLISH
        AppLocale.CHINESE -> Locale.SIMPLIFIED_CHINESE
    }

    private fun updateContextLocale(base: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = base.resources.configuration
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}