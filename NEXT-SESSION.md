# 交接文档（2026-09-22 继续）

App 版本 **0.6.0**（versionCode 83）。本轮保留 DSH 3080 及全部 Android WebView 兼容层，Codex 页面从旧 cdesktop 切换到维护中的 LimLLL/codex-webui；底层仍由官方 Codex CLI/app-server 提供能力，并保留 4500 端口的原生 WebSocket 回退。首次点击 Codex WebUI 时，App 会在 Debian 中固定 commit、安装 pnpm 依赖、生成官方 schema、构建前后端、生成设备本地 API Key 并自动登录 WebView。此前已完成的附件缓存管理、窄屏专注模式、当前工作区服务重启、诊断分享、返回键关闭保护、小屏弹窗布局、按工作区保存工具栏位置、WebView 渲染自愈、网页 `capture` 图片输入相机回传、桌面图标快捷入口、系统分享文字导入、原生 Codex 语音输入和 DSH 移动端适配均保持不变。

## 本轮 Codex WebUI 迁移重点

- **官方兜底**：WebUI 只负责页面、会话和附件交互；它通过 stdio 启动官方 Codex app-server。Android 原生回退仍直连独立的 `ws://127.0.0.1:4500`。
- **DSH 不改端口、不改源码**：DeepSeek Harness 继续使用 3080；下拉菜单、提问卡、401 鉴权识别、文件 URI 暂存和全局可拖动工具栏的兼容代码均保留。
- **可复现安装**：上游固定在 `e98ee58ac8c80780258474e0f13ca67a463a2726`，安装目录为 `/root/.codex-harness-mobile/codex-webui`，运行日志为 Termux HOME 下的 `codex-webui.log`。
- **自动登录**：WebUI 生成的随机 key 只保存在 Debian 本地，Termux 回调只在 App 内存中短暂传递；WebView 使用同源 `/api/auth/login` 换取 JWT，不要求用户手动复制内部 key。
- **附件链路**：LimLLL WebUI 的上传接口会把文件落到 Linux 文件系统，随后把可读路径交给官方 app-server；Android 仍负责 DSH 的 content URI 暂存和 FileProvider 权限。
- **首次构建注意**：第一次安装会下载 Node 依赖并编译 `better-sqlite3`、`node-pty` 等原生模块，手机上可能需要几分钟；构建失败时先看 `codex-webui.log`，不要反复点启动。

本轮新代码已经完成 Android 编译级检查；真机上的首次 WebUI 构建、自动登录、附件、多会话和 Termux 重开回归仍需在 ADB 设备上完成。

---

## 一、已验证可用

| 功能 | 状态 | 证据 |
|---|---|---|
| **DSH 启动** | ✅ | `node --expose-internals /usr/bin/dsh web --no-open --port 3080`，App 自动启动后 3080 在 5–6 秒内就绪 |
| **DSH 界面** | ✅ | 完整加载：侧栏、对话/轨迹标签、输入框、状态栏、历史数据完好 |
| **Codex（旧 cdesktop 基线）** | ✅（历史） | `Main server on :3200`，作为迁移前回退证据保留 |
| **Codex WebUI 迁移** | ⏳ | Android 编译级检查已通过；待真机首次 pnpm 构建、自动登录、附件和重开验证 |
| **Termux 回调链路** | ✅ | `I TermuxResultReceiver: result received: url=http://127.0.0.1:3080/?token=...` |
| **Termux 重开后恢复 DSH** | ✅ | 关闭/重开 Termux 后自动重新 dispatch，3080 恢复，无 `EADDRINUSE` |
| **WebView 手机布局** | ✅ | 真机已验证 Harness 主界面、输入框和附件按钮可见，键盘弹出时页面不塌陷 |
| **DSH 下拉菜单** | ✅ | 修复 Android WebView 中 `100vh` 被解析为 0 导致菜单高度为 0；模型、权限、顶部更多菜单均已真机验证可展开 |
| **手机专注模式** | ✅ | 打开 Codex/Harness 网页后隐藏 App 顶栏、底栏和包装栏；工具入口默认在右侧中部，并支持在安全区域内按住拖到屏幕任意位置，避免遮挡 DSH 控件 |
| **连接诊断** | ⏳ | 新版源码已实现网络类型、Termux 权限、4500/3200/3080 本地端口、公网 HTTPS 和 Termux 回调成功/失败结果检测；本地 Harness 无 token 的预期 401 已改为“服务可达，需要访问令牌”；待 ADB 重连后点击验证 |
| **DSH 提问选项** | ✅ | 修复 Android WebView 将 DSH 卡片的 `60vh` 上限解析为 0，导致卡片只剩底部 10px、选项和提交按钮不可触摸；同时保留标题限高、内部滚动和换行 |
| **网页加载反馈** | ✅ | Codex/Harness WebView 显示真实加载进度和页面名称；主页面错误时保留明确的重新加载入口 |
| **专注模式刷新** | ✅ | 隐藏原生工具栏后，仍可从浮动工具栏刷新当前 Codex/Harness WebView，并复用加载进度反馈 |
| **前台恢复** | ✅ | App 从后台回到前台立即重查 Termux、端口和回调地址，不必等待轮询周期 |
| **长任务屏幕常亮** | ✅ | 专注模式工具栏可选保持屏幕常亮，适合等待长时间任务；默认关闭并记住用户选择 |
| **文件选择回传** | ✅ | MIUI 文件选择器可打开；选择结果经过 URI 解析、缓存暂存和 FileProvider 回传 |
| **系统返回键** | ✅ | 内嵌页面优先返回 App 工作台，不直接退出整个应用 |

---

## 二、本轮已完成的修复与后续注意

### 已完成 — 文件上传链路

`MainActivity.kt` 的 `showFileChooser` / `buildFileChooserIntent` 已完成：
- 原实现只用 `params.createIntent()`，无备选。在 MIUI / Android 11+ 上该 intent 常解析不到处理器，代码返回 `false`，**WebView 会静默吞掉点击**
- 新实现依次尝试 5 个候选 intent，取第一个能 `resolveActivity` 的
- 按 `acceptTypes` 设置 MIME，多类型用 `EXTRA_MIME_TYPES`，支持多选
- 取消选择也回调 `null`（否则页面 file input 永久禁用）
- 新增：上一次未返回的选择器会被主动解除阻塞
文件选择结果会复制到 App cache 下的 `webview-upload/`，再通过 `FileProvider` 交给 WebView，避免 MIUI provider 的临时 URI 在选择器关闭后失效。WebView 调试日志 tag 为 `HarnessWebView`。

### 已完成 — DSH 设置与移动布局

注入的移动布局会把设置弹窗改为紧凑顶部标签，并让内容区独立滚动；同时修正 Android WebView 在该页面上出现的根节点零高度问题。真机已验证主页面、输入区和键盘布局。

### 已完成 — DSH 下拉菜单

DSH 的模型、权限和顶部更多操作菜单使用 `100vh` 计算最大高度；Android WebView 在移动端会把该表达式算成 0。移动端注入样式现在改用实际的 visual viewport 高度，并为菜单开启滚动和触摸层级。三类菜单已在真机 APK 上直接展开验证。

### 已完成源码修复 — DSH 提问选项、诊断误报与可移动工具栏

- DSH 的 `ask_user_question` 卡片会把很长的 `question` 原文放进标题；标题无限增长时会把选项和提交按钮压到 0 高度。Android 侧现在对 CSS Module 的 `headingBlock/title` 加高度上限、内部滚动和长文本换行，选项不再被标题挤出卡片。
- Android WebView 还会把 DSH 卡片原 CSS 中的 `max-height: min(60vh, 520px)` 解析成 0；现在仅对 composer 内的提问卡改用实际 visual viewport 高度，恢复选项区和底部提交栏。真机上已确认提问选项可点击。
- Harness 根地址不带一次性 token 时返回 HTTP 401 是正常鉴权行为，不代表 3080 服务停止。连接诊断现在将本机 401–403 识别为“服务可达，需要访问令牌”，不再显示成故障。
- 专注模式的工具入口默认收起到右侧中部；按住收起按钮或展开后的工具栏即可在整个安全区域内拖动，位置边界会自动限制在屏幕内；位置会保存，展开后可一键归位。工具栏还提供页面刷新和可持久化的屏幕常亮开关。
- 连接诊断弹窗支持复制诊断摘要，便于在手机上把网络、Termux、端口、回调和错误信息一次性转发出来。
- 原生 Codex 备用对话的消息文本支持长按选择复制；流式回复只在用户接近底部时自动滚动，用户向上查看旧内容不会被打断；输入框支持软键盘“发送”动作。
- 最近使用的工作区写入 `mobile_preferences.last_workspace`，再次打开 App 会优先回到上次的 Codex/Harness 页面。
- Codex/Harness 页面包装栏新增网页后退、前进和刷新按钮；按钮状态由 WebView 的 `canGoBack()` / `canGoForward()` 实时同步，避免手机用户只能依赖系统返回键。
- 工作区切换改为两个页面同时保活、当前页面置顶；切换 Codex/Harness 不再销毁旧 WebView，旧页面的滚动位置、表单输入和网页状态可以继续使用。只有当前工作区启用返回键和触摸层级，后台工作区不会抢操作。
- `HarnessViewportFix` 增加页面级安装保护：同一个 WebView 多次触发 `onPageFinished` 时不再重复创建 MutationObserver/resize listener；DSH 流式消息造成的 DOM 更新通过 `requestAnimationFrame` 合并，降低手机端重复重排。
- 文件选择回传增加状态提示：选中文件后先显示正在准备，暂存 URI 完成后显示已准备，异常时显示失败原因；每次新选择都会使旧暂存任务失效，旧任务不会再次回调已解除的 WebView 文件输入。
- 手机相机附件：当网页通过 `<input type="file" accept="image/*" capture>` 请求拍照时，Android 直接打开系统相机，将照片写入私有缓存；相机取消、无输出、暂存取消或 Activity 销毁都会删除临时照片，成功后仍沿用 URI 读取、大小校验和 FileProvider 回传。
- 桌面图标快捷入口：`shortcuts.xml` 提供 Codex、Harness、连接诊断三个静态入口；`MainActivity` 通过 `onCreate` 和 `onNewIntent` 统一消费请求，打开已存在页面时保持 WebView 状态，不会因快捷跳转重复下发 Termux 启动命令。
- 系统分享导入：Manifest 接收 `ACTION_SEND` / `ACTION_PROCESS_TEXT` 的 `text/plain`；文本限制为最多 100000 个字符，切到原生 Codex 后只写入 `composer_draft` 并等待用户主动点击发送，返回或新建对话仍沿用原有清理逻辑。
- Codex/Harness 的 WebView 错误卡片新增“恢复服务”：复用本地健康检查补启缺失的 Codex/Harness 服务，等待检查完成后自动触发当前页面刷新；服务本来在线时也可用来修复失效的页面连接。
- 专注模式展开后的浮动工具栏新增工作区切换按钮；切换到已打开的工作区会继续使用其保活 WebView，切换到未打开的工作区则退出专注模式并显示对应启动页。
- 连接诊断新增“查看最近 Termux 日志”：通过回调读取 `harness.log`、`codex-webui.log`、`codex.log` 各最近 80 行，展示在可滚动弹窗中并支持复制；日志回调不会再被误判为 Harness URL，也会沿用 token 脱敏。
- 健康轮询按 Activity 前后台状态调整：前台每 5 秒检查并保留自动恢复；退到后台时改为 30 秒低频检查且跳过主动恢复；收到 `ON_RESUME` 后立即刷新 Termux、端口和 WebView 回调状态。
- 最近日志弹窗新增“分享”，通过 Android 原生 `ACTION_SEND` 分享已经脱敏的纯文本日志，适合直接发给排查人员或保存到其他 App。
- 原生 Codex 备用对话把未发送输入保存到 `codex_mobile_settings.composer_draft`；进程被系统回收后可恢复，发送和新建对话会清除草稿。
- 原生 Codex 消息列表根据可见位置显示“↓ 回到底部”按钮；用户上滑查看旧消息时不被流式输出强行拉回，点击按钮可回到最新回复或工作指示器。
- 专注模式工具栏新增“字 N%”菜单，分别调整 Codex/Harness WebView 的文字缩放（80%–140%）；新值保存到 `mobile_preferences.web_text_zoom_codex` / `web_text_zoom_harness`，旧版统一 `web_text_zoom` 会作为首次迁移默认值，切换工作区或重载页面后仍保持。
- Codex/Harness WebView 新增下载回调：通过 Android `DownloadManager` 保存网络文件，自动带上当前 WebView 的 Cookie、User-Agent 和 MIME 类型，下载结果进入系统通知和公共“下载”目录；不支持的 `blob:` 等链接会给出明确提示。
- Codex/Harness WebView 新增加载看门狗：`onPageStarted` 后 15 秒仍未完成且页面没有明确错误时，按现有 1 秒/2 秒策略最多自动重试两次；成功完成会取消看门狗，401–403 等鉴权响应不会被重复重试，最终失败仍落到原有“重新加载 / 恢复服务 / 连接诊断”卡片。
- 本地服务操作新增互斥锁：顶部启动/停止、工作区启动、页面错误恢复和后台自动恢复不能同时下发命令；Codex 工作台最多等待 90 秒、Harness 45 秒、普通服务 30 秒观察真实端口状态，超时只释放 UI 锁并交给前台健康轮询继续检查，避免把“命令已发送”误报成“服务已就绪”。
- Termux 后台保活：`health()` 和连接诊断会读取 `PowerManager.isIgnoringBatteryOptimizations("com.termux")`；Harness 首页和诊断弹窗显示状态，点击后优先打开针对 Termux 的系统请求页，系统不支持时回退到电池优化列表。App 不会自动申请豁免，最终选择由用户在系统设置中完成。
- 横屏/小高度布局：`LocalConfiguration` 检测横屏或 `screenHeightDp <= 500`，AppHeader 从 48dp 压缩到 42dp 并隐藏状态副标题，AppNavigation 从 56dp 压缩到 48dp 且隐藏文字标签；专注模式和 WebView 本身不改变，系统返回键、IME 避让和可移动工具栏逻辑保持不变。
- 旋转保活：`MainActivity` 在 Manifest 中声明 `orientation|screenSize|screenLayout|smallestScreenSize` 配置变化由自身处理，避免横竖屏切换触发 Activity/WebView 重建；Compose 仍能收到新的 `LocalConfiguration`，因此紧凑顶部栏/底部导航会实时切换。
- 网页内查找：Codex/Harness WebView 包装栏新增搜索按钮，弹窗调用 `findAllAsync` 并支持 `findNext(true/false)`、清除匹配和 `ImeAction.Search`；只作用于当前网页，不会把搜索词写入 Codex/Harness 会话或本地配置。
- 附件暂存进度：`stageUploadUri` 改用 64 KiB 缓冲区逐段复制，通过主线程节流更新“第 N/M 个文件、文件名、已复制/总大小”；大小未知时显示“大小未知”，重复打开选择器仍以 `uploadRequestId` 丢弃旧进度和旧回调。
- 附件暂存取消与校验：保留当前上传回调和协程任务；用户关闭进度提示、重新打开选择器或销毁 Activity 时取消旧任务并解除 WebView 等待；复制完成后按 `OpenableColumns.SIZE` 校验目标文件大小，失败删除半成品并抛出错误，不再回退到原始 provider URI。
- 实时网络状态：`LiveRuntimeState` 记录 `ConnectivityManager` 的活动网络、传输类型和 `NET_CAPABILITY_VALIDATED`；健康摘要会在本地服务在线但没有外网/网络未验证时追加提示，Harness 手机环境卡片增加网络状态行。
- 手机存储保护：`stageUploadUri` 读取 `StatFs.availableBytes`，对已知大小的文件预留 1 MiB 安全余量；不足时在复制前抛出带“需要/当前可用”信息的错误，复制异常统一删除目标半成品。
- 原生 Codex 对话恢复：`CodexWebSocketClient` 将当前 `threadId` 保存到 `codex_mobile_settings.active_thread_id`；初始化或重连后自动发送 `thread/read`，恢复完成前公开 `restoring` 状态并禁用 Composer；新建对话会清除该线程记忆，恢复失败也会清除失效线程。
- 原生 Codex 整段分享：`CodexNativeScreen` 按“你/系统/Codex”角色拼接非空消息，限制为最后 100000 个字符后调用 `ACTION_SEND`；空对话和无分享处理器时显示明确提示。
- 原生 Codex 后台完成通知：Manifest 声明 `POST_NOTIFICATIONS`；顶部铃铛默认关闭，点击后才调用系统权限请求，权限通过后写入 `mobile_preferences.background_completion_notifications`；后台期间从 busy 变为完成时创建通知渠道并发送可点击通知，回前台时不再重复提示。
- 原生 Codex 对话内查找：`CodexNativeScreen` 保存 `messageSearchQuery`，按消息文本不区分大小写生成 `visibleMessages`；列表滚动和“回到底部”按筛选结果工作，零匹配时显示明确空状态，清除按钮恢复完整对话。
- 网络切换即时刷新：`MobileWorkbench` 在前台通过 `ConnectivityManager.registerDefaultNetworkCallback` 监听 `onAvailable`、`onLost` 和 `onCapabilitiesChanged`；事件合并 350ms 后调用现有 `refresh()`，离开前台时注销回调并取消待执行刷新。
- 原生 Codex 紧凑操作栏：`CodexNativeScreen` 仅保留返回/新建按钮和一个 `DropdownMenu` 入口；菜单内按手机触摸尺寸提供配置、历史、对话查找、通知开关与整段分享，避免窄屏按钮超出可视区域。
- 网页错误详情复制：新增 `copyWebErrorDetails`，Codex 工作台和 DeepSeek Harness 的错误卡在现有操作下方提供复制入口，内容包含页面名称、错误文本和当前 WebView URL，不改变网页会话或重载状态。
- 附件缓存管理：连接诊断新增“附件暂存”卡片，异步显示 `webview-upload` 的文件数量、总占用和超过 24 小时的可清理数量；“清理 24 小时前缓存”只删除旧暂存文件，清理完成后立即刷新统计并反馈释放空间。
- 窄屏专注工具栏：`FocusTools` 根据 `screenWidthDp <= 420` 隐藏重复的 Codex/Harness 标题，适配加入沉浸模式入口后的完整按钮组，保留所有功能按钮并重新计算拖动边界；连接诊断图标按网络可用性显示青色、未验证黄色或不可用红色，并更新无障碍描述。
- 当前工作区服务重启：`DiagnosticsDialog` 新增“重启”入口；Codex 工作台调用 `restartCodexDesktopAndOpen(forceRestart = true)`，Harness 调用 `restartHarnessAndOpen(forceRestart = true)`，先关闭当前 WebView，再强制清理旧进程、等待对应端口和回调恢复，操作锁防止重复下发。
- 诊断分享：将原有诊断摘要抽成统一文本生成函数，复制和系统 `ACTION_SEND` 共用同一份内容；分享保留网络、Termux、当前工作区、附件缓存、端口探测和最近回调信息，并沿用运行时已有的 token 脱敏。
- 返回键关闭保护：`MobileWorkbench` 将网页专注模式的第二次系统返回改为显示确认框；键盘可见时仍交给系统先收起 IME，网页包装栏的后退按钮仍优先使用 WebView 历史记录，只有确认“关闭工作区”才调用原有关闭回调。
- 弹窗高度响应式：新增 `mobileDialogMaxHeight`，按 `screenHeightDp` 为诊断、日志、Codex 设置、历史和审批内容区预留标题/确认按钮空间；大屏可显示更多内容，小屏自动缩短并依靠 `LazyColumn`/滚动容器继续访问完整内容。
- 工具栏按工作区保存：`FocusTools` 使用 `focus_tools_codex_dx/dy` 和 `focus_tools_harness_dx/dy` 两组偏好；首次读取时回退到旧的 `focus_tools_dx/dy`，拖动、归位、屏幕尺寸重新限制边界时都只写入当前工作区坐标。
- WebView 渲染进程自愈：Codex/Harness 各自记录 `rendererRecoveryAttempt`；`onRenderProcessGone` 首次触发时延迟 600ms 递增 `reloadKey` 重建 AndroidView，页面成功完成后清零；同一页面再次崩溃则保留原错误卡，避免崩溃循环。
- 沉浸模式：`MobileWorkbench` 将 `immersive_mode` 保存到 `mobile_preferences`；网页专注、用户开启且 Activity 在前台时通过 `WindowInsetsControllerCompat` 隐藏 system bars，使用系统手势可暂时显示，条件不满足或 effect 释放时恢复 system bars，不改变默认非沉浸布局。
- 原生 Codex 每条消息头部新增“复制”和系统分享按钮；复制写入 Android 剪贴板并显示确认提示，分享沿用系统 `ACTION_SEND` 面板，长按选择复制仍保留。
- 专注模式浮动工具条额外应用 `imePadding`；软键盘弹出时工具条和拖动边界避开 IME，WebView 输入框仍可正常使用。
- 返回键逻辑读取 IME 可见状态：MobileWorkbench、Codex 原生对话、Codex WebView、Harness WebView 在键盘可见时暂不拦截返回键，让系统先收起键盘；键盘收起后仍按原有层级返回工作区或网页历史。
- Codex 历史弹窗新增本地搜索框，按标题和摘要不区分大小写筛选；支持清除关键词、继续对话和原有刷新，搜索无结果时显示明确提示。
- 顶部停止按钮改为两步确认：只有在确认“停止全部”后才关闭 Codex、Codex 工作台和 Harness 窗口并下发 `stopAll()`；启动路径不受影响。
- Codex/Harness WebView 在主框架网络错误、HTTP 408/429/5xx 时自动重试两次，等待 1 秒和 2 秒；成功后清零重试计数，401–403 不重试，避免把令牌失效误当成临时网络问题。
- Activity 在 `ON_PAUSE` 时记录原生 Codex 是否正在执行；回到前台后若该任务已经完成，显示一次“Codex 已在后台完成”提示，之后清除标记，不影响 WebView 或会话状态。

### 后续注意

- **Codex WebUI 首次启动较慢**：第一次需要 pnpm 安装依赖和构建原生模块；优先查看 `codex-webui.log`，不要连续点击启动。
- **Codex WebUI 登录页重复出现**：检查 `.webui-api-key`、`.env` 和 Termux 回调日志，确认 `CODEX_WEBUI_KEY` 已回传；不要把 key 粘贴到公共 issue。
- **Codex WebUI 找不到 Codex**：确认 Debian 内 `codex login status` 正常，并检查 `.env` 中 `CODEX_HOME=/root/.codex` 与实际登录目录一致。
- **`MainActivity.kt` 仍是单文件 UI**：后续大改前建议拆成 `ui/` 与 `web/` 子包
- **0.5.32 APK 已在桌面环境完成 assembleDebug/lintDebug**；设备重连后执行 `adb install -r app\\build\\outputs\\apk\\debug\\app-debug.apk`，重点回归原生 Codex 后台完成/回前台提示和标记清理、网页网络错误自动重试/成功清零、401–403 不重试、停止按钮确认/取消/确认后停止、历史对话关键词搜索/清除/无结果提示/继续对话、键盘打开时返回键先收起 IME、键盘收起后再返回网页/工作区、软键盘弹出时浮动工具条避让和拖动、原生 Codex 消息复制/系统分享/长按复制、网页文字缩放在 Codex/Harness、切换工作区和重载后的保持、原生 Codex 回到底部/上滑不抢位置、草稿恢复/发送清理/新建清理、日志查看/复制/系统分享、前后台轮询节流和回前台恢复、App 内日志读取、专注模式工作区切换、页面错误后的恢复服务/自动刷新、附件暂存提示和重复选择、流式 DSH 布局、双页面切换状态保留、网页后退/前进/刷新、工作区记忆、原生 Codex 键盘发送、加载进度、工具栏归位、Termux 重开、DSH 下拉菜单和附件选择

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
回环地址不受影响，但公网下载极不稳定。旧 cdesktop 的离线预置流程只保留作历史排障记录；当前 Codex WebUI 使用固定源码和 pnpm lockfile 构建，首次安装必须确保 Debian 能访问 GitHub/npm registry，构建完成后运行不再重复下载。

---

## 四、关键实现说明

### 启动命令（已定稿，勿随意改）
```bash
# Harness — DSH >= 0.1.6 需要 --expose-internals 加载原生 shim
node --expose-internals /usr/bin/dsh web --no-open --port 3080

# Codex WebUI — 在 Debian 中运行固定版本的 Node/Nest 服务
cd /root/.codex-harness-mobile/codex-webui
set -a; . .env; set +a
exec node dist/main.js
```

**为什么 DSH 需要 `--expose-internals`**：`@deepseek-ai/dsh` 从 `0.1.6-alpha.2` 起加载一个顶替 `node-addon-require-builtin` 的 shim，而那个原生插件**没有 Android 构建**。注意 **`NODE_OPTIONS` 不接受这个参数**（Node 出于安全禁止），必须直接传给 node。

**备选方案**：npm 上 `latest` 其实是 `0.1.5-rc.2`（更新前正常工作的版本），回退可完全避开该原生插件。

### Codex WebUI 安装位置
```
/root/.codex-harness-mobile/codex-webui/
  dist/main.js
  public/index.html
  .env
  .webui-api-key
  .codex-harness-mobile-ready
```
`.codex-harness-mobile-ready` 记录固定 commit；`.webui-api-key` 和 `.env` 只在 Debian 本地保存，不能提交或复制到公共仓库。

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
| Codex WebUI | `LimLLL/codex-webui` pinned commit，端口 3200 |
| App | `0.6.0`（versionCode 83） |

### 构建命令
```powershell
$env:JAVA_HOME = "<path-to-jdk-17>"
$env:ANDROID_HOME = "<path-to-android-sdk>"
.\gradlew.bat --no-daemon :app:assembleDebug :app:lintDebug --console=plain
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 七、仓库状态

`.gitignore` 已建立（排除约 570MB 构建产物与调试截图），**并已完成首次提交**：

```
20ffee1  baseline: CodexHarness Mobile 0.5.9 - Harness and Codex(cdesktop) both working on device
         66 files, 0.18 MB
```

这个提交固定了「编译通过、两个界面在真机上可用」的状态，可作为回退点。本轮改动在其上继续进行；`git diff --check` 和 `assembleDebug + lintDebug` 已通过，0.5.65 的系统分享文字导入、桌面图标快捷入口、手机相机附件回传、原生 Codex 语音输入、按工作区网页缩放、错误详情 token 脱敏、可选沉浸模式、420dp 窄屏阈值、WebView 渲染自愈、按工作区保存工具栏位置、弹窗高度响应式、返回键关闭保护、诊断分享、当前工作区服务重启、窄屏专注工具栏、附件缓存管理、网页错误详情复制、原生 Codex 紧凑操作栏、网络切换即时刷新、对话内查找、后台完成通知、整段对话分享、原生 Codex 对话恢复、存储保护、网络状态显示、附件取消、严格暂存校验以及前面功能的真机回归均已写入源码。

`.tools/oneoff-file-recovery/` 里是我修复文件编码时用的一次性脚本（含 dex 字符串提取与逐行还原）——**正常情况下不需要再跑**，保留仅供追溯。`.tools/` 根目录下的其余脚本是常用的设备调试工具。
