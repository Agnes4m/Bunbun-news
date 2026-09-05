package moe.bunbun.news.data.image

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 把一组图片 URL 灌进 Coil ImageLoader 的磁盘缓存，**不显示**。
 *
 * 设计要点：
 * - 使用 Coil 的 ImageLoader.enqueue + listener(...)，失败也不抛
 * - 单张图片下载失败不影响其它图
 * - 复用 UI 端 ImageLoader 的磁盘缓存路径（cacheDir/image_cache），
 *   ReaderScreen 打开文章时直接命中缓存，省一次网络往返
 */
@Singleton
class ImagePrefetchHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
) {

    /**
     * 预下载一组 URL；返回成功请求的条数。
     * 同步返回（不等待每个图完成），调用方可在 IO 线程上稍等片刻让下载跑完。
     */
    fun prefetch(urls: List<String>): Int {
        var count = 0
        urls.forEach { url ->
            if (url.isBlank()) return@forEach
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false) // 硬件 Bitmap 关闭；磁盘缓存场景不需要
                    .listener(
                        onSuccess = { _, _ -> count++ },
                        onError = { _, _ -> /* 单图失败忽略 */ },
                    )
                    .build()
                imageLoader.enqueue(request)
            } catch (_: Throwable) {
                // 单条 URL 解析失败 / OOM 等都不影响后续
            }
        }
        return count
    }
}