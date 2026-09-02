# 架构说明（v0.1 草稿）

> 本文档描述 **文闻（Bunbun News）** 的技术架构。v0.1 阶段为 Android 单模块，v1.0 计划抽取 KMP 核心。

## 总览

```
┌────────────────────────────────────────────────┐
│                    UI 层 (Compose)              │
│  Timeline │ Feeds │ Reader │ Starred │ Settings │
└───────────────┬────────────────────────────────┘
                │ StateFlow / ViewModel (UDF)
┌───────────────▼────────────────────────────────┐
│              Domain 层 (UseCase)                │
│  SyncFeeds │ MarkRead │ ToggleStar │ ManageFeed │
└───────────────┬────────────────────────────────┘
                │ Repository 接口
┌───────────────▼────────────────────────────────┐
│             Data 层 (Repository)                │
│  FeedRepository │ ArticleRepository             │
└───────┬──────────────────────────┬─────────────┘
        │                          │
┌───────▼────────┐        ┌────────▼─────────────┐
│  Room 数据库    │        │  RSS 抓取层           │
│  feeds/articles │        │  FeedFetcher(OkHttp) │
│  DataStore 偏好  │        │  FeedParser(Rome)    │
│  Coil 图片缓存   │        │  SyncWorker(WM)      │
└─────────────────┘        └─────────────────────┘
```

## 数据流

1. **同步**：`SyncWorker`（WorkManager）定时触发 → `SyncFeedsUseCase` → `FeedRepository` → `FeedFetcher`（带 ETag/If-Modified-Since）→ `FeedParser`（Rome 解析）→ 写入 Room。
2. **读取**：`TimelineViewModel` 订阅 `ArticleDao` 的 Flow → Compose 重组渲染。
3. **交互**：点击已读/收藏 → `MarkReadUseCase` / `ToggleStarUseCase` → 更新 Room → Flow 通知 UI。

## 关键决策

| 决策 | 选择 | 理由 |
|---|---|---|
| 单 Activity | ✅ | Compose + Navigation Compose，避免多 Activity 冗余 |
| Hilt DI | ✅ | 构造注入，模块化清晰 |
| Room | ✅ | 类型安全 SQL，Flow 响应式查询 |
| Rome | ✅ | 唯一同时支持 RSS/Atom/JSON Feed/OPML 的库 |
| 混合架构 | 默认离线 + 可选同步 | v0.4 起可挂 Miniflux/FreshRSS |

## 目录结构

```
app/src/main/kotlin/moe/bunbun/news/
├── ui/        # Compose 界面 + ViewModel
├── data/      # Room、Repository、RSS、偏好
├── domain/    # 实体 + 用例
├── sync/      # WorkManager 后台任务
└── di/        # Hilt 模块
```

## 依赖注入图

- `DatabaseModule` → 提供 Room Database、DAO
- `NetworkModule` → 提供 OkHttp、Rome SyndFeed
- `RepositoryModule` → 绑定 Repository 接口

> ⚠️ 本文档随代码演进持续更新。新增组件请遵循上述分层，勿让 UI 直接访问数据层。
