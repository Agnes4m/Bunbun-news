package moe.bunbun.news.domain.model

import java.time.Instant

data class Article(
    val id: String,
    val feedId: String,
    val guid: String,
    val title: String,
    val author: String?,
    val url: String,
    val contentHtml: String?,
    val excerpt: String?,
    val publishedAt: Instant?,
    val fetchedAt: Instant,
    val isRead: Boolean,
    val isStarred: Boolean,
    val clusterId: String?,
)