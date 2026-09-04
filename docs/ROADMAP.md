# 🛣️ 路线图

> **文闻（Bunbun News）** 的版本规划。原路线图中 v0.2 / v0.3 / v0.4 / v0.5+ 的全部特性合并到下一个大版本 **v0.2.0** 完成；v0.1 → v0.2 之间用 **v0.1.x** 作为增量补丁号。

## 版本总览

| 版本 | 性质 | 内容 |
|---|---|---|
| **v0.1.0** ✅ | 已发布 | 核心阅读 MVP（5 Tab + 分类 + 首启引导 + 6 个国内推荐 RSS 源）|
| **v0.1.1** ~ **v0.1.x** | 增量补丁 | v0.1 → v0.2 过渡期的 bug 修复 / 文案打磨 / CI 优化 / 单源补充 |
| **v0.2.0** | **大合并版** | 把原路线 v0.2（体验）/ v0.3（聚合）/ v0.4（后端）/ v0.5+（AI 摘要）全部合并到这一个版本 |
| **v1.0** | 远期 | core 模块抽 KMP + 桌面端 + 正式签名 |

---

## v0.1.0 — 核心阅读 ✅ 已发布

**目标**：一个能装到手机、干净好用的 RSS + 事件订阅阅读器 MVP。

**5 Tab 结构**（类 Bilibili 布局）：

| Tab | 内容 |
|---|---|
| 🏠 首页 | 今日文章时间线 |
| 🔍 搜索 | LIKE 搜索文章标题/摘要（v0.2 升级 FTS4）|
| 🏷️ 分类 | 按 feed.category 过滤（chips 切换综合/科技/财经/生活/国际）|
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
- [x] **M10 修复**：FeedDao `REPLACE` 触发 `ON DELETE CASCADE` 把文章级联清空 → 改用 `@Upsert`
- [x] **M11**：分类 Tab + Reader 闪退修复（SavedStateHandle → setArticleId）+ 内置 6 个推荐 RSS 源 + 首启引导页

### v0.1.0 关键指标

| 项 | 数值 |
|---|---|
| 单元测试 | 47/47 通过 |
| APK 大小 | 19.2 MB (debug) |
| GitHub Release | [v0.1.0](https://github.com/Agnes4m/Bunbun-news/releases/tag/v0.1.0) |
| 推荐 RSS 源 | 6 个国内稳定可达源（少数派 / 阮一峰 / IT之家 / 爱范儿 / InfoQ 中文 / 开源中国） |
| 实测抓取 | 163 篇文章入库，0 fetch/parse failed |
| 屏幕数 | 5 主 + 6 子 |
| 支持 RSS 格式 | RSS 2.0 + Atom + OPML 2.0 |

### v0.1.0 关键技术亮点

- **混合订阅**：同时支持订阅 RSS 新闻源 + 聚类事件
- **简化版事件聚类**：URL 规范化 + 标题 token 化 + top 6 排序签名（v0.2 升级 SimHash）
- **LIKE 搜索**：FTS4 全文索引留 v0.2 升级
- **混合时间线**：一次 SQL JOIN 搞定源+事件订阅
- **Hilt + WorkManager**：@HiltWorker 实现 Worker 注入
- **首启引导**：DataStore `firstLaunchDone` 标记 + OnboardingScreen + 一键导入推荐源
- **数据库迁移保护**：用 `@Upsert` 而非 REPLACE，避免外键 `ON DELETE CASCADE` 误删

---

## v0.1.x — 增量补丁（v0.1 → v0.2 之间）

每个补丁独立 commit / tag。bug 修了、文案改了、单源补了都可发。预期可能的小补丁：

- [ ] **v0.1.1**：完善 README 截图、关于页文案细化、`strings.xml` 文案补全
- [ ] **v0.1.2**：单测覆盖 Reader / Onboarding 路径
- [ ] **v0.1.3**：根据真实使用反馈修 bug（分类 chips 默认值调整、滑动冲突等）
- [ ] **v0.1.x**：任何非破坏性的小修补

> 任何 **新功能 / 架构变更** 都不进 v0.1.x，全部进 v0.2。

---

## v0.2.0 — 大合并版（合并原 v0.2 / v0.3 / v0.4 / v0.5+）

**目标**：把原路线图中"离线体验 / 聚合去重 / 后端同步 / AI 摘要"四个方向全部做到 v0.2 一个大版本里。v0.2 不再做子版本拆分（小改进直接发 v0.2.x patch）。

### 🎨 主题 A：离线 + 体验（原 v0.2 内容）

- [ ] **Readability4J 全文提取**：Reader 展示 RSS 摘要前先过 Readability，截断/被省略的正文补全
- [ ] **图片预下载缓存**：Coil Disk Cache + 自定义 ImageLoader，文章内的图片后台预下到本地（用户在地铁/无网也能看）
- [ ] **三主题**：明 / 暗 / 护眼（DataStore 已有 `darkMode`，需要扩展为 `ThemeMode` 枚举）
- [ ] **Material You 动态配色**：Android 12+ 取 wallpaper color，< 12 用静态 Material3 调色
- [ ] **分类分组完善**：分类 chips 增加"全部分类"入口 + 分类内按日期分组
- [ ] **首页热度算法**：按"被多少个 feed 报道同一事件（clusterId）"排序，给热门事件加权
- [ ] **FTS4 全文搜索**：替换 `LIKE '%query%'`（Room `@Fts4` virtual table）
- [ ] **主题切换真正生效**：在 SettingsScreen 切换后立刻 Compose recomposition（之前 DataStore 流未正确 collect）

### 🧩 主题 B：完整聚合去重（原 v0.3 内容）

- [ ] **URL 规范化升级**：覆盖更多追踪参数（utm_* 已有，补充 gclid / fbclid / _hsenc / spm 等）
- [ ] **标题 SimHash 替代 Jaccard**：64-bit SimHash + 海明距离，更快更准
- [ ] **时间窗聚类**：同一事件页面相邻 24h 内强制合并
- [ ] **"同一事件 N 源"合并卡片 UI**：首页/订阅时间线出现 clusterSize > 1 的事件时，顶部显示"📰 N 个源都在报道"，点开看所有源

### 🔄 主题 C：可选后端同步（原 v0.3 + v0.4 内容）

- [ ] **Miniflux / FreshRSS REST API 对接**：用户填 endpoint + API token，可选把已读/收藏/订阅同步到自托管后端
- [ ] **Fever 协议兼容**：作为轻量备选
- [ ] **双向同步**：本地标记已读 → 推送到后端；后端文章 → 拉到本地
- [ ] **冲突解决**：以本地时间戳为准；删除本地标记需要用户在设置里显式触发

### 🤖 主题 D：AI 摘要（原 v0.5+ 内容）

- [ ] **云端 API 摘要**：默认 DeepSeek（中文友好 + 便宜），可切 OpenAI / 自定义 endpoint
- [ ] **本地模型**：MediaPipe Gemma 2B（约 1.5GB），无网也能摘要；可在设置里开关
- [ ] **摘要缓存**：Room 加 `article_summary` 字段 + `summaryGeneratedAt` 时间戳，避免重复生成
- [ ] **离线回看**：摘要生成后存本地，删云端 token 后仍可用
- [ ] **SettingsScreen 入口**：API Key / 模型选择 / 关闭 AI 摘要

### v0.2.0 关键指标（目标）

| 项 | 目标值 |
|---|---|
| 单元测试 | 100+ 通过 |
| APK 大小 | < 25 MB（debug，含 FTS4 / Readability / Coil）|
| 推荐 RSS 源 | 扩展到 15+（补全国际/财经分类，需要解决被墙问题）|
| 主题 | 3 套（明/暗/护眼）+ Material You |
| 后端协议 | Miniflux REST + Fever |
| AI 摘要 | 云端 + 本地双模 |
| 主界面 | 支持"按事件/按源"两种视图切换 |

> ⚠️ **风险**：v0.2 是个大跃进，估算需要 1-2 个月开发 + 大量测试。任何子主题遇到阻塞可以单独延后到 v0.2.x 补丁。

---

## v1.0 — 多平台（远期）

- [ ] core 模块抽取为 KMP（共享 Room / 网络 / 数据层）
- [ ] 桌面端 Compose Multiplatform（Windows / Linux）
- [ ] 正式签名 + Play 商店 / F-Droid 发布

---

> 变更日志见 [CHANGELOG.md](CHANGELOG.md) · 架构说明见 [ARCHITECTURE.md](ARCHITECTURE.md)
