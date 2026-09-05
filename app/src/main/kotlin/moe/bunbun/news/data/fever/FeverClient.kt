package moe.bunbun.news.data.fever

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest

/**
 * Fever Retrofit 工厂 + api_key 工具。
 *
 * Fever api_key = md5("username:password")，32 位十六进制。
 */
object FeverClient {

    fun create(
        baseUrl: String,
        apiKeyProvider: () -> String?,
        okHttpClient: OkHttpClient,
    ): FeverApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient) // Fever 不需要额外 header，鉴权字段在 body 里
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(FeverApi::class.java).also {
            // 把 api_key 提供器封到 instance 上以便调用方取用
            currentApiKey = apiKeyProvider
        }
    }

    /**
     * 当前 client 的 api_key 提供器（每次调用都取最新值，便于用户在设置页修改密码后即时生效）
     */
    private var currentApiKey: (() -> String?)? = null
    fun apiKey(): String? = currentApiKey?.invoke()

    /** 计算 Fever 协议要求的 api_key：md5("username:password") 十六进制 */
    fun computeApiKey(username: String, password: String): String {
        val input = "$username:$password"
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 在请求参数 Map 里自动加上当前客户端的 api_key（如果存在）
     */
    fun signedParams(extra: Map<String, String> = emptyMap()): Map<String, String> {
        val key = apiKey()
        val map = LinkedHashMap<String, String>()
        if (!key.isNullOrBlank()) map["api_key"] = key
        map.putAll(extra)
        return map
    }
}