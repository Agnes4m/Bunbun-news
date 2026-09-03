package moe.bunbun.news.domain.model

import java.time.Instant

data class Feed(
    val id: String,
    val url: String,
    val title: String,
    val siteUrl: String?,
    val iconUrl: String?,
    val category: String?,
    val lastSyncAt: Instant?,
    val etag: String?,
    val lastModified: String?,
    val createdAt: Instant,
)