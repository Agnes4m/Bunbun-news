package moe.bunbun.news.data.syncbridge

/**
 * 后端类型（v0.2 主题 C 子 3）。
 *
 * sealed class 让 UI 层用 when 强制处理每种类型，编译期保证不漏。
 */
sealed class SyncBackend {
    /** 纯本地，没有任何云端同步（默认） */
    object LocalOnly : SyncBackend()

    /** Miniflux v1 REST API */
    data class Miniflux(
        val baseUrl: String,
        val username: String,
        val apiKey: String,
    ) : SyncBackend()

    /** Fever 协议 */
    data class Fever(
        val baseUrl: String,
        val username: String,
        /** md5("username:password")，由 FeverClient.computeApiKey 算 */
        val apiKey: String,
    ) : SyncBackend()

    /** 人类可读标签（SettingsScreen 渲染用） */
    fun displayName(): String = when (this) {
        LocalOnly -> "本地离线（默认）"
        is Miniflux -> "Miniflux ($baseUrl)"
        is Fever -> "Fever ($baseUrl)"
    }
}