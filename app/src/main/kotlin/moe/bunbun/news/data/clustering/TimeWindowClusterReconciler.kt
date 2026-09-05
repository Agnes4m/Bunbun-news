package moe.bunbun.news.data.clustering

import java.time.Duration
import java.time.Instant

/**
 * 时间窗 + SimHash 二次聚类。
 *
 * 用法：在每轮 RSS 同步完成后调用，输入"最近若干天"的全部文章（含已有 clusterId），
 * 输出"需要写回 DB 的 id → 新 clusterId"映射。
 *
 * 算法：
 * 1. 按 publishedAt 升序排序（无时间则用 fetchedAt / 0L）
 * 2. 滑动窗口：只和窗口内 publishedAt 差 ≤ windowHours 的"前驱"对比
 * 3. 对每对文章做 SimHash 海明距离比较：≤ threshold 即视为同一事件
 * 4. Union-Find 合并 cluster；规范化 root 取"最早加入的"clusterId
 *
 * 与 ClusterEngine 的关系：ClusterEngine 在入库前对单篇文章算 clusterId（基于词袋 hash），
 * 这层 Reconcile 在多文章入库后做"跨文章"合并。两者并存：单文章可先有 clusterId，
 * 时间窗内相似文章会被统一到最早的 clusterId。
 */
object TimeWindowClusterReconciler {

    /** 输入文章快照（不需要把整个 Article 都传进来） */
    data class Snapshot(
        val id: String,
        val title: String,
        val publishedAt: Instant?,
        val existingClusterId: String?,
    )

    /** 合并计划：articleId -> 应该写回的新 clusterId */
    data class MergePlan(
        val updates: Map<String, String>,
    ) {
        val isEmpty: Boolean get() = updates.isEmpty()
        val size: Int get() = updates.size
    }

    fun reconcile(
        articles: List<Snapshot>,
        window: Duration = Duration.ofHours(24),
        threshold: Int = 3,
    ): MergePlan {
        if (articles.isEmpty()) return MergePlan(emptyMap())

        // 按 publishedAt 升序，null 排到最后（fetchedAt 兜底）
        val sorted = articles.sortedBy { it.publishedAt ?: Instant.MAX }

        val uf = UnionFind(sorted.size)
        // 给每个节点一个 canonical clusterId：优先取 existingClusterId，否则用占位（之后会被 union 覆盖）
        val canonicalIds = sorted.map { it.existingClusterId ?: "tmp-${it.id}" }.toMutableList()

        // 滑动窗口：扫到第 i 个时，j 是 i 之前 publishedAt 差 ≤ window 的最早下标
        var windowStart = 0
        for (i in sorted.indices) {
            // 维护窗口：windowStart 之前的不再需要对比
            val pi = sorted[i].publishedAt
            while (windowStart < i) {
                val pw = sorted[windowStart].publishedAt
                if (pi != null && pw != null && Duration.between(pw, pi) > window) {
                    windowStart++
                } else {
                    break
                }
            }
            // 计算当前文章的 simhash 一次
            val hashI = SimHash.hash(sorted[i].title)

            // 只对比窗口内前驱
            for (j in windowStart until i) {
                val hashJ = SimHash.hash(sorted[j].title)
                if (SimHash.isSimilar(hashI, hashJ, threshold)) {
                    uf.union(j, i)
                }
            }
        }

        // 计算每个 root 的最终 canonical clusterId：取该 group 内**出现最早**的文章的 clusterId
        // （保持时间序的"祖先"语义；后续字母序更小的 clusterId 不会"篡位"）
        val rootToCanonical = mutableMapOf<Int, String>()
        for (i in sorted.indices) {
            val root = uf.find(i)
            val cid = sorted[i].existingClusterId
            if (cid == null) continue
            val current = rootToCanonical[root]
            if (current == null) {
                rootToCanonical[root] = cid
            }
            // 已设定就不变（保留最早一篇的 clusterId）
        }

        // 生成 updates：仅当文章当前 clusterId 与规范 root 不一致时写回
        val updates = mutableMapOf<String, String>()
        for (i in sorted.indices) {
            val root = uf.find(i)
            val newClusterId = rootToCanonical[root] ?: continue
            val oldClusterId = sorted[i].existingClusterId
            if (oldClusterId != newClusterId) {
                updates[sorted[i].id] = newClusterId
            }
        }
        return MergePlan(updates)
    }

    /** 路径压缩 + 按秩合并的 Union-Find */
    private class UnionFind(n: Int) {
        private val parent = IntArray(n) { it }
        private val rank = IntArray(n)

        fun find(x: Int): Int {
            var r = x
            while (parent[r] != r) r = parent[r]
            var cur = x
            while (parent[cur] != r) {
                val next = parent[cur]
                parent[cur] = r
                cur = next
            }
            return r
        }

        fun union(a: Int, b: Int) {
            val ra = find(a)
            val rb = find(b)
            if (ra == rb) return
            when {
                rank[ra] < rank[rb] -> parent[ra] = rb
                rank[ra] > rank[rb] -> parent[rb] = ra
                else -> {
                    parent[rb] = ra
                    rank[ra]++
                }
            }
        }
    }
}