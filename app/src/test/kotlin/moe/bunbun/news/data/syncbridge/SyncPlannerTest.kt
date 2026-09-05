package moe.bunbun.news.data.syncbridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncPlannerTest {

    private val miniflux = SyncBackend.Miniflux(
        baseUrl = "https://miniflux.example.com",
        username = "alice",
        apiKey = "tok",
    )

    @Test
    fun `LocalOnly backend always returns no steps regardless of direction`() {
        for (direction in SyncDirection.values()) {
            assertTrue(
                "LocalOnly + $direction 应无步骤",
                SyncPlanner.plan(SyncBackend.LocalOnly, direction).isEmpty(),
            )
        }
    }

    @Test
    fun `DISABLED direction returns no steps even with backend`() {
        assertTrue(SyncPlanner.plan(miniflux, SyncDirection.DISABLED).isEmpty())
    }

    @Test
    fun `PULL_ONLY returns pull steps only`() {
        val plan = SyncPlanner.plan(miniflux, SyncDirection.PULL_ONLY)
        assertEquals(
            listOf(SyncPlanner.Step.PullFeeds, SyncPlanner.Step.PullEntries),
            plan,
        )
    }

    @Test
    fun `PUSH_ONLY returns push steps only`() {
        val plan = SyncPlanner.plan(miniflux, SyncDirection.PUSH_ONLY)
        assertEquals(
            listOf(SyncPlanner.Step.PushReadStatus, SyncPlanner.Step.PushStarred),
            plan,
        )
    }

    @Test
    fun `BIDIRECTIONAL returns all four steps in order`() {
        val plan = SyncPlanner.plan(miniflux, SyncDirection.BIDIRECTIONAL)
        assertEquals(
            listOf(
                SyncPlanner.Step.PullFeeds,
                SyncPlanner.Step.PullEntries,
                SyncPlanner.Step.PushReadStatus,
                SyncPlanner.Step.PushStarred,
            ),
            plan,
        )
    }

    @Test
    fun `Fever backend works identically to Miniflux for planning`() {
        val fever = SyncBackend.Fever(
            baseUrl = "https://fever.example.com",
            username = "bob",
            apiKey = "abcd",
        )
        val plan = SyncPlanner.plan(fever, SyncDirection.BIDIRECTIONAL)
        assertEquals(4, plan.size)
    }

    @Test
    fun `statusLabel includes backend name`() {
        assertTrue(
            "LocalOnly",
            SyncPlanner.statusLabel(SyncBackend.LocalOnly, SyncDirection.BIDIRECTIONAL) == "本地",
        )
        assertTrue(
            "应包含 baseUrl",
            SyncPlanner.statusLabel(miniflux, SyncDirection.BIDIRECTIONAL).contains("miniflux.example.com"),
        )
        assertTrue(
            "DISABLED 应有'已暂停'",
            SyncPlanner.statusLabel(miniflux, SyncDirection.DISABLED).contains("已暂停"),
        )
    }

    @Test
    fun `direction helpers match canPull canPush semantics`() {
        assertEquals(true, SyncDirection.PULL_ONLY.canPull())
        assertEquals(false, SyncDirection.PULL_ONLY.canPush())
        assertEquals(true, SyncDirection.PUSH_ONLY.canPush())
        assertEquals(false, SyncDirection.PUSH_ONLY.canPull())
        assertEquals(true, SyncDirection.BIDIRECTIONAL.canPull())
        assertEquals(true, SyncDirection.BIDIRECTIONAL.canPush())
        assertEquals(false, SyncDirection.DISABLED.canPull())
        assertEquals(false, SyncDirection.DISABLED.canPush())
    }
}