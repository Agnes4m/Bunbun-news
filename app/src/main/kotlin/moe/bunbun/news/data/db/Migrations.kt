package moe.bunbun.news.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v0.2 主题 A 子 6 — Room schema 1→2 迁移：新增 FTS4 全文索引表。
 *
 * 因为开发期一直开 `fallbackToDestructiveMigration`，装机升级会丢数据。
 * 此 commit 开始补正式迁移：v0.2.x 用户从 v0.1.x 升级不会再清表。
 */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) 建 articles_fts FTS4 表（与 @Fts4(contentEntity = ArticleEntity::class) 一致）
        //    schema：FTS4(content=articles, content_rowid=rowid)
        //    注意：content_rowid 选项是列名（裸标识符），不能用 `=` 加反引号字符串
        db.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `articles_fts` USING FTS4(
                `title`,
                `excerpt`,
                content=`articles`,
                content_rowid=rowid
            )
            """.trimIndent(),
        )
        // 2) 同步索引的 trigger（Room @Fts4 默认生成）
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS `room_fts_content_insert_articles_fts` AFTER INSERT ON `articles`
            BEGIN
                INSERT INTO `articles_fts`(`rowid`, `title`, `excerpt`)
                VALUES (new.`rowid`, new.`title`, new.`excerpt`);
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS `room_fts_content_delete_articles_fts` AFTER DELETE ON `articles`
            BEGIN
                INSERT INTO `articles_fts`(`articles_fts`, `rowid`, `title`, `excerpt`)
                VALUES ('delete', old.`rowid`, old.`title`, old.`excerpt`);
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS `room_fts_content_update_articles_fts` AFTER UPDATE ON `articles`
            BEGIN
                INSERT INTO `articles_fts`(`articles_fts`, `rowid`, `title`, `excerpt`)
                VALUES ('delete', old.`rowid`, old.`title`, old.`excerpt`);
                INSERT INTO `articles_fts`(`rowid`, `title`, `excerpt`)
                VALUES (new.`rowid`, new.`title`, new.`excerpt`);
            END
            """.trimIndent(),
        )
        // 3) Backfill：把现有 articles 的 rowid + title + excerpt 复制到 FTS 表
        db.execSQL(
            """
            INSERT INTO `articles_fts`(`rowid`, `title`, `excerpt`)
            SELECT `rowid`, `title`, `excerpt` FROM `articles`
            """.trimIndent(),
        )
    }
}

/**
 * v0.2 主题 D 子 3 — Room schema 2→3 迁移：新增 summary_cache 表。
 */
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `summary_cache` (
                `articleId` TEXT NOT NULL,
                `summary` TEXT NOT NULL,
                `providerLabel` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`articleId`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_summary_cache_updatedAt` ON `summary_cache` (`updatedAt` DESC)",
        )
    }
}