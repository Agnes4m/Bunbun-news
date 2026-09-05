package moe.bunbun.news.data.fever

import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Fever 协议 v1（http://www.feedafever.com/api）。
 *
 * 鉴权：每个请求带 `api_key` 字段（username:password MD5）和可选 `group_id` 字段。
 * 服务端返回 JSON 对象（含 api_version / auth 字段；auth=0 表示凭据错）。
 *
 * 主要 endpoint：
 * - ?api_key=&groups= → group 列表
 * - ?api_key=&feeds=&feed_group_ids= → feed 列表
 * - ?api_key=&items=&since= → 文章列表
 * - ?api_key=&mark=item&as=read → 标记已读
 *
 * 协议所有 endpoint 都用同一个 URL（POST /api/），用 FieldMap 区分操作。
 */
interface FeverApi {

    @FormUrlEncoded
    @POST("api/")
    suspend fun call(@FieldMap params: Map<String, String>): FeverResponse
}