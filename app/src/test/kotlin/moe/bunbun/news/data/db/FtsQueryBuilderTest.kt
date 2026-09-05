package moe.bunbun.news.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class FtsQueryBuilderTest {

    @Test
    fun `empty input returns empty string`() {
        assertEquals("", FtsQueryBuilder.build(""))
        assertEquals("", FtsQueryBuilder.build("   "))
    }

    @Test
    fun `single ascii token gets prefix wildcard`() {
        assertEquals("rss*", FtsQueryBuilder.build("rss"))
        assertEquals("hello*", FtsQueryBuilder.build("hello"))
    }

    @Test
    fun `multiple ascii tokens are space-joined`() {
        assertEquals("rss* reader*", FtsQueryBuilder.build("rss reader"))
    }

    @Test
    fun `cjk characters pass through without wildcard`() {
        // 纯中文：unicode61 已按字分词，前缀通配符意义不大，原样输出
        assertEquals("新闻", FtsQueryBuilder.build("新闻"))
    }

    @Test
    fun `mixed cjk and ascii tokens are handled independently`() {
        // 中文 token 原样（unicode61 按字分词），拉丁 token 加 *
        assertEquals("rss* 聚合", FtsQueryBuilder.build("rss 聚合"))
    }

    @Test
    fun `quoted phrase passes through verbatim`() {
        assertEquals("\"exact phrase\"", FtsQueryBuilder.build("\"exact phrase\""))
    }

    @Test
    fun `token already ending in wildcard passes through`() {
        assertEquals("ip*", FtsQueryBuilder.build("ip*"))
    }

    @Test
    fun `question mark wildcard is preserved`() {
        assertEquals("r?ss", FtsQueryBuilder.build("r?ss"))
    }

    @Test
    fun `numeric token gets prefix wildcard`() {
        assertEquals("2026*", FtsQueryBuilder.build("2026"))
    }

    @Test
    fun `whitespace-separated tokens are trimmed`() {
        // 多个空格合并为单个分隔符（不出现空 token）
        assertEquals("a* b*", FtsQueryBuilder.build("  a   b  "))
    }
}