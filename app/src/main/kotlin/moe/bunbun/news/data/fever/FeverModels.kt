package moe.bunbun.news.data.fever

import com.google.gson.annotations.SerializedName

/** Fever 顶层响应：总带 api_version / auth，再视请求带 groups/feeds/items 等
 */
data class FeverResponse(
    @SerializedName("api_version") val apiVersion: Int = 0,
    val auth: Int = 0,
    val groups: List<FeverGroup>? = null,
    val feeds: List<FeverFeed>? = null,
    val items: List<FeverItem>? = null,
    val total_items: Int? = null,
    val last_refreshed_on_time: String? = null,
)

data class FeverGroup(
    val id: Long,
    val title: String,
)

data class FeverFeed(
    val id: Long,
    val feed_url: String,
    val title: String,
    val site_url: String? = null,
    val icon_url: String? = null,
    val group_id: Long? = null,
)

data class FeverItem(
    val id: Long,
    val feed_id: Long,
    val title: String,
    val author: String? = null,
    val url: String,
    val html: String? = null,
    val content: String? = null,
    val published: Long? = null,
    val created_on_time: Long? = null,
    val is_read: Int = 0,
    val is_starred: Int = 0,
)