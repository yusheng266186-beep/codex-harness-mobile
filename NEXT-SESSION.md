# 交接文档（2026-09-22 11:30）

App 版本 **0.5.9**（versionCode 26），已装在手机上。源码全部干净（无编码损坏、无裸 CR）。

---

## 一、已验证可用

| 功能 | 状态 | 证据 |
|---|---|---|
| **DSH 启动** | ✅ | `node --expose-internals /usr/bin/dsh web --no-open --port 3080`，App 自动启动后 3080 在 5–6 秒内就绪 |
| **DSH 界面** | ✅ | 完整加载：侧栏、对话/轨迹标签、输入框、状态栏、历史数据完好 |
| **Codex（cdesktop）** | ✅ | `Main server on :3200`，界面显示「欢迎回来」、文件夹选择、模型开关 `GPT-5.5 · 极高` |
| **cdesktop 二进制** | ✅ | 静态 AArch64（无 glibc 依赖）在 proot 里正常运行，启动约 72 秒 |
| **Termux 回调链路** | ✅ | `I TermuxResultReceiver: result received: url=http://127.0.0.1:3080/?token=...` |

---

## 二、未解决的问题（按优先级）

### P0 — 文件上传（本轮改动，**尚未实测通过**）

App 已经装了修复版（0.5.9），但**我没有成功验证**。目前只确认：
- DSH 输入区的附件按钮存在（`+` 图标，界面底部左侧）
- 我点击时点偏了，界面切换到了 DSH 主页，没看到选择器

**修复内容**（`MainActivity.kt` 的 `showFileChooser` / `buildFileChooserIntent`）：
- 原实现只用 `params.createIntent()`，无备选。在 MIUI / Android 11+ 上该 intent 常解析不到处理器，代码返回 `false`，**WebView 会静默吞掉点击**
- 新实现依次尝试 5 个候选 intent，取第一个能 `resolveActivity` 的
- 按 `acceptTypes` 设置 MIME，多类型用 `EXTRA_MIME_TYPES`，支持多选
- 取消选择也回调 `null`（否则页面 file input 永久禁用）
- 新增：上一次未返回的选择器会被主动解除阻塞

**下一步要做**：打开 Harness → 点输入框的 `+` → 观察是否弹出系统选择器。三种失败形态原因不同：
1. **选择器不弹** → `resolveActivity` 全失败，或 `onShowFileChooser` 没被调用
2. **弹了但列表空** → MIME 过滤过窄，或 MIUI 的内容提供者问题
3. **选了没上传** → WebView 侧或 DSH 的 `/api/upload` 问题

调试手段：`WebView.setWebContentsDebuggingEnabled(true)` 已启用，配合 `MainActivity` 里新增的 `onConsoleMessage` 日志（tag `HarnessWebView`）可以看到页面侧报错。

### P1 — DSH 设置 → 模型 面板内容空白

设置弹窗能打开（标签：通用设置 / 模型 / 内置工具 / Agent 预设 / 已归档对话），但**「模型」标签下的内容区是空白的**。这是 DSH 前端的问题还是 WebView 渲染问题，**未定位**。

影响：用户无法通过界面填写 DeepSeek API Key。注意这条文案也过时了：「首次进入 Harness 后，请在设置 → 模型中填写 DeepSeek API Key」。

### P2 — 零散问题

- **App 横幅文案过时**：显示「首次启动会在 Debian/ARM64 中下载 cdesktop 组件，约 50 MB」，实际二进制已预置，不下载
- **cdesktop 遥测报错**：`services::services::analytics: Error sending event 'session_start'`（posthog 连不上），无害但刷日志
- **cdesktop 找不到 `claude` / `opencode`**：只装了 Codex，属预期
- **`MainActivity.kt` 已 1268 行 / 43+ 函数**：单文件承载全部 UI，后续大改前建议先拆包

---

## 三、必须知道的环境约束

这些是踩过坑得出的，**违反会浪费大量时间**：

### 1. ADB 的 USB 连接极不稳定
今天掉线十几次。写自动化时要假定设备随时消失，每步都重新检查。`sshd` 也会随 Termux 一起被杀。

### 2. Termux 是 `RUN_COMMAND` 的前提
App 靠 Termux 的 `RunCommandService` 启动后端。**Termux 进程不在时，App 的所有启动按钮都无效**，只会显示「后端服务未启动」。装 APK 会杀掉 Termux，要用 UI 重新拉起。

### 3. `input text` 会破坏特殊字符
`;`、`&&`、`()`、`|` 都会被弄坏。**不要用它串联命令**，用 `.tools/phone.ps1` 的 `RunScript` 把脚本推到设备再执行。

### 4. 手机息屏会让 `input tap` 失效
每次点击前先 `input keyevent 224` 唤醒。锁屏状态下点击会进通知栏。

### 5. 不要用 PowerShell 的 `Set-Content` 写源码
`Set-Content` / `Out-File` 在中文 Windows 上按 ANSI(GBK) 写，会**损坏文件里的所有中文**。这就是我这次弄坏 `MainActivity.kt` 的原因。要用：
```powershell
[System.IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding($false)))
```
反过来，`Get-Content` 显示中文乱码时**不代表文件坏了**（它按 ANSI 读），要用 `read` 工具或 `[System.IO.File]::ReadAllText($p, [Text.Encoding]::UTF8)` 确认。

### 6. Kotlin 原始字符串里的 `\` 行继续符不可靠
`TermuxRuntimeManager.kt` 里如果把 shell 命令写成多行加 `\`，经 Intent extras 传递后会断掉，导致 bash 报 `syntax error near unexpected token`，**整个脚本崩掉**。所有 shell 命令必须写成**单行**（已全部改好，别再改回去）。

### 7. `proot-distro` 5.9.0 不支持 `--bind`
但 **Termux 的 home 在 Debian 里天然可见**：`/data/data/com.termux/files/home/`。传文件不需要 bind。

### 8. `/sdcard` 从 Termux 不可写
没有存储权限。传文件用 Termux home。

### 9. `pkill` 会杀掉自己的 ssh 会话
sshd 子进程在同一会话里。测试脚本要把结果**写文件**，不要依赖 stdout。

### 10. 本机 npm 走一个没启动的代理
`HTTP_PROXY=127.0.0.1:10808`。npm 会 `ECONNREFUSED`，用 `Invoke-WebRequest` 直连 registry。

### 11. 手机 VPN 用 fake-IP DNS（`198.18.0.x`）
回环地址不受影响，但公网下载极不稳定 —— 这就是 cdesktop 那 48.9 MB 首次下载卡在 2% 的原因。**已经通过预置二进制绕过**，不要再让它联网下载。

---

## 四、关键实现说明

### 启动命令（已定稿，勿随意改）
```bash
# Harness — DSH >= 0.1.6 需要 --expose-internals 加载原生 shim
node --expose-internals /usr/bin/dsh web --no-open --port 3080

# Codex — 直接执行预置的静态二进制，不走 npx
$HOME/.cdesktop/bin/v0.2.3-20260519022845/linux-arm64/cdesktop   # HOST=127.0.0.1 PORT=3200
```

**为什么 DSH 需要 `--expose-internals`**：`@deepseek-ai/dsh` 从 `0.1.6-alpha.2` 起加载一个顶替 `node-addon-require-builtin` 的 shim，而那个原生插件**没有 Android 构建**。注意 **`NODE_OPTIONS` 不接受这个参数**（Node 出于安全禁止），必须直接传给 node。

**备选方案**：npm 上 `latest` 其实是 `0.1.5-rc.2`（更新前正常工作的版本），回退可完全避开该原生插件。

### cdesktop 预置位置
```
/root/.cdesktop/bin/v0.2.3-20260519022845/linux-arm64/
  cdesktop  140MB   cdesktop-mcp  19.9MB   cdesktop-review  8.0MB   (+ 原始 .zip)
```
wrapper 的 `ensureBinary()` 逻辑是「zip 已存在就直接返回，不联网也不取 manifest」，所以首次启动零下载。原始归档同时也留在 Termux 的 `~/.cdesktop-staging/`。

### Termux 回调接收器（`TermuxResultReceiver.kt`）
- **必须** `android:exported="true"` 且**不加 `android:permission`**
- 原因：`android:permission` 约束的是**发送方**，Termux 没声明我们的权限，广播会被系统丢弃 —— 而 `am broadcast` 仍返回 `Broadcast completed: result=0`，这个假成功信号极易误判
- 安全性由「只接受 `127.0.0.1:3080/3200` 回环 URL」保证
- **不要**用 `Binder.getCallingUid()` 校验发送方：广播场景下它返回 App 自己的 uid，会把功能挡死

---

## 五、调试通道

```powershell
adb forward tcp:8022 tcp:8022     # Termux sshd
ssh -p 8022 127.0.0.1
```

`.tools/phone.ps1` 提供：`Sh`（远程命令）、`Shot`（截图，二进制安全）、`RunScript`（推脚本并执行）、`EnsureShell`（自动恢复 sshd）、`LoopbackPorts`（解析 `/proc/net/tcp` 拿监听端口）。

**注意**：`.tools/phone.ps1` 必须存为 **UTF-8 with BOM**，否则 PowerShell 5.1 会把里面的中文路径读成乱码。

---

## 六、设备与环境现状

| 项目 | 值 |
|---|---|
| 设备 | 小米 25128PNA1C / Android 17 / ARM64 / 15GB RAM |
| Termux | `0.119.0-beta.3`，`allow-external-apps = true` |
| Debian | proot-distro 5.9.0，rootfs 在 `containers/debian` |
| Node.js | `v22.23.2` |
| Codex CLI | `0.155.1`，ChatGPT 已登录（`codex login status` 可验）|
| DeepSeek Harness | `0.1.6-alpha.2`（需 `--expose-internals`）|
| cdesktop | `0.2.3`，端口 3200（预览代理 37453）|
| App | `0.5.9` |

### 构建命令
```powershell
$env:JAVA_HOME = "D:\Documents\ChatGPT\手机搭建Linux\jdk17\jdk-17.0.20.1+1"
$env:ANDROID_HOME = "D:\Android\Sdk"
.\gradle-dist\gradle-8.10.2\bin\gradle.bat --no-daemon :app:assembleDebug --console=plain
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 七、仓库状态

`.gitignore` 已建立（排除约 570MB 构建产物与调试截图），**并已完成首次提交**：

```
20ffee1  baseline: CodexHarness Mobile 0.5.9 - Harness and Codex(cdesktop) both working on device
         66 files, 0.18 MB
```

这个提交固定了「编译通过、两个界面在真机上可用」的状态，可作为回退点。工作树当前干净。

`.tools/oneoff-file-recovery/` 里是我修复文件编码时用的一次性脚本（含 dex 字符串提取与逐行还原）——**正常情况下不需要再跑**，保留仅供追溯。`.tools/` 根目录下的其余脚本是常用的设备调试工具。
