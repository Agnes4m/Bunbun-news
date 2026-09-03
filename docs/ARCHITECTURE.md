# 架构说明（v0.1 草稿）

> 本文档描述 **文闻（Bunbun News）** 的技术架构。v0.1 阶段为 Android 单模块，v1.0 计划抽取 KMP 核心。

## 总览

```
┌─────────────────────────────────────────────────────────────────┐
│                  UI 层 (Compose)                                 │
│  HomeScreen │ SearchScreen │ SubscriptionsScreen │ ProfileScreen │
│            └──────── ReaderScreen (全屏) ──────────┘             │
│  ManageFeedsScreen / AddFeedDialog / SettingsScreen（Profile内嵌）│
└────────────────────────────┬────────────────────────────────────┘
                             │ StateFlow / ViewModel (UDF)
┌────────────────────────────▼────────────────────────────────────┐
│                    Domain 层 (UseCase)                           │
│  SyncFeeds │ MarkRead │ ToggleStar │ ManageSubscription │         │
│  SearchArticles │ RecordHistory                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ Repository 接口
┌────────────────────────────▼────────────────────────────────────┐
│                  Data 层 (Repository)                            │
│  FeedRepo │ ArticleRepo │ SubscriptionRepo │ HistoryRepo          │
└─────┬───────────────┬───────────────────┬────────────────────┘
      │               │                   │
┌─────▼────────┐  ┌────▼─────────────┐  ┌──▼──────────────┐
│  Room DB     │  │  RSS 抓取层       │  │  搜索层         │
│  feeds       │  │  FeedFetcher      │  │  Room FTS4      │
│  articles    │  │  FeedParser       │  │  (标题+摘要)    │
│  + FTS4      │  │  + ClusterEngine  │  │                  │
│  subscriptions│  │    (URL+Title)   │  │                  │
│  history     │  │  SyncWorker       │  │                  │
└──────────────┘  └──────────────────┘  └──────────────────┘
```

## 核心数据流

### 同步流
```
SyncWorker → SyncFeedsUseCase → FeedRepository
  → FeedFetcher（带 ETag/If-Modified-Since）→ FeedParser（RSS-Parser）
  → ClusterEngine（URL 规范化 + 标题 Jaccard 算 clusterId）
  → 写入 Room (articles + FTS)
```

### 阅读流
```
用户点击文章 → ReaderScreen
  → 写入 HistoryEntity（readAt + scrollPercent）
  → WebView 加载原文
  → "订阅此事件"按钮 → SubscriptionRepo.add(type=event, targetId=clusterId)
```

### 订阅流（混合源 + 事件）
```
SubscriptionsScreen
  → SubscriptionRepository.observeAll() 返回 Flow<List<Subscription>>
  → 按 type 字段筛选（"feed" 或 "event"）
  → 渲染时间线：event 类型合并显示同 clusterId 的所有源
```

### 搜索流
```
SearchScreen 输入查询 → SearchUseCase
  → Room FTS4 MATCH 查询
  → 按 publishedAt 倒序返回文章列表
```

## 关键决策

| 决策 | 选择 | 理由 |
|---|---|---|
| 单 Activity | ✅ | Compose + Navigation Compose |
| Hilt DI | ✅ | 构造注入 |
| Room + FTS4 | ✅ | 类型安全 SQL，自带全文搜索 |
| RSS-Parser | ✅ | prof18 维护，无依赖冲突，比 Rome 干净 |
| 事件聚类简化版 | URL 规范化 + 标题 Jaccard | v0.1 够用；v0.3 升级 SimHash |
| 订阅两种类型 | type ∈ {feed, event} | 一张 SubscriptionEntity 表解决 |
| 单 Database + 单 Repository 集合 | ✅ | v0.1 单 App，无需拆分多模块 |

## 目录结构

```
app/src/main/kotlin/moe/bunbun/news/
├── ui/
│   ├── theme/      # Color, Theme, Type
│   ├── nav/        # ZixunNavHost（4 Tab + 子页面路由）
│   ├── home/       # HomeScreen + ViewModel
│   ├── search/     # SearchScreen + ViewModel
│   ├── subscriptions/  # SubscriptionsScreen + ViewModel
│   ├── profile/    # ProfileScreen + 子页面（Settings/About/Starred/History）
│   ├── managefeeds/   # ManageFeedsScreen + AddFeedDialog
│   └── reader/     # ReaderScreen + ViewModel
├── data/
│   ├── db/         # Room: Entities, DAOs, Database, FTS
│   ├── repo/       # Repository 接口 + 实现
│   ├── rss/        # FeedFetcher, FeedParser, ClusterEngine, OpmlImporter
│   └── prefs/      # UserPreferences (DataStore)
├── domain/
│   ├── model/      # 领域模型
│   └── usecase/    # 业务用例
├── sync/           # SyncWorker (WorkManager)
└── di/             # Hilt 模块
```

## 数据模型（v0.1 完整）

```kotlin
@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val id: String,           // url 的 hash
    val url: String,
    val title: String,
    val siteUrl: String?,
    val iconUrl: String?,
    val category: String? = null,
    val lastSyncAt: Long? = null,
    val etag: String? = null,
    val lastModified: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "articles",
    indices = [Index("feedId"), Index("publishedAt"),
               Index("clusterId"),
               Index(value = ["feedId","guid"], unique = true)],
)
@Fts4(contentEntity = ArticleEntity::class)  // 虚拟 FTS 表
data class ArticleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "feedId") val feedId: String,
    val guid: String,
    val title: String,
    val author: String?,
    val url: String,
    val contentHtml: String?,
    val excerpt: String?,
    val publishedAt: Long?,
    val fetchedAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val clusterId: String? = null,        // M3 计算
)

@Entity(tableName = "subscriptions", indices = [Index("type"), Index("targetId")])
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val type: String,                     // "feed" 或 "event"
    val targetId: String,
    val title: String,                    // 冗余存储，避免 join
    val notifyEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "history", indices = [Index(value = ["readAt"], orders = [Index.Order.DESC])])
data class HistoryEntity(
    @PrimaryKey val articleId: String,
    val readAt: Long,
    val scrollPercent: Float = 0f,
)
```

## 依赖注入图

- `DatabaseModule` → Room Database、DAO、FtsEntity DAO
- `NetworkModule` → OkHttp、Json
- `RepositoryModule` → 绑定 Repository 接口
- `UseCaseModule` → 提供 UseCase 实例（v0.2+ 再加）

> ⚠️ 本文档随代码演进持续更新。