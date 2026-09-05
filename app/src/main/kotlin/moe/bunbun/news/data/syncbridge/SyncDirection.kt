package moe.bunbun.news.data.syncbridge

/**
 * 后端同步方向。
 *
 * 用户配置：
 * - PULL_ONLY：纯本地，只把云端 feed/entry 拉下来（隐私默认）
 * - PUSH_ONLY：只把本地 read/star 状态推到云端（用户不下载云端内容）
 * - BIDIRECTIONAL：云端 + 本地双向（Miniflux 用户常选）
 */
enum class SyncDirection {
    PULL_ONLY,
    PUSH_ONLY,
    BIDIRECTIONAL,
    DISABLED;

    fun canPull(): Boolean = this == PULL_ONLY || this == BIDIRECTIONAL
    fun canPush(): Boolean = this == PUSH_ONLY || this == BIDIRECTIONAL
}