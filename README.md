# Codex Harness Mobile

一个把 Android 手机、Termux/Debian、Codex CLI、cdesktop 和 DeepSeek Harness 连接起来的移动工作台。

这是一个独立的 Android 项目，和用户已有的 DSH 项目分开维护。它不是 @deepseek-ai/dsh 的源码，也不把 Codex 或 DeepSeek 的密钥、会话数据和服务器代码打包进来；它负责在手机上启动本地运行时、把本地网页嵌入 WebView，并提供适合触摸屏的文件上传、诊断、恢复和导航体验。

公开仓库地址：https://github.com/yusheng266186-beep/codex-harness-mobile

## 项目状态

| 项目 | 当前值 |
|---|---|
| Android applicationId | app.codexharness.mobile |
| 当前版本 | 0.5.65 / versionCode 82 |
| 最低 Android 版本 | API 29（Android 10） |
| 编译 SDK | Android API 36 |
| UI 技术 | Kotlin + Jetpack Compose + Material 3 |
| 本地运行时 | Termux + proot-distro Debian |
| Codex 服务 | Codex app-server 127.0.0.1:4500 |
| Codex 工作台 | cdesktop 127.0.0.1:3200 |
| DeepSeek Harness | DSH Web 127.0.0.1:3080 |
| 开源许可 | MIT；第三方组件仍以各自许可证为准 |

当前源码已经在真实 Android ARM64 设备上验证过以下主链路：Termux 命令桥接、Debian 内服务启动、Codex 工作台打开、DeepSeek Harness 打开、DSH 文件选择回传、DSH 下拉菜单、DSH 提问选项、Termux 重开后的服务恢复和可移动浮动工具栏。不同厂商系统的 Termux 后台限制、文件选择器行为和语音识别服务可能不同，首次部署仍应按本文的验收清单逐项验证。

## 目录

- [它解决什么问题](#它解决什么问题)
- [整体架构](#整体架构)
- [端口和数据流](#端口和数据流)
- [源码导览](#源码导览)
- [功能清单](#功能清单)
- [构建 Android APK](#构建-android-apk)
- [首次配置手机运行时](#首次配置手机运行时)
- [部署 cdesktop ARM64 组件](#部署-cdesktop-arm64-组件)
- [安装和启动 App](#安装和启动-app)
- [关键实现说明](#关键实现说明)
- [文件上传为什么可靠](#文件上传为什么可靠)
- [DSH 移动端兼容层](#dsh-移动端兼容层)
- [Termux 重启与进程恢复](#termux-重启与进程恢复)
- [安全、隐私和边界](#安全隐私和边界)
- [调试与验收](#调试与验收)
- [常见问题](#常见问题)
- [发布新版本](#发布新版本)
- [贡献代码](#贡献代码)
- [许可证和第三方组件](#许可证和第三方组件)

## 它解决什么问题

在 Android 手机上直接运行 Codex CLI 和 DeepSeek Harness，会同时遇到几类问题：

1. Android App 不能直接当作普通 Linux shell 使用，需要通过 Termux 的 RUN_COMMAND 服务进入 Debian/proot 环境。
2. DSH 版本较新时需要 Node.js 的 --expose-internals，而 NODE_OPTIONS 不接受这个参数，启动命令必须直接调用 node --expose-internals /usr/bin/dsh。
3. cdesktop 的 npm wrapper 首次可能下载大型 ARM64 二进制；手机网络或代理环境下下载会长时间卡住，因此项目支持把经过 SHA-256 校验的组件预置到 cdesktop cache。
4. Android WebView 和桌面浏览器对 vh、触摸事件、文件 URI、键盘和渲染进程的处理不同。网页能显示，不代表菜单、提问卡、文件上传和滚动真的可用。
5. Termux 被关闭后，旧的 proot/Node 进程可能残留，或者新进程没有正确拿到旧端口和一次性访问 URL，导致 App 看起来像“窗口打不开”。

这个 App 的目标不是重新实现 Codex 或 DSH，而是把这些运行时问题集中在一个可诊断、可恢复、对手机友好的壳里。

## 整体架构

~~~text
┌────────────────────────────────────────────────────────────┐
│ Android App: MainActivity + Jetpack Compose                │
│                                                            │
│  工作台导航 / 诊断 / 浮动工具栏 / 原生 Codex 回退客户端     │
│       │                         │                         │
│       ├── CodexDesktopWebScreen ─┐                         │
│       └── HarnessWebScreen       │ Android WebView          │
│                                  │                         │
│  TermuxRuntimeManager ─ RuntimeBridge ─ WebView 回调/状态   │
│       │                                                     │
│       │ Intent: com.termux.RUN_COMMAND                      │
└───────┼────────────────────────────────────────────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────┐
│ Termux                                                      │
│  bash / proot-distro / sshd / 回环网络                        │
└────────────────────────────────────────────────────────────┘
        │
        ▼
┌────────────────────────────────────────────────────────────┐
│ Debian                                                       │
│  codex app-server :4500                                     │
│  cdesktop          :3200                                    │
│  dsh web           :3080                                    │
└────────────────────────────────────────────────────────────┘
~~~

### 一次“启动并打开”的完整流程

以 DeepSeek Harness 为例：

1. Compose 页面调用 TermuxRuntimeManager.startHarness。
2. 运行时管理器检查 Termux 是否安装，以及 App 是否有 RUN_COMMAND 权限。
3. App 通过 com.termux.RUN_COMMAND 发送一条单行 bash 命令，命令在 Termux HOME 中执行。
4. Termux 进入 Debian，使用 nohup setsid 启动 node --expose-internals /usr/bin/dsh web --no-open --port 3080。
5. 输出写入 ~/.codex-harness-mobile/harness.log，进程号写入 harness.pid。
6. DSH 打印带一次性 token 的本地 URL 后，通过 TermuxResultReceiver 回传给 App。
7. Receiver 只接受 127.0.0.1:3080 或 127.0.0.1:3200 的 URL，脱敏日志后写入桥接状态。
8. App 在 WebView 中打开真实 URL；如果用户随后从网页进入设置、附件或提问流程，WebView 继续保留网页会话。

Codex 工作台的流程相同，只是目标端口为 3200。原生 Codex 回退模式则直接由 CodexWebSocketClient 连接 4500，不依赖 cdesktop 页面。

## 端口和数据流

所有服务都应该只监听手机本机回环地址，不向局域网暴露：

| 地址 | 用途 | 健康检查 | 备注 |
|---|---|---|---|
| 127.0.0.1:4500 | Codex app-server | /readyz | 原生回退对话和流式消息 |
| 127.0.0.1:3200 | cdesktop | / | 多会话 Codex 工作台 |
| 127.0.0.1:3080 | DeepSeek Harness | / | 没有 token 的直接访问可能返回 401，这是鉴权正常行为 |
| https://www.gstatic.com/generate_204 | 公网网络诊断 | HTTP 响应 | 用于区分本地服务正常和外网不可用 |

运行时会同时做两种检查：

- 快速健康检查：只判断端口/HTTP 是否可达，用于顶部状态和前台轮询。
- 详细诊断：记录网络类型、NET_CAPABILITY_VALIDATED、Termux 权限、响应码、延迟、最近一次命令及三个本地日志的尾部。

本地 Harness 返回 401–403 时，诊断将其显示为“服务可达，需要访问令牌”，而不是误报为服务停止。这样可以把“Node/HTTP 服务没启动”和“网页没有携带一次性 token”区分开。

## 源码导览

~~~text
.
├── app/
│   ├── build.gradle.kts                    # Android 模块、版本和依赖
│   └── src/main/
│       ├── AndroidManifest.xml             # 权限、Termux receiver、FileProvider、分享入口
│       ├── java/app/codexharness/mobile/
│       │   ├── MainActivity.kt              # Compose UI、WebView、文件选择、相机、分享、语音
│       │   └── runtime/
│       │       ├── RuntimeBridge.kt         # UI 与运行时之间的抽象模型
│       │       ├── TermuxRuntimeManager.kt  # 命令分发、服务启动/停止、健康检查和诊断
│       │       ├── TermuxResultReceiver.kt  # 接收 Termux 回调并过滤本地 URL
│       │       ├── CodexWebSocketClient.kt  # 原生 Codex app-server WebSocket 客户端
│       │       ├── CodexAppServerClient.kt  # 协议边界接口
│       │       └── DeepSeekHarnessClient.kt # DSH Web 集成边界接口
│       └── res/
│           ├── xml/file_paths.xml           # FileProvider 只开放附件暂存目录
│           ├── xml/shortcuts.xml            # Codex/Harness/诊断桌面快捷入口
│           └── values/                      # 主题和快捷入口文本
├── .tools/
│   ├── phone.ps1                            # Windows ADB/SSH/截图/远程脚本工具
│   ├── deploy-cdesktop.ps1                  # 校验并推送 cdesktop ARM64 归档
│   ├── stage-cdesktop.sh                    # 在 Debian 内写入 cdesktop cache
│   ├── startdsh-termux.sh                  # Termux 侧 detached DSH 启动示例
│   ├── dsh-fix-launch.sh                   # 带 --expose-internals 的启动示例
│   ├── verify.sh                            # 端口、HTTP、进程和日志验证
│   └── oneoff-file-recovery/               # 历史文件修复工具，正常运行不需要
├── bootstrap-termux.sh                      # Termux 包和 Debian 容器初始化
├── bootstrap-debian.sh                      # Debian、Node、Codex、DSH 初始化
├── enable-termux-bridge.sh                  # 开启 allow-external-apps
├── NEXT-SESSION.md                          # 真机验证记录和历史排障结论
└── README.md                                # 本文档
~~~

MainActivity.kt 目前集中承载移动端交互和两个 WebView 的包装逻辑。这样便于在同一处处理 Android 生命周期、键盘、文件选择器和 WebView；运行时命令、网络探测和进程清理由 TermuxRuntimeManager 独立负责。后续如果功能继续扩展，可以把两个 WebView 和诊断弹窗拆成独立 Kotlin 文件，但不要把 Termux 命令字符串复制到多个 UI 分支中。

## 功能清单

### 运行时和服务生命周期

- 一键启动 Codex app-server、cdesktop 和 DeepSeek Harness。
- 以独立 PID 文件和日志文件记录本地进程。
- 启动前检查 Termux 安装状态和 RUN_COMMAND 权限。
- Termux 关闭后重新打开 App 时重新探测端口，并清理残留 DSH/cdesktop 进程再恢复服务。
- 启动、停止、恢复和当前工作区重启共用操作锁，连续点击不会重复启动多个进程。
- Codex、cdesktop 和 Harness 可单独重启，不必让另一个工作区一起中断。
- App 退到后台时降低健康轮询频率，回到前台立即刷新。
- 检测 Termux 是否已忽略电池优化，并提供系统设置入口。
- 顶部“停止”需要二次确认，避免误触清掉正在运行的任务。

### Codex

- 优先使用 cdesktop Web 工作台，支持多会话、模型、思考强度、文件、终端、审批和 Git 工作流。
- 保留原生 Codex WebSocket 客户端作为轻量回退入口。
- 原生模式支持初始化、创建线程、流式 assistant 消息、命令审批、历史读取和线程恢复。
- 保存当前线程 ID，重连后尝试 thread/read 恢复对话。
- 输入草稿本地保存；系统回收 App 后可以恢复未发送文字。
- 原生对话支持消息复制、单条分享、整段分享、当前对话搜索、历史搜索和回到底部。
- 支持 Android 系统分享/处理文本，把外部文字预填到 Codex 草稿，不自动发送。
- 支持系统语音识别，把识别结果追加到当前 Codex 草稿；不要求 App 自己保存录音。
- 可选开启后台完成通知；默认关闭，只有用户主动开启时才申请通知权限。

### DeepSeek Harness 和 WebView

- 在 Android WebView 内打开官方 DSH Web，不复制或重实现 DSH 前端。
- WebView 开启 JavaScript、DOM storage、数据库、缩放、滚动和文件选择回调。
- 页面加载进度、HTTP 错误、网络错误和渲染进程崩溃都有可见恢复入口。
- 网络错误以及 408/429/5xx 最多自动重试两次；401–403 不自动重试。
- Codex 与 Harness WebView 切换时保持各自实例、滚动位置、表单和网页会话。
- 支持网页后退、前进、刷新和网页内查找。
- 下载交给 Android DownloadManager，带上当前 WebView Cookie 和 User-Agent。
- capture 图片输入可直接打开系统相机，图片暂存后再回传给网页。

### 手机交互

- 专注模式隐藏 App 原生顶栏和底栏，把屏幕留给网页。
- 浮动工具栏可以按住拖动按钮在整个安全区域移动，不固定在某个网页控件上方。
- Codex 和 Harness 分别保存工具栏位置、网页文字缩放比例和工作区状态。
- 工具栏跟随软件键盘调整边界，不会被 IME 遮住。
- 可选沉浸模式隐藏 Android 系统栏；离开专注模式或 App 进入后台时自动恢复。
- 横屏、小高度和窄屏自动压缩顶栏、底栏、工具栏和弹窗内容区。
- 系统返回键按键盘、网页历史、工作区和退出确认的优先级处理，避免误退出。
- App 图标长按可直接打开 Codex、Harness 或连接诊断。

### 诊断和排障

- 显示网络类型、网络是否可用、是否通过系统验证、Termux 权限和本地端口。
- 可读取 Harness、cdesktop、Codex 三份日志最近 80 行。
- 诊断摘要和日志支持复制、系统分享，并对一次性 token 做脱敏。
- 显示附件暂存文件数量、空间占用和超过 24 小时的缓存。
- 可清理旧附件缓存，不会删除 Codex/Harness 的会话和配置。

## 构建 Android APK

### 构建机要求

- JDK 17。
- Android SDK Platform 36 和对应 Build Tools。
- Android SDK Platform Tools（adb）。
- Git。
- Gradle Wrapper 会自动下载 Gradle 8.10.2；首次构建需要网络。

项目使用：

- Android Gradle Plugin 8.7.3。
- Kotlin 2.0.21。
- Jetpack Compose BOM 2024.12.01。
- androidx.activity、lifecycle、Material 3、Navigation Compose。
- OkHttp 4.12.0 和 Kotlin Coroutines 1.9.0。

### Windows PowerShell

~~~powershell
git clone https://github.com/yusheng266186-beep/codex-harness-mobile.git
Set-Location codex-harness-mobile

$env:JAVA_HOME = "C:\Path\To\jdk-17"
$env:ANDROID_HOME = "C:\Path\To\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME

.\gradlew.bat --no-daemon :app:assembleDebug :app:lintDebug --console=plain
~~~

### Linux/macOS

~~~bash
git clone https://github.com/yusheng266186-beep/codex-harness-mobile.git
cd codex-harness-mobile
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/Android/Sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
chmod +x gradlew
./gradlew --no-daemon :app:assembleDebug :app:lintDebug --console=plain
~~~

输出文件：

~~~text
app/build/outputs/apk/debug/app-debug.apk
~~~

调试构建不包含 cdesktop 二进制、Termux rootfs、Codex 登录信息或 DeepSeek API Key。gitignore 会排除 Gradle 缓存、SDK/JDK 压缩包、APK、临时阶段目录、根目录调试截图和本地日志。

## 首次配置手机运行时

下面的步骤只需要在新手机或新 Termux 环境执行一次。脚本是辅助流程，执行前应阅读并根据自己的 Termux 发行版调整。

### 1. 安装 Termux

从可信的 Termux 发布渠道安装主程序。不要把 Play Store 版本和 F-Droid/GitHub 版本的插件混装。首次打开 Termux 后，建议先执行：

~~~bash
termux-setup-storage
~~~

然后在 Android 系统设置中允许 Termux：

- 后台运行或忽略电池优化（不同厂商名称不同）。
- 存储访问（如果要使用 /sdcard/Download 里的脚本或 cdesktop 归档）。
- 本项目需要的外部 App 命令权限。App 首页也会提示当前权限状态。

### 2. 开启 Termux 外部命令桥

enable-termux-bridge.sh 会写入：

~~~text
~/.termux/termux.properties
allow-external-apps = true
~~~

可以把脚本复制到手机后执行：

~~~bash
cp ~/storage/downloads/enable-termux-bridge.sh ~/enable-termux-bridge.sh
chmod +x ~/enable-termux-bridge.sh
bash ~/enable-termux-bridge.sh
~~~

执行后重启 Termux，确保新配置生效。

### 3. 安装 Termux 依赖和 Debian

把 bootstrap-termux.sh 放到 Termux HOME 后执行：

~~~bash
chmod +x ~/bootstrap-termux.sh
bash ~/bootstrap-termux.sh
~~~

它会安装 git、curl、wget、proot-distro、openssh 和 termux-tools，再安装 Debian rootfs。脚本最后出现 TERMUX_BOOTSTRAP_COMPLETE 才表示这一步完成。

### 4. 初始化 Debian、Node.js、Codex 和 DSH

进入 Debian：

~~~bash
proot-distro login debian
~~~

把 bootstrap-debian.sh 复制到 Debian 内执行：

~~~bash
chmod +x /path/to/bootstrap-debian.sh
bash /path/to/bootstrap-debian.sh
~~~

它会安装 Node.js、构建工具和 Python 基础包，并安装：

~~~bash
npm install -g --include=optional @openai/codex
npm install -g @deepseek-ai/dsh
~~~

然后完成 Codex 登录。登录动作在手机上执行，App 不保存令牌：

~~~bash
codex login --device-auth
codex login status
~~~

验证 DSH 版本：

~~~bash
dsh --version
node --version
~~~

### 5. DSH 的 --expose-internals

从 DSH 0.1.6-alpha.2 一类版本开始，启动时可能加载 node-addon-require-builtin shim。Android/ARM64 没有对应原生构建，因此 Node 必须暴露 internals：

~~~bash
node --expose-internals /usr/bin/dsh web --no-open --port 3080
~~~

以下写法通常不可靠：

~~~bash
NODE_OPTIONS=--expose-internals dsh web
~~~

原因是 NODE_OPTIONS 会拒绝这个参数。App 内部的启动命令已经把参数放在 node 后面，并会把进程以 detached 方式运行。

## 部署 cdesktop ARM64 组件

cdesktop npm 包本身只是 wrapper，首次运行会尝试取得较大的 Rust ARM64 组件。如果手机网络环境会把下载卡在很低的进度，可以使用项目提供的离线阶段流程。

### 组件位置

wrapper 查找的目标目录是：

~~~text
~/.cdesktop/bin/v0.2.3-20260519022845/linux-arm64/
~~~

需要的归档名称：

~~~text
cdesktop.zip
cdesktop-mcp.zip
cdesktop-review.zip
~~~

这些归档不提交到 GitHub。请从 cdesktop 官方发布物取得与脚本中 tag 对应的 ARM64 归档，并先核对 .tools/deploy-cdesktop.ps1 中的 SHA-256。不要把来源不明的可执行文件推送到手机或仓库。

### Windows 推送和校验

.tools/deploy-cdesktop.ps1 会自动从 ANDROID_ADB、ANDROID_HOME、ANDROID_SDK_ROOT 或 PATH 找 adb，不会依赖某台电脑的绝对路径。

~~~powershell
$env:ANDROID_HOME = "C:\Path\To\Android\Sdk"
$env:ANDROID_ADB = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"

New-Item -ItemType Directory -Force "$env:TEMP\cdesktop-probe" | Out-Null

.\.tools\deploy-cdesktop.ps1
~~~

脚本会：

1. 检查 ADB 设备。
2. 检查三个 zip 是否存在。
3. 对每个 zip 计算 SHA-256。
4. 推送到手机的 /sdcard/Download/cdesktop/。
5. 推送 .tools/stage-cdesktop.sh。
6. 可选安装当前调试 APK。

在 Termux 中执行阶段脚本：

~~~bash
bash /sdcard/Download/cdesktop/stage-cdesktop.sh
~~~

它会把归档绑定到 Debian，写入 ~/.cdesktop/bin/<tag>/linux-arm64/，解压并设置执行权限，再安装 cdesktop@0.2.3 wrapper。这样 App 启动 cdesktop 时优先使用本地静态 ARM64 二进制，避免把首次运行依赖在一次不稳定的网络下载上。

## 安装和启动 App

手机打开 USB 调试并接受电脑授权后：

~~~powershell
adb devices -l
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n app.codexharness.mobile/.MainActivity
~~~

如果使用无线 ADB，先确认 adb devices 显示的是 device，不是 unauthorized 或 offline。App 覆盖安装不会删除它自己的 SharedPreferences、WebView 数据、Termux 数据或 Debian 数据。

首次启动建议按这个顺序：

1. 在 App 首页确认 Termux 已安装、命令权限已授予、网络状态正常。
2. 点击 Codex 工作台“启动并打开”，第一次 cdesktop 启动可能需要等待 Rust 后端初始化。
3. 点击 DeepSeek Harness“启动并打开”，等待 3080 和一次性 URL 回调。
4. 如果 Harness 页面没有打开，先进入“连接诊断”，读取 harness.log，不要直接重复点击几十次启动按钮。
5. 在 DSH 内先测试模型下拉菜单和提问卡，再测试附件选择；这样能区分 WebView 布局问题和 API 鉴权问题。

## 关键实现说明

### TermuxRuntimeManager

这是 Android 和手机 Linux 环境之间的核心边界，主要职责包括：

- 使用 com.termux.app.RunCommandService 发送 /data/data/com.termux/files/usr/bin/bash -lc <script>。
- 通过 PendingIntent 接收需要 URL/日志结果的命令回调。
- 维护 Codex、cdesktop、Harness 的启动、重启、停止和 PID 清理脚本。
- 用本机 HTTP 探测检查 4500、3200、3080。
- 将 401–403 的本地响应标记为“服务可达但需要 token”。
- 读取网络传输类型和系统网络验证状态。
- 读取 Termux 电池优化豁免状态。
- 返回有限长度的日志尾部，避免大日志阻塞 Intent 和 UI。

一个重要约束是：通过 Android Intent extra 传给 Termux 的命令必须保持在单行。脚本中的多行 shell 片段如果使用反斜杠续行，经过 Intent/Termux 解析后可能变成破损的 bash 语法。需要执行较长逻辑时，应把脚本写入手机文件，再从 Termux 执行；现有工具脚本就是这么做的。

### TermuxResultReceiver

Receiver 必须在 Manifest 中 android:exported="true"，并且不能误加只允许 App 自己 UID 的 android:permission。Termux 作为发送方没有声明 App 自定义权限时，广播会被系统静默丢弃，am broadcast 仍可能显示完成，排查会非常困难。

因此安全边界放在结果内容：

- 只接受 127.0.0.1。
- 只接受 3080 或 3200。
- 只把符合 URL 形状的结果写入 WebView 桥接状态。
- 日志展示前对 token= 等一次性凭证脱敏。

这不是通用广播认证方案；它的使用前提是 PendingIntent 由本 App 创建，并且实际服务只绑定回环地址。

### CodexWebSocketClient

原生回退客户端通过 OkHttp WebSocket 连接 ws://127.0.0.1:4500，对 app-server 消息做轻量解析。它负责：

- 初始化客户端能力。
- 创建线程、发送用户轮次。
- 合并增量 assistant 文本和系统进度。
- 处理命令审批。
- 记录当前 threadId。
- 重连后读取上一个线程。

CodexAppServerClient 是协议接口边界，方便未来把具体 Codex 版本的协议解析进一步拆出去。协议字段应跟随手机里实际安装的 Codex CLI 版本同步，不要只凭旧日志猜字段。

### WebView 生命周期和自愈

Codex 和 Harness WebView 都实现了以下策略：

1. 页面开始加载时启动 15 秒看门狗。
2. 页面成功完成时取消看门狗并同步网页前进/后退状态。
3. 网络错误、408、429、5xx 使用 1 秒和 2 秒的有限重试。
4. 401–403 保留错误卡片和手动处理入口。
5. renderer 被系统回收时最多重建一次，避免无限 reload 循环。
6. 页面错误卡可以重新加载、恢复对应服务或打开连接诊断。

两个网页实例同时保活，但只有当前工作区获得触摸层级和返回键处理权，防止后台 WebView 抢走点击事件。

## 文件上传为什么可靠

### 原始问题

Android 文件选择器可以成功弹出，用户也能看到并选中一个文件，但如果 App 把系统返回的临时 content URI 直接交给 WebView，选择器关闭后 provider 的临时授权可能已经失效。结果就是：

- UI 看起来像“已经选择”；
- JavaScript 的 file input 收到回调；
- DSH 页面实际读取文件时失败，或者附件列表里找不到文件。

“能选中”不等于“网页已经拿到一个可读取的文件”。

### 当前回传链路

~~~text
DSH <input type=file>
        │ WebChromeClient.onShowFileChooser
        ▼
选择合适的 ACTION_GET_CONTENT / ACTION_OPEN_DOCUMENT intent
        │ ActivityResult
        ▼
解析 data 和 ClipData，处理单选、多选、取消
        │
        ▼
读取 displayName / size，检查可用空间
        │
        ▼
复制到 cacheDir/webview-upload/，64 KiB 分块并显示进度
        │
        ├─ 大小已知：复制后校验源大小和目标大小
        ├─ 取消/新请求：取消旧 Job，不再回调旧 WebView
        └─ 失败：删除半成品，不回退到原始失效 URI
        │
        ▼
FileProvider 生成稳定 content URI，并授予 WebView 读取权限
        │
        ▼
ValueCallback<Array<Uri>>.onReceiveValue(stagedUris)
~~~

关键实现位于 MainActivity.kt：

- showFileChooser：解除上一次卡住的 callback，构造可解析的选择器。
- buildFileChooserIntent：兼容不同厂商对 ACTION_GET_CONTENT、ACTION_OPEN_DOCUMENT 和 MIME 类型的实现。
- parseFileChooserResult：统一处理 data 与 ClipData。
- stageUploadUri：在 IO 协程中复制、计算大小、检查空间和校验结果。
- FileProvider：只暴露 cache/webview-upload/，不会开放整个 App 数据目录。
- uploadRequestId：丢弃已经过期的选择器回调和进度更新。
- 24 小时缓存清理：启动时和诊断页都可以清理旧暂存文件。

网页声明 accept="image/*" capture 时会走相机分支：照片先写入 App 私有缓存，再复用同一套暂存、校验和 FileProvider 流程。相机取消、Activity 销毁或上传失败时会删除临时文件。

### 调试上传问题

~~~powershell
adb logcat -s HarnessWebView MainActivity
~~~

应能看到类似以下阶段：

~~~text
file chooser returned 1 readable URI(s): content://...
附件已准备，可以在 Harness 中继续发送
~~~

如果只看到选择器打开而看不到 readable URI(s)，重点检查 ActivityResult 和 provider；如果看到已回传但 DSH 不显示，重点检查网页是否在等待附件上传请求、API 鉴权和网络，而不是重新修改系统选择器。

## DSH 移动端兼容层

MainActivity.kt 中的 HarnessViewportFix 在页面完成后注入一次，并用页面标志避免重复安装。它不修改 DSH 源码，而是针对 Android WebView 的布局行为做最小适配：

### 下拉菜单

DSH 菜单使用 100vh 计算可视高度。部分 Android WebView 场景把该值解析成 0，菜单因此被压成不可见或无法触摸。兼容层使用 visualViewport.height 和实际页面高度重新限制菜单，并为菜单内容开启滚动和触摸层级。

### 提问卡

ask_user_question 卡片的标题可能很长，原始 CSS 的标题会把选项区和提交栏挤到几乎 0 高度；同时 max-height: min(60vh, 520px) 在部分 WebView 上也会变成 0。兼容层只针对 composer 内的提问卡：

- 限制标题块高度。
- 让标题内部滚动并允许长文本换行。
- 用 visual viewport 重新计算卡片最大高度。
- 保留选项区和底部提交区的最小触摸空间。

### 401 鉴权响应

直接访问 http://127.0.0.1:3080/ 而不带 DSH 一次性 token 时，HTTP 401 是预期行为。App 通过 Termux 的命令结果拿到带 token 的 URL 后才加载真实页面。诊断页因此把本机 401–403 标记为可达，不会因为“没有 token 的探测请求被拒绝”而误触发服务重启。

### 触摸和键盘

WebView 启用手势滚动、内部滚动容器和键盘布局；App 的浮动工具栏使用 imePadding 和当前可见窗口边界，避免输入框获得焦点后工具栏落到键盘下面。

## Termux 重启与进程恢复

“关闭 Termux 后重新打开，DSH 窗口打不开”通常不是单一原因，常见组合是：旧的 Node/proot 进程还占用 3080、旧 pid 文件失效、一次性 URL 已过期、Termux 的外部命令权限未重新加载，或者新进程启动命令缺少 --expose-internals。

当前恢复流程：

1. App 回到前台时重新检查 Termux、4500、3200、3080 和网络状态。
2. 如果用户点击 Harness 的“启动并打开”，先读取现有日志中的 URL 和端口状态。
3. 强制重启时递归清理 pid 文件对应的进程树，并扫描明确的 DSH/cdesktop 命令行，避免旧进程占住端口。
4. 重新以 nohup setsid proot-distro login debian 启动，标准输入重定向到 /dev/null，日志写入持久目录。
5. 等待端口和日志中的 URL 真正出现，命令超时才释放 UI 锁。
6. 通过回调把新 URL 返回给 App，WebView 再加载新页面。

日志位置：

~~~text
~/.codex-harness-mobile/codex.log
~/.codex-harness-mobile/cdesktop.log
~/.codex-harness-mobile/harness.log
~/.codex-harness-mobile/*.pid
~~~

如果 Termux 自己被系统杀死，Android 厂商的后台策略仍可能阻止进程长期存活；建议对 Termux 关闭电池优化，并在系统设置中允许后台活动。

## 安全、隐私和边界

- App 不读取、不上传、不提交 Codex 登录令牌或 DeepSeek API Key。Codex/Harness 的凭据由手机里的各自 CLI/Web 应用管理。
- 服务启动脚本明确使用 127.0.0.1，不要把端口改成 0.0.0.0，除非你已经理解局域网暴露风险。
- Manifest 的 usesCleartextTraffic=true 是为了访问手机回环上的 HTTP 服务，不代表应该把服务暴露到公网。
- Termux 回调 receiver 为了兼容 Termux 的 PendingIntent 发送方而 exported，但只接受两个回环端口的 URL；日志和诊断内容会对 token 脱敏。
- FileProvider 只开放 cache/webview-upload/，不开放整个 /data/data/... 目录。
- 语音输入调用 Android 系统 RecognizerIntent，App 不录制或保存音频；设备没有可用识别器时会提示用户。
- 通知权限默认不申请。后台完成提醒需要用户主动打开。
- cdesktop 归档、APK、SDK/JDK、Termux APK、手机截图和运行日志不应该提交到公共仓库。
- .tools 中的 pkill、进程树清理和远程脚本会修改手机运行时；执行前请确认设备和目标环境，不要把它们当成通用服务器部署脚本。

## 调试与验收

### 构建级检查

~~~powershell
.\gradlew.bat --no-daemon :app:assembleDebug :app:lintDebug --console=plain
git diff --check
~~~

Lint 中的 Android API 弃用提示不等于构建失败；真正发布前应确认没有 error、没有 Kotlin 编译错误，并查看输出 APK 的版本号。

### ADB 基础检查

~~~powershell
adb devices -l
adb shell pm path app.codexharness.mobile
adb shell dumpsys package app.codexharness.mobile | Select-String 'versionCode|versionName'
adb logcat -c
adb logcat -s MobileWorkbench TermuxRuntimeManager TermuxResultReceiver HarnessWebView CodexWebView
~~~

### Termux 端口检查

在 Termux 中：

~~~bash
for p in 3080 3200 4500; do
  if (exec 3<>/dev/tcp/127.0.0.1/$p) 2>/dev/null; then
    echo "$p OPEN"
  else
    echo "$p CLOSED"
  fi
done
curl -i --max-time 5 http://127.0.0.1:3080/
~~~

无 token 时 Harness 返回 401 并不自动说明启动失败；应结合 harness.log 和 App 诊断判断。

### 真机手动验收清单

| 场景 | 预期 |
|---|---|
| 冷启动 App | 状态页能区分 Termux、Codex、cdesktop、Harness 是否在线 |
| 启动 Harness | 3080 就绪后收到带 token 的本地 URL 并打开网页 |
| 关闭再打开 Termux | 点击恢复后不会因旧 pid 或 EADDRINUSE 永久打不开 |
| DSH 模型/权限/更多菜单 | 下拉层可见、可滚动、可触摸 |
| DSH 提问卡 | 长问题标题不会挤掉选项和提交按钮，选项可点 |
| DSH 文件附件 | 文件选择器关闭后，页面仍能读取附件并显示文件名/大小 |
| 多选附件 | 每个文件依次显示暂存进度，旧请求不会覆盖新请求 |
| 拍照附件 | 系统相机取消和成功回传都不会留下错误状态 |
| 401 探测 | 诊断显示“服务可达，需要访问令牌”，不反复重启服务 |
| 浮动工具栏 | 收起或展开状态均可拖到不同位置，且不遮挡固定网页控件 |
| 键盘 | 键盘打开时工具栏避让，返回键先收起键盘 |
| 横竖屏 | WebView 会话、滚动、输入和工具栏状态保留 |
| Termux 后台 | 回到 App 后健康状态立即刷新，后台轮询不会高频唤醒 |
| Codex 语音输入 | 识别文字进入草稿，不会绕过用户确认自动发送 |
| 系统分享文字 | 分享内容进入 Codex 草稿，长度超过限制时安全截断 |

### 常用工具脚本

先在 PowerShell 中加载：

~~~powershell
. .\.tools\phone.ps1
~~~

可用函数：

| 函数 | 作用 |
|---|---|
| Adb | 调用当前自动探测到的 adb |
| Shot | 从设备拉取二进制安全截图 |
| Tap / Swipe | 发送触摸操作 |
| TypeText / Enter | 输入和回车 |
| Focus | 查看当前 Android 窗口 |
| LoopbackPorts | 解析设备 /proc/net/tcp 的监听端口 |
| EnsureShell | 通过 sshd/ADB forward 恢复 Termux 远程 shell |
| RunScript | 推送脚本并在 Termux 或 Debian 执行 |

脚本使用 $PSScriptRoot、ANDROID_HOME、ANDROID_SDK_ROOT 或 PATH 自动定位 adb，不依赖某个开发者的本机绝对路径。

## 常见问题

### App 能打开，但 Harness 页面打不开

按顺序检查：

1. App 诊断页中的 Termux 命令权限是否为已授予。
2. harness.log 是否出现 dsh web 的启动信息。
3. 3080 是否被旧 Node 进程占用。
4. DSH 版本是否需要 node --expose-internals。
5. App 是否收到了新的回调 URL；旧页面中的 token 可能已经失效。

不要只看 App 首页的“服务在线”文字，优先复制诊断摘要和最近日志。

### 日志显示 HTTP 401

如果请求是 http://127.0.0.1:3080/ 且没有 token，401 是预期鉴权响应。正确处理是让 App 通过 Termux 回调拿到 DSH 打印的带 token URL。只有在带 token 的 URL 也持续 401 时，才进一步检查 DSH 会话、URL 是否过期或 WebView Cookie。

### 文件选中了，但 DSH 里没有文件

确认日志中出现 file chooser returned ... readable URI(s) 和“附件已准备”。如果没有，检查 Android 文件选择器、存储权限和 provider；如果有但网页没有显示，检查 DSH 页面是否仍在等待上传、网络/API 鉴权是否正常。当前实现不会把未经校验的原始 URI 直接交给 WebView，宁可显示失败，也不会制造“选择成功但页面读不到”的假状态。

### 下拉菜单或提问选项不显示

这通常是旧页面仍在 WebView 中，或者移动兼容 CSS 尚未重新注入。先点当前工作区刷新；若仍不行，关闭当前网页后重新打开 Harness。确认是最新 APK，并查看 HarnessWebView 日志中 onPageFinished 和 viewport 修复是否执行。提问卡重点检查标题是否过长、visual viewport 是否为 0。

### cdesktop 首次启动卡在下载进度

用 .tools/deploy-cdesktop.ps1 校验并推送三个 ARM64 zip，再执行 stage-cdesktop.sh。确认目标目录中同时存在 zip 和解压后的可执行文件。不要把 .stage/ 或归档提交到 GitHub；它们体积大且与设备架构绑定。

### 出现 EADDRINUSE

先在 App 里执行当前工作区“重启”，再查看对应 pid/log。必要时在 Termux 中确认：

~~~bash
ps -A -o PID,PPID,ARGS | grep -Ei 'dsh|cdesktop|codex|proot' | grep -v grep
~~~

不要随便杀掉整个 Termux；使用项目的进程树清理逻辑可以减少误伤其他 Termux 任务。

### WebView 页面空白或渲染进程崩溃

检查 Android System WebView/Chrome 是否更新，确认手机内存和电池策略；然后使用页面错误卡片的“重新加载”或“恢复服务”。实现只会自动重建一次 renderer，连续崩溃会留下诊断入口，避免无限重载。

### 语音按钮没有识别器

Android 设备需要安装并启用系统语音识别服务。App 没有录音权限或自建语音服务；无法解析 RecognizerIntent 时会显示明确提示，文字输入仍可正常使用。

## 发布新版本

建议每次发布按以下顺序操作：

1. 在 app/build.gradle.kts 同时递增 versionCode 和 versionName。
2. 更新 README 的版本表和 NEXT-SESSION.md 的验证记录。
3. 执行 assembleDebug 和 lintDebug。
4. 在至少一台真实 ARM64 手机完成冷启动、Termux 重启、DSH 菜单、提问卡和文件上传验收。
5. 检查 git diff --check。
6. 检查待提交文件，确保没有 APK、token、API Key、截图、JDK、Gradle 分发包和 cdesktop zip。
7. 创建提交和 tag，再推送到独立的 GitHub 仓库。

示例：

~~~powershell
git status --short --untracked-files=all
git diff --check
git add README.md NEXT-SESSION.md app .tools bootstrap-*.sh enable-termux-bridge.sh
git diff --cached --stat
git commit -m "release: Codex Harness Mobile 0.5.65"
git tag v0.5.65
git push origin main --follow-tags
~~~

公共仓库只发布 Android 壳和可复现的脚本/文档，不发布用户运行时数据。若要生成可供他人下载的 APK，建议另建 GitHub Actions release 流程，并在 release 说明中明确它不包含 Termux、Debian、Codex 登录或 DeepSeek Key。

## 贡献代码

提交改动时请尽量按边界修改：

- Android UI 和 WebView 行为放在 MainActivity.kt 或拆出的 UI 文件。
- Termux 命令、端口等待、日志和进程清理由 TermuxRuntimeManager 负责。
- 新增 WebView 文件能力时必须同时考虑取消、Activity 销毁、URI 权限和临时文件清理。
- 新增诊断输出时要脱敏 URL token、凭据和用户内容。
- 修改 DSH CSS 兼容层时只增加针对 Android WebView 的选择器，不覆盖桌面浏览器的正常布局。
- 启动命令要验证真实端口/回调后才能把操作标记为成功。
- 新增功能至少补充 README 的架构、配置、测试和排障说明。

提交前运行：

~~~bash
./gradlew :app:assembleDebug :app:lintDebug
git diff --check
~~~

## 许可证和第三方组件

本仓库中原创 Android 壳、运行时桥接和文档按根目录 LICENSE 的 MIT License 发布。依赖库和外部运行时不因此改变许可证：

- AndroidX、Jetpack Compose、Kotlin、OkHttp、Kotlin Coroutines：遵循各自项目许可证。
- cdesktop：使用其官方发布的组件和许可证；本仓库不重新分发其大型 ARM64 二进制。
- Codex CLI 和 DeepSeek Harness：由用户在自己的 Debian/Termux 环境安装，许可证、服务条款和账号凭据由各自项目/服务负责。

如果把本项目与第三方二进制或内部部署脚本一起分发，请额外保留第三方 NOTICE、版权和使用条款。
