package moe.bunbun.news.data.syncbridge

import kotlinx.coroutines.flow.first
import moe.bunbun.news.data.fever.FeverClient
import moe.bunbun.news.data.prefs.BackendType
import moe.bunbun.news.data.prefs.UserPreferences

/**
 * 把 UserPreferences 里的平字段（backendType/url/username/apiKey）
 * 映射回 SyncBackend sealed class。
 *
 * 规则：
 * - LOCAL 或未填 URL/username/apiKey → LocalOnly
 * - MINIFLUX → Miniflux(baseUrl, username, apiKey)
 * - FEVER → Fever(baseUrl, username, apiKey)
 *   Fever 协议要求 apiKey = md5("username:password")，由 UI 层在保存前算好
 */
object BackendConfigResolver {

    suspend fun resolve(prefs: UserPreferences): SyncBackend {
        val type = prefs.backendType.first()
        val url = prefs.backendUrl.first()
        val username = prefs.backendUsername.first()
        val apiKey = prefs.backendApiKey.first()
        return when (type) {
            BackendType.LOCAL -> SyncBackend.LocalOnly
            BackendType.MINIFLUX ->
                if (!url.isNullOrBlank() && !username.isNullOrBlank() && !apiKey.isNullOrBlank()) {
                    SyncBackend.Miniflux(url.trimEnd('/'), username, apiKey)
                } else {
                    SyncBackend.LocalOnly
                }
            BackendType.FEVER ->
                if (!url.isNullOrBlank() && !username.isNullOrBlank() && !apiKey.isNullOrBlank()) {
                    SyncBackend.Fever(url.trimEnd('/'), username, apiKey)
                } else {
                    SyncBackend.LocalOnly
                }
        }
    }

    /**
     * Fever 用户输入原始 username + password 时，给 UI 层算 apiKey
     * 后再调用 setBackend(type=FEVER, apiKey=...)
     */
    fun computeFeverApiKey(username: String, password: String): String =
        FeverClient.computeApiKey(username, password)
}