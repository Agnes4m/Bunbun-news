package moe.bunbun.news.data.miniflux

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Miniflux 鉴权：每个请求注入 `X-Auth-Token` header。
 *
 * token 由 MinifluxConfig 提供（存 DataStore）。
 */
class MinifluxAuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("X-Auth-Token", token)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}