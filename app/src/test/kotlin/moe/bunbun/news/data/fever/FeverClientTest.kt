package moe.bunbun.news.data.fever

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeverClientTest {

    @Test
    fun `computeApiKey returns lowercase hex md5`() {
        // 已知向量：md5("testuser:testpass") = ?
        // 这里只验证长度 32 + 全 hex + 不同输入不同结果
        val key1 = FeverClient.computeApiKey("alice", "secret")
        assertEquals(32, key1.length)
        assertTrue("应为小写十六进制", key1.all { it in '0'..'9' || it in 'a'..'f' })

        val key2 = FeverClient.computeApiKey("bob", "secret")
        assertTrue("不同输入应产生不同 key", key1 != key2)
    }

    @Test
    fun `computeApiKey is deterministic`() {
        val a = FeverClient.computeApiKey("user", "pass")
        val b = FeverClient.computeApiKey("user", "pass")
        assertEquals(a, b)
    }

    @Test
    fun `computeApiKey for empty password still works`() {
        val key = FeverClient.computeApiKey("user", "")
        assertEquals(32, key.length)
    }

    @Test
    fun `computeApiKey known vector`() {
        // 标准 md5("hello:world") = 6de41d334b7ce946682da48776a10bb9
        val key = FeverClient.computeApiKey("hello", "world")
        assertEquals("6de41d334b7ce946682da48776a10bb9", key)
    }

    @Test
    fun `signedParams with no api_key still works`() {
        // 没有当前 client（测试隔离）时 signedParams 不带 api_key
        val params = FeverClient.signedParams(mapOf("groups" to "1"))
        assertEquals(mapOf("groups" to "1"), params)
    }

    @Test
    fun `signedParams extra takes precedence`() {
        // 测试用反射调用 FeverClient.signedParams 时传入 map 顺序保留
        val params = FeverClient.signedParams(
            linkedMapOf("api_key" to "OVERRIDE", "items" to "2"),
        )
        // 当没有 currentApiKey 时，传入的 api_key 直接透传
        assertEquals("OVERRIDE", params["api_key"])
        assertEquals("2", params["items"])
    }
}