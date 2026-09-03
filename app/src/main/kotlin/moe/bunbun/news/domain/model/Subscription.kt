package moe.bunbun.news.domain.model

import java.time.Instant

enum class SubscriptionType { FEED, EVENT }

data class Subscription(
    val id: String,
    val type: SubscriptionType,
    val targetId: String,
    val title: String,
    val notifyEnabled: Boolean,
    val createdAt: Instant,
)