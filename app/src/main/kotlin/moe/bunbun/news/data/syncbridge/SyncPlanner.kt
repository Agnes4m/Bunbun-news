package moe.bunbun.news.data.syncbridge

/**
 * 根据当前 backend + direction 推导出"下一步应做什么"的工具。
 *
 * 实际执行由 SyncOrchestrator 负责；这里只产出意图列表，
 * 让单测能验证策略逻辑、不依赖网络/数据库。
 */
object SyncPlanner {

    /**
     * 在一次 sync 周期内要执行的步骤。
     */
    sealed class Step {
        object PullFeeds : Step()
        object PullEntries : Step()
        object PushReadStatus : Step()
        object PushStarred : Step()
    }

    fun plan(backend: SyncBackend, direction: SyncDirection): List<Step> {
        if (backend is SyncBackend.LocalOnly || direction == SyncDirection.DISABLED) {
            return emptyList()
        }
        val steps = mutableListOf<Step>()
        if (direction.canPull()) {
            steps += Step.PullFeeds
            steps += Step.PullEntries
        }
        if (direction.canPush()) {
            steps += Step.PushReadStatus
            steps += Step.PushStarred
        }
        return steps
    }

    /** 给 UI 显示当前配置的状态标签 */
    fun statusLabel(backend: SyncBackend, direction: SyncDirection): String {
        if (backend is SyncBackend.LocalOnly) return "本地"
        if (direction == SyncDirection.DISABLED) return "${backend.displayName()}（已暂停）"
        return "${backend.displayName()}（${label(direction)}）"
    }

    private fun label(d: SyncDirection): String = when (d) {
        SyncDirection.PULL_ONLY -> "仅下载"
        SyncDirection.PUSH_ONLY -> "仅上传"
        SyncDirection.BIDIRECTIONAL -> "双向"
        SyncDirection.DISABLED -> "已暂停"
    }
}