# Codex Harness Mobile 0.5.4

Android 壳负责 Termux/Debian 启动、权限和文件选择器；Codex 用成熟的开源 cdesktop 工作台，Harness 用官方 `dsh web` 界面。

- Codex：直接执行预置的 cdesktop ARM64 二进制，固定本机 `127.0.0.1:3200`，不经过 `npx`。
- Harness：官方 `dsh web`，固定本机 `127.0.0.1:3080`；已有会话、配置和文件保存在 Termux/Debian，不随 APK 覆盖安装删除。
- Android WebView：启用 JavaScript、持久化存储、滚动和文件选择器，适配手机触摸和附件上传。

> 收工状态、待验证项和已知坑，见 [NEXT-SESSION.md](NEXT-SESSION.md)。

## DSH 0.1.6+ 需要 --expose-internals

`@deepseek-ai/dsh` 从 `0.1.6-alpha.2` 起会加载一个顶替 `node-addon-require-builtin`
的 shim，而那个原生插件**没有 Android 构建**，直接启动会失败：

```
Error: dsh: host preparation failed: requireBuiltin: Node internals are not exposed.
```

注意 `NODE_OPTIONS` **不接受** `--expose-internals`，必须直接传给 node：

```bash
node --expose-internals /usr/bin/dsh web --no-open --port 3080
```

## 为什么预置 cdesktop 二进制

`cdesktop` 的 npm 包只有 53 KB，是个 wrapper；真正的 Rust 二进制（约 49 MB）在首次运行时
从 `binaries.cdesktop.ai` 下载。在这台手机的网络环境下该下载会长时间停滞（实测卡在 2%）。

wrapper 的 `ensureBinary()` 逻辑是：目标 zip 已存在就直接返回，不联网也不取 manifest。
因此把校验过的归档放到 `~/.cdesktop/bin/<tag>/linux-arm64/` 即可让首次启动完全离线。

暂存的三个组件（AArch64，静态链接，不受 Debian glibc 版本影响）：

| 组件 | 压缩包 | 解压后 |
|---|---|---|
| `cdesktop` | 48.85 MiB | 133.9 MB |
| `cdesktop-mcp` | 8.47 MB | 18.97 MB |
| `cdesktop-review` | 3.49 MB | 7.65 MB |

参考项目：[cdesktop](https://github.com/cdesktop-ai/cdesktop)（Apache-2.0，`BloopAI/vibe-kanban` 衍生）、[codex-harness](https://github.com/Luoyehe/codex-harness)、[codex-remote-console](https://github.com/bluebolt43/codex-remote-console)。

Android 原生 GUI，用于同时使用手机内的 Codex CLI 与 DeepSeek Harness。

当前版本已接入真实本地运行时：

- Codex 开源工作台：优先打开 cdesktop，提供多会话、模型/思考强度、文件、终端、Git、审批和附件等 GUI 能力；App 内仍保留原生 app-server 客户端作为协议回退。
- 原生 Codex 回退客户端：连接 `ws://127.0.0.1:4500`，支持初始化、创建线程、发送轮次、流式消息、历史和命令审批。
- DeepSeek Harness：一键在 Termux/Debian 内启动，通过一次性安全 URL 回到 App，并在 WebView 中显示完整官方界面。
- Harness 移动适配：修复 Android WebView 的零高度视口问题，并把设置页重排为适合手机的顶部标签布局。
- Harness 触摸适配：启用 Android 手势滚动、页面触摸传递、内部滚动容器和缩放控制。
- Harness 附件上传：接入 Android 系统文件选择器，支持网页声明的单选/多选附件、MIME 类型和取消回传。
- 真实健康检查：检测 Termux 权限、Codex `/readyz` 与 Harness HTTP 服务。
- 打开 App 自动补启未运行的后端，也可一键启动、停止和刷新；所有服务只监听手机回环地址。
- 自适应图标、Material 3 深色界面、页面和状态过渡动画。

## 构建

需要 Android SDK 36、JDK 17 和 Gradle 8.10+：

```powershell
.\gradle-dist\gradle-8.10.2\bin\gradle.bat assembleDebug lintDebug
```

APK 输出在 `app/build/outputs/apk/debug/app-debug.apk`。项目不保存 Codex 登录令牌或 DeepSeek API Key；它们由手机内的 Codex/Harness 各自管理。

## 手机端前置条件

1. Termux 中设置 `allow-external-apps = true` 并运行 `termux-reload-settings`。
2. 在 Android 的 App 权限中允许“在 Termux 环境中运行命令”。
3. Debian 中已安装 Node.js、`@openai/codex`、`@deepseek-ai/dsh` 与 `bubblewrap`。
4. Codex 已通过 `codex login --device-auth` 登录。

## 使用

1. 打开 App 后等待顶部显示“Codex 与 Harness 均已就绪”。
2. 在 Codex 页直接输入任务；涉及命令执行时可在原生确认框中允许或拒绝。
3. 在 DeepSeek Harness 页点击“启动并打开”。首次进入可填写 DeepSeek API Key，也可以“稍后配置”。
4. 之后可从 Harness 左下角“设置”进入“模型”，填写或替换 API Key；密钥由 Harness 保存在手机本地，App 不读取密钥。

建议为 Termux、Debian、Node.js、Codex、Harness 及缓存预留 3 GB 可用空间。全新安装通常需要 25–60 分钟，主要取决于网络和 npm 下载速度；当前调试 APK 约 56 MB。

## 目录约定

```text
app/src/main/java/app/codexharness/mobile/MainActivity.kt             # 中文原生 UI
app/src/main/java/app/codexharness/mobile/runtime/TermuxRuntimeManager.kt
app/src/main/java/app/codexharness/mobile/runtime/CodexWebSocketClient.kt
```
