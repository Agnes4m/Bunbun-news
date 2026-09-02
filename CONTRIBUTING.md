# 贡献指南

感谢你对 **文闻（Bunbun News）** 感兴趣！无论你是报告 Bug、提新功能、还是提交代码，都欢迎。

---

## 📋 提 Issue

在创建 Issue 前，请先：

1. **搜索**是否已有相同或相关的 Issue。
2. 使用对应的模板（GitHub 会自动提供）：
   - 🐛 [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) —— 崩溃、闪退、行为异常
   - 💡 [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) —— 新功能建议

### Bug 报告小贴士

- 提供**复现步骤**（做了什么 → 发生了什么 → 期望是什么）
- 附上**设备信息**（型号、Android 版本、应用版本号）
- 如崩溃请贴 **Logcat 堆栈**（用 `adb logcat` 抓取）

---

## 🛠️ 提交代码（PR）

### 分支规范

- `main`：稳定分支，只能通过 PR 合入。
- 新功能/修复：从 `main` 切分支，命名如 `feat/xxx`、`fix/xxx`、`docs/xxx`。

### 提交信息规范

```
M<n>: <简述>           # 里程碑提交（如 M3: 完成 RSS 解析）
feat: 新增 xx 功能
fix: 修复 xx 崩溃
docs: 更新 README
refactor: 重构 xx
test: 增加 xx 测试
```

### 开发流程

```bash
git checkout -b feat/my-feature
# 写代码...
./gradlew assembleDebug        # 确认能编译
./gradlew test                 # 确认测试通过
git commit -m "feat: 新增 xx"
git push origin feat/my-feature
```

然后到 GitHub 提交 PR，选择 [Pull Request 模板](.github/PULL_REQUEST_TEMPLATE.md)。

### PR 检查清单

- [ ] 代码通过 `./gradlew assembleDebug` 编译
- [ ] 相关测试通过（如涉及）
- [ ] 遵循现有代码风格（Kotlin 官方风格 + ktlint）
- [ ] 没有把密钥/证书/本地配置提交进去
- [ ] 更新了相关文档（如改动行为）

---

## 🎨 设计约定

- 本项目的 UI 哲学是**极简**：能少一个按钮就少一个。
- 新增依赖前先在 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) 确认是否符合架构。
- 涉及《东方 Project》形象的资源需为**原创/可商用**，不得直接使用官方插画。

---

## 📝 许可证

本项目使用 [Apache License 2.0](LICENSE)。提交 PR 即表示你同意你的贡献按此许可证分发。
