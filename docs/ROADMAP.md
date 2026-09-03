# 🛣️ 路线图

> **文闻（Bunbun News）** 的版本规划。里程碑对应每个可安装、可验证的版本。

## v0.1 —— 核心阅读（当前）

**目标**：一个能装到手机、干净好用的 RSS + 事件订阅阅读器 MVP。

**4 Tab 结构**（类 Bilibili 布局）：

| Tab | 内容 |
|---|---|
| 🏠 首页 | 今日文章时间线（热度算法 v0.2 再加） |
| 🔍 搜索 | FTS4 全文搜索文章标题/摘要 |
| 📡 订阅 | 已订阅源 + 已订阅事件的混合时间线（右上角进 ManageFeedsScreen）|
| 👤 个人 | 历史记录 / 收藏 / 订阅管理入口 / 设置 / 关于 |

### 里程碑

- [x] M0：GitHub 仓库初始化（LICENSE、CI、模板）
- [x] M1：工程脚手架（Gradle、Compose、Theme、底部导航占位）
- [ ] M2：**数据层**（Room：Feed + Article + Subscription + History + FTS4 表 + clusterId）
- [ ] M3：RSS 抓取解析（RSS-Parser + ETag 缓存 + **简化版聚类**：URL 规范化 + 标题 Jaccard）
- [ ] M4：同步引擎（WorkManager 定时拉取）
- [ ] M5：ManageFeedsScreen（增删改 RSS 源 + OPML 导入导出）
- [ ] M6：HomeScreen（今日时间线）+ SearchScreen（FTS 查询）+ SubscriptionsScreen（源+事件混合时间线）
- [ ] M7：ReaderScreen + **"订阅此事件"按钮** + 写历史
- [ ] M8：ProfileScreen 整合（历史/收藏/订阅入口/设置/关于）
- [ ] M9：装机验证 + Release v0.1.0

### 关键技术亮点

- **订阅两种目标**：RSS 新闻源 + 聚类事件（同一事件多源报道作为一个时间线单位）
- **事件聚类简化版**：URL 规范化（去 utm_* 等追踪参数）+ 标题 Jaccard 相似度（v0.3 升级）
- **FTS4 全文搜索**：Room 自带支持，无需额外服务

## v0.2 —— 离线 + 体验

- [ ] Readability4J 全文提取（解决 RSS 摘要截断）
- [ ] 图片预下载缓存（离线看图）
- [ ] 明 / 暗 / 护眼三主题
- [ ] Material You 动态配色
- [ ] 分类分组
- [ ] 🆕 **首页热度算法**（按"被多少个 feed 报道同一事件"排序）

## v0.3 —— 完整聚合去重

- [ ] URL 规范化升级（更多追踪参数规则）
- [ ] 标题 SimHash 替代 Jaccard（更高效）
- [ ] 时间窗聚类（同事件 24h 内合并）
- [ ] "同一事件 N 源"合并卡片 UI

## v0.4 —— 可选同步

- [ ] Miniflux / FreshRSS REST API 对接
- [ ] Fever 协议兼容
- [ ] 已读 / 收藏 / 订阅 双向同步

## v0.5+ —— AI 摘要

- [ ] 云端 API 摘要（OpenAI / DeepSeek 等）
- [ ] 可选 MediaPipe Gemma 2B 本地推理
- [ ] 摘要缓存 + 离线回看

## v1.0 —— 多平台

- [ ] core 模块抽取为 KMP
- [ ] 桌面端 Compose Multiplatform（Windows / Linux）
- [ ] 正式签名 + Play 商店 / F-Droid 发布

---

> 变更日志见 [CHANGELOG.md](CHANGELOG.md) · 架构说明见 [ARCHITECTURE.md](ARCHITECTURE.md)