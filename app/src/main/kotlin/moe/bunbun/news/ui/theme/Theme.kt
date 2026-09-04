package moe.bunbun.news.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import moe.bunbun.news.data.prefs.ThemeMode

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    background = md_theme_dark_background,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

/**
 * 护眼主题（v0.2）：暖米黄背景 + 深棕字，对比度足够但比纯白柔和。
 * 不走 dynamicColor，固定色板（用户选护眼时强制启用）。
 */
private val EyeCareColors = lightColorScheme(
    primary = Color(0xFF7A6230),     // 暖棕
    onPrimary = Color(0xFFFFF8E7),
    secondary = Color(0xFF8C7048),
    onSecondary = Color(0xFFFFF8E7),
    background = Color(0xFFF5F1E0),   // 浅米黄底
    surface = Color(0xFFFAF6E8),
    onSurface = Color(0xFF3A3220),    // 深棕字
)

/**
 * 主题包装（v0.2：从 Boolean? 升级到 ThemeMode 枚举）
 *
 * @param themeMode 用户主题偏好（null = 跟随系统）
 * @param dynamicColor 是否使用 Material You 动态配色（Android 12+ 有效）；
 *                     护眼主题下强制忽略 dynamicColor（固定色板）
 */
@Composable
fun BunbunNewsTheme(
    themeMode: ThemeMode? = null,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> pickStatic(false, dynamicColor)
        ThemeMode.DARK -> pickStatic(true, dynamicColor)
        ThemeMode.EYE_CARE -> EyeCareColors  // 护眼固定色板，不用 dynamicColor
        null -> pickStatic(isSystemInDarkTheme(), dynamicColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

/**
 * 按是否深色 + dynamicColor 选色板（集中逻辑避免重复）。
 */
@Composable
private fun pickStatic(useDark: Boolean, dynamicColor: Boolean) = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    useDark -> DarkColors
    else -> LightColors
}
