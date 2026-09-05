package moe.bunbun.news.data.db

import androidx.room.Entity
import androidx.room.Fts4

/**
 * 文章全文索引（FTS4）。
 *
 * 使用 contentEntity 模式，Room 会在插入/更新/删除 articles 表时自动同步索引内容，
 * 不用手动写 trigger。
 *
 * 只索引 title + excerpt 两个字段，正文 contentHtml 通常很长、索引收益低；
 * 搜索 UI 也以「标题 + 摘要」为主。
 *
 * 分词器：默认 unicode61（按 Unicode 标准分词，对中文按字切分；
 * 短语搜索可用双引号包住）。
 */
@Fts4(contentEntity = ArticleEntity::class)
@Entity(tableName = "articles_fts")
data class ArticleFtsEntity(
    val title: String,
    val excerpt: String?,
)