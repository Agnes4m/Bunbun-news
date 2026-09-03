# 变更日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 和 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [0.1.0] - 2026-09-03 🎉 首个公开版本

### Added
- **数据层**：Room 数据库（Feed / Article / Subscription / History 四张表 + FTS-ready 索引）
- **RSS 抓取**：OkHttp 客户端 + ETag/Last-Modified 条件请求 + 304 跳过
- **RSS 解析**：自写 XmlPullParser 解析器（支持 RSS 2.0 + Atom + dc:creator + content:encoded）
- **事件聚类**：URL 规范化 + 标题 token 化 + 停用词过滤 + top 6 排序签名
- **同步引擎**：WorkManager 30min 周期 + HiltWorker 注入 + 联网约束 + 指数退避
- **订阅管理**：ManageFeedsScreen（增删改 RSS 源）+ AddFeedDialog + 同步 + 导入示例 OPML
- **OPML 支持**：解析（嵌套分类 + rss/atom 类型过滤）+ 导出
- **4 Tab 主屏幕**（类 Bilibili 布局）：
  - 🏠 首页（今日文章时间线 + 热度图标占位）
  - 🔍 搜索（300ms 防抖 + LIKE 搜索）
  - 📡 订阅（混合源/事件时间线 + 右上角管理入口）
  - 👤 个人（统计卡 + 5 个子页面入口）
- **ReaderScreen**：WebView 渲染原文 + 顶部星标 + "订阅此事件"按钮 + 自动写历史
- **Profile 子页面**：
  - 历史记录（按时间倒序）
  - 收藏（按 publishedAt 倒序）
  - 设置（深色模式开关 + 同步频率 15/30/60/120 min）
  - 关于（版本/致谢/许可）
- **二级导航**：从订阅 Tab 和个人 Tab 都可进入管理订阅；从个人页进入历史/收藏/设置/关于
- **DataStore Preferences**：UserPreferences 包装（深色模式、同步间隔、首次启动标记）
- **统计卡**：实时显示订阅源数、订阅项数、收藏数、历史数
- **47 个单元测试** 全部通过（UrlNormalizer 16 + ClusterEngine 13 + FeedParser 6 + OpmlImporter 5 + Mappers 7）

### Technical
- **技术栈**：Kotlin 2.0.21 + AGP 8.7.3 + JDK 21 + Jetpack Compose + Material 3 + Hilt 2.52
- **架构**：MVVM + UDF + Repository + 单一 Activity + Navigation Compose
- **数据库**：Room 2.6.1（4 表 + 多索引）
- **网络**：OkHttp 4.12 + Retrofit 2.11（备用，目前用 OkHttp 直接调）
- **异步**：Coroutines 1.8.1 + StateFlow + Flow
- **图片**：Coil 2.7.0（备用，M7+ 用）
- **后端任务**：WorkManager 2.9.1 + HiltWorker 1.2.0
- **存储偏好**：DataStore Preferences 1.1.1
- **日志**：Timber 5.0.1
- **RSS 解析**：prof18/RSS-Parser-android 6.0.10（**未使用**，被自写 XmlPullParser 替代）
- **测试依赖**：kxml2 2.3.0（提供纯 JVM XmlPullParser，避开 Android 单元测试 mock）

### Limitations (v0.1.0 已知限制)
- 无 全文提取（依赖 RSS 原文，v0.2 加 Readability4J）
- 无 图片缓存（图片按需加载，v0.2 加 Coil 预下载）
- 无 FTS 搜索（用 LIKE，v0.2 升级 FTS4）
- 主题切换只存设置不真正生效（v0.2 接入）
- 事件聚类简化版（v0.3 升级 SimHash）
- 无 后端同步（v0.4 加 Miniflux/FreshRSS 适配）

## [Unreleased] - 计划中

### Planned (v0.2)
- Readability4J 全文提取
- 图片预下载 + 离线
- 三主题 + 动态色
- 热度算法
- FTS4 搜索
