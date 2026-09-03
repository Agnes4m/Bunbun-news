package moe.bunbun.news.data

import moe.bunbun.news.data.db.ArticleEntity
import moe.bunbun.news.data.db.FeedEntity
import moe.bunbun.news.data.db.SubscriptionEntity
import moe.bunbun.news.domain.model.Article
import moe.bunbun.news.domain.model.Feed
import moe.bunbun.news.domain.model.Subscription
import moe.bunbun.news.domain.model.SubscriptionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class MappersTest {

    @Test
    fun `FeedEntity toDomain preserves all fields`() {
        val now = System.currentTimeMillis()
        val entity = FeedEntity(
            id = "feed-1",
            url = "https://example.com/feed.xml",
            title = "Example Feed",
            siteUrl = "https://example.com",
            iconUrl = "https://example.com/favicon.ico",
            category = "Tech",
            lastSyncAt = now,
            etag = "etag-123",
            lastModified = "Mon, 01 Sep 2026",
            createdAt = now,
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.url, domain.url)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.siteUrl, domain.siteUrl)
        assertEquals(entity.iconUrl, domain.iconUrl)
        assertEquals(entity.category, domain.category)
        assertEquals(now, domain.lastSyncAt!!.toEpochMilli())
        assertEquals(entity.etag, domain.etag)
        assertEquals(entity.lastModified, domain.lastModified)
    }

    @Test
    fun `Feed round-trip preserves identity`() {
        val original = Feed(
            id = "feed-1",
            url = "https://example.com/feed.xml",
            title = "Example",
            siteUrl = null,
            iconUrl = null,
            category = null,
            lastSyncAt = null,
            etag = null,
            lastModified = null,
            createdAt = Instant.parse("2026-09-01T00:00:00Z"),
        )

        val restored = original.toEntity().toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.url, restored.url)
        assertEquals(original.createdAt, restored.createdAt)
        assertNull(restored.siteUrl)
    }

    @Test
    fun `ArticleEntity toDomain handles null publishedAt`() {
        val entity = ArticleEntity(
            id = "art-1",
            feedId = "feed-1",
            guid = "guid-1",
            title = "Test",
            author = null,
            url = "https://example.com/article/1",
            contentHtml = "<p>body</p>",
            excerpt = "summary",
            publishedAt = null,
            fetchedAt = 1000L,
            isRead = false,
            isStarred = true,
            clusterId = "cluster-abc",
        )

        val domain = entity.toDomain()

        assertNull(domain.publishedAt)
        assertEquals(1000L, domain.fetchedAt.toEpochMilli())
        assertTrue(domain.isStarred)
        assertEquals("cluster-abc", domain.clusterId)
    }

    @Test
    fun `Subscription type FEED maps correctly`() {
        val entity = SubscriptionEntity(
            id = SubscriptionEntity.forFeed("feed-1"),
            type = SubscriptionEntity.TYPE_FEED,
            targetId = "feed-1",
            title = "Some Feed",
            notifyEnabled = false,
            createdAt = 5000L,
        )

        val domain = entity.toDomain()

        assertEquals(SubscriptionType.FEED, domain.type)
        assertEquals("feed:feed-1", domain.id)
        assertEquals("feed-1", domain.targetId)
        assertEquals("Some Feed", domain.title)
    }

    @Test
    fun `Subscription type EVENT maps correctly`() {
        val entity = SubscriptionEntity(
            id = SubscriptionEntity.forEvent("cluster-xyz"),
            type = SubscriptionEntity.TYPE_EVENT,
            targetId = "cluster-xyz",
            title = "Tesla Earnings",
            notifyEnabled = true,
            createdAt = 8000L,
        )

        val domain = entity.toDomain()
        val restored = domain.toEntity()

        assertEquals(SubscriptionType.EVENT, domain.type)
        assertEquals("event:cluster-xyz", domain.id)
        assertEquals("event:cluster-xyz", restored.id)
        assertEquals(SubscriptionEntity.TYPE_EVENT, restored.type)
    }

    @Test
    fun `Article clusterId round-trips`() {
        val original = Article(
            id = "art-1",
            feedId = "feed-1",
            guid = "g",
            title = "T",
            author = null,
            url = "https://x",
            contentHtml = null,
            excerpt = null,
            publishedAt = Instant.ofEpochMilli(2000L),
            fetchedAt = Instant.ofEpochMilli(3000L),
            isRead = false,
            isStarred = false,
            clusterId = "c-1",
        )

        val restored = original.toEntity().toDomain()
        assertEquals("c-1", restored.clusterId)
    }

    @Test
    fun `Subscription companion forFeed produces deterministic id`() {
        assertEquals(
            SubscriptionEntity.forFeed("abc"),
            SubscriptionEntity.forFeed("abc"),
        )
        assertTrue(SubscriptionEntity.forFeed("abc").startsWith("feed:"))
    }
}