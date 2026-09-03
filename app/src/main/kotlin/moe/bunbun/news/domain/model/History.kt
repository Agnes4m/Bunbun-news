package moe.bunbun.news.domain.model

import java.time.Instant

data class History(
    val articleId: String,
    val readAt: Instant,
    val scrollPercent: Float,
)