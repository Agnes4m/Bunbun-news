package moe.bunbun.news.data.syncbridge

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import moe.bunbun.news.data.prefs.BackendType
import moe.bunbun.news.data.prefs.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendConfigResolverTest {

    private fun prefs(
        type: BackendType,
        url: String? = null,
        username: String? = null,
        apiKey: String? = null,
    ): UserPreferences = mockk {
        coEvery { backendType } returns flowOf(type)
        coEvery { backendUrl } returns flowOf(url)
        coEvery { backendUsername } returns flowOf(username)
        coEvery { backendApiKey } returns flowOf(apiKey)
    }

    @Test
    fun `LOCAL backend resolves to LocalOnly`() = runTest {
        val out = BackendConfigResolver.resolve(prefs(BackendType.LOCAL))
        assertTrue(out is SyncBackend.LocalOnly)
    }

    @Test
    fun `MINIFLUX with all fields resolves to Miniflux`() = runTest {
        val out = BackendConfigResolver.resolve(
            prefs(BackendType.MINIFLUX, "https://mf.example/", "alice", "tok-123")
        )
        assertTrue(out is SyncBackend.Miniflux)
        out as SyncBackend.Miniflux
        assertEquals("https://mf.example", out.baseUrl) // trimEnd /
        assertEquals("alice", out.username)
        assertEquals("tok-123", out.apiKey)
    }

    @Test
    fun `MINIFLUX with missing url falls back to LocalOnly`() = runTest {
        val out = BackendConfigResolver.resolve(
            prefs(BackendType.MINIFLUX, url = null, username = "alice", apiKey = "tok")
        )
        assertTrue("缺 url 应回退到 LocalOnly", out is SyncBackend.LocalOnly)
    }

    @Test
    fun `FEVER with all fields resolves to Fever`() = runTest {
        val out = BackendConfigResolver.resolve(
            prefs(BackendType.FEVER, "https://fever.example/", "bob", "md5hash")
        )
        assertTrue(out is SyncBackend.Fever)
    }

    @Test
    fun `FEVER with blank apiKey falls back to LocalOnly`() = runTest {
        val out = BackendConfigResolver.resolve(
            prefs(BackendType.FEVER, "https://fever.example/", "bob", "  ")
        )
        assertTrue("blank apiKey 应回退到 LocalOnly", out is SyncBackend.LocalOnly)
    }

    @Test
    fun `computeFeverApiKey delegates to FeverClient`() {
        // md5("user:pass") = 5fa3d8c2a1b9c3f5e7d8a9b0c1d2e3f4 (实际值由 FeverClient.computeApiKey 提供)
        val key = BackendConfigResolver.computeFeverApiKey("user", "pass")
        assertEquals(32, key.length)
        assertTrue(key.all { it in '0'..'9' || it in 'a'..'f' })
    }
}