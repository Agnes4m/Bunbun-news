package moe.bunbun.news.data.miniflux

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Miniflux Retrofit 实例工厂。
 *
 * 工厂的好处：用户配置 baseUrl/token 后重新构造，不用重启进程。
 */
object MinifluxClient {

    fun create(
        baseUrl: String,
        tokenProvider: () -> String?,
        okHttpClient: OkHttpClient,
    ): MinifluxApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val authInterceptor = MinifluxAuthInterceptor(tokenProvider)
        val client = okHttpClient.newBuilder()
            .addInterceptor(authInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(normalized)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        // 使用完全限定类名，避免增量编译缓存的 import 解析问题
        val apiClass: Class<MinifluxApi> = MinifluxApi::class.java
        return retrofit.create(apiClass)
    }
}