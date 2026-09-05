package moe.bunbun.news.data.summary

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * DeepSeek Retrofit 工厂。
 */
object DeepSeekClient {

    const val DEFAULT_BASE_URL = "https://api.deepseek.com/v1/"
    const val DEFAULT_MODEL = "deepseek-chat"

    fun create(
        apiKeyProvider: () -> String?,
        okHttpClient: OkHttpClient,
        baseUrl: String = DEFAULT_BASE_URL,
    ): DeepSeekChatApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(DeepSeekChatApi::class.java).also {
            // 仅记录 apiKeyProvider 到单例，让 ChatApi 调用方取用
            currentKey = apiKeyProvider
        }
    }

    private var currentKey: (() -> String?)? = null
    fun authorizationHeader(): String? = currentKey?.invoke()?.let { "Bearer $it" }
}