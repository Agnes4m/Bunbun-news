<div align="center">

# 🗞️ 文闻 · Bunbun News

**轻量级 Android RSS 新闻聚合器** —— 拒绝臃肿，只读你想看的。

> 吉祥物：射命丸文（《东方 Project》的鸦天狗记者）
> 快、轻、准 —— 和文文一样的采访速度，给你干净的资讯流。

</div>

---

## ✨ 特性

- ✅ **极简阅读体验**：无广告、无推荐、无社交。只看你订阅的内容。
- ✅ **多格式订阅**：RSS 2.0 / Atom / JSON Feed / OPML 导入导出。
- ✅ **离线阅读**：文章内容本地缓存，断网也能看。
- ✅ **稍后读**：收藏的文章跨设备可回看。
- ✅ **后台同步**：WorkManager 定时拉取，下拉立即刷新。
- ✅ **Material You**：跟随系统动态配色，明/暗/护眼主题。
- 🚧 **智能聚合去重**（v0.3）：同一事件多源报道自动合并。
- 🚧 **AI 摘要**（v0.5+）：可选云端 API 或本地模型生成一句话摘要。

> ⚠️ **本项目为《东方 Project》粉丝作品**，东方 Project 版权归 © 上海アリス幻樂団（ZUN）所有。项目不使用官方插画，仅为设定致敬。

---

## 📸 截图

<!-- TODO: 等 v0.1 里程碑完成后补截图
![时间线](docs/screenshots/timeline.png)
![阅读器](docs/screenshots/reader.png)
![订阅管理](docs/screenshots/feeds.png)
-->

---

## 🚀 构建

> 需要 JDK 17+（推荐 JDK 21）和 Android SDK 34+。

```bash
# 1. 克隆仓库
git clone https://github.com/<your-username>/Bunbun-news.git
cd Bunbun-news

# 2. 设置 ANDROID_HOME（若未配置）
#   export ANDROID_HOME=$HOME/Android/Sdk   # Linux/macOS
#   setx ANDROID_HOME "C:\Android\Sdk"      # Windows

# 3. 构建 Debug APK
./gradlew assembleDebug

# 4. 安装到设备 / 模拟器
adb install app/build/outputs/apk/debug/app-debug.apk

# 5. 运行单元测试
./gradlew test
```

Debug APK 输出在 `app/build/outputs/apk/debug/`，目标体积 < 5 MB。

---

## 🛣️ 路线图

| 版本 | 主题 | 能力 |
|---|---|---|
| **v0.1** | 核心阅读 | 订阅管理、RSS 解析、时间线、阅读器、收藏、设置 |
| v0.2 | 离线 + 体验 | 全文提取、图片预下载、三主题、分类 |
| v0.3 | 聚合去重 | 多源同事件合并 |
| v0.4 | 可选同步 | Miniflux / FreshRSS 后端同步 |
| v0.5+ | AI 摘要 | 云端 + 本地模型 |
| v1.0 | 多平台 | KMP 抽核，桌面端 |

详见 [docs/ROADMAP.md](docs/ROADMAP.md)。

---

## 📦 技术栈

- **Kotlin 2.0** + **Jetpack Compose** + **Material 3**
- **MVVM + UDF + Hilt** 依赖注入
- **Room** 本地数据库 + **DataStore** 偏好
- **prof18/RSS-Parser**（RSS/Atom/RDF 解析，KMP 库）+ **Readability4J**（正文提取，v0.2）
- **OkHttp + Retrofit** 网络层 · **WorkManager** 后台同步
- **Coil** 图片加载 · **Navigation Compose** 导航

---

## 🤝 贡献

欢迎提 Issue 和 PR！请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

- 🐛 报告 Bug：[新建 Issue](https://github.com/<your-username>/Bunbun-news/issues/new/choose)
- 💡 提功能：[Feature Request 模板](https://github.com/<your-username>/Bunbun-news/issues/new?template=feature_request.md)

---

## 📄 License

[Apache License 2.0](LICENSE) © Bunbun News contributors

---

## 🙏 致谢

本项目借鉴了以下优秀开源项目的思路，致谢：

- [**Read You**](https://github.com/Ashinch/ReadYou) —— Material You RSS 阅读器范式
- [**FeedFlow**](https://github.com/prof18/feed-flow) —— KMP 跨平台范式
- [**Miniflux**](https://github.com/miniflux/v2) —— 自托管后端架构参考
- [**ROME**](https://github.com/rometools/rome) —— RSS 解析库
- [**东方 Project**](https://www.touhou-project.com/) —— 吉祥物射命丸文（非官方粉丝作品）

---

<div align="center">
<sub>文文：「今天的头条，就决定是——你订阅的那些新闻啦！」🪶</sub>
</div>
