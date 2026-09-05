package moe.bunbun.news.data.summary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryPromptBuilderTest {

    @Test
    fun `detects Chinese when text is mostly CJK`() {
        assertEquals(SummaryPromptBuilder.Lang.CHINESE, SummaryPromptBuilder.detectLanguage("中文新闻聚合器"))
        assertEquals(SummaryPromptBuilder.Lang.CHINESE, SummaryPromptBuilder.detectLanguage(""))
    }

    @Test
    fun `detects English when text is mostly ASCII letters`() {
        assertEquals(SummaryPromptBuilder.Lang.ENGLISH, SummaryPromptBuilder.detectLanguage("RSS reader"))
        assertEquals(SummaryPromptBuilder.Lang.ENGLISH, SummaryPromptBuilder.detectLanguage("Apple launches new iPhone"))
    }

    @Test
    fun `mixed text falls to MIXED`() {
        // 中文 5 字 + 5 字母（不算空格），总 10
        // cjk=5, ascii=5, total=10 → 谁都不 *2 > 10 → MIXED
        assertEquals(SummaryPromptBuilder.Lang.MIXED, SummaryPromptBuilder.detectLanguage("中文Apple"))
    }

    @Test
    fun `build returns zh prompt for Chinese title`() {
        val p = SummaryPromptBuilder.build("RSS 阅读器回归", "深度报道：...")
        assertEquals(SummaryPromptBuilder.ZH_SYSTEM, p.system)
        assertTrue("user 应含标题", p.user.contains("RSS 阅读器回归"))
        assertTrue("user 应含 body", p.user.contains("深度报道：..."))
    }

    @Test
    fun `build returns en prompt for English title`() {
        val p = SummaryPromptBuilder.build("Apple launches iPhone", "Some article body.")
        assertEquals(SummaryPromptBuilder.EN_SYSTEM, p.system)
        assertTrue(p.user.contains("Apple launches iPhone"))
    }

    @Test
    fun `build truncates long body to maxBodyChars`() {
        val longBody = "X".repeat(10_000)
        val p = SummaryPromptBuilder.build("标题", longBody, maxBodyChars = 100)
        // 标题 + "X" * 100 = 101 个字符（不严格计算换行）
        assertTrue("truncate 后 user 不应过长", p.user.length < 500)
        assertTrue("应包含截断后的 body 片段", p.user.contains("X".repeat(50)))
        assertTrue("不应包含未截断的全部", !p.user.contains("X".repeat(5_000)))
    }

    @Test
    fun `build tolerates empty body`() {
        val p = SummaryPromptBuilder.build("标题", "")
        assertTrue(p.user.contains("标题"))
        assertTrue("空 body 不应抛错", p.user.isNotBlank())
    }

    @Test
    fun `DeepSeekResponse firstContent extracts first choice`() {
        val r = DeepSeekChatResponse(
            choices = listOf(
                DeepSeekChoice(message = DeepSeekMessage("assistant", "  摘要文本  ")),
                DeepSeekChoice(message = DeepSeekMessage("assistant", "第二条")),
            ),
        )
        assertEquals("摘要文本", r.firstContent())
    }

    @Test
    fun `DeepSeekResponse firstContent returns empty when no choices`() {
        assertEquals("", DeepSeekChatResponse().firstContent())
    }
}