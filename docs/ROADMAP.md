# 🛣️ 路线图

> **文闻（Bunbun News）** 的版本规划。里程碑对应每个可安装、可验证的版本。

## v0.1 —— 核心阅读（当前）

**目标**：一个能装到手机、干净好用的 RSS 阅读器 MVP。

- [x] M0：GitHub 仓库初始化（LICENSE、CI、模板）
- [ ] M1：工程脚手架（Gradle、Compose、Hilt、Theme）
- [ ] M2：数据层（Room：Feed + Article）
- [ ] M3：RSS 抓取解析（Rome + ETag 缓存）
- [ ] M4：同步引擎（WorkManager 定时拉取）
- [ ] M5：订阅管理 UI（添加/删除/OPML 导入导出）
- [ ] M6：时间线 UI（按时间倒序 + 下拉刷新）
- [ ] M7：阅读器（WebView 原文 + 已读/收藏）
- [ ] M8：收藏 + 设置
- [ ] M9：装机验证 + Release v0.1.0

## v0.2 —— 离线 + 体验

- [ ] Readability4J 全文提取（解决 RSS 摘要截断）
- [ ] 图片预下载缓存（离线看图）
- [ ] 明 / 暗 / 护眼三主题
- [ ] Material You 动态配色
- [ ] 分类分组

## v0.3 —— 聚合去重

- [ ] URL 规范化（去 utm_ 参数、归一化）
- [ ] 标题 Jaccard 相似度 + 时间窗聚类
- [ ] "同一事件 N 源"合并卡片

## v0.4 —— 可选同步

- [ ] Miniflux / FreshRSS REST API 对接
- [ ] Fever 协议兼容
- [ ] 已读 / 收藏双向同步

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
