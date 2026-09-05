package moe.bunbun.news.data.miniflux

/** 当前登录用户 */
data class MinifluxUser(
    val id: Long,
    val username: String,
    val is_admin: Boolean = false,
)

data class MinifluxFeed(
    val id: Long,
    val user_id: Long,
    val title: String,
    val site_url: String? = null,
    val feed_url: String,
    val category: MinifluxCategory? = null,
    val icon_url: String? = null,
    val checked_at: String? = null,
)

data class MinifluxCategory(
    val id: Long,
    val title: String,
)

data class MinifluxCreateFeedRequest(
    val feed_url: String,
    val category_id: Long? = null,
    val title: String? = null,
)

data class MinifluxEntry(
    val id: Long,
    val user_id: Long,
    val feed_id: Long,
    val title: String,
    val url: String,
    val author: String? = null,
    val content: String? = null,
    val hash: String,
    val published: String? = null,
    val created: String? = null,
    val status: String, // "read" | "unread" | "removed"
    val starred: Boolean = false,
)

data class MinifluxEntryList(
    val total: Int,
    val entries: List<MinifluxEntry>,
)

data class MinifluxUpdateEntriesRequest(
    val entry_ids: List<Long>,
    val status: String,
)

data class MinifluxSaveEntryRequest(
    val url: String,
    val title: String? = null,
    val content: String? = null,
    val published_at: String? = null,
)