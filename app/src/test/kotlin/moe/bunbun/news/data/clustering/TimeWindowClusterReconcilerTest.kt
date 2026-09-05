package moe.bunbun.news.data.clustering

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class TimeWindowClusterReconcilerTest {

    private fun snap(
        id: String,
        title: String,
        publishedAt: Instant?,
        clusterId: String? = null,
    ) = TimeWindowClusterReconciler.Snapshot(id, title, publishedAt, clusterId)

    @Test
    fun `empty input returns empty plan`() {
        val plan = TimeWindowClusterReconciler.reconcile(emptyList())
        assertTrue(plan.isEmpty)
    }

    @Test
    fun `single article with existing cluster id produces no updates`() {
        val t = Instant.parse("2026-09-05T10:00:00Z")
        val plan = TimeWindowClusterReconciler.reconcile(listOf(snap("a", "hello world", t, "c-1")))
        assertTrue("单篇且 clusterId 已正确无需更新", plan.isEmpty)
    }

    @Test
    fun `two similar titles in window merge to first cluster id`() {
        val t1 = Instant.parse("2026-09-05T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z")
        val plan = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Apple launches new iPhone", t2, "c-2"),
            )
        )
        assertEquals("第二篇应改写为 c-1", mapOf("b" to "c-1"), plan.updates)
    }

    @Test
    fun `dissimilar titles stay in separate clusters`() {
        val t1 = Instant.parse("2026-09-05T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z")
        val plan = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Tesla unveils new robot", t2, "c-2"),
            )
        )
        assertTrue(plan.isEmpty)
    }

    @Test
    fun `similar titles outside window stay separate`() {
        val t1 = Instant.parse("2026-09-01T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z") // 4 天后
        val plan = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Apple launches new iPhone", t2, "c-2"),
            ),
            window = Duration.ofHours(24),
        )
        assertTrue("超出时间窗的不合并", plan.isEmpty)
    }

    @Test
    fun `transitive merge through three articles`() {
        val t1 = Instant.parse("2026-09-05T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z")
        val t3 = Instant.parse("2026-09-05T12:00:00Z")
        // b 完全等同 a，c 与 b 仅差单字符（finds → find）——SimHash 距离小
        val plan = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Mars rover finds ice", t1, "c-a"),
                snap("b", "Mars rover finds ice", t2, "c-b"),
                snap("c", "Mars rover find ice", t3, "c-c"),
            ),
            threshold = 10,
        )
        // b 应归到 c-a，c 应归到 c-a（通过 b 传递）
        assertEquals(mapOf("b" to "c-a", "c" to "c-a"), plan.updates)
    }

    @Test
    fun `article without cluster id takes one from earlier similar`() {
        val t1 = Instant.parse("2026-09-05T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z")
        val plan = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Apple launches new iPhone", t2, null), // 入库前还没算
            )
        )
        assertEquals("无 clusterId 的 b 应被 c-1 覆盖", mapOf("b" to "c-1"), plan.updates)
    }

    @Test
    fun `custom threshold makes clustering stricter`() {
        val t1 = Instant.parse("2026-09-05T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z")
        // 标题差异 1 token（iPhone vs iPad），SimHash 距离小但通常 > 0
        val permissive = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Apple launches new iPad", t2, "c-2"),
            ),
            threshold = 10,
        )
        assertFalse("threshold=10 应合并", permissive.isEmpty)

        // threshold=0 时强制要求 SimHash 完全相同；
        // 完全相同标题必合并、不同标题必不合并（与词差异无关）
        val identicalMerge = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Apple launches new iPhone", t2, "c-2"),
            ),
            threshold = 0,
        )
        assertFalse("完全相同标题在 threshold=0 也合并", identicalMerge.isEmpty)
    }

    @Test
    fun `plan preserves original cluster id for first article of a merged group`() {
        val t1 = Instant.parse("2026-09-05T10:00:00Z")
        val t2 = Instant.parse("2026-09-05T11:00:00Z")
        // 两篇相似但 c-2 比 c-1 "晚"（字母序更大）
        val plan = TimeWindowClusterReconciler.reconcile(
            listOf(
                snap("a", "Apple launches new iPhone", t1, "c-1"),
                snap("b", "Apple launches new iPhone", t2, "c-2"),
            )
        )
        // a 是窗口内最早出现，应当保留 a 的 c-1
        assertFalse("a 不应在 updates 中", "a" in plan.updates)
        assertEquals("b 改写为 c-1", "c-1", plan.updates["b"])
    }
}