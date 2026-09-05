package moe.bunbun.news.data.summary

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * DeepSeek API（OpenAI Chat Completions 兼容协议）。
 *
 * 默认 baseUrl: https://api.deepseek.com/v1/
 * 默认 model:   deepseek-chat
 */
interface DeepSeekChatApi {

    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekChatRequest,
    ): DeepSeekChatResponse
}