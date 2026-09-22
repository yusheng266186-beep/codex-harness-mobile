package app.codexharness.mobile

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.core.view.WindowCompat
import app.codexharness.mobile.runtime.CodexUiMessage
import app.codexharness.mobile.runtime.CodexWebSocketClient
import app.codexharness.mobile.runtime.CodexRunSettings
import app.codexharness.mobile.runtime.CodexDesktopBridgeState
import app.codexharness.mobile.runtime.HarnessBridgeState
import app.codexharness.mobile.runtime.LiveRuntimeState
import app.codexharness.mobile.runtime.TermuxRuntimeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.webkit.ValueCallback

private val Ink = Color(0xFF090C12)
private val Panel = Color(0xFF111620)
private val PanelRaised = Color(0xFF171D29)
private val Line = Color(0xFF293244)
private val TextPrimary = Color(0xFFF3F6FC)
private val TextSecondary = Color(0xFF9AA6BA)
private val Violet = Color(0xFF9887FF)
private val Cyan = Color(0xFF5BD8D0)
private val Green = Color(0xFF5ED594)
private val Amber = Color(0xFFFFC568)
private val Red = Color(0xFFFF7C8C)

private val HarnessViewportFix = """
    (() => {
      if (window.__codexHarnessViewportFix) {
        window.__codexHarnessViewportFix();
        return;
      }
      const mobileStyle = document.createElement('style');
      mobileStyle.id = 'codex-harness-mobile-layout';
      mobileStyle.textContent = `
        @media (max-width: 560px) {
          [role="dialog"] {
            box-sizing: border-box !important;
            max-height: calc(100dvh - 24px) !important;
            height: auto !important;
            overflow: hidden !important;
          }
          [role="dialog"] > nav + div,
          [role="dialog"] > div:last-child,
          [role="dialog"] section,
          [role="dialog"] main,
          [role="dialog"] [class*="overflow-y-auto"],
          [role="dialog"] [class*="overflow-auto"],
          [role="dialog"] [data-radix-scroll-area-viewport] {
            box-sizing: border-box !important;
            min-height: 0 !important;
            max-height: calc(100dvh - 150px) !important;
            overflow-y: auto !important;
            overscroll-behavior-y: contain !important;
            touch-action: pan-y !important;
            -webkit-overflow-scrolling: touch !important;
          }
          [role="dialog"]:has(> nav) { flex-direction: column !important; }
          [role="dialog"] > nav {
            box-sizing: border-box !important;
            width: 100% !important;
            height: auto !important;
            padding: 12px !important;
          }
          [role="dialog"] > nav > div:first-child { display: none !important; }
          [role="dialog"] > nav > div:nth-child(2) {
            width: 100% !important;
            flex-direction: row !important;
            gap: 4px !important;
          }
          [role="dialog"] > nav > div:nth-child(2) > button {
            flex: 1 1 0 !important;
            min-width: 0 !important;
            padding: 8px 4px !important;
          }
          [role="dialog"] > nav > div:nth-child(2) > button svg { display: none !important; }
          [role="dialog"] > nav > div:nth-child(2) > button span {
            font-size: 12px !important;
            white-space: nowrap !important;
            text-align: center !important;
          }
          [role="dialog"] > nav + div {
            width: 100% !important;
            min-width: 0 !important;
            flex: 1 1 0 !important;
          }
        }
        html, body, #root { touch-action: pan-y !important; }
        [data-slot], [class*="scroll"], [class*="content"], [role="dialog"] { -webkit-overflow-scrolling: touch; }
      `;
      document.head.appendChild(mobileStyle);
      const tracked = [];
      const seen = new WeakMap();
      const viewportUnit = /(-?\d*\.?\d+)(?:d|s|l)?vh\b/gi;
      const trackStyle = (style) => {
        let names = seen.get(style);
        if (!names) {
          names = new Set();
          seen.set(style, names);
        }
        for (const name of Array.from(style)) {
          const value = style.getPropertyValue(name);
          viewportUnit.lastIndex = 0;
          if (!viewportUnit.test(value) || names.has(name)) continue;
          names.add(name);
          tracked.push({ style, name, value, priority: style.getPropertyPriority(name) });
        }
      };
      const walkRules = (rules) => {
        for (const rule of Array.from(rules)) {
          try {
            if (rule.cssRules) walkRules(rule.cssRules);
            if (rule.style) trackStyle(rule.style);
          } catch (_) {}
        }
      };
      const collect = () => {
        for (const sheet of Array.from(document.styleSheets)) {
          try { walkRules(sheet.cssRules); } catch (_) {}
        }
      };
      const apply = () => {
        const height = Math.max(window.innerHeight, 1);
        const pixelHeight = height + 'px';
        for (const element of [document.documentElement, document.body, document.getElementById('root')]) {
          if (!element) continue;
          element.style.setProperty('height', pixelHeight, 'important');
          element.style.setProperty('min-height', pixelHeight, 'important');
        }
        for (const item of tracked) {
          viewportUnit.lastIndex = 0;
          const value = item.value.replace(viewportUnit, (_, amount) =>
            (parseFloat(amount) * height / 100) + 'px');
          item.style.setProperty(item.name, value, item.priority);
        }
      };
      let scheduled = false;
      const refresh = () => {
        if (scheduled) return;
        scheduled = true;
        requestAnimationFrame(() => {
          scheduled = false;
          collect();
          apply();
        });
      };
      window.__codexHarnessViewportFix = refresh;
      window.addEventListener('resize', refresh);
      new MutationObserver(refresh).observe(document.documentElement, { childList: true, subtree: true });
      collect();
      apply();
    })();
""".trimIndent()

private enum class Workspace { CODEX, HARNESS }

class MainActivity : ComponentActivity() {
    private lateinit var runtime: TermuxRuntimeManager
    private val codex by lazy { CodexWebSocketClient(applicationContext) }
    private var pendingFileCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val callback = pendingFileCallback
        pendingFileCallback = null
        val uris = runCatching {
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        }.getOrNull()
        // Cancelling (or an unparsable result) must still answer the WebView, or the
        // page's file input stays disabled until it is reloaded.
        callback?.onReceiveValue(uris?.takeIf { it.isNotEmpty() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        // Lets us inspect the embedded Harness/cdesktop pages over chrome://inspect
        // (adb forward tcp:9222 localabstract:webview_devtools_remote_<pid>).
        WebView.setWebContentsDebuggingEnabled(true)
        runtime = TermuxRuntimeManager(applicationContext)
        acceptHarnessIntent(intent)
        if (ActivityCompat.checkSelfPermission(this, TermuxRuntimeManager.RUN_COMMAND_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(TermuxRuntimeManager.RUN_COMMAND_PERMISSION), 1201)
        }
        setContent {
            MobileWorkbench(
                runtime = runtime,
                codex = codex,
                harnessUrl = HarnessBridgeState.url,
                onCloseHarness = { HarnessBridgeState.url = null },
                codexDesktopUrl = CodexDesktopBridgeState.url,
                onCloseCodexDesktop = { CodexDesktopBridgeState.url = null },
                onRequestPermission = {
                    ActivityCompat.requestPermissions(this, arrayOf(TermuxRuntimeManager.RUN_COMMAND_PERMISSION), 1201)
                },
                onShowFileChooser = ::showFileChooser,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptHarnessIntent(intent)
    }

    override fun onDestroy() {
        pendingFileCallback?.onReceiveValue(null)
        pendingFileCallback = null
        codex.disconnect()
        super.onDestroy()
    }

    private fun acceptHarnessIntent(intent: Intent?) {
        if (intent?.action == "app.codexharness.mobile.OPEN_HARNESS") {
            intent.getStringExtra("harness_url")?.let { HarnessBridgeState.url = it }
        }
    }

    private fun showFileChooser(
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams,
    ): Boolean {
        // A previous chooser that never returned would otherwise keep the WebView's
        // picker permanently blocked.
        pendingFileCallback?.onReceiveValue(null)
        pendingFileCallback = callback

        val intent = buildFileChooserIntent(params)
        if (intent.resolveActivity(packageManager) == null) {
            // Nothing can handle it: unblock the page instead of leaving the picker
            // waiting forever.
            pendingFileCallback = null
            callback.onReceiveValue(null)
            return false
        }
        return runCatching {
            fileChooserLauncher.launch(intent)
            true
        }.getOrElse {
            pendingFileCallback = null
            callback.onReceiveValue(null)
            false
        }
    }

    /**
     * Builds a file-picker intent that survives OEM ROMs.
     *
     * WebChromeClient.FileChooserParams.createIntent() alone is not reliable: on
     * several OEM builds its ACTION_GET_CONTENT intent resolves to nothing, and on
     * MIUI an intent carrying a narrow MIME filter can fail to list content
     * providers at all. So we try a series of candidates and take the first one
     * the system can actually resolve.
     */
    private fun buildFileChooserIntent(params: WebChromeClient.FileChooserParams): Intent {
        val mimeTypes = params.acceptTypes
            .filter { it.isNotBlank() }
            .map { if (it.contains('/')) it else "$it/*" }
            .distinct()
        val allowMultiple = params.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE

        fun base(action: String, wide: Boolean = false) = Intent(action).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            when {
                wide || mimeTypes.isEmpty() -> type = "*/*"
                mimeTypes.size == 1 -> type = mimeTypes.first()
                else -> {
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
                }
            }
        }

        val candidates = listOf(
            runCatching { params.createIntent() }.getOrNull(),
            base(Intent.ACTION_OPEN_DOCUMENT),
            base(Intent.ACTION_GET_CONTENT),
            base(Intent.ACTION_OPEN_DOCUMENT, wide = true),
            base(Intent.ACTION_GET_CONTENT, wide = true),
        )
        return candidates.filterNotNull().firstOrNull { it.resolveActivity(packageManager) != null }
            ?: base(Intent.ACTION_GET_CONTENT, wide = true)
    }
}

@Composable
private fun MobileWorkbench(
    runtime: TermuxRuntimeManager,
    codex: CodexWebSocketClient,
    harnessUrl: String?,
    onCloseHarness: () -> Unit,
    codexDesktopUrl: String?,
    onCloseCodexDesktop: () -> Unit,
    onRequestPermission: () -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
) {
    var selected by rememberSaveable { mutableStateOf(Workspace.CODEX) }
    var health by remember { mutableStateOf(LiveRuntimeState(checking = true, detail = "正在检测手机环境…")) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        health = health.copy(checking = true)
        health = runtime.health()
        if (health.codexOnline && !codex.connected && !codex.connecting) codex.connect()
    }

    LaunchedEffect(Unit) {
        refresh()
        if (
            health.termuxInstalled &&
            health.commandPermission &&
            (!health.codexOnline || !health.harnessOnline)
        ) {
            actionMessage = runtime.startAll().fold(
                onSuccess = { "正在自动启动本地运行时…"},
                onFailure = { it.message ?: "自动启动失败" },
            )
            delay(2_500)
            refresh()
        }
        while (true) {
            delay(5_000)
            refresh()
        }
    }

    LaunchedEffect(harnessUrl) {
        if (harnessUrl != null) actionMessage = null
    }

    MaterialTheme(colorScheme = mobileColors()) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Scaffold(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                containerColor = Ink,
                topBar = {
                    AppHeader(
                        health = health,
                        running = health.codexOnline || health.harnessOnline,
                        onRefresh = { scope.launch { refresh() } },
                        onToggle = {
                            scope.launch {
                                val wasRunning = health.codexOnline || health.harnessOnline
                                val result = if (wasRunning) runtime.stopAll() else runtime.startAll()
                                actionMessage = result.fold(
                                    onSuccess = { if (wasRunning) "已发送停止指令" else "正在启动本地服务…" },
                                    onFailure = { it.message ?: "操作失败" },
                                )
                                delay(2_200)
                                refresh()
                            }
                        },
                    )
                },
                bottomBar = { AppNavigation(selected = selected, onSelected = { selected = it }) },
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    AnimatedVisibility(visible = actionMessage != null, enter = fadeIn(), exit = fadeOut()) {
                        actionMessage?.let { InlineNotice(it) { actionMessage = null } }
                    }
                    AnimatedContent(targetState = selected, label = "工作区切换") { page ->
                        when (page) {
                            Workspace.CODEX -> CodexScreen(
                                modifier = Modifier.fillMaxSize(),
                                health = health,
                                client = codex,
                                desktopUrl = codexDesktopUrl,
                                onCloseDesktop = onCloseCodexDesktop,
                                onShowFileChooser = onShowFileChooser,
                                onStartDesktop = {
                                    scope.launch {
                                        actionMessage = runtime.restartCodexDesktopAndOpen().fold(
                                            onSuccess = { "正在启动 Codex 开源工作台，首次启动会下载组件…"},
                                            onFailure = { it.message ?: "Codex 工作台启动失败"},
                                        )
                                        delay(2_500)
                                        refresh()
                                    }
                                },
                                onPermission = onRequestPermission,
                            )

                            Workspace.HARNESS -> HarnessScreen(
                                modifier = Modifier.fillMaxSize(),
                                health = health,
                                harnessUrl = harnessUrl,
                                onCloseWeb = onCloseHarness,
                                onStartAndOpen = {
                                    scope.launch {
                                        actionMessage = runtime.restartHarnessAndOpen().fold(
                                            onSuccess = { "正在启动 Harness，稍后会自动打开图形界面…"},
                                            onFailure = { it.message ?: "Harness 启动失败" },
                                        )
                                        delay(2_500)
                                        refresh()
                                    }
                                },
                                onOpenTermux = {
                                    actionMessage = runtime.openTermux().fold(
                                        onSuccess = { "已打开 Termux 日志" },
                                        onFailure = { it.message ?: "无法打开 Termux" },
                                    )
                                },
                                onPermission = onRequestPermission,
                                onShowFileChooser = onShowFileChooser,
                            )
                        }
                    }
                }
            }
        }
    }

    codex.approval?.let { approval ->
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Rounded.Security, null, tint = Amber) },
            title = { Text("需要你的确认") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(approval.reason, color = TextSecondary)
                    Text(
                        approval.command,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Ink).padding(12.dp),
                        color = TextPrimary,
                        fontSize = 12.sp,
                    )
                }
            },
            confirmButton = { Button(onClick = { codex.respondToApproval(true) }) { Text("允许一次") } },
            dismissButton = { TextButton(onClick = { codex.respondToApproval(false) }) { Text("拒绝") } },
            containerColor = PanelRaised,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppHeader(health: LiveRuntimeState, running: Boolean, onRefresh: () -> Unit, onToggle: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Violet), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Bolt, null, tint = Ink)
                }
                Spacer(Modifier.width(11.dp))
                Column {
                    Text("Codex 移动工作台", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(health.detail, color = if (running) Green else TextSecondary, fontSize = 10.sp, maxLines = 1)
                }
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                if (health.checking) CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp, color = Cyan)
                else Icon(Icons.Rounded.Refresh, "刷新状态", tint = TextSecondary)
            }
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (running) Color(0xFF2B2026) else Violet,
                    contentColor = if (running) Red else Ink,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Icon(if (running) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, null, Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(if (running) "停止" else "启动", fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
        },
    )
}

@Composable
private fun AppNavigation(selected: Workspace, onSelected: (Workspace) -> Unit) {
    NavigationBar(containerColor = Panel, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = selected == Workspace.CODEX,
            onClick = { onSelected(Workspace.CODEX) },
            icon = { Icon(Icons.Rounded.Code, null) },
            label = { Text("Codex") },
            colors = navColors(),
        )
        NavigationBarItem(
            selected = selected == Workspace.HARNESS,
            onClick = { onSelected(Workspace.HARNESS) },
            icon = { Icon(Icons.Rounded.Terminal, null) },
            label = { Text("DeepSeek Harness") },
            colors = navColors(),
        )
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Ink,
    selectedTextColor = TextPrimary,
    indicatorColor = Violet,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
)

@Composable
private fun CodexScreen(
    modifier: Modifier,
    health: LiveRuntimeState,
    client: CodexWebSocketClient,
    desktopUrl: String?,
    onCloseDesktop: () -> Unit,
    onStartDesktop: () -> Unit,
    onPermission: () -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
) {
    if (desktopUrl != null) {
        CodexDesktopWebScreen(modifier, desktopUrl, onCloseDesktop, onShowFileChooser)
        return
    }
    CodexDesktopLanding(modifier, health, onStartDesktop, onPermission)
}

@Composable
private fun CodexDesktopLanding(
    modifier: Modifier,
    health: LiveRuntimeState,
    onStartDesktop: () -> Unit,
    onPermission: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.background(Ink),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("Codex 开源工作台", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("基于成熟开源 GUI，手机上使用会话、文件、终端、审批和模型控制", color = TextSecondary, fontSize = 12.sp)
            }
        }
        item {
            RuntimeBanner(
                online = false,
                title = "Codex 工作台尚未打开",
                detail = "首次启动会在 Debian/ARM64 中下载 cdesktop 组件，约 50 MB。",
                action = if (!health.commandPermission) "授予权限" else "启动并打开",
                onAction = if (!health.commandPermission) onPermission else onStartDesktop,
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = PanelRaised), border = BorderStroke(1.dp, Line)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("已接入功能", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("· 多会话与历史记录\n· 模型和思考强度选择\n· 文件浏览、编辑、生成和差异查看\n· 终端、Git、审批和中断\n· 手机端响应式布局与附件上传", color = TextSecondary, fontSize = 12.sp, lineHeight = 19.sp)
                }
            }
        }
        item {
            Button(
                onClick = if (!health.commandPermission) onPermission else onStartDesktop,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Violet, contentColor = Ink),
            ) {
                Icon(Icons.Rounded.OpenInNew, null, Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(if (!health.commandPermission) "授予 Termux 权限" else "启动 Codex 工作台")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CodexDesktopWebScreen(
    modifier: Modifier,
    url: String,
    onClose: () -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
) {
    Column(modifier.background(Ink)) {
        Row(Modifier.fillMaxWidth().background(Panel).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Code, null, tint = Violet, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text("Codex 开源工作台", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("返回控制台") }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(android.graphics.Color.rgb(9, 12, 18))
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    isVerticalScrollBarEnabled = true
                    overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
                    setOnTouchListener { view, _ ->
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, pageUrl: String?) {
                            super.onPageFinished(view, pageUrl)
                            view.evaluateJavascript(HarnessViewportFix, null)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(webView: WebView?, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean =
                            onShowFileChooser(callback, params)
                    }
                    tag = url
                    doOnLayout { view -> (view as? WebView)?.loadUrl(url) }
                }
            },
            update = { view ->
                if (view.tag != url && view.width > 0 && view.height > 0) {
                    view.tag = url
                    view.loadUrl(url)
                }
            },
        )
    }
}

@Composable
private fun CodexNativeScreen(
    modifier: Modifier,
    health: LiveRuntimeState,
    client: CodexWebSocketClient,
    onStart: () -> Unit,
    onPermission: () -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    Column(modifier.background(Ink)) {
        RuntimeBanner(
            online = client.connected,
            title = if (client.connected) "Codex 已连接" else "Codex 尚未连接",
            detail = client.statusText,
            action = when {
                !health.commandPermission -> "授予权限"
                health.codexOnline -> "重新连接"
                else -> "启动 Codex"
            },
            onAction = when {
                !health.commandPermission -> onPermission
                health.codexOnline -> client::connect
                else -> onStart
            },
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("原生 Codex 对话", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("对接手机内 Codex app-server", color = TextSecondary, fontSize = 11.sp)
            }
            OutlinedButton(onClick = client::newConversation, border = BorderStroke(1.dp, Line)) {
                Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("新对话", fontSize = 11.sp)
            }
            Spacer(Modifier.width(7.dp))
            OutlinedButton(onClick = { showSettings = true }, border = BorderStroke(1.dp, Line)) {
                Icon(Icons.Rounded.Settings, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("配置", fontSize = 11.sp)
            }
            Spacer(Modifier.width(5.dp))
            OutlinedButton(onClick = { showHistory = true; client.loadThreads() }, border = BorderStroke(1.dp, Line)) {
                Text("历史", fontSize = 11.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            WorkflowButton("检查工程") { draft = "请检查当前工作区的项目结构、关键配置和未提交改动，先给出简明报告。" }
            WorkflowButton("修改文件") { draft = "请先定位需要修改的文件，说明计划后直接完成修改并总结变更。"}
            WorkflowButton("生成文件") { draft = "请根据我的需求创建所需文件，写入完整内容并验证生成结果。"}
        }
        Text(
            "${if (client.settings.sandboxMode == "workspaceWrite") "工作区可读写" else "只读模式"} · ${client.settings.model} · 思考 ${client.settings.effort}",
            color = if (client.settings.sandboxMode == "workspaceWrite") Green else Amber,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
        )
        HorizontalDivider(color = Line)
        if (client.messages.isEmpty()) EmptyConversation(Modifier.weight(1f)) else {
            val messageListState = rememberLazyListState()
            LaunchedEffect(client.messages.size, client.busy) {
                if (client.messages.isNotEmpty()) {
                    messageListState.animateScrollToItem(client.messages.lastIndex)
                }
            }
            LazyColumn(
                state = messageListState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(client.messages) { MessageBubble(it) }
                if (client.busy && client.messages.lastOrNull()?.pending != true) item { WorkingIndicator() }
            }
        }
        Composer(
            value = draft,
            enabled = client.connected && !client.busy,
            busy = client.busy,
            onValueChange = { draft = it },
            onStop = client::interrupt,
            onSend = {
                if (draft.isNotBlank()) {
                    client.sendUserMessage(draft)
                    draft = ""
                }
            },
        )
    }
    if (showSettings) {
        CodexSettingsDialog(
            settings = client.settings,
            onDismiss = { showSettings = false },
            onSave = { next -> client.updateSettings(next); showSettings = false },
        )
    }
    if (showHistory) {
        CodexHistoryDialog(
            client = client,
            onDismiss = { showHistory = false },
            onResume = { thread -> client.resumeThread(thread); showHistory = false },
        )
    }
}

@Composable
private fun RowScope.WorkflowButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, Line),
        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
        modifier = Modifier.weight(1f),
    ) { Text(label, fontSize = 10.sp, maxLines = 1) }
}

@Composable
private fun CodexSettingsDialog(
    settings: CodexRunSettings,
    onDismiss: () -> Unit,
    onSave: (CodexRunSettings) -> Unit,
) {
    var draft by remember(settings) { mutableStateOf(settings) }
    var modelMenu by remember { mutableStateOf(false) }
    var effortMenu by remember { mutableStateOf(false) }
    var sandboxMenu by remember { mutableStateOf(false) }
    var approvalMenu by remember { mutableStateOf(false) }
    val models = listOf(
        "gpt-5.6-terra" to "均衡编码（推荐）",
        "gpt-5.6-sol" to "高质量工作流",
        "gpt-5.6-luna" to "快速响应",
        "gpt-5.5" to "稳定兼容",
        "gpt-5.3-codex" to "Codex 专用模型",
    )
    val efforts = listOf("low" to "低：更快", "medium" to "中：均衡", "high" to "高：更深入", "xhigh" to "极高：最充分")
    val sandboxes = listOf("workspaceWrite" to "工作区可读写", "readOnly" to "只读保护")
    val approvals = listOf("on-request" to "需要时询问", "never" to "从不询问")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Codex 运行配置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("模型", color = TextSecondary, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { modelMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(models.firstOrNull { it.first == draft.model }?.second ?: draft.model, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        Text(draft.model, fontSize = 10.sp, color = TextSecondary)
                    }
                    DropdownMenu(expanded = modelMenu, onDismissRequest = { modelMenu = false }, containerColor = PanelRaised, border = BorderStroke(1.dp, Line)) {
                        models.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Column { Text(label, color = TextPrimary); Text(value, fontSize = 10.sp, color = TextSecondary) } },
                                onClick = { draft = draft.copy(model = value); modelMenu = false },
                            )
                        }
                    }
                }
                Text("思考强度", color = TextSecondary, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { effortMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(efforts.firstOrNull { it.first == draft.effort }?.second ?: draft.effort, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        Text(draft.effort, fontSize = 10.sp, color = TextSecondary)
                    }
                    DropdownMenu(expanded = effortMenu, onDismissRequest = { effortMenu = false }, containerColor = PanelRaised, border = BorderStroke(1.dp, Line)) {
                        efforts.forEach { (value, label) -> DropdownMenuItem(text = { Text(label, color = TextPrimary) }, onClick = { draft = draft.copy(effort = value); effortMenu = false }) }
                    }
                }
                Text("文件权限", color = TextSecondary, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { sandboxMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(sandboxes.firstOrNull { it.first == draft.sandboxMode }?.second ?: draft.sandboxMode, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        Text("文件", fontSize = 10.sp, color = TextSecondary)
                    }
                    DropdownMenu(expanded = sandboxMenu, onDismissRequest = { sandboxMenu = false }, containerColor = PanelRaised, border = BorderStroke(1.dp, Line)) {
                        sandboxes.forEach { (value, label) -> DropdownMenuItem(text = { Text(label, color = TextPrimary) }, onClick = { draft = draft.copy(sandboxMode = value); sandboxMenu = false }) }
                    }
                }
                Text("命令审批", color = TextSecondary, fontSize = 12.sp)
                Box {
                    OutlinedButton(onClick = { approvalMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(approvals.firstOrNull { it.first == draft.approvalPolicy }?.second ?: draft.approvalPolicy, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        Text("安全", fontSize = 10.sp, color = TextSecondary)
                    }
                    DropdownMenu(expanded = approvalMenu, onDismissRequest = { approvalMenu = false }, containerColor = PanelRaised, border = BorderStroke(1.dp, Line)) {
                        approvals.forEach { (value, label) -> DropdownMenuItem(text = { Text(label, color = TextPrimary) }, onClick = { draft = draft.copy(approvalPolicy = value); approvalMenu = false }) }
                    }
                }
                OutlinedTextField(
                    value = draft.cwd,
                    onValueChange = { draft = draft.copy(cwd = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("工作目录（可选）") },
                    placeholder = { Text("留空使用 Codex 默认工程目录") },
                    singleLine = true,
                )
                Text(
                    if (draft.sandboxMode == "workspaceWrite") "工作区可读取、修改和生成文件；命令执行按审批策略处理。"
                    else "只读模式：可分析文件，但不会写入工程。",
                    color = if (draft.sandboxMode == "workspaceWrite") Green else Amber,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }
        },
        confirmButton = { Button(onClick = { onSave(draft) }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        containerColor = PanelRaised,
    )
}

@Composable
private fun CodexHistoryDialog(
    client: CodexWebSocketClient,
    onDismiss: () -> Unit,
    onResume: (app.codexharness.mobile.runtime.CodexThreadSummary) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("历史对话", modifier = Modifier.weight(1f))
                TextButton(onClick = client::loadThreads, enabled = !client.loadingThreads) {
                    if (client.loadingThreads) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Rounded.Refresh, "刷新历史", Modifier.size(17.dp))
                }
            }
        },
        text = {
            if (client.threads.isEmpty() && !client.loadingThreads) {
                Text("暂无已保存对话。完成一次 Codex 对话后，这里会显示可继续的任务。", color = TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (client.loadingThreads) item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = Violet, strokeWidth = 2.dp)
                        }
                    }
                    items(client.threads, key = { it.id }) { thread ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onResume(thread) },
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            border = BorderStroke(1.dp, Line),
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(thread.title, color = TextPrimary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(thread.preview, color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        containerColor = PanelRaised,
    )
}

@Composable
private fun EmptyConversation(modifier: Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Color(0x1F9887FF)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Code, null, tint = Violet, modifier = Modifier.size(30.dp))
            }
            Text("在手机上使用 Codex", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("可以阅读工程、修改文件、运行命令并处理审批", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MessageBubble(message: CodexUiMessage) {
    val user = message.role == "user"
    val system = message.role == "system"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(18.dp))
                .background(if (user) Color(0xFF282446) else if (system) Color(0xFF3B232A) else PanelRaised)
                .border(1.dp, if (system) Color(0xFF6B3440) else Line, RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Text(
                if (user) "你" else if (system) "系统" else "Codex",
                color = if (user) Violet else if (system) Red else Cyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(message.text, color = TextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
            if (message.pending) {
                Spacer(Modifier.height(8.dp))
                Text("正在生成…", color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun WorkingIndicator() {
    Row(
        Modifier.clip(RoundedCornerShape(14.dp)).background(PanelRaised).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(15.dp), color = Violet, strokeWidth = 2.dp)
        Spacer(Modifier.width(8.dp))
        Text("Codex 正在思考和执行…", color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun Composer(
    value: String,
    enabled: Boolean,
    busy: Boolean,
    onValueChange: (String) -> Unit,
    onStop: () -> Unit,
    onSend: () -> Unit,
) {
    val scale by animateFloatAsState(if (enabled && value.isNotBlank()) 1f else 0.92f, label = "发送按钮")
    Row(
        Modifier.fillMaxWidth().background(Panel).padding(12.dp).windowInsetsPadding(WindowInsets.navigationBars).imePadding(),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            placeholder = { Text(if (busy) "Codex 正在处理，可点击停止" else if (enabled) "告诉 Codex 你想完成什么…" else "启动 Codex 后即可输入", fontSize = 12.sp) },
            minLines = 1,
            maxLines = 5,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Violet,
                unfocusedBorderColor = Line,
                focusedContainerColor = PanelRaised,
                unfocusedContainerColor = PanelRaised,
                cursorColor = Violet,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledContainerColor = PanelRaised,
                disabledBorderColor = Line,
            ),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = if (busy) onStop else onSend,
            enabled = busy || (enabled && value.isNotBlank()),
            modifier = Modifier.size(52.dp).scale(scale).clip(CircleShape).background(if (busy) Red else if (enabled && value.isNotBlank()) Violet else Line),
        ) {
            if (busy) Icon(Icons.Rounded.Stop, "停止", tint = Ink)
            else Icon(Icons.AutoMirrored.Rounded.Send, "发送", tint = if (enabled && value.isNotBlank()) Ink else TextSecondary)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HarnessScreen(
    modifier: Modifier,
    health: LiveRuntimeState,
    harnessUrl: String?,
    onCloseWeb: () -> Unit,
    onStartAndOpen: () -> Unit,
    onOpenTermux: () -> Unit,
    onPermission: () -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
) {
    val url = harnessUrl
    if (url != null) {
        Column(modifier.background(Ink)) {
            Row(Modifier.fillMaxWidth().background(Panel).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Verified, null, tint = Green, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                Text("DeepSeek Harness 本地界面", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onCloseWeb) { Text("返回控制台") }
            }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(android.graphics.Color.rgb(9, 12, 18))
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        isVerticalScrollBarEnabled = true
                        overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
                        setOnTouchListener { view, event ->
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                            false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, pageUrl: String?) {
                                super.onPageFinished(view, pageUrl)
                                view.evaluateJavascript(HarnessViewportFix, null)
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: android.webkit.ConsoleMessage): Boolean {
                                Log.i(
                                    "HarnessWebView",
                                    "${msg.messageLevel()} ${msg.sourceId()}:${msg.lineNumber()} ${msg.message()}",
                                )
                                return false
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>,
                                fileChooserParams: FileChooserParams,
                            ): Boolean {
                                return onShowFileChooser(filePathCallback, fileChooserParams)
                            }
                        }
                        tag = url
                        doOnLayout { laidOutView ->
                            (laidOutView as? WebView)?.let { webView ->
                                if (webView.url == null) webView.loadUrl(url)
                            }
                        }
                    }
                },
                update = { view ->
                    if (view.tag != url && view.width > 0 && view.height > 0) {
                        view.tag = url
                        view.loadUrl(url)
                    }
                },
            )
        }
    } else {
        HarnessDashboard(modifier, health, onStartAndOpen, onOpenTermux, onPermission)
    }
}

@Composable
private fun HarnessDashboard(
    modifier: Modifier,
    health: LiveRuntimeState,
    onStartAndOpen: () -> Unit,
    onOpenTermux: () -> Unit,
    onPermission: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.background(Ink),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("DeepSeek Harness", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("本地智能体、模型、插件与会话的图形工作台", color = TextSecondary, fontSize = 12.sp)
            }
        }
        item {
            RuntimeBanner(
                online = health.harnessOnline,
                title = if (health.harnessOnline) "Harness 服务运行中" else "Harness 服务未启动",
                detail = if (health.harnessOnline) "本机地址 127.0.0.1:3080" else "点击按钮后会在 App 内自动打开",
                action = if (!health.commandPermission) "授予权限" else if (health.harnessOnline) "重新打开" else "启动并打开",
                onAction = if (!health.commandPermission) onPermission else onStartAndOpen,
            )
        }
        item { Text("手机环境", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = PanelRaised), border = BorderStroke(1.dp, Line)) {
                StatusRow(Icons.Rounded.Terminal, "Termux", if (health.termuxInstalled) "已安装" else "未安装", health.termuxInstalled)
                HorizontalDivider(color = Line)
                StatusRow(Icons.Rounded.Memory, "Debian / ARM64", "Proot 本地环境", health.termuxInstalled)
                HorizontalDivider(color = Line)
                StatusRow(Icons.Rounded.Code, "Codex app-server", if (health.codexOnline) "运行中" else "已停止", health.codexOnline)
                HorizontalDivider(color = Line)
                StatusRow(Icons.Rounded.Language, "DeepSeek Harness", if (health.harnessOnline) "运行中" else "已停止", health.harnessOnline)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF121A21)), border = BorderStroke(1.dp, Color(0xFF27404A))) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Security, null, tint = Cyan)
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("仅本机访问", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Codex 与 Harness 只监听 127.0.0.1，不向局域网或互联网开放。", color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = if (!health.commandPermission) onPermission else onStartAndOpen,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Violet, contentColor = Ink),
                ) {
                    Icon(Icons.Rounded.OpenInNew, null, Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (!health.commandPermission) "授予权限" else "启动并打开")
                }
                OutlinedButton(onClick = onOpenTermux, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Line)) {
                    Icon(Icons.Rounded.Terminal, null, Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("终端日志")
                }
            }
        }
        item {
            Text(
                "首次进入 Harness 后，请在“设置 → 模型”中填写 DeepSeek API Key，并选择工作目录。密钥保存在手机本地的 Harness 配置中。",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun RuntimeBanner(online: Boolean, title: String, detail: String, action: String, onAction: () -> Unit) {
    val tint by animateColorAsState(if (online) Green else Amber, animationSpec = tween(250), label = "状态颜色")
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (online) Color(0xFF10231D) else Color(0xFF241E14)),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(detail, color = TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            TextButton(onClick = onAction) { Text(action, color = tint, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun StatusRow(icon: ImageVector, title: String, detail: String, online: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (online) Cyan else TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(11.dp))
        Text(title, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Icon(if (online) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline, null, tint = if (online) Green else TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(detail, color = if (online) Green else TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun InlineNotice(text: String, onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF1B2230)).padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Link, null, tint = Cyan, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) { Icon(Icons.Rounded.MoreVert, "关闭", tint = TextSecondary) }
    }
}

@Composable
private fun mobileColors() = MaterialTheme.colorScheme.copy(
    primary = Violet,
    onPrimary = Ink,
    background = Ink,
    onBackground = TextPrimary,
    surface = Panel,
    onSurface = TextPrimary,
    surfaceVariant = PanelRaised,
    onSurfaceVariant = TextSecondary,
    outline = Line,
    error = Red,
)
