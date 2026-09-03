# 🛣️ 路线图

> **文闻（Bunbun News）** 的版本规划。里程碑对应每个可安装、可验证的版本。

## v0.1.0 — 核心阅读 ✅ 已发布

**目标**：一个能装到手机、干净好用的 RSS + 事件订阅阅读器 MVP。

**4 Tab 结构**（类 Bilibili 布局）：

| Tab | 内容 |
|---|---|
| 🏠 首页 | 今日文章时间线（热度算法 v0.2 再加） |
| 🔍 搜索 | LIKE 搜索文章标题/摘要 |
| 📡 订阅 | 已订阅源 + 已订阅事件的混合时间线（右上角进 ManageFeedsScreen）|
| 👤 个人 | 统计卡 + 历史/收藏/订阅管理/设置/关于 |

### 里程碑（全部完成）

- [x] M0：GitHub 仓库初始化（LICENSE、CI、模板）
- [x] M1：工程脚手架（Gradle、Compose、Theme、4 Tab 导航占位）
- [x] M2：**数据层**（Room：Feed + Article + Subscription + History + clusterId）
- [x] M3：RSS 抓取解析（OkHttp + ETag + 自写 XmlPullParser + 简化版聚类）
- [x] M4：同步引擎（WorkManager + HiltWorker + 30min 周期）
- [x] M5：ManageFeedsScreen + AddFeedDialog + OPML 导入导出
- [x] M6：HomeScreen + SearchScreen + SubscriptionsScreen 完整版
- [x] M7：ReaderScreen + "订阅此事件"按钮 + 历史记录
- [x] M8：ProfileScreen 整合（历史/收藏/设置/关于）
- [x] M9：装机验证 + Release v0.1.0

### v0.1.0 关键指标

| 项 | 数值 |
|---|---|
| 单元测试 | 47/47 通过 |
| APK 大小 | 19.2 MB (debug) |
| 本地 commit | 9 个（M0-M8）|
| 屏幕数 | 4 主 + 6 子 |
| 支持 RSS 格式 | RSS 2.0 + Atom + OPML 2.0 |

### 关键技术亮点

- **混合订阅**：同时支持订阅 RSS 新闻源 + 聚类事件
- **简化版事件聚类**：URL 规范化 + 标题 token 化 + top 6 排序签名
- **LIKE 搜索**：FTS 留 v0.2 优化
- **混合时间线**：一次 SQL JOIN 搞定源+事件订阅
- **Hilt + WorkManager**：@HiltWorker 实现 Worker 注入

## v0.2 — 离线 + 体验

- [ ] Readability4J 全文提取（解决 RSS 摘要截断）
- [ ] 图片预下载缓存（离线看图）
- [ ] 明 / 暗 / 护眼三主题（DataStore 已就位）
- [ ] Material You 动态配色
- [ ] 分类分组
- [ ] 🆕 **首页热度算法**（按"被多少个 feed 报道同一事件"排序）
- [ ] FTS4 全文搜索（替换 LIKE）
- [ ] 主题切换真正生效（读取 DataStore）

## v0.3 — 完整聚合去重

- [ ] URL 规范化升级（更多追踪参数规则）
- [ ] 标题 SimHash 替代 Jaccard（更高效）
- [ ] 时间窗聚类（同事件 24h 内合并）
- [ ] "同一事件 N 源"合并卡片 UI

## v0.4 — 可选同步

- [ ] Miniflux / FreshRSS REST API 对接
- [ ] Fever 协议兼容
- [ ] 已读 / 收藏 / 订阅 双向同步

## v0.5+ — AI 摘要

- [ ] 云端 API 摘要（OpenAI / DeepSeek 等）
- [ ] 可选 MediaPipe Gemma 2B 本地推理
- [ ] 摘要缓存 + 离线回看

## v1.0 — 多平台

- [ ] core 模块抽取为 KMP
- [ ] 桌面端 Compose Multiplatform（Windows / Linux）
- [ ] 正式签名 + Play 商店 / F-Droid 发布

---

> 变更日志见 [CHANGELOG.md](CHANGELOG.md) · 架构说明见 [ARCHITECTURE.md](ARCHITECTURE.md)
