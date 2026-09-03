package moe.bunbun.news.data

import moe.bunbun.news.data.db.ArticleEntity
import moe.bunbun.news.data.db.FeedEntity
import moe.bunbun.news.data.db.HistoryEntity
import moe.bunbun.news.data.db.SubscriptionEntity
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.domain.model.Feed
import moe.bunbun.news.domain.model.History
import moe.bunbun.news.domain.model.Subscription
import moe.bunbun.news.domain.model.SubscriptionType
import java.time.Instant

internal fun FeedEntity.toDomain(): Feed = Feed(
    id = id,
    url = url,
    title = title,
    siteUrl = siteUrl,
    iconUrl = iconUrl,
    category = category,
    lastSyncAt = lastSyncAt?.let(Instant::ofEpochMilli),
    etag = etag,
    lastModified = lastModified,
    createdAt = Instant.ofEpochMilli(createdAt),
)

internal fun Feed.toEntity(): FeedEntity = FeedEntity(
    id = id,
    url = url,
    title = title,
    siteUrl = siteUrl,
    iconUrl = iconUrl,
    category = category,
    lastSyncAt = lastSyncAt?.toEpochMilli(),
    etag = etag,
    lastModified = lastModified,
    createdAt = createdAt.toEpochMilli(),
)

internal fun ArticleEntity.toDomain(): Article = Article(
    id = id,
    feedId = feedId,
    guid = guid,
    title = title,
    author = author,
    url = url,
    contentHtml = contentHtml,
    excerpt = excerpt,
    publishedAt = publishedAt?.let(Instant::ofEpochMilli),
    fetchedAt = Instant.ofEpochMilli(fetchedAt),
    isRead = isRead,
    isStarred = isStarred,
    clusterId = clusterId,
)

internal fun Article.toEntity(): ArticleEntity = ArticleEntity(
    id = id,
    feedId = feedId,
    guid = guid,
    title = title,
    author = author,
    url = url,
    contentHtml = contentHtml,
    excerpt = excerpt,
    publishedAt = publishedAt?.toEpochMilli(),
    fetchedAt = fetchedAt.toEpochMilli(),
    isRead = isRead,
    isStarred = isStarred,
    clusterId = clusterId,
)

internal fun SubscriptionEntity.toDomain(): Subscription = Subscription(
    id = id,
    type = if (type == SubscriptionEntity.TYPE_EVENT) SubscriptionType.EVENT else SubscriptionType.FEED,
    targetId = targetId,
    title = title,
    notifyEnabled = notifyEnabled,
    createdAt = Instant.ofEpochMilli(createdAt),
)

internal fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(
    id = id,
    type = if (type == SubscriptionType.EVENT) SubscriptionEntity.TYPE_EVENT else SubscriptionEntity.TYPE_FEED,
    targetId = targetId,
    title = title,
    notifyEnabled = notifyEnabled,
    createdAt = createdAt.toEpochMilli(),
)

internal fun HistoryEntity.toDomain(): History = History(
    articleId = articleId,
    readAt = Instant.ofEpochMilli(readAt),
    scrollPercent = scrollPercent,
)

internal fun History.toEntity(): HistoryEntity = HistoryEntity(
    articleId = articleId,
    readAt = readAt.toEpochMilli(),
    scrollPercent = scrollPercent,
)