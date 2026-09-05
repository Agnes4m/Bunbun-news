package moe.bunbun.news.data.miniflux

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Miniflux v1 REST API（https://miniflux.app/docs/api.html）。
 *
 * 鉴权：每次请求带 `X-Auth-Token` header（由 MinifluxAuthInterceptor 注入）。
 */
interface MinifluxApi {

    /** 健康检查 / 当前用户信息 */
    @GET("v1/me")
    suspend fun me(): MinifluxUser

    /** 全量 feed 列表 */
    @GET("v1/feeds")
    suspend fun feeds(): List<MinifluxFeed>

    /** 创建 feed */
    @POST("v1/feeds")
    suspend fun createFeed(@Body body: MinifluxCreateFeedRequest): MinifluxFeed

    /** 删除 feed */
    @DELETE("v1/feeds/{id}")
    suspend fun deleteFeed(@Path("id") id: Long)

    /** 触发单个 feed 重抓 */
    @PUT("v1/feeds/{id}/refresh")
    suspend fun refreshFeed(@Path("id") id: Long)

    /** 触发全部 feed 重抓 */
    @PUT("v1/feeds/refresh")
    suspend fun refreshAllFeeds()

    /** 文章列表（可按 feed_id / category_id / 状态过滤） */
    @GET("v1/entries")
    suspend fun entries(
        @Query("feed_id") feedId: Long? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): MinifluxEntryList

    /** 标记已读 / 未读 / 收藏 */
    @PUT("v1/entries")
    suspend fun updateEntries(@Body body: MinifluxUpdateEntriesRequest)

    /** 保存对外文章（miniflux 会触发抓取） */
    @POST("v1/save-entry")
    suspend fun saveEntry(@Body body: MinifluxSaveEntryRequest): MinifluxEntry

    /** 显式接收 X-Auth-Token，避免依赖 interceptor，便于单测直接传 token */
    @GET("v1/me")
    suspend fun meWithToken(@Header("X-Auth-Token") token: String): MinifluxUser
}