package moe.bunbun.news.data.summary

import com.google.gson.annotations.SerializedName

data class DeepSeekChatRequest(
    val model: String,
    val messages: List<DeepSeekMessage>,
    val temperature: Double = 0.3,
    val max_tokens: Int = 200,
    val stream: Boolean = false,
)

data class DeepSeekMessage(
    val role: String, // "system" | "user" | "assistant"
    val content: String,
)

data class DeepSeekChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<DeepSeekChoice> = emptyList(),
    val usage: DeepSeekUsage? = null,
)

data class DeepSeekChoice(
    val index: Int = 0,
    val message: DeepSeekMessage,
    @SerializedName("finish_reason") val finishReason: String? = null,
)

data class DeepSeekUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int = 0,
    @SerializedName("completion_tokens") val completionTokens: Int = 0,
    @SerializedName("total_tokens") val totalTokens: Int = 0,
)

/** 把 ChatResponse 取第一条 choice 的 message.content；返回空串表示无可用结果 */
fun DeepSeekChatResponse.firstContent(): String =
    choices.firstOrNull()?.message?.content?.trim().orEmpty()