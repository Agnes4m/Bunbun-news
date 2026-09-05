package moe.bunbun.news.data.miniflux

import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MinifluxAuthInterceptorTest {

    /** 末端 chain：原样回传它收到的 request */
    private fun chainFor(request: Request): Interceptor.Chain {
        return object : Interceptor.Chain {
            override fun request(): Request = request
            override fun proceed(req: Request): Response = Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody())
                .build()
            override fun connection() = null
            override fun call(): Call = throw UnsupportedOperationException()
            override fun connectTimeoutMillis() = 0
            override fun readTimeoutMillis() = 0
            override fun writeTimeoutMillis() = 0
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }
    }

    @Test
    fun `injects X-Auth-Token when token is present`() {
        val interceptor = MinifluxAuthInterceptor { "tok-123" }
        val original = Request.Builder().url("https://miniflux.example/v1/me").build()

        val captured = interceptor.intercept(chainFor(original)).request
        assertEquals("tok-123", captured.header("X-Auth-Token"))
    }

    @Test
    fun `omits header when token provider returns null`() {
        val interceptor = MinifluxAuthInterceptor { null }
        val original = Request.Builder().url("https://miniflux.example/v1/me").build()
        val captured = interceptor.intercept(chainFor(original)).request
        assertNull("未配置 token 时不应注入", captured.header("X-Auth-Token"))
    }

    @Test
    fun `omits header when token provider returns blank`() {
        val interceptor = MinifluxAuthInterceptor { "   " }
        val original = Request.Builder().url("https://miniflux.example/v1/me").build()
        val captured = interceptor.intercept(chainFor(original)).request
        assertNull(captured.header("X-Auth-Token"))
    }

    @Test
    fun `does not modify other headers`() {
        val interceptor = MinifluxAuthInterceptor { "tok-1" }
        val original = Request.Builder()
            .url("https://miniflux.example/v1/entries")
            .header("Accept", "application/json")
            .build()
        val captured = interceptor.intercept(chainFor(original)).request
        assertEquals("application/json", captured.header("Accept"))
        assertEquals("tok-1", captured.header("X-Auth-Token"))
    }

    @Test
    fun `tokenProvider is called on every request`() {
        var calls = 0
        val interceptor = MinifluxAuthInterceptor {
            calls++
            "tok-$calls"
        }
        val original = Request.Builder().url("https://miniflux.example/v1/me").build()
        interceptor.intercept(chainFor(original))
        interceptor.intercept(chainFor(original))
        assertEquals(2, calls)
    }
}