package app.codexharness.mobile

import android.annotation.SuppressLint
import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import android.app.DownloadManager
import android.webkit.WebChromeClient
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.codexharness.mobile.runtime.CodexUiMessage
import app.codexharness.mobile.runtime.CodexWebSocketClient
import app.codexharness.mobile.runtime.CodexRunSettings
import app.codexharness.mobile.runtime.CodexWebUiBridgeState
import app.codexharness.mobile.runtime.HarnessBridgeState
import app.codexharness.mobile.runtime.DiagnosticProbe
import app.codexharness.mobile.runtime.LiveRuntimeState
import app.codexharness.mobile.runtime.RuntimeDiagnostics
import app.codexharness.mobile.runtime.TermuxCommandState
import app.codexharness.mobile.runtime.TermuxRuntimeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt
import android.webkit.ValueCallback
import org.json.JSONObject

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

@Composable
private fun mobileDialogMaxHeight(reservedDp: Int = 180): Dp {
    val availableHeight = LocalConfiguration.current.screenHeightDp - reservedDp
    return availableHeight.coerceAtLeast(180).dp
}

private val HarnessViewportFix = """
    (() => {
      if (window.__codexHarnessMobileLayoutInstalled) {
        window.__codexHarnessViewportFix?.();
        return;
      }
      window.__codexHarnessMobileLayoutInstalled = true;
      const styleId = 'codex-harness-mobile-layout';
      const mobileStyleText = `
        :root { --codex-harness-vh: 100vh; }
        html, body, #root {
          width: 100% !important;
          height: var(--codex-harness-vh) !important;
          min-height: 0 !important;
          /* Keep the browser's native long-press/selection gesture recognizer
           * active. Scrollable menus and dialog panes get pan-y below. */
          touch-action: auto !important;
          -webkit-user-select: text !important;
          user-select: text !important;
          -webkit-touch-callout: default !important;
        }
        body { overflow: hidden !important; }
        /* DSH marks message bubbles as non-selectable on desktop.  On a phone
         * that removes the only practical way to copy a question or answer.
         * Restore selection for content, while keeping actual controls from
         * showing selection handles when they are tapped. */
        body * {
          -webkit-user-select: text !important;
          user-select: text !important;
          -webkit-touch-callout: default !important;
        }
        button, [role="button"], [role="menuitem"], [role="option"],
        input[type="button"], input[type="submit"], input[type="reset"] {
          -webkit-user-select: none !important;
          user-select: none !important;
          -webkit-touch-callout: none !important;
        }
        [data-slot], [class*="scroll"], [class*="content"], [role="dialog"] {
          -webkit-overflow-scrolling: touch;
        }
        /*
         * DeepSeek's popup menus use max-height: min(360px, 100vh - 96px).
         * Android WebView can resolve that 100vh expression to zero after the
         * host applies the mobile visual-viewport height to html/body/#root.
         * Keep the portal mounted in body, but give menus a real mobile
         * viewport bound so their children are not clipped to a zero-height
         * box.  The same rule covers the model, permission, command and
         * overflow menus, including menus opened from another menu.
         */
        [role="menu"], [role="listbox"] {
          height: auto !important;
          max-height: min(360px, calc(var(--codex-harness-vh) - 96px)) !important;
          overflow-y: auto !important;
          overscroll-behavior-y: contain !important;
          touch-action: pan-y !important;
          -webkit-overflow-scrolling: touch !important;
          z-index: 10000 !important;
        }
        /* The question/action picker is rendered through a Radix-style portal
         * and does not always carry role=menu.  Give both the portal wrapper
         * and its content the same real mobile bounds, otherwise WebView can
         * resolve the available-height variable to zero and the choices exist
         * in the DOM but cannot be seen or touched. */
        [data-radix-popper-content-wrapper],
        [data-radix-menu-content], [data-radix-select-content],
        [data-radix-popover-content],
        [data-slot="dropdown-menu-content"], [data-slot="select-content"],
        [data-slot="popover-content"] {
          box-sizing: border-box !important;
          height: auto !important;
          min-height: 0 !important;
          max-width: calc(100vw - 24px) !important;
          max-height: min(360px, calc(var(--codex-harness-vh) - 96px)) !important;
          overflow-y: auto !important;
          overscroll-behavior-y: contain !important;
          touch-action: pan-y !important;
          -webkit-overflow-scrolling: touch !important;
          z-index: 10001 !important;
        }
        /* dsh's ask_user_question card renders the model's question verbatim
         * in a non-shrinking heading.  A long Chinese question can therefore
         * consume the whole capped card, leaving the option list and submit
         * footer at zero height.  Keep the heading scrollable so every choice
         * remains reachable on a phone. */
        [class*="headingBlock"] {
          min-width: 0 !important;
          max-height: min(180px, calc(var(--codex-harness-vh) - 96px)) !important;
          overflow-y: auto !important;
          overscroll-behavior-y: contain !important;
          flex: 0 1 auto !important;
        }
        [class*="headingBlock"] [class*="title"] {
          overflow-wrap: anywhere !important;
          word-break: break-word !important;
        }
        /* The DSH stylesheet expresses this card's cap as 60vh.  On the
         * Android WebView used here that external vh expression can resolve
         * to 0 even though the measured visual viewport is valid.  The result
         * is a live question card whose section is only its 10px bottom
         * padding: the options and footer exist in the DOM, but are below the
         * viewport and cannot receive a tap.  Scope the replacement to the
         * composer so ordinary message cards keep their native sizing. */
        [class*="composerSeat"] [class*="_card"] {
          height: auto !important;
          max-height: min(520px, calc(var(--codex-harness-vh) - 24px)) !important;
          min-height: 0 !important;
        }
        [class*="composerSeat"] [class*="_body"] {
          min-height: 0 !important;
        }
        [class*="composerSeat"] [class*="_options"] {
          min-height: 0 !important;
          overflow-y: auto !important;
          overscroll-behavior-y: contain !important;
          touch-action: pan-y !important;
        }
        @media (max-width: 560px) {
          [role="dialog"] {
            box-sizing: border-box !important;
            width: calc(100vw - 24px) !important;
            max-width: calc(100vw - 24px) !important;
            height: calc(var(--codex-harness-vh) - 24px) !important;
            max-height: calc(var(--codex-harness-vh) - 24px) !important;
            min-height: 0 !important;
            display: flex !important;
            flex-direction: column !important;
            overflow: hidden !important;
          }
          [role="dialog"] > nav {
            box-sizing: border-box !important;
            width: 100% !important;
            height: auto !important;
            min-height: 0 !important;
            flex: 0 0 auto !important;
            padding: 8px 10px !important;
            overflow-x: auto !important;
            overflow-y: hidden !important;
            touch-action: pan-x !important;
          }
          [role="dialog"] > nav > div:first-child { display: none !important; }
          [role="dialog"] > nav > div:last-child,
          [role="dialog"] [class*="VOzbGW_navList"] {
            width: 100% !important;
            min-width: 0 !important;
            flex-direction: row !important;
            gap: 3px !important;
          }
          [role="dialog"] > nav button,
          [role="dialog"] [class*="VOzbGW_navCell"] {
            flex: 1 1 0 !important;
            min-width: 0 !important;
            padding: 7px 3px !important;
            white-space: nowrap !important;
          }
          [role="dialog"] > nav button svg { display: none !important; }
          [role="dialog"] > nav button span {
            max-width: 100% !important;
            overflow: hidden !important;
            text-overflow: ellipsis !important;
            font-size: 12px !important;
            text-align: center !important;
          }
          [role="dialog"] > nav + div,
          [role="dialog"] [class*="VOzbGW_content"] {
            box-sizing: border-box !important;
            width: 100% !important;
            min-width: 0 !important;
            min-height: 0 !important;
            max-height: none !important;
            flex: 1 1 auto !important;
            display: flex !important;
            flex-direction: column !important;
            overflow: hidden !important;
          }
          [role="dialog"] [class*="VOzbGW_header"] {
            flex: 0 0 auto !important;
          }
          [role="dialog"] [class*="VOzbGW_options"],
          [role="dialog"] [class*="overflow-y-auto"],
          [role="dialog"] [class*="overflow-auto"],
          [role="dialog"] [data-radix-scroll-area-viewport] {
            box-sizing: border-box !important;
            width: 100% !important;
            min-height: 0 !important;
            height: auto !important;
            max-height: none !important;
            flex: 1 1 auto !important;
            overflow-y: auto !important;
            overscroll-behavior-y: contain !important;
            touch-action: pan-y !important;
          }
        }
      `;
      const install = () => {
        if (!document.getElementById(styleId)) {
          const style = document.createElement('style');
          style.id = styleId;
          style.textContent = mobileStyleText;
          document.head.appendChild(style);
        }
        const height = Math.max(
          (window.visualViewport && window.visualViewport.height) || window.innerHeight || 1,
          1,
        );
        document.documentElement.style.setProperty('--codex-harness-vh', height + 'px');
      };
      // DSH updates its message tree frequently while a response is streaming.
      // Coalesce those mutations into one viewport/style pass per frame instead
      // of recalculating layout for every individual DOM node.
      let frameId = 0;
      const scheduleInstall = () => {
        if (frameId !== 0) return;
        frameId = window.requestAnimationFrame(() => {
          frameId = 0;
          install();
        });
      };
      window.__codexHarnessViewportFix = scheduleInstall;
      window.addEventListener('resize', scheduleInstall, { passive: true });
      if (window.visualViewport) window.visualViewport.addEventListener('resize', scheduleInstall, { passive: true });
      new MutationObserver(scheduleInstall).observe(document.documentElement, { childList: true, subtree: true });
      scheduleInstall();
    })();
""".trimIndent()

/**
 * The Linux WebUI protects its REST and Socket.IO endpoints with a local JWT.
 * The mobile shell receives the device-local API key from the Termux callback,
 * then performs the same login flow as the WebUI's own login page.  The key
 * never leaves the loopback WebView and is never written to Android logs.
 */
private fun codexWebUiAutoLoginScript(apiKey: String): String {
    val encodedKey = JSONObject.quote(apiKey)
    return """
        (() => {
          const apiKey = $encodedKey;
          const storageKey = 'codex.webui.jwt';
          if (!apiKey || sessionStorage.getItem(storageKey) || window.__codexHarnessAutoLoginStarted) return;
          window.__codexHarnessAutoLoginStarted = true;
          fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ apiKey }),
          })
            .then((response) => response.ok ? response.json() : Promise.reject(new Error('WebUI login failed')))
            .then((data) => {
              if (!data || typeof data.accessToken !== 'string') throw new Error('WebUI login response missing token');
              sessionStorage.setItem(storageKey, data.accessToken);
              window.location.replace('/');
            })
            .catch(() => { window.__codexHarnessAutoLoginStarted = false; });
        })();
    """.trimIndent()
}

private enum class Workspace { CODEX, HARNESS }

private data class WebDownload(
    val url: String,
    val userAgent: String?,
    val contentDisposition: String?,
    val mimeType: String?,
)

private data class UploadInfo(
    val displayName: String?,
    val sizeBytes: Long?,
)

private data class UploadCacheInfo(
    val fileCount: Int = 0,
    val totalBytes: Long = 0L,
    val staleFileCount: Int = 0,
    val staleBytes: Long = 0L,
)

private data class UploadCacheCleanupResult(
    val removedFileCount: Int = 0,
    val removedBytes: Long = 0L,
)

private const val UploadCacheMaxAgeMs = 24L * 60L * 60L * 1000L

private fun formatByteCount(bytes: Long?): String {
    if (bytes == null || bytes < 0L) return "大小未知"
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
        bytes < 1024L * 1024L * 1024L -> "${bytes / (1024L * 1024L)} MB"
        else -> "${bytes / (1024L * 1024L * 1024L)} GB"
    }
}

private fun readUploadCacheInfo(cacheDir: File, now: Long = System.currentTimeMillis()): UploadCacheInfo {
    val directory = File(cacheDir, "webview-upload")
    val cutoff = now - UploadCacheMaxAgeMs
    var fileCount = 0
    var totalBytes = 0L
    var staleFileCount = 0
    var staleBytes = 0L
    directory.listFiles()?.forEach { file ->
        if (!file.isFile) return@forEach
        val bytes = file.length().coerceAtLeast(0L)
        fileCount++
        totalBytes += bytes
        if (file.lastModified() in 1 until cutoff) {
            staleFileCount++
            staleBytes += bytes
        }
    }
    return UploadCacheInfo(fileCount, totalBytes, staleFileCount, staleBytes)
}

private fun clearStaleUploadCache(cacheDir: File, now: Long = System.currentTimeMillis()): UploadCacheCleanupResult {
    val directory = File(cacheDir, "webview-upload")
    val cutoff = now - UploadCacheMaxAgeMs
    var removedFileCount = 0
    var removedBytes = 0L
    directory.listFiles()?.forEach { file ->
        if (!file.isFile || file.lastModified() !in 1 until cutoff) return@forEach
        val bytes = file.length().coerceAtLeast(0L)
        if (file.delete()) {
            removedFileCount++
            removedBytes += bytes
        } else {
            Log.d("MainActivity", "unable to remove stale upload ${file.name}")
        }
    }
    return UploadCacheCleanupResult(removedFileCount, removedBytes)
}

private fun redactSensitiveText(value: String): String {
    return value
        .replace(
            Regex("(?i)([?&#](?:token|access_token|auth|authorization)=)[^&#\\s]+"),
            "$1<已隐藏>",
        )
        .replace(
            Regex("(?i)((?:token|access_token|authorization)\\s*[=:])[^&\\s]+"),
            "$1<已隐藏>",
        )
}

private fun copyWebErrorDetails(context: Context, title: String, message: String, pageUrl: String?) {
    val text = buildString {
        appendLine(redactSensitiveText(title))
        appendLine(redactSensitiveText(message))
        append("页面地址：")
        append(redactSensitiveText(pageUrl ?: "未知"))
    }
    context.getSystemService(ClipboardManager::class.java)
        ?.setPrimaryClip(ClipData.newPlainText("网页错误详情", text))
    Toast.makeText(context, "错误详情已复制", Toast.LENGTH_SHORT).show()
}

class MainActivity : ComponentActivity() {
    private lateinit var runtime: TermuxRuntimeManager
    private val codex by lazy { CodexWebSocketClient(applicationContext) }
    private var notificationPermissionGranted by mutableStateOf(false)
    private var shortcutRequest by mutableStateOf<String?>(null)
    private var sharedTextRequest by mutableStateOf<String?>(null)
    private var pendingFileCallback: ValueCallback<Array<Uri>>? = null
    private var activeUploadCallback: ValueCallback<Array<Uri>>? = null
    private var uploadJob: Job? = null
    private var uploadStatus by mutableStateOf<String?>(null)
    private var uploadRequestId = 0
    private var pendingCaptureUri: Uri? = null
    private var pendingCaptureFile: File? = null
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val callback = pendingFileCallback
        pendingFileCallback = null
        val data = result.data
        val captureUri = pendingCaptureUri
        val captureFile = pendingCaptureFile
        pendingCaptureUri = null
        pendingCaptureFile = null
        val selected = parseFileChooserResult(result.resultCode, data).ifEmpty {
            if (
                result.resultCode == Activity.RESULT_OK &&
                captureUri != null &&
                captureFile?.isFile == true &&
                captureFile.length() > 0L
            ) {
                listOf(captureUri)
            } else {
                emptyList()
            }
        }
        if (callback == null) {
            captureFile?.let { runCatching { it.delete() } }
            return@registerForActivityResult
        }
        if (selected.isEmpty()) {
            // Cancelling (or an unparsable result) must still answer the WebView, or
            // the page's file input stays disabled until it is reloaded.
            uploadRequestId++
            uploadJob?.cancel()
            uploadJob = null
            if (activeUploadCallback === callback) activeUploadCallback = null
            uploadStatus = null
            captureFile?.let { runCatching { it.delete() } }
            callback.onReceiveValue(null)
            return@registerForActivityResult
        }

        // The picker may return a provider URI whose temporary grant belongs only to
        // the Activity. Copying it into our cache gives the WebView renderer a stable,
        // readable URI even on MIUI's file provider and after the picker closes.
        val resultFlags = data?.flags ?: 0
        uploadJob?.cancel()
        uploadJob = null
        val requestId = ++uploadRequestId
        activeUploadCallback = callback
        uploadStatus = "正在准备 ${selected.size} 个附件（${formatByteCount(selected.mapNotNull { queryUploadInfo(it).sizeBytes }.sum().takeIf { it > 0L })}）…"
        uploadJob = lifecycleScope.launch {
            try {
                val uploadUris = withContext(Dispatchers.IO) {
                    selected.mapIndexed { index, source ->
                        val info = queryUploadInfo(source)
                        withContext(Dispatchers.Main.immediate) {
                            if (requestId == uploadRequestId) {
                                uploadStatus = "正在准备附件 ${index + 1}/${selected.size}：${info.displayName ?: "未命名文件"}（0/${formatByteCount(info.sizeBytes)}）"
                            }
                        }
                        var lastReportedAt = 0L
                        stageUploadUri(source, resultFlags) { copiedBytes, totalBytes ->
                            val now = System.currentTimeMillis()
                            if (copiedBytes == totalBytes || now - lastReportedAt >= 150L) {
                                lastReportedAt = now
                                withContext(Dispatchers.Main.immediate) {
                                    if (requestId == uploadRequestId) {
                                        uploadStatus = "正在准备附件 ${index + 1}/${selected.size}：${info.displayName ?: "未命名文件"}（${formatByteCount(copiedBytes)}/${formatByteCount(totalBytes)}）"
                                    }
                                }
                            }
                        }
                    }.toTypedArray()
                }
                if (requestId != uploadRequestId) return@launch
                Log.i(TAG, "file chooser returned ${uploadUris.size} readable URI(s): ${uploadUris.joinToString()}")
                callback.onReceiveValue(uploadUris)
                if (activeUploadCallback === callback) activeUploadCallback = null
                if (requestId == uploadRequestId) {
                    uploadStatus = "附件已准备，可以在 Harness 中继续发送"
                    delay(2_500)
                    if (requestId == uploadRequestId) uploadStatus = null
                }
            } catch (error: Exception) {
                if (requestId != uploadRequestId) return@launch
                Log.w(TAG, "file chooser staging failed", error)
                callback.onReceiveValue(null)
                if (activeUploadCallback === callback) activeUploadCallback = null
                if (requestId == uploadRequestId) {
                    uploadStatus = "附件准备失败：${error.message ?: "无法读取文件"}"
                }
            } finally {
                captureFile?.let { runCatching { it.delete() } }
                if (requestId == uploadRequestId) uploadJob = null
            }
        }
    }
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationPermissionGranted = granted
    }
    private val voiceInputLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return@registerForActivityResult
        val draft = getSharedPreferences("codex_mobile_settings", MODE_PRIVATE)
            .getString("composer_draft", "")
            .orEmpty()
            .trim()
        sharedTextRequest = if (draft.isBlank()) spoken else "$draft\n$spoken"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        // Lets us inspect the embedded Harness/Codex WebUI pages over chrome://inspect
        // (adb forward tcp:9222 localabstract:webview_devtools_remote_<pid>).
        WebView.setWebContentsDebuggingEnabled(true)
        runtime = TermuxRuntimeManager(applicationContext)
        cleanupStagedUploads()
        acceptAppIntent(intent)
        if (ActivityCompat.checkSelfPermission(this, TermuxRuntimeManager.RUN_COMMAND_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(TermuxRuntimeManager.RUN_COMMAND_PERMISSION), 1201)
        }
        setContent {
            MobileWorkbench(
                runtime = runtime,
                codex = codex,
                harnessUrl = HarnessBridgeState.url,
                onCloseHarness = { HarnessBridgeState.url = null },
                codexWebUiUrl = CodexWebUiBridgeState.url,
                codexWebUiApiKey = CodexWebUiBridgeState.apiKey,
                onCloseCodexWebUi = { CodexWebUiBridgeState.url = null },
                shortcutRequest = shortcutRequest,
                onShortcutConsumed = { shortcutRequest = null },
                sharedTextRequest = sharedTextRequest,
                onSharedTextConsumed = { sharedTextRequest = null },
                onStartVoiceInput = ::startVoiceInput,
                onRequestPermission = {
                    ActivityCompat.requestPermissions(this, arrayOf(TermuxRuntimeManager.RUN_COMMAND_PERMISSION), 1201)
                },
                onShowFileChooser = ::showFileChooser,
                onDownload = ::handleWebDownload,
                uploadStatus = uploadStatus,
                onDismissUploadStatus = ::dismissUploadStatus,
                notificationPermissionGranted = notificationPermissionGranted,
                onRequestNotificationPermission = ::requestNotificationPermission,
                onNotifyBackgroundCompletion = ::notifyCodexCompletion,
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationPermissionGranted = true
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出要交给 Codex 的任务")
        }
        if (intent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, "手机没有可用的语音识别服务", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching { voiceInputLauncher.launch(intent) }
            .onFailure { Toast.makeText(this, "无法启动语音输入：${it.message ?: "未知错误"}", Toast.LENGTH_SHORT).show() }
    }

    private fun notifyCodexCompletion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CODEX_COMPLETION_CHANNEL,
                    "Codex 后台任务",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Codex 在手机后台完成任务时的提醒"
                },
            )
        }
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            CODEX_COMPLETION_NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        runCatching {
            NotificationManagerCompat.from(this).notify(
                CODEX_COMPLETION_NOTIFICATION_ID,
                NotificationCompat.Builder(this, CODEX_COMPLETION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Codex 已完成")
                    .setContentText("返回 Codex 移动工作台查看最新回复")
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .build(),
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptAppIntent(intent)
    }

    override fun onDestroy() {
        uploadRequestId++
        uploadJob?.cancel()
        uploadJob = null
        val uploadCallback = activeUploadCallback ?: pendingFileCallback
        activeUploadCallback = null
        pendingFileCallback = null
        uploadCallback?.onReceiveValue(null)
        clearPendingCapture()
        codex.disconnect()
        super.onDestroy()
    }

    private fun dismissUploadStatus() {
        val preparing = uploadStatus?.startsWith("正在准备") == true
        uploadRequestId++
        if (preparing) {
            uploadJob?.cancel()
            uploadJob = null
            val callback = activeUploadCallback ?: pendingFileCallback
            activeUploadCallback = null
            pendingFileCallback = null
            clearPendingCapture()
            callback?.onReceiveValue(null)
            uploadStatus = "附件准备已取消"
            val cancelledRequestId = uploadRequestId
            lifecycleScope.launch {
                delay(1_800)
                if (cancelledRequestId == uploadRequestId) uploadStatus = null
            }
        } else {
            uploadStatus = null
        }
    }

    private fun acceptAppIntent(intent: Intent?) {
        when (intent?.action) {
            "app.codexharness.mobile.OPEN_HARNESS" -> {
                intent.getStringExtra("harness_url")?.let { HarnessBridgeState.url = it }
            }
            "app.codexharness.mobile.OPEN_CODEX" -> shortcutRequest = "CODEX"
            "app.codexharness.mobile.OPEN_HARNESS_SHORTCUT" -> shortcutRequest = "HARNESS"
            "app.codexharness.mobile.OPEN_DIAGNOSTICS" -> shortcutRequest = "DIAGNOSTICS"
            Intent.ACTION_SEND, Intent.ACTION_PROCESS_TEXT -> {
                val sharedText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
                    ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                sharedTextRequest = sharedText
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.take(100_000)
            }
        }
    }

    private fun handleWebDownload(download: WebDownload) {
        val uri = runCatching { Uri.parse(download.url) }.getOrNull()
        if (uri == null || !URLUtil.isNetworkUrl(download.url)) {
            Toast.makeText(this, "暂不支持保存此类下载链接", Toast.LENGTH_SHORT).show()
            return
        }
        val fileName = URLUtil.guessFileName(download.url, download.contentDisposition, download.mimeType)
            .replace(Regex("[\\x00-\\x1F\\x7F\\\\/:*?\"<>|]"), "_")
            .trim()
            .ifBlank { "codex-harness-download" }
        val request = runCatching {
            DownloadManager.Request(uri).apply {
                setTitle(fileName)
                setDescription("Codex Harness 移动工作台")
                setMimeType(download.mimeType ?: "application/octet-stream")
                download.userAgent?.takeIf { it.isNotBlank() }?.let { addRequestHeader("User-Agent", it) }
                CookieManager.getInstance().getCookie(download.url)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { addRequestHeader("Cookie", it) }
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
            }
        }.getOrElse {
            Toast.makeText(this, "无法准备下载：${it.message ?: "链接无效"}", Toast.LENGTH_SHORT).show()
            return
        }
        val manager = getSystemService(DownloadManager::class.java)
        val id = runCatching { manager?.enqueue(request) }.getOrNull()
        if (id == null) {
            Toast.makeText(this, "无法开始下载，请检查系统下载服务", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "已开始下载：$fileName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cleanupStagedUploads() {
        runCatching { clearStaleUploadCache(cacheDir) }
            .onFailure { Log.d(TAG, "unable to clean stale upload cache", it) }
    }

    private fun showFileChooser(
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams,
    ): Boolean {
        // A previous chooser that never returned would otherwise keep the WebView's
        // picker permanently blocked.
        uploadRequestId++
        uploadJob?.cancel()
        uploadJob = null
        val previousCallback = activeUploadCallback ?: pendingFileCallback
        activeUploadCallback = null
        pendingFileCallback = null
        previousCallback?.onReceiveValue(null)
        clearPendingCapture()
        pendingFileCallback = callback

        val intent = buildFileChooserIntent(params)
        Log.i(
            TAG,
            "show file chooser accept=${params.acceptTypes.contentToString()} mode=${params.mode} action=${intent.action} type=${intent.type}",
        )
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
            clearPendingCapture()
            callback.onReceiveValue(null)
            false
        }
    }

    private fun clearPendingCapture() {
        pendingCaptureFile?.let { runCatching { it.delete() } }
        pendingCaptureFile = null
        pendingCaptureUri = null
    }

    private fun parseFileChooserResult(resultCode: Int, data: Intent?): List<Uri> {
        if (resultCode != Activity.RESULT_OK || data == null) return emptyList()
        val direct = buildList {
            data.clipData?.let { clip ->
                for (index in 0 until clip.itemCount) add(clip.getItemAt(index).uri)
            }
            data.data?.let { uri -> if (!contains(uri)) add(uri) }
        }
        if (direct.isNotEmpty()) return direct.distinct()
        return runCatching {
            WebChromeClient.FileChooserParams.parseResult(resultCode, data)?.toList().orEmpty()
        }.getOrDefault(emptyList()).distinct()
    }

    private suspend fun stageUploadUri(
        source: Uri,
        resultFlags: Int,
        onProgress: suspend (copiedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): Uri {
        val readFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (resultFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0) {
            runCatching {
                contentResolver.takePersistableUriPermission(source, resultFlags and readFlag)
            }.onFailure { Log.d(TAG, "persistable URI grant unavailable for $source", it) }
        }
        runCatching { grantUriPermission(packageName, source, readFlag) }

        val uploadInfo = queryUploadInfo(source)
        val displayName = uploadInfo.displayName
        val safeName = displayName
            ?.substringAfterLast('/')
            ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
            ?.trim('_')
            ?.take(120)
            ?.ifBlank { null }
            ?: "attachment"
        val directory = File(cacheDir, "webview-upload").apply { mkdirs() }
        uploadInfo.sizeBytes?.takeIf { it >= 0L }?.let { expectedBytes ->
            val availableBytes = StatFs(directory.path).availableBytes
            val safetyMargin = 1L * 1024L * 1024L
            if (expectedBytes > availableBytes || availableBytes - expectedBytes < safetyMargin) {
                error("手机可用空间不足：需要至少 ${formatByteCount(expectedBytes)}，当前可用 ${formatByteCount(availableBytes)}")
            }
        }
        var target = File(directory, safeName)
        if (target.exists()) target = File(directory, "${UUID.randomUUID()}-$safeName")

        return runCatching {
            val input = contentResolver.openInputStream(source) ?: error("cannot open $source")
            input.use { stream ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copiedBytes = 0L
                    var read: Int
                    do {
                        read = stream.read(buffer)
                        if (read > 0) {
                            output.write(buffer, 0, read)
                            copiedBytes += read
                            onProgress(copiedBytes, uploadInfo.sizeBytes ?: -1L)
                        }
                    } while (read >= 0)
                    output.flush()
                }
            }
            val copiedFileBytes = target.length()
            uploadInfo.sizeBytes?.takeIf { it >= 0L }?.let { expectedBytes ->
                if (copiedFileBytes != expectedBytes) {
                    error("文件暂存校验失败（${formatByteCount(copiedFileBytes)}/${formatByteCount(expectedBytes)}）")
                }
            }
            val staged = FileProvider.getUriForFile(this, "$packageName.fileprovider", target)
            grantUriPermission(packageName, staged, readFlag)
            staged
        }.onFailure {
            runCatching { target.delete() }
            Log.w(TAG, "staging upload URI failed: $source", it)
        }.getOrThrow()
    }

    private fun queryUploadInfo(source: Uri): UploadInfo = runCatching {
        contentResolver.query(
            source,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use UploadInfo(null, null)
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            UploadInfo(
                displayName = nameIndex.takeIf { it >= 0 }?.let { cursor.getString(it) },
                sizeBytes = sizeIndex.takeIf { it >= 0 && !cursor.isNull(it) }?.let { cursor.getLong(it) },
            )
        } ?: UploadInfo(null, null)
    }.getOrDefault(UploadInfo(null, null))

    private companion object {
        const val TAG = "MobileWorkbench"
        const val CODEX_COMPLETION_CHANNEL = "codex_completion"
        const val CODEX_COMPLETION_NOTIFICATION_ID = 4701
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

        // Honor HTML <input capture> on phones.  The camera writes into the same
        // private cache that the normal picker path stages from, so the WebView
        // still receives a stable FileProvider URI after the camera closes.
        val wantsImageCapture = params.isCaptureEnabled &&
            !allowMultiple &&
            (mimeTypes.isEmpty() || mimeTypes.any { it == "image/*" || it.startsWith("image/") })
        if (wantsImageCapture) {
            val captureDirectory = File(cacheDir, "webview-upload").apply { mkdirs() }
            val captureFile = File(captureDirectory, "capture-${UUID.randomUUID()}.jpg")
            val captureUri = runCatching {
                FileProvider.getUriForFile(this, "$packageName.fileprovider", captureFile)
            }.getOrNull()
            if (captureUri != null) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    type = "image/*"
                    putExtra(MediaStore.EXTRA_OUTPUT, captureUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    clipData = ClipData.newRawUri("capture", captureUri)
                }
                if (cameraIntent.resolveActivity(packageManager) != null) {
                    pendingCaptureFile = captureFile
                    pendingCaptureUri = captureUri
                    return cameraIntent
                }
            }
            runCatching { captureFile.delete() }
        }

        fun base(action: String, wide: Boolean = false) = Intent(action).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (action == Intent.ACTION_OPEN_DOCUMENT) {
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
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
            base(Intent.ACTION_OPEN_DOCUMENT),
            base(Intent.ACTION_GET_CONTENT),
            runCatching {
                params.createIntent().apply { addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            }.getOrNull(),
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
    codexWebUiUrl: String?,
    codexWebUiApiKey: String?,
    onCloseCodexWebUi: () -> Unit,
    shortcutRequest: String?,
    onShortcutConsumed: () -> Unit,
    sharedTextRequest: String?,
    onSharedTextConsumed: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onRequestPermission: () -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
    onDownload: (WebDownload) -> Unit,
    uploadStatus: String?,
    onDismissUploadStatus: () -> Unit,
    notificationPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onNotifyBackgroundCompletion: () -> Unit,
) {
    val appContext = LocalContext.current
    val appPreferences = remember(appContext) {
        appContext.getSharedPreferences("mobile_preferences", android.content.Context.MODE_PRIVATE)
    }
    var selected by rememberSaveable {
        mutableStateOf(
            if (appPreferences.getString("last_workspace", "CODEX") == Workspace.HARNESS.name) {
                Workspace.HARNESS
            } else {
                Workspace.CODEX
            },
        )
    }
    var health by remember { mutableStateOf(LiveRuntimeState(checking = true, detail = "正在检测手机环境…")) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var chromeVisible by rememberSaveable { mutableStateOf(true) }
    var diagnosticsVisible by rememberSaveable { mutableStateOf(false) }
    var diagnosticsLoading by remember { mutableStateOf(false) }
    var diagnostics by remember { mutableStateOf<RuntimeDiagnostics?>(null) }
    var diagnosticsError by remember { mutableStateOf<String?>(null) }
    var uploadCacheInfo by remember { mutableStateOf(UploadCacheInfo()) }
    var uploadCacheLoading by remember { mutableStateOf(false) }
    var logsVisible by rememberSaveable { mutableStateOf(false) }
    var logsLoading by remember { mutableStateOf(false) }
    var runtimeLogs by remember { mutableStateOf<String?>(null) }
    var keepScreenOn by rememberSaveable {
        mutableStateOf(appPreferences.getBoolean("keep_screen_on", false))
    }
    var immersiveMode by rememberSaveable {
        mutableStateOf(appPreferences.getBoolean("immersive_mode", false))
    }
    val legacyWebTextZoom = remember(appPreferences) {
        appPreferences.getInt("web_text_zoom", 100).coerceIn(80, 140)
    }
    var codexTextZoom by rememberSaveable {
        mutableStateOf(appPreferences.getInt("web_text_zoom_codex", legacyWebTextZoom).coerceIn(80, 140))
    }
    var harnessTextZoom by rememberSaveable {
        mutableStateOf(appPreferences.getInt("web_text_zoom_harness", legacyWebTextZoom).coerceIn(80, 140))
    }
    var backgroundNotifications by rememberSaveable {
        mutableStateOf(appPreferences.getBoolean("background_completion_notifications", false))
    }
    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var codexReloadRequest by rememberSaveable { mutableStateOf(0) }
    var harnessReloadRequest by rememberSaveable { mutableStateOf(0) }
    var harnessWindowClosedByUser by rememberSaveable { mutableStateOf(false) }
    var codexWindowClosedByUser by rememberSaveable { mutableStateOf(false) }
    var runtimeStoppedByUser by rememberSaveable { mutableStateOf(false) }
    var stopConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var closeWorkspaceConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var codexWasBusyInBackground by rememberSaveable { mutableStateOf(false) }
    var appInForeground by remember { mutableStateOf(true) }
    var runtimeActionInFlight by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val latestHarnessUrl by rememberUpdatedState(harnessUrl)
    val latestCodexWebUiUrl by rememberUpdatedState(codexWebUiUrl)
    val latestCodexWebUiApiKey by rememberUpdatedState(codexWebUiApiKey)
    val activity = appContext as? Activity
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val configuration = LocalConfiguration.current
    val compactChrome = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenHeightDp <= 500
    val webTextZoom = if (selected == Workspace.CODEX) codexTextZoom else harnessTextZoom

    fun beginRuntimeAction(): Boolean {
        if (runtimeActionInFlight) {
            actionMessage = "已有本地服务操作进行中，请稍候…"
            Toast.makeText(appContext, "本地服务操作进行中，请稍候", Toast.LENGTH_SHORT).show()
            return false
        }
        runtimeActionInFlight = true
        return true
    }

    fun toggleBackgroundNotifications() {
        if (backgroundNotifications) {
            backgroundNotifications = false
            appPreferences.edit().putBoolean("background_completion_notifications", false).apply()
        } else if (notificationPermissionGranted) {
            backgroundNotifications = true
            appPreferences.edit().putBoolean("background_completion_notifications", true).apply()
        } else {
            notificationPermissionRequested = true
            onRequestNotificationPermission()
        }
    }

    LaunchedEffect(notificationPermissionGranted, notificationPermissionRequested) {
        if (notificationPermissionRequested && notificationPermissionGranted) {
            notificationPermissionRequested = false
            backgroundNotifications = true
            appPreferences.edit().putBoolean("background_completion_notifications", true).apply()
        }
    }

    LaunchedEffect(selected) {
        appPreferences.edit().putString("last_workspace", selected.name).apply()
    }

    DisposableEffect(activity, keepScreenOn) {
        if (keepScreenOn) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            if (keepScreenOn) activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    suspend fun refresh() {
        health = health.copy(checking = true)
        health = runtime.health()
        if (health.codexOnline && !codex.connected && !codex.connecting) codex.connect()
    }

    suspend fun waitForRuntime(
        timeoutMs: Long,
        ready: (LiveRuntimeState) -> Boolean,
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            val snapshot = runtime.health()
            health = snapshot
            if (ready(snapshot)) return true
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0L) return false
            delay(remaining.coerceAtMost(1_000L))
        }
    }

    LaunchedEffect(codex.busy, appInForeground) {
        if (appInForeground && codexWasBusyInBackground && !codex.busy) {
            actionMessage = "Codex 已在后台完成，回到前台即可查看最新回复。"
            codexWasBusyInBackground = false
        }
    }

    LaunchedEffect(codex.busy, appInForeground, backgroundNotifications) {
        if (!appInForeground && codexWasBusyInBackground && !codex.busy && backgroundNotifications) {
            onNotifyBackgroundCompletion()
            codexWasBusyInBackground = false
        }
    }

    fun runDiagnostics() {
        if (diagnosticsLoading) return
        scope.launch {
            diagnosticsLoading = true
            diagnosticsError = null
            val result = runCatching { runtime.diagnostics() }
            diagnostics = result.getOrNull()
            diagnosticsError = result.exceptionOrNull()?.message
            diagnosticsLoading = false
        }
    }

    fun refreshUploadCache() {
        if (uploadCacheLoading) return
        scope.launch {
            uploadCacheLoading = true
            try {
                uploadCacheInfo = withContext(Dispatchers.IO) {
                    readUploadCacheInfo(appContext.cacheDir)
                }
            } finally {
                uploadCacheLoading = false
            }
        }
    }

    fun clearStaleUploadCache() {
        if (uploadCacheLoading) return
        scope.launch {
            uploadCacheLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val cleanup = clearStaleUploadCache(appContext.cacheDir)
                    cleanup to readUploadCacheInfo(appContext.cacheDir)
                }
                uploadCacheInfo = result.second
                actionMessage = if (result.first.removedFileCount == 0) {
                    "没有需要清理的旧附件缓存"
                } else {
                    "已清理 ${result.first.removedFileCount} 个旧附件，释放 ${formatByteCount(result.first.removedBytes)}"
                }
            } catch (error: Exception) {
                actionMessage = "附件缓存清理失败：${error.message ?: "无法访问缓存目录"}"
            } finally {
                uploadCacheLoading = false
            }
        }
    }

    fun openDiagnostics() {
        diagnosticsVisible = true
        runDiagnostics()
        refreshUploadCache()
    }

    LaunchedEffect(shortcutRequest) {
        when (shortcutRequest) {
            "CODEX" -> {
                selected = Workspace.CODEX
                chromeVisible = true
                closeWorkspaceConfirmVisible = false
            }
            "HARNESS" -> {
                selected = Workspace.HARNESS
                chromeVisible = true
                closeWorkspaceConfirmVisible = false
            }
            "DIAGNOSTICS" -> {
                chromeVisible = true
                closeWorkspaceConfirmVisible = false
                openDiagnostics()
            }
        }
        if (shortcutRequest != null) onShortcutConsumed()
    }

    LaunchedEffect(sharedTextRequest) {
        if (!sharedTextRequest.isNullOrBlank()) {
            selected = Workspace.CODEX
            chromeVisible = true
            closeWorkspaceConfirmVisible = false
        }
    }

    fun openTermuxBatterySettings() {
        actionMessage = runtime.openTermuxBatterySettings().fold(
            onSuccess = { "已打开系统电池优化设置；建议允许 Termux 后台运行。" },
            onFailure = { it.message ?: "无法打开电池优化设置" },
        )
    }

    fun openLogs() {
        logsVisible = true
        if (logsLoading) return
        scope.launch {
            logsLoading = true
            runtimeLogs = null
            val startedAt = System.currentTimeMillis()
            val result = runtime.readRuntimeLogs()
            if (result.isFailure) {
                runtimeLogs = result.exceptionOrNull()?.message ?: "无法读取 Termux 日志"
            } else {
                for (attempt in 0 until 24) {
                    if (TermuxCommandState.completedAt?.let { it >= startedAt } == true) break
                    delay(250)
                }
                runtimeLogs = TermuxCommandState.lastOutput ?: "Termux 暂未返回日志，请确认 Termux 仍在运行。"
            }
            logsLoading = false
        }
    }

    fun recoverLocalServices() {
        if (diagnosticsLoading || !beginRuntimeAction()) return
        runtimeStoppedByUser = false
        scope.launch {
            try {
                actionMessage = "正在根据诊断结果恢复本地服务…"
                val current = runtime.health()
                val codexResult = if (current.codexOnline) {
                    Result.success(Unit)
                } else {
                    runtime.startCodex()
                }
                val harnessResult = if (current.harnessOnline) {
                    Result.success(Unit)
                } else {
                    runtime.startHarness()
                }
                val failure = codexResult.exceptionOrNull() ?: harnessResult.exceptionOrNull()
                if (failure == null) {
                    val ready = waitForRuntime(30_000L) { state -> state.codexOnline && state.harnessOnline }
                    actionMessage = if (ready) "本地服务已恢复" else "恢复命令已发送，服务仍在启动，后台会继续检查…"
                } else {
                    actionMessage = failure.message ?: "恢复服务失败"
                }
                refresh()
                runDiagnostics()
                if (failure == null) {
                    when {
                        selected == Workspace.CODEX && latestCodexWebUiUrl != null -> codexReloadRequest++
                        selected == Workspace.HARNESS && latestHarnessUrl != null -> harnessReloadRequest++
                    }
                }
            } finally {
                runtimeActionInFlight = false
            }
        }
    }

    fun restartCurrentWorkspace() {
        if (diagnosticsLoading || !beginRuntimeAction()) return
        runtimeStoppedByUser = false
        val workspace = selected
        when (workspace) {
            Workspace.CODEX -> {
                codexWindowClosedByUser = false
                onCloseCodexWebUi()
            }
            Workspace.HARNESS -> {
                harnessWindowClosedByUser = false
                onCloseHarness()
            }
        }
        scope.launch {
            try {
                val label = if (workspace == Workspace.CODEX) "Codex WebUI" else "Harness"
                actionMessage = "正在重启 $label…"
                val result = if (workspace == Workspace.CODEX) {
                    runtime.restartCodexWebUiAndOpen(forceRestart = true)
                } else {
                    runtime.restartHarnessAndOpen(forceRestart = true)
                }
                val failure = result.exceptionOrNull()
                if (failure != null) {
                    actionMessage = failure.message ?: "$label 重启失败"
                } else {
                    val ready = waitForRuntime(if (workspace == Workspace.CODEX) 90_000L else 45_000L) { state ->
                        if (workspace == Workspace.CODEX) state.codexWebUiOnline else state.harnessOnline
                    }
                    actionMessage = if (ready) "$label 已重启，正在打开…" else "重启命令已发送，$label 仍在启动…"
                }
                refresh()
                runDiagnostics()
            } finally {
                runtimeActionInFlight = false
            }
        }
    }

    LaunchedEffect(Unit) {
        var recoveryInFlight = false
        var lastRecoveryAt = 0L

        refresh()

        suspend fun recoverRuntime(openHarnessWindow: Boolean, openCodexWebUiWindow: Boolean) {
            if (recoveryInFlight || runtimeActionInFlight) return
            if (!appInForeground) return
            val now = System.currentTimeMillis()
            if (now - lastRecoveryAt < 15_000L) return
            if (runtimeStoppedByUser) return
            if (!health.termuxInstalled || !health.commandPermission) return

            recoveryInFlight = true
            runtimeActionInFlight = true
            lastRecoveryAt = now
            try {
                actionMessage = "检测到 Termux 运行时中断，正在恢复本地服务…"
                if (openHarnessWindow) selected = Workspace.HARNESS
                else if (openCodexWebUiWindow) selected = Workspace.CODEX

                val codexRequested = !health.codexOnline
                val harnessRequested = openHarnessWindow || !health.harnessOnline
                val webUiRequested = openCodexWebUiWindow

                val codexResult = if (health.codexOnline) {
                    Result.success(Unit)
                } else {
                    runtime.startCodex()
                }
                val harnessResult = when {
                    openHarnessWindow -> runtime.restartHarnessAndOpen()
                    health.harnessOnline -> Result.success(Unit)
                    else -> runtime.startHarness()
                }
                val webUiResult = if (openCodexWebUiWindow) {
                    runtime.restartCodexWebUiAndOpen()
                } else {
                    Result.success(Unit)
                }
                val failure = codexResult.exceptionOrNull()
                    ?: harnessResult.exceptionOrNull()
                    ?: webUiResult.exceptionOrNull()
                if (failure == null) {
                    val ready = waitForRuntime(90_000L) { state ->
                        (!codexRequested || state.codexOnline) &&
                            (!harnessRequested || state.harnessOnline) &&
                            (!webUiRequested || state.codexWebUiOnline || latestCodexWebUiUrl != null)
                    }
                    actionMessage = if (ready) "本地服务已恢复" else "恢复命令已发送，服务仍在启动，后台会继续检查…"
                } else {
                    actionMessage = failure.message ?: "恢复服务失败"
                }
                if (appInForeground) refresh()
            } finally {
                recoveryInFlight = false
                runtimeActionInFlight = false
            }
        }

        // On a cold app launch the service may already be listening, but the
        // callback that carries its current URL is gone. Re-read that URL so
        // the Harness window is restored instead of leaving the dashboard up.
        if (
                health.termuxInstalled &&
            health.commandPermission &&
            (!health.codexOnline || !health.harnessOnline ||
                (health.harnessOnline && latestHarnessUrl == null && !harnessWindowClosedByUser) ||
                (health.codexWebUiOnline && latestCodexWebUiUrl == null && !codexWindowClosedByUser) ||
                (!health.codexWebUiOnline && latestCodexWebUiUrl != null && !codexWindowClosedByUser))
        ) {
            recoverRuntime(
                openHarnessWindow = !harnessWindowClosedByUser,
                openCodexWebUiWindow = !codexWindowClosedByUser &&
                    (health.codexWebUiOnline || latestCodexWebUiUrl != null),
            )
        }

        while (true) {
            delay(if (appInForeground) 5_000 else 30_000)
            if (!appInForeground) continue
            refresh()
            if (
                    health.termuxInstalled &&
                health.commandPermission &&
                (!health.codexOnline || !health.harnessOnline ||
                    (health.harnessOnline && latestHarnessUrl == null && !harnessWindowClosedByUser) ||
                    (health.codexWebUiOnline && latestCodexWebUiUrl == null && !codexWindowClosedByUser) ||
                    (!health.codexWebUiOnline && latestCodexWebUiUrl != null && !codexWindowClosedByUser))
            ) {
                // Termux can be swiped away independently of this Activity.
                // Health polling is also the recovery trigger; the cooldown
                // prevents a failed launch from flooding RunCommandService.
                recoverRuntime(
                    openHarnessWindow = !harnessWindowClosedByUser,
                    openCodexWebUiWindow = !codexWindowClosedByUser &&
                        (health.codexWebUiOnline || latestCodexWebUiUrl != null),
                )
            }
        }
    }

    // Termux can be closed or resumed while this Activity stays in the task
    // stack.  Refresh immediately when the app returns to the foreground so
    // the UI does not wait for the five-second polling interval (and so a
    // restored Harness/WebUI URL is picked up without reopening the app).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appInForeground = true
                if (codexWasBusyInBackground && !codex.busy) {
                    actionMessage = "Codex 已在后台完成，回到前台即可查看最新回复。"
                    codexWasBusyInBackground = false
                }
                scope.launch { refresh() }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                appInForeground = false
                if (codex.busy) codexWasBusyInBackground = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(appInForeground) {
        val connectivity = appContext.getSystemService(ConnectivityManager::class.java)
        if (!appInForeground || connectivity == null) {
            onDispose { }
        } else {
            var registered = false
            var pendingRefresh: Job? = null
            fun scheduleNetworkRefresh() {
                pendingRefresh?.cancel()
                pendingRefresh = scope.launch {
                    delay(350L)
                    refresh()
                }
            }
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = scheduleNetworkRefresh()

                override fun onLost(network: Network) = scheduleNetworkRefresh()

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) =
                    scheduleNetworkRefresh()
            }
            runCatching {
                connectivity.registerDefaultNetworkCallback(callback)
                registered = true
            }.onFailure { Log.w("MobileWorkbench", "无法注册网络状态回调", it) }
            onDispose {
                pendingRefresh?.cancel()
                if (registered) runCatching { connectivity.unregisterNetworkCallback(callback) }
            }
        }
    }

    LaunchedEffect(harnessUrl) {
        if (harnessUrl != null && !harnessWindowClosedByUser) {
            selected = Workspace.HARNESS
            actionMessage = null
        }
    }

    LaunchedEffect(codexWebUiUrl) {
        if (codexWebUiUrl != null && !codexWindowClosedByUser) {
            selected = Workspace.CODEX
            actionMessage = null
        }
    }

    val webWorkspaceOpen = when (selected) {
        Workspace.CODEX -> codexWebUiUrl != null
        Workspace.HARNESS -> harnessUrl != null
    }
    DisposableEffect(activity, webWorkspaceOpen, chromeVisible, immersiveMode, appInForeground) {
        val window = activity?.window
        val immersiveActive = webWorkspaceOpen && !chromeVisible && immersiveMode && appInForeground
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (immersiveActive) controller.hide(WindowInsetsCompat.Type.systemBars())
            else controller.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            window?.let {
                WindowCompat.getInsetsController(it, it.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    LaunchedEffect(harnessUrl, codexWebUiUrl, selected) {
        chromeVisible = !webWorkspaceOpen
    }

    LaunchedEffect(webWorkspaceOpen) {
        if (!webWorkspaceOpen) closeWorkspaceConfirmVisible = false
    }

    fun closeSelectedWorkspace() {
        closeWorkspaceConfirmVisible = false
        when (selected) {
            Workspace.CODEX -> {
                codexWindowClosedByUser = true
                onCloseCodexWebUi()
            }
            Workspace.HARNESS -> {
                harnessWindowClosedByUser = true
                onCloseHarness()
            }
        }
    }

    BackHandler(enabled = webWorkspaceOpen && !chromeVisible && !imeVisible) {
        chromeVisible = true
    }
    BackHandler(enabled = webWorkspaceOpen && chromeVisible && !imeVisible) {
        closeWorkspaceConfirmVisible = true
    }

    fun runRuntimeToggle(stop: Boolean) {
        if (!beginRuntimeAction()) return
        scope.launch {
            try {
                runtimeStoppedByUser = stop
                if (stop) {
                    harnessWindowClosedByUser = true
                    codexWindowClosedByUser = true
                    onCloseHarness()
                    onCloseCodexWebUi()
                } else {
                    harnessWindowClosedByUser = false
                    codexWindowClosedByUser = false
                }
                val result = if (stop) runtime.stopAll() else runtime.startAll()
                if (result.isSuccess) {
                    val ready = if (stop) {
                        waitForRuntime(15_000L) { state ->
                            !state.codexOnline && !state.codexWebUiOnline && !state.harnessOnline
                        }
                    } else {
                        waitForRuntime(30_000L) { state -> state.codexOnline && state.harnessOnline }
                    }
                    actionMessage = when {
                        stop && ready -> "本地服务已停止"
                        stop -> "停止命令已发送，服务仍在退出…"
                        ready -> "本地服务已就绪"
                        else -> "启动命令已发送，服务仍在启动，后台会继续检查…"
                    }
                } else {
                    actionMessage = result.exceptionOrNull()?.message ?: "操作失败"
                }
                refresh()
            } finally {
                runtimeActionInFlight = false
            }
        }
    }

    MaterialTheme(colorScheme = mobileColors()) {
        Box(Modifier.fillMaxSize()) {
            Surface(Modifier.fillMaxSize(), color = Ink) {
                Scaffold(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                containerColor = Ink,
                topBar = {
                    if (chromeVisible) {
                        AppHeader(
                            health = health,
                            running = health.codexOnline || health.codexWebUiOnline || health.harnessOnline,
                            workspaceOpen = webWorkspaceOpen,
                            actionBusy = runtimeActionInFlight,
                            compact = compactChrome,
                            onRefresh = { scope.launch { refresh() } },
                            onDiagnostics = ::openDiagnostics,
                            onToggleChrome = { chromeVisible = false },
                            onToggle = {
                                val running = health.codexOnline || health.codexWebUiOnline || health.harnessOnline
                                if (running) stopConfirmVisible = true else runRuntimeToggle(stop = false)
                            },
                        )
                    }
                },
                bottomBar = {
                    if (chromeVisible) {
                        AppNavigation(selected = selected, compact = compactChrome, onSelected = { selected = it })
                    }
                },
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        AnimatedVisibility(visible = actionMessage != null && !webWorkspaceOpen, enter = fadeIn(), exit = fadeOut()) {
                            actionMessage?.let { InlineNotice(it) { actionMessage = null } }
                        }
                        AnimatedVisibility(visible = uploadStatus != null, enter = fadeIn(), exit = fadeOut()) {
                            uploadStatus?.let { InlineNotice(it, onDismissUploadStatus) }
                        }
                        Box(Modifier.fillMaxSize()) {
                            CodexScreen(
                                modifier = Modifier.fillMaxSize().zIndex(if (selected == Workspace.CODEX) 1f else 0f),
                                active = selected == Workspace.CODEX,
                                health = health,
                                client = codex,
                                webUiUrl = codexWebUiUrl,
                                webUiApiKey = latestCodexWebUiApiKey,
                                textZoom = codexTextZoom,
                                sharedText = sharedTextRequest,
                                onSharedTextConsumed = onSharedTextConsumed,
                                onStartVoiceInput = onStartVoiceInput,
                                showFrameChrome = chromeVisible,
                                onCloseDesktop = {
                                    codexWindowClosedByUser = true
                                    onCloseCodexWebUi()
                                },
                                onDiagnostics = ::openDiagnostics,
                                onRecover = ::recoverLocalServices,
                                reloadRequest = codexReloadRequest,
                                onShowFileChooser = onShowFileChooser,
                                onDownload = onDownload,
                                onStartWebUi = {
                                    if (beginRuntimeAction()) {
                                        runtimeStoppedByUser = false
                                        codexWindowClosedByUser = false
                                        scope.launch {
                                            try {
                                                val result = runtime.restartCodexWebUiAndOpen()
                                                if (result.isSuccess) {
                                                    val ready = waitForRuntime(90_000L) { state ->
                                                        state.codexWebUiOnline || latestCodexWebUiUrl != null
                                                    }
                                                    actionMessage = if (ready) {
                                                        "Codex 工作台已就绪，正在打开…"
                                                    } else {
                                                        "启动命令已发送，工作台仍在启动，后台会继续检查…"
                                                    }
                                                } else {
                                                    actionMessage = result.exceptionOrNull()?.message ?: "Codex 工作台启动失败"
                                                }
                                                refresh()
                                            } finally {
                                                runtimeActionInFlight = false
                                            }
                                        }
                                    }
                                },
                                onStartCodex = {
                                    if (beginRuntimeAction()) {
                                        runtimeStoppedByUser = false
                                        scope.launch {
                                            try {
                                                val result = runtime.startCodex()
                                                if (result.isSuccess) {
                                                    val ready = waitForRuntime(30_000L) { state -> state.codexOnline }
                                                    actionMessage = if (ready) "Codex 服务已就绪" else "Codex 启动命令已发送，后台会继续检查…"
                                                } else {
                                                    actionMessage = result.exceptionOrNull()?.message ?: "Codex 服务启动失败"
                                                }
                                                refresh()
                                            } finally {
                                                runtimeActionInFlight = false
                                            }
                                        }
                                    }
                                },
                                onPermission = onRequestPermission,
                                backgroundNotificationsEnabled = backgroundNotifications,
                                onToggleBackgroundNotifications = ::toggleBackgroundNotifications,
                            )
                            HarnessScreen(
                                modifier = Modifier.fillMaxSize().zIndex(if (selected == Workspace.HARNESS) 1f else 0f),
                                active = selected == Workspace.HARNESS,
                                health = health,
                                harnessUrl = harnessUrl,
                                textZoom = harnessTextZoom,
                                showFrameChrome = chromeVisible,
                                onCloseWeb = {
                                    harnessWindowClosedByUser = true
                                    onCloseHarness()
                                },
                                onStartAndOpen = {
                                    if (beginRuntimeAction()) {
                                        runtimeStoppedByUser = false
                                        harnessWindowClosedByUser = false
                                        scope.launch {
                                            try {
                                                val result = runtime.restartHarnessAndOpen()
                                                if (result.isSuccess) {
                                                    val ready = waitForRuntime(45_000L) { state ->
                                                        state.harnessOnline || latestHarnessUrl != null
                                                    }
                                                    actionMessage = if (ready) "Harness 服务已就绪，正在打开…" else "Harness 启动命令已发送，后台会继续检查…"
                                                } else {
                                                    actionMessage = result.exceptionOrNull()?.message ?: "Harness 启动失败"
                                                }
                                                refresh()
                                            } finally {
                                                runtimeActionInFlight = false
                                            }
                                        }
                                    }
                                },
                                onOpenTermux = {
                                    actionMessage = runtime.openTermux().fold(
                                        onSuccess = { "已打开 Termux，可查看 harness.log 和 codex-webui.log" },
                                        onFailure = { it.message ?: "无法打开 Termux" },
                                    )
                                },
                                onPermission = onRequestPermission,
                                onDiagnostics = ::openDiagnostics,
                                onBatterySettings = ::openTermuxBatterySettings,
                                onRecover = ::recoverLocalServices,
                                reloadRequest = harnessReloadRequest,
                                onShowFileChooser = onShowFileChooser,
                                onDownload = onDownload,
                            )
                        }
                    }
                }
            }
            if (webWorkspaceOpen && !chromeVisible) {
                FocusTools(
                    selected = selected,
                    keepScreenOn = keepScreenOn,
                    webTextZoom = webTextZoom,
                    immersiveMode = immersiveMode,
                    networkAvailable = health.networkAvailable,
                    networkValidated = health.networkValidated,
                    onToggleKeepScreenOn = {
                        val next = !keepScreenOn
                        keepScreenOn = next
                        appPreferences.edit().putBoolean("keep_screen_on", next).apply()
                    },
                    onToggleImmersiveMode = {
                        val next = !immersiveMode
                        immersiveMode = next
                        appPreferences.edit().putBoolean("immersive_mode", next).apply()
                    },
                    onSetWebTextZoom = { next ->
                        val bounded = next.coerceIn(80, 140)
                        if (selected == Workspace.CODEX) {
                            codexTextZoom = bounded
                            appPreferences.edit().putInt("web_text_zoom_codex", bounded).apply()
                        } else {
                            harnessTextZoom = bounded
                            appPreferences.edit().putInt("web_text_zoom_harness", bounded).apply()
                        }
                    },
                    onReloadPage = {
                        if (selected == Workspace.CODEX) codexReloadRequest++ else harnessReloadRequest++
                    },
                    onSwitchWorkspace = {
                        selected = if (selected == Workspace.CODEX) Workspace.HARNESS else Workspace.CODEX
                    },
                    onShowChrome = { chromeVisible = true },
                    onDiagnostics = ::openDiagnostics,
                )
            }
        }
        if (diagnosticsVisible) {
            DiagnosticsDialog(
                diagnostics = diagnostics,
                loading = diagnosticsLoading,
                error = diagnosticsError,
                lastAction = actionMessage,
                actionBusy = runtimeActionInFlight,
                currentWorkspaceLabel = if (selected == Workspace.CODEX) "Codex WebUI" else "Harness",
                onRefresh = ::runDiagnostics,
                onRecover = ::recoverLocalServices,
                onLogs = ::openLogs,
                onBatterySettings = ::openTermuxBatterySettings,
                uploadCacheInfo = uploadCacheInfo,
                uploadCacheLoading = uploadCacheLoading,
                onRefreshUploadCache = ::refreshUploadCache,
                onClearStaleUploadCache = ::clearStaleUploadCache,
                onRestartCurrentWorkspace = ::restartCurrentWorkspace,
                onDismiss = { diagnosticsVisible = false },
            )
        }
        if (logsVisible) {
            RuntimeLogsDialog(
                logs = runtimeLogs,
                loading = logsLoading,
                onRefresh = ::openLogs,
                onDismiss = { logsVisible = false },
            )
        }
        if (stopConfirmVisible) {
            AlertDialog(
                onDismissRequest = { stopConfirmVisible = false },
                icon = { Icon(Icons.Rounded.Stop, null, tint = Red) },
                title = { Text("停止本地服务？") },
                text = {
                    Text(
                        "这会停止 Codex、Codex WebUI 和 Harness，正在执行的任务可能被中断。",
                        color = TextSecondary,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            stopConfirmVisible = false
                            runRuntimeToggle(stop = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Ink),
                    ) { Text("停止全部") }
                },
                dismissButton = { TextButton(onClick = { stopConfirmVisible = false }) { Text("取消") } },
                containerColor = PanelRaised,
            )
        }
        if (closeWorkspaceConfirmVisible) {
            AlertDialog(
                onDismissRequest = { closeWorkspaceConfirmVisible = false },
                icon = { Icon(Icons.Rounded.Close, null, tint = Amber) },
                title = { Text("关闭当前工作区？") },
                text = {
                    Text(
                        "这会关闭当前网页工作区；未提交的网页输入或临时页面状态可能需要重新打开。",
                        color = TextSecondary,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = ::closeSelectedWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink),
                    ) { Text("关闭工作区") }
                },
                dismissButton = { TextButton(onClick = { closeWorkspaceConfirmVisible = false }) { Text("继续使用") } },
                containerColor = PanelRaised,
            )
        }
    }

    codex.approval?.let { approval ->
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Rounded.Security, null, tint = Amber) },
            title = { Text("需要你的确认") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = mobileDialogMaxHeight(200)).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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

@Composable
private fun AppHeader(
    health: LiveRuntimeState,
    running: Boolean,
    workspaceOpen: Boolean,
    actionBusy: Boolean,
    compact: Boolean,
    onRefresh: () -> Unit,
    onDiagnostics: () -> Unit,
    onToggleChrome: () -> Unit,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 42.dp else 48.dp)
            .background(Ink)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(Violet), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Bolt, null, tint = Ink, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(7.dp))
        Column(Modifier.weight(1f)) {
            Text("Codex 移动工作台", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            if (!compact) {
                Text(health.detail, color = if (running) Green else TextSecondary, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
            if (health.checking) CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = Cyan)
            else Icon(Icons.Rounded.Refresh, "刷新状态", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDiagnostics, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.NetworkCheck, "诊断连接", tint = Cyan, modifier = Modifier.size(18.dp))
        }
        if (workspaceOpen) {
            IconButton(onClick = onToggleChrome, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.MoreVert, "隐藏工具栏", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
        Button(
            onClick = onToggle,
            enabled = !actionBusy,
            modifier = Modifier.height(31.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) Color(0xFF2B2026) else Violet,
                contentColor = if (running) Red else Ink,
            ),
            contentPadding = PaddingValues(horizontal = if (compact) 8.dp else 10.dp, vertical = 0.dp),
        ) {
            if (actionBusy) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = if (running) Red else Ink)
            else Icon(if (running) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, null, Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (actionBusy) "处理中" else if (running) "停止" else "启动", fontSize = 10.sp)
        }
        Spacer(Modifier.width(2.dp))
    }
}

@Composable
private fun AppNavigation(selected: Workspace, compact: Boolean, onSelected: (Workspace) -> Unit) {
    NavigationBar(
        modifier = Modifier.height(if (compact) 48.dp else 56.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Panel,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = selected == Workspace.CODEX,
            onClick = { onSelected(Workspace.CODEX) },
            icon = { Icon(Icons.Rounded.Code, null) },
            label = { Text("Codex", fontSize = 11.sp, maxLines = 1) },
            alwaysShowLabel = !compact,
            colors = navColors(),
        )
        NavigationBarItem(
            selected = selected == Workspace.HARNESS,
            onClick = { onSelected(Workspace.HARNESS) },
            icon = { Icon(Icons.Rounded.Terminal, null) },
            label = { Text("Harness", fontSize = 11.sp, maxLines = 1) },
            alwaysShowLabel = !compact,
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
private fun FocusTools(
    selected: Workspace,
    keepScreenOn: Boolean,
    webTextZoom: Int,
    immersiveMode: Boolean,
    networkAvailable: Boolean,
    networkValidated: Boolean,
    onToggleKeepScreenOn: () -> Unit,
    onToggleImmersiveMode: () -> Unit,
    onSetWebTextZoom: (Int) -> Unit,
    onReloadPage: () -> Unit,
    onSwitchWorkspace: () -> Unit,
    onShowChrome: () -> Unit,
    onDiagnostics: () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    // The expanded toolbar gained the optional immersive-mode action; keep
    // the title collapsed on normal narrow phones as well as very small ones.
    val compactTools = configuration.screenWidthDp <= 420
    val diagnosticsTint = when {
        !networkAvailable -> Red
        !networkValidated -> Amber
        else -> Cyan
    }
    val diagnosticsDescription = when {
        !networkAvailable -> "诊断连接（无网络）"
        !networkValidated -> "诊断连接（网络未验证）"
        else -> "诊断连接"
    }
    val preferences = remember(context) {
        context.getSharedPreferences("mobile_preferences", android.content.Context.MODE_PRIVATE)
    }
    val workspaceKey = if (selected == Workspace.CODEX) "codex" else "harness"
    val workspacePositionPrefix = "focus_tools_$workspaceKey"
    var expanded by rememberSaveable { mutableStateOf(false) }
    var zoomMenuVisible by remember { mutableStateOf(false) }
    var dragX by rememberSaveable(workspaceKey) {
        mutableStateOf(
            preferences.getFloat(
                "${workspacePositionPrefix}_dx",
                preferences.getFloat("focus_tools_dx", 0f),
            ),
        )
    }
    var dragY by rememberSaveable(workspaceKey) {
        mutableStateOf(
            preferences.getFloat(
                "${workspacePositionPrefix}_dy",
                preferences.getFloat("focus_tools_dy", 0f),
            ),
        )
    }
    var hostSize by remember { mutableStateOf(IntSize.Zero) }
    var toolSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(hostSize, toolSize, workspaceKey) {
        if (hostSize.width > 0 && hostSize.height > 0 && toolSize.width > 0 && toolSize.height > 0) {
            val anchoredLeft = (hostSize.width - toolSize.width).coerceAtLeast(0).toFloat()
            val anchoredTop = (hostSize.height - toolSize.height).coerceAtLeast(0) / 2f
            val boundedX = dragX.coerceIn(-anchoredLeft, 0f)
            val boundedY = dragY.coerceIn(
                -anchoredTop,
                (hostSize.height - toolSize.height).coerceAtLeast(0) - anchoredTop,
            )
            if (dragX != boundedX || dragY != boundedY) {
                dragX = boundedX
                dragY = boundedY
                preferences.edit()
                    .putFloat("${workspacePositionPrefix}_dx", boundedX)
                    .putFloat("${workspacePositionPrefix}_dy", boundedY)
                    .apply()
            }
        }
    }

    val dragModifier = Modifier
        .onSizeChanged { toolSize = it }
        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
        .pointerInput(hostSize, toolSize, workspaceKey) {
            detectDragGestures { change, amount ->
                change.consume()
                if (hostSize.width <= 0 || hostSize.height <= 0 || toolSize.width <= 0 || toolSize.height <= 0) return@detectDragGestures

                // The control is aligned to the center-right by default.  Keep
                // the drag offset relative to that anchor, while clamping the
                // resulting rectangle to the entire safe-drawing area.
                val anchoredLeft = (hostSize.width - toolSize.width).coerceAtLeast(0).toFloat()
                val anchoredTop = (hostSize.height - toolSize.height).coerceAtLeast(0) / 2f
                val minX = -anchoredLeft
                val maxX = 0f
                val minY = -anchoredTop
                val maxY = (hostSize.height - toolSize.height).coerceAtLeast(0) - anchoredTop
                val nextX = (dragX + amount.x).coerceIn(minX, maxX)
                val nextY = (dragY + amount.y).coerceIn(minY, maxY)
                dragX = nextX
                dragY = nextY
                preferences.edit()
                    .putFloat("${workspacePositionPrefix}_dx", nextX)
                    .putFloat("${workspacePositionPrefix}_dy", nextY)
                    .apply()
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            // Keep the floating controls above the software keyboard while a
            // WebView input is focused; the drag bounds then use the visible
            // area instead of letting the toolbar disappear behind the IME.
            .imePadding()
            .padding(end = 2.dp)
            .onSizeChanged { hostSize = it },
    ) {
        if (!expanded) {
            IconButton(
                onClick = { expanded = true },
                modifier = dragModifier
                    .align(Alignment.CenterEnd)
                    .size(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0xCC111620))
                    .border(1.dp, Line, RoundedCornerShape(17.dp)),
            ) {
                Icon(Icons.Rounded.MoreVert, "打开工具，可按住拖动", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        } else {
            Row(
                modifier = dragModifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xEE111620))
                    .border(1.dp, Line, RoundedCornerShape(18.dp))
                    .padding(start = 10.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!compactTools) {
                    Text(
                        if (selected == Workspace.CODEX) "Codex · 可拖动" else "Harness · 可拖动",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
                IconButton(
                    onClick = {
                        dragX = 0f
                        dragY = 0f
                        preferences.edit()
                            .putFloat("${workspacePositionPrefix}_dx", 0f)
                            .putFloat("${workspacePositionPrefix}_dy", 0f)
                            .apply()
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Rounded.Refresh, "工具栏归位", tint = TextSecondary, modifier = Modifier.size(17.dp))
                }
                Box {
                    TextButton(
                        onClick = { zoomMenuVisible = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                    ) {
                        Text("字 ${webTextZoom}%", color = TextSecondary, fontSize = 10.sp)
                    }
                    DropdownMenu(
                        expanded = zoomMenuVisible,
                        onDismissRequest = { zoomMenuVisible = false },
                        containerColor = PanelRaised,
                        border = BorderStroke(1.dp, Line),
                    ) {
                        listOf(80, 90, 100, 110, 120, 130, 140).forEach { zoom ->
                            DropdownMenuItem(
                                text = { Text(if (zoom == 100) "${zoom}%（默认）" else "${zoom}%", color = TextPrimary) },
                                onClick = {
                                    onSetWebTextZoom(zoom)
                                    zoomMenuVisible = false
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = onToggleKeepScreenOn, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Rounded.Bolt,
                        if (keepScreenOn) "关闭屏幕常亮" else "保持屏幕常亮",
                        tint = if (keepScreenOn) Amber else TextSecondary,
                        modifier = Modifier.size(17.dp),
                    )
                }
                IconButton(onClick = onToggleImmersiveMode, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (immersiveMode) Icons.Rounded.Close else Icons.Rounded.OpenInNew,
                        if (immersiveMode) "退出沉浸模式" else "进入沉浸模式",
                        tint = if (immersiveMode) Violet else TextSecondary,
                        modifier = Modifier.size(17.dp),
                    )
                }
                IconButton(onClick = onReloadPage, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Refresh, "刷新当前页面", tint = Cyan, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onSwitchWorkspace, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Rounded.SwapHoriz,
                        if (selected == Workspace.CODEX) "切换到 Harness" else "切换到 Codex",
                        tint = Violet,
                        modifier = Modifier.size(17.dp),
                    )
                }
                IconButton(onClick = onDiagnostics, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.NetworkCheck, diagnosticsDescription, tint = diagnosticsTint, modifier = Modifier.size(17.dp))
                }
                TextButton(
                    onClick = onShowChrome,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp),
                ) { Text("工具", color = TextPrimary, fontSize = 10.sp) }
                IconButton(onClick = { expanded = false }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.Close, "收起工具", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsDialog(
    diagnostics: RuntimeDiagnostics?,
    loading: Boolean,
    error: String?,
    lastAction: String?,
    actionBusy: Boolean,
    currentWorkspaceLabel: String,
    uploadCacheInfo: UploadCacheInfo,
    uploadCacheLoading: Boolean,
    onRefresh: () -> Unit,
    onRecover: () -> Unit,
    onLogs: () -> Unit,
    onBatterySettings: () -> Unit,
    onRefreshUploadCache: () -> Unit,
    onClearStaleUploadCache: () -> Unit,
    onRestartCurrentWorkspace: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var copied by rememberSaveable { mutableStateOf(false) }
    fun diagnosticsText(): String {
        val snapshot = diagnostics
        return buildString {
            appendLine("Codex Harness Mobile 连接诊断")
            appendLine("检查时间：${snapshot?.checkedAt ?: "暂无"}")
            if (snapshot == null) {
                appendLine(error ?: "暂无诊断结果")
            } else {
                appendLine("网络：${snapshot.networkTransport}，可用=${snapshot.networkAvailable}，已验证=${snapshot.networkValidated}")
                appendLine("Termux：已安装=${snapshot.termuxInstalled}，命令权限=${snapshot.commandPermission}，忽略电池优化=${snapshot.termuxBatteryExempt}")
                appendLine("当前工作区：$currentWorkspaceLabel")
                appendLine("附件缓存：${uploadCacheInfo.fileCount} 个文件，占用 ${formatByteCount(uploadCacheInfo.totalBytes)}，可清理 ${uploadCacheInfo.staleFileCount} 个")
                snapshot.lastCommand?.let { appendLine("最近命令：$it") }
                snapshot.lastCommandResult?.let { appendLine("最近回调：$it") }
                snapshot.probes.forEach { probe ->
                    appendLine("${probe.name}：${if (probe.ok) "正常" else "失败"}，${probe.detail}${probe.latencyMs?.let { "，${it}ms" } ?: ""}")
                }
            }
        }
    }
    fun copyDiagnostics() {
        val text = diagnosticsText()
        context.getSystemService(ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText("连接诊断", text))
        copied = true
    }
    fun shareDiagnostics() {
        if (diagnostics == null && error == null) return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Codex Harness Mobile 连接诊断")
            putExtra(Intent.EXTRA_TEXT, diagnosticsText())
        }
        runCatching {
            context.startActivity(Intent.createChooser(shareIntent, "分享连接诊断"))
        }.onFailure {
            Toast.makeText(context, "无法打开系统分享面板", Toast.LENGTH_SHORT).show()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NetworkCheck, null, tint = Cyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("连接诊断", color = TextPrimary, modifier = Modifier.weight(1f))
                TextButton(onClick = ::copyDiagnostics, enabled = diagnostics != null || error != null) {
                    Text(if (copied) "已复制" else "复制", fontSize = 11.sp)
                }
                TextButton(onClick = ::shareDiagnostics, enabled = diagnostics != null || error != null) {
                    Text("分享", fontSize = 11.sp)
                }
                IconButton(onClick = onRefresh, enabled = !loading && !actionBusy, modifier = Modifier.size(32.dp)) {
                    if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Cyan)
                    else Icon(Icons.Rounded.Refresh, "重新检测", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            if (diagnostics == null && loading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Cyan)
                    Spacer(Modifier.height(10.dp))
                    Text("正在检测网络和本地服务…", color = TextSecondary, fontSize = 12.sp)
                }
            } else if (diagnostics == null) {
                Text(error ?: "还没有检测结果，点击右上角重新检测。", color = if (error == null) TextSecondary else Red, fontSize = 12.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = mobileDialogMaxHeight()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        DiagnosticLine(
                            "网络",
                            "${diagnostics.networkTransport}${if (diagnostics.networkValidated) " · 已验证" else if (diagnostics.networkAvailable) " · 可用但未验证" else " · 不可用"}",
                            diagnostics.networkAvailable,
                        )
                    }
                    item { DiagnosticLine("Termux", if (diagnostics.termuxInstalled) "已安装" else "未安装", diagnostics.termuxInstalled) }
                    item { DiagnosticLine("命令权限", if (diagnostics.commandPermission) "已授予" else "未授予", diagnostics.commandPermission) }
                    item {
                        DiagnosticLine(
                            "Termux 后台保活",
                            if (diagnostics.termuxBatteryExempt) "已忽略电池优化" else "可能被系统回收",
                            diagnostics.termuxBatteryExempt,
                        )
                    }
                    item {
                        TextButton(onClick = onBatterySettings, enabled = diagnostics.termuxInstalled) {
                            Text("打开 Termux 电池优化设置", fontSize = 11.sp)
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            border = BorderStroke(1.dp, Line),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("当前工作区服务", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("$currentWorkspaceLabel · 仅重启当前服务", color = TextSecondary, fontSize = 10.sp)
                                }
                                TextButton(
                                    onClick = onRestartCurrentWorkspace,
                                    enabled = !loading && !actionBusy,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                ) { Text(if (actionBusy) "处理中" else "重启") }
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Panel),
                            border = BorderStroke(1.dp, Line),
                        ) {
                            Column(
                                Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("附件暂存", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = onRefreshUploadCache,
                                        enabled = !uploadCacheLoading,
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        if (uploadCacheLoading) {
                                            CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = Cyan)
                                        } else {
                                            Icon(Icons.Rounded.Refresh, "刷新附件缓存", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Text(
                                    if (uploadCacheLoading) "正在读取缓存占用…"
                                    else "${uploadCacheInfo.fileCount} 个文件 · ${formatByteCount(uploadCacheInfo.totalBytes)}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                )
                                Text(
                                    if (uploadCacheInfo.staleFileCount > 0) {
                                        "${uploadCacheInfo.staleFileCount} 个文件超过 24 小时（${formatByteCount(uploadCacheInfo.staleBytes)}），可以清理。"
                                    } else {
                                        "超过 24 小时的暂存附件会在下次启动时自动清理。"
                                    },
                                    color = if (uploadCacheInfo.staleFileCount > 0) Amber else TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                )
                                TextButton(
                                    onClick = onClearStaleUploadCache,
                                    enabled = !uploadCacheLoading && uploadCacheInfo.staleFileCount > 0,
                                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                ) {
                                    Text("清理 24 小时前缓存", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    if (lastAction != null) {
                        item {
                            Text("最近状态", color = TextSecondary, fontSize = 11.sp)
                            Text(lastAction, color = TextPrimary, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (diagnostics.lastCommand != null) {
                        item { Text("最近下发：${diagnostics.lastCommand}", color = TextSecondary, fontSize = 11.sp) }
                    }
                    if (diagnostics.lastCommandResult != null) {
                        item {
                            DiagnosticLine(
                                "最近回调",
                                if (diagnostics.lastCommandSucceeded == true) "已收到服务地址" else "未收到服务地址",
                                diagnostics.lastCommandSucceeded == true,
                            )
                            Text(
                                diagnostics.lastCommandResult,
                                color = if (diagnostics.lastCommandSucceeded == true) TextSecondary else Red,
                                fontSize = 10.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    items(diagnostics.probes) { probe -> DiagnosticProbeRow(probe) }
                    item {
                        Text(
                            if (diagnostics.allLocalServicesHealthy) "本地服务均可访问；如果页面仍卡住，优先重新加载 WebView。"
                            else "至少一个本地端点未响应；如果尚未打开 Codex WebUI，3200 端口可暂时忽略。",
                            color = if (diagnostics.allLocalServicesHealthy) Green else Amber,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth()) {
                TextButton(onClick = onLogs, enabled = !loading && !actionBusy) { Text("查看最近 Termux 日志") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("关闭") }
                    Button(onClick = onRecover, enabled = !loading && !actionBusy) { Text("恢复 Codex/Harness") }
                }
            }
        },
        containerColor = PanelRaised,
    )
}

@Composable
private fun RuntimeLogsDialog(
    logs: String?,
    loading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var copied by rememberSaveable { mutableStateOf(false) }
    fun copyLogs() {
        val text = logs ?: return
        context.getSystemService(ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText("Termux 日志", text))
        copied = true
    }
    fun shareLogs() {
        val text = logs ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Codex Harness Mobile Termux 日志")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(Intent.createChooser(shareIntent, "分享 Termux 日志"))
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Terminal, null, tint = Cyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("最近 Termux 日志", color = TextPrimary, modifier = Modifier.weight(1f))
                TextButton(onClick = ::shareLogs, enabled = logs != null && !loading) {
                    Text("分享", fontSize = 11.sp)
                }
                TextButton(onClick = ::copyLogs, enabled = logs != null && !loading) {
                    Text(if (copied) "已复制" else "复制", fontSize = 11.sp)
                }
                IconButton(onClick = onRefresh, enabled = !loading, modifier = Modifier.size(32.dp)) {
                    if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Cyan)
                    else Icon(Icons.Rounded.Refresh, "重新读取日志", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            if (loading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Cyan)
                    Spacer(Modifier.height(10.dp))
                    Text("正在从 Termux 读取最近日志…", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = mobileDialogMaxHeight(150)).verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        logs ?: "暂无日志结果，点击右上角重新读取。",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        containerColor = PanelRaised,
    )
}

@Composable
private fun DiagnosticLine(label: String, value: String, ok: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            null,
            tint = if (ok) Green else Amber,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = if (ok) Green else Amber, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DiagnosticProbeRow(probe: DiagnosticProbe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Ink),
        border = BorderStroke(1.dp, Line),
    ) {
        Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.Top) {
            Icon(
                if (probe.ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                null,
                tint = if (probe.ok) Green else Red,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(probe.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(probe.detail, color = if (probe.ok) Green else Red, fontSize = 11.sp)
                Text(probe.endpoint, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            probe.latencyMs?.let { Text("${it}ms", color = TextSecondary, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun CodexScreen(
    modifier: Modifier,
    active: Boolean,
    health: LiveRuntimeState,
    client: CodexWebSocketClient,
    webUiUrl: String?,
    webUiApiKey: String?,
    textZoom: Int,
    sharedText: String?,
    onSharedTextConsumed: () -> Unit,
    onStartVoiceInput: () -> Unit,
    showFrameChrome: Boolean,
    onCloseDesktop: () -> Unit,
    onDiagnostics: () -> Unit,
    onRecover: () -> Unit,
    reloadRequest: Int,
    onStartWebUi: () -> Unit,
    onStartCodex: () -> Unit,
    onPermission: () -> Unit,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
    onDownload: (WebDownload) -> Unit,
    backgroundNotificationsEnabled: Boolean,
    onToggleBackgroundNotifications: () -> Unit,
) {
    var nativeMode by rememberSaveable { mutableStateOf(false) }
    var pendingSharedText by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(webUiUrl) {
        if (webUiUrl != null) nativeMode = false
    }
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            pendingSharedText = sharedText
            nativeMode = true
            onSharedTextConsumed()
        }
    }
    val showNative = nativeMode || pendingSharedText != null
    if (webUiUrl != null && !showNative) {
        CodexWebUiScreen(
            modifier = modifier,
            active = active,
            url = webUiUrl,
            apiKey = webUiApiKey,
            textZoom = textZoom,
            showFrameChrome = showFrameChrome,
            onClose = onCloseDesktop,
            onDiagnostics = onDiagnostics,
            onRecover = onRecover,
            reloadRequest = reloadRequest,
            onShowFileChooser = onShowFileChooser,
            onDownload = onDownload,
        )
        return
    }
    if (showNative) {
        CodexNativeScreen(
            modifier = modifier,
            active = active,
            health = health,
            client = client,
            onStart = onStartCodex,
            onPermission = onPermission,
            initialDraft = pendingSharedText,
            onStartVoiceInput = onStartVoiceInput,
            onBack = {
                nativeMode = false
                pendingSharedText = null
            },
            backgroundNotificationsEnabled = backgroundNotificationsEnabled,
            onToggleBackgroundNotifications = onToggleBackgroundNotifications,
        )
    } else {
        CodexWebUiLanding(modifier, health, onStartWebUi, onPermission, onOpenNative = { nativeMode = true })
    }
}

@Composable
private fun CodexWebUiLanding(
    modifier: Modifier,
    health: LiveRuntimeState,
    onStartWebUi: () -> Unit,
    onPermission: () -> Unit,
    onOpenNative: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.background(Ink),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column {
                Text("Codex 官方 WebUI", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("维护中的 Linux WebUI，底层由官方 Codex app-server 提供能力和回退", color = TextSecondary, fontSize = 12.sp)
            }
        }
        item {
            RuntimeBanner(
                online = health.codexWebUiOnline,
                title = if (health.codexWebUiOnline) "Codex WebUI 运行中" else "Codex WebUI 尚未打开",
                detail = if (health.codexWebUiOnline) "本机地址 127.0.0.1:3200，可直接恢复窗口。" else "首次启动会在 Debian 中准备 WebUI 和官方 Codex 运行时，之后可离线快速启动。",
                action = if (!health.commandPermission) "授予权限" else if (health.codexWebUiOnline) "打开 WebUI" else "启动并打开",
                onAction = if (!health.commandPermission) onPermission else onStartWebUi,
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = PanelRaised), border = BorderStroke(1.dp, Line)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("已接入功能", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("· 多会话与历史记录\n· 模型和思考强度选择\n· 文件浏览、编辑、生成和差异查看\n· 终端、Git、审批和中断\n· 手机端响应式布局与附件上传", color = TextSecondary, fontSize = 12.sp, lineHeight = 19.sp)
                }
            }
        }
        item {
            Button(
                onClick = if (!health.commandPermission) onPermission else onStartWebUi,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Violet, contentColor = Ink),
            ) {
                Icon(Icons.Rounded.OpenInNew, null, Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(if (!health.commandPermission) "授予 Termux 权限" else "启动 Codex WebUI")
            }
        }
        item {
            OutlinedButton(
                onClick = onOpenNative,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Line),
            ) {
                Icon(Icons.Rounded.Code, null, Modifier.size(17.dp), tint = Cyan)
                Spacer(Modifier.width(6.dp))
                Text("使用原生 Codex 对话（备用模式）")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CodexWebUiScreen(
    modifier: Modifier,
    active: Boolean,
    url: String,
    apiKey: String?,
    textZoom: Int,
    showFrameChrome: Boolean,
    onClose: () -> Unit,
    onDiagnostics: () -> Unit,
    onRecover: () -> Unit,
    reloadRequest: Int,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
    onDownload: (WebDownload) -> Unit,
) {
    val context = LocalContext.current
    var pageError by remember(url) { mutableStateOf<String?>(null) }
    var pageLoading by remember(url) { mutableStateOf(true) }
    var pageProgress by remember(url) { mutableStateOf(0) }
    var reloadKey by remember(url) { mutableStateOf(0) }
    var autoRetryAttempt by remember(url) { mutableStateOf(0) }
    var rendererRecoveryAttempt by remember(url) { mutableStateOf(0) }
    var autoRetryGeneration by remember(url) { mutableStateOf(0) }
    var loadWatchdogGeneration by remember(url) { mutableStateOf(0) }
    var webViewRef by remember(url) { mutableStateOf<WebView?>(null) }
    var canGoBack by remember(url) { mutableStateOf(false) }
    var canGoForward by remember(url) { mutableStateOf(false) }
    var findVisible by remember(url) { mutableStateOf(false) }
    fun syncNavigation(view: WebView) {
        canGoBack = view.canGoBack()
        canGoForward = view.canGoForward()
    }
    fun scheduleAutoRetry(view: WebView, failedUrl: String?, finalMessage: String) {
        loadWatchdogGeneration++
        if (autoRetryAttempt >= 2) {
            pageLoading = false
            pageError = finalMessage
            return
        }
        val attempt = autoRetryAttempt + 1
        autoRetryAttempt = attempt
        val generation = autoRetryGeneration + 1
        autoRetryGeneration = generation
        val delayMs = attempt * 1_000L
        pageLoading = true
        pageError = "页面暂时无法连接，${delayMs / 1_000} 秒后自动重试（$attempt/2）"
        view.postDelayed({
            if (autoRetryGeneration == generation && view.isAttachedToWindow && (failedUrl == null || view.url == failedUrl)) {
                pageError = null
                view.reload()
            }
        }, delayMs)
    }
    fun armLoadWatchdog(view: WebView, pageUrl: String?) {
        val generation = loadWatchdogGeneration + 1
        loadWatchdogGeneration = generation
        view.postDelayed({
            if (
                loadWatchdogGeneration == generation &&
                pageLoading &&
                pageError == null &&
                view.isAttachedToWindow &&
                (pageUrl == null || view.url == pageUrl)
            ) {
                val message = "工作台加载超时，请检查本地服务或稍后重试"
                scheduleAutoRetry(view, pageUrl, message)
                Log.w("CodexWebView", "main frame load watchdog timed out: $pageUrl")
            }
        }, 15_000L)
    }
    fun manuallyReload() {
        autoRetryAttempt = 0
        rendererRecoveryAttempt = 0
        autoRetryGeneration++
        loadWatchdogGeneration++
        pageError = null
        webViewRef?.reload()
    }
    LaunchedEffect(reloadRequest) {
        if (reloadRequest > 0) {
            autoRetryAttempt = 0
            rendererRecoveryAttempt = 0
            autoRetryGeneration++
            loadWatchdogGeneration++
            pageError = null
            reloadKey++
        }
    }
    LaunchedEffect(apiKey, webViewRef, pageLoading) {
        if (!apiKey.isNullOrBlank() && !pageLoading) {
            webViewRef?.evaluateJavascript(codexWebUiAutoLoginScript(apiKey), null)
        }
    }
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler(enabled = active && showFrameChrome && !imeVisible) {
        val webView = webViewRef
        if (webView?.canGoBack() == true) webView.goBack() else onClose()
    }
    Column(modifier.background(Ink)) {
        if (showFrameChrome) {
            Row(Modifier.fillMaxWidth().height(44.dp).background(Panel).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Code, null, tint = Violet, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text("Codex WebUI", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
                IconButton(
                    onClick = { webViewRef?.goBack() },
                    enabled = canGoBack,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "网页后退", tint = if (canGoBack) TextPrimary else Line, modifier = Modifier.size(17.dp))
                }
                IconButton(
                    onClick = { webViewRef?.goForward() },
                    enabled = canGoForward,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, "网页前进", tint = if (canGoForward) TextPrimary else Line, modifier = Modifier.size(17.dp))
                }
                IconButton(
                    onClick = ::manuallyReload,
                    enabled = webViewRef != null,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Rounded.Refresh, "刷新网页", tint = Cyan, modifier = Modifier.size(17.dp))
                }
                IconButton(
                    onClick = { findVisible = true },
                    enabled = webViewRef != null,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Rounded.Search, "查找网页内容", tint = TextSecondary, modifier = Modifier.size(17.dp))
                }
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) { Text("返回", fontSize = 11.sp) }
            }
        }
        Box(Modifier.fillMaxSize()) {
            key(reloadKey) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                        webViewRef = this
                        setBackgroundColor(android.graphics.Color.rgb(9, 12, 18))
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.allowContentAccess = true
                    settings.allowFileAccess = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.setSupportZoom(true)
                    settings.textZoom = textZoom
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    isVerticalScrollBarEnabled = true
                    overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
                    setOnTouchListener { view, event ->
                        val keepGesture = event.actionMasked != MotionEvent.ACTION_UP &&
                            event.actionMasked != MotionEvent.ACTION_CANCEL
                        view.parent?.requestDisallowInterceptTouchEvent(keepGesture)
                        if (event.actionMasked == MotionEvent.ACTION_UP) view.performClick()
                        false
                    }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, pageUrl: String?, favicon: android.graphics.Bitmap?) {
                                pageError = null
                                pageLoading = true
                                pageProgress = 5
                                armLoadWatchdog(view, pageUrl)
                                syncNavigation(view)
                                super.onPageStarted(view, pageUrl, favicon)
                            }

                            override fun onPageFinished(view: WebView, pageUrl: String?) {
                                super.onPageFinished(view, pageUrl)
                                loadWatchdogGeneration++
                                autoRetryAttempt = 0
                                rendererRecoveryAttempt = 0
                                autoRetryGeneration++
                                pageProgress = 100
                                pageLoading = false
                                syncNavigation(view)
                                if (!apiKey.isNullOrBlank()) {
                                    view.evaluateJavascript(codexWebUiAutoLoginScript(apiKey), null)
                                }
                                view.evaluateJavascript(HarnessViewportFix, null)
                            }

                            override fun onReceivedError(
                                view: WebView,
                                request: android.webkit.WebResourceRequest,
                                error: android.webkit.WebResourceError,
                            ) {
                                if (request.isForMainFrame) {
                                    val message = "Codex WebUI 页面暂时无法连接：${error.description}"
                                    scheduleAutoRetry(view, request.url.toString(), message)
                                    Log.w("CodexWebView", "main frame error ${error.errorCode}: ${error.description}")
                                }
                                super.onReceivedError(view, request, error)
                            }

                            override fun onReceivedHttpError(
                                view: WebView,
                                request: android.webkit.WebResourceRequest,
                                response: android.webkit.WebResourceResponse,
                            ) {
                                if (request.isForMainFrame && response.statusCode >= 400) {
                                    val message = "Codex WebUI 返回 HTTP ${response.statusCode}，请稍后重试"
                                    if (response.statusCode >= 500 || response.statusCode == 408 || response.statusCode == 429) {
                                        scheduleAutoRetry(view, request.url.toString(), message)
                                    } else {
                                        loadWatchdogGeneration++
                                        pageLoading = false
                                        pageError = message
                                    }
                                    Log.w("CodexWebView", "main frame HTTP ${response.statusCode}: ${request.url}")
                                }
                                super.onReceivedHttpError(view, request, response)
                            }

                            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                                loadWatchdogGeneration++
                                pageLoading = false
                                val rendererMessage = if (detail.didCrash()) {
                                    "Codex WebUI 渲染进程异常退出"
                                } else {
                                    "Codex WebUI 渲染进程已被系统回收"
                                }
                                if (rendererRecoveryAttempt == 0) {
                                    rendererRecoveryAttempt = 1
                                    pageLoading = true
                                    pageError = "$rendererMessage，正在自动恢复…"
                                    view.postDelayed({
                                        if (webViewRef === view) {
                                            pageError = null
                                            reloadKey++
                                        }
                                    }, 600L)
                                } else {
                                    pageError = "$rendererMessage，请重新加载"
                                }
                                Log.e("CodexWebView", "renderer gone crashed=${detail.didCrash()}")
                                return true
                            }
                        }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            pageProgress = newProgress.coerceIn(0, 100)
                            if (newProgress >= 100) pageLoading = false
                            super.onProgressChanged(view, newProgress)
                        }

                        override fun onShowFileChooser(webView: WebView?, callback: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean =
                            onShowFileChooser(callback, params)
                    }
                    setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
                        onDownload(WebDownload(downloadUrl, userAgent, contentDisposition, mimeType))
                    }
                        tag = "$url#$reloadKey"
                        doOnLayout { view -> (view as? WebView)?.loadUrl(url) }
                    }
                },
                update = { view ->
                    view.settings.textZoom = textZoom
                    val key = "$url#$reloadKey"
                    if (view.tag != key && view.width > 0 && view.height > 0) {
                        view.tag = key
                        view.loadUrl(url)
                    }
                    },
                    onRelease = { view ->
                        view.stopLoading()
                        view.webChromeClient = null
                        view.destroy()
                    },
                )
            }
            if (pageLoading && pageError == null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Panel.copy(alpha = 0.94f)),
                ) {
                    LinearProgressIndicator(
                        progress = { pageProgress.coerceIn(0, 100) / 100f },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = Violet,
                        trackColor = Line,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp, color = Violet)
                        Spacer(Modifier.width(7.dp))
                        Text("正在打开 Codex WebUI… ${pageProgress.coerceIn(0, 100)}%", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
            pageError?.let { message ->
                Card(
                    modifier = Modifier.align(Alignment.Center).padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PanelRaised),
                    border = BorderStroke(1.dp, Line),
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(message, color = TextPrimary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = {
                                autoRetryAttempt = 0
                                rendererRecoveryAttempt = 0
                                autoRetryGeneration++
                                loadWatchdogGeneration++
                                pageError = null
                                reloadKey++
                            }) { Text("重新加载") }
                            TextButton(onClick = onRecover) { Text("恢复服务") }
                            TextButton(onClick = onDiagnostics) { Text("连接诊断") }
                            }
                            TextButton(onClick = {
                                copyWebErrorDetails(context, "Codex WebUI 页面错误", message, webViewRef?.url ?: url)
                            }) { Text("复制详情") }
                        }
                }
            }
        }
    }
    if (findVisible) {
        WebFindDialog(
            webView = webViewRef,
            pageTitle = "Codex WebUI",
            onDismiss = { findVisible = false },
        )
    }
}

@Composable
private fun WebFindDialog(
    webView: WebView?,
    pageTitle: String,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }

    fun moveToMatch(forward: Boolean) {
        val term = query.trim()
        if (term.isBlank()) {
            webView?.clearMatches()
            return
        }
        webView?.findAllAsync(term)
        webView?.post { webView.findNext(forward) }
    }

    fun dismiss() {
        webView?.clearMatches()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = ::dismiss,
        title = { Text("查找网页内容") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("在 $pageTitle 当前页面内搜索", color = TextSecondary, fontSize = 11.sp)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("关键词") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { moveToMatch(true) }),
                )
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(onClick = { moveToMatch(false) }, enabled = webView != null && query.isNotBlank()) {
                        Text("上一个")
                    }
                    TextButton(onClick = { moveToMatch(true) }, enabled = webView != null && query.isNotBlank()) {
                        Text("下一个")
                    }
                }
                TextButton(onClick = ::dismiss) { Text("完成") }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                query = ""
                webView?.clearMatches()
            }) { Text("清除") }
        },
        containerColor = PanelRaised,
    )
}

@Composable
private fun CodexNativeScreen(
    modifier: Modifier,
    active: Boolean,
    health: LiveRuntimeState,
    client: CodexWebSocketClient,
    onStart: () -> Unit,
    onPermission: () -> Unit,
    initialDraft: String? = null,
    onStartVoiceInput: () -> Unit,
    onBack: () -> Unit,
    backgroundNotificationsEnabled: Boolean,
    onToggleBackgroundNotifications: () -> Unit,
) {
    val context = LocalContext.current
    val draftPreferences = remember(context) {
        context.getSharedPreferences("codex_mobile_settings", android.content.Context.MODE_PRIVATE)
    }
    var draft by rememberSaveable {
        mutableStateOf(initialDraft ?: (draftPreferences.getString("composer_draft", "") ?: ""))
    }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showMessageSearch by rememberSaveable { mutableStateOf(false) }
    var messageSearchQuery by rememberSaveable { mutableStateOf("") }
    var moreMenuVisible by remember { mutableStateOf(false) }
    LaunchedEffect(initialDraft) {
        if (!initialDraft.isNullOrBlank() && draft != initialDraft) {
            draft = initialDraft
            draftPreferences.edit().putString("composer_draft", initialDraft).apply()
        }
    }
    fun updateDraft(value: String) {
        draft = value
        draftPreferences.edit().putString("composer_draft", value).apply()
    }
    fun shareConversation() {
        val fullText = client.messages
            .filter { it.text.isNotBlank() }
            .joinToString("\n\n") { message ->
                val role = when (message.role) {
                    "user" -> "你"
                    "system" -> "系统"
                    else -> "Codex"
                }
                "【$role】\n${message.text.trim()}"
            }
        if (fullText.isBlank()) {
            Toast.makeText(context, "暂无可分享的对话内容", Toast.LENGTH_SHORT).show()
            return
        }
        val shareText = if (fullText.length > 100_000) {
            "（对话较长，已截取最后 100000 个字符）\n\n${fullText.takeLast(100_000)}"
        } else {
            fullText
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Codex 对话")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    },
                    "分享 Codex 对话",
                ),
            )
        }.onFailure {
            Toast.makeText(context, "无法打开系统分享面板", Toast.LENGTH_SHORT).show()
        }
    }
    val normalizedMessageSearch = messageSearchQuery.trim()
    val visibleMessages = if (normalizedMessageSearch.isBlank()) {
        client.messages
    } else {
        client.messages.filter { it.text.contains(normalizedMessageSearch, ignoreCase = true) }
    }
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler(enabled = active && !imeVisible, onBack = onBack)
    Column(modifier.background(Ink)) {
        RuntimeBanner(
            online = client.connected && !client.restoring,
            title = when {
                client.restoring -> "正在恢复上次对话"
                client.connected -> "Codex 已连接"
                else -> "Codex 尚未连接"
            },
            detail = client.statusText,
            action = when {
                client.restoring -> "请稍候"
                !health.commandPermission -> "授予权限"
                health.codexOnline -> "重新连接"
                else -> "启动 Codex"
            },
            onAction = when {
                client.restoring -> ({ })
                !health.commandPermission -> onPermission
                health.codexOnline -> client::connect
                else -> onStart
            },
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("原生 Codex 对话", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("对接手机内 Codex app-server", color = TextSecondary, fontSize = 10.sp, maxLines = 1)
            }
            TextButton(
                onClick = onBack,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
            ) { Text("返回", fontSize = 10.sp) }
            Spacer(Modifier.width(2.dp))
            OutlinedButton(
                onClick = {
                    client.newConversation()
                    updateDraft("")
                },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp),
                border = BorderStroke(1.dp, Line),
            ) {
                Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(3.dp))
                Text("新建", fontSize = 10.sp)
            }
            Box {
                IconButton(
                    onClick = { moreMenuVisible = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Rounded.MoreVert, "更多对话操作", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = moreMenuVisible,
                    onDismissRequest = { moreMenuVisible = false },
                    containerColor = PanelRaised,
                    border = BorderStroke(1.dp, Line),
                ) {
                    DropdownMenuItem(
                        text = { Text("Codex 运行配置", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Settings, null, tint = TextSecondary) },
                        onClick = { moreMenuVisible = false; showSettings = true },
                    )
                    DropdownMenuItem(
                        text = { Text("历史对话", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextSecondary) },
                        onClick = { moreMenuVisible = false; showHistory = true; client.loadThreads() },
                    )
                    DropdownMenuItem(
                        text = { Text("查找当前对话", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextSecondary) },
                        enabled = client.messages.any { it.text.isNotBlank() },
                        onClick = { moreMenuVisible = false; showMessageSearch = true },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (backgroundNotificationsEnabled) "关闭后台完成通知" else "开启后台完成通知",
                                color = TextPrimary,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (backgroundNotificationsEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                                null,
                                tint = if (backgroundNotificationsEnabled) Amber else TextSecondary,
                            )
                        },
                        onClick = { moreMenuVisible = false; onToggleBackgroundNotifications() },
                    )
                    DropdownMenuItem(
                        text = { Text("分享整段对话", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Share, null, tint = TextSecondary) },
                        enabled = client.messages.any { it.text.isNotBlank() },
                        onClick = { moreMenuVisible = false; shareConversation() },
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            WorkflowButton("检查工程") { updateDraft("请检查当前工作区的项目结构、关键配置和未提交改动，先给出简明报告。") }
            WorkflowButton("修改文件") { updateDraft("请先定位需要修改的文件，说明计划后直接完成修改并总结变更。") }
            WorkflowButton("生成文件") { updateDraft("请根据我的需求创建所需文件，写入完整内容并验证生成结果。") }
        }
        Text(
            if (normalizedMessageSearch.isBlank()) {
                "${if (client.settings.sandboxMode == "workspaceWrite") "工作区可读写" else "只读模式"} · ${client.settings.model} · 思考 ${client.settings.effort}"
            } else {
                "搜索“$normalizedMessageSearch” · 匹配 ${visibleMessages.size} 条"
            },
            color = if (client.settings.sandboxMode == "workspaceWrite") Green else Amber,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
        )
        HorizontalDivider(color = Line)
        if (client.messages.isEmpty() && client.restoring) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(Modifier.size(24.dp), color = Violet, strokeWidth = 2.dp)
                Spacer(Modifier.height(10.dp))
                Text("正在恢复上次对话…", color = TextSecondary, fontSize = 12.sp)
            }
        } else if (visibleMessages.isEmpty()) {
            if (client.messages.isEmpty()) {
                EmptyConversation(Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.Search, null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("没有匹配的消息", color = TextPrimary, fontSize = 14.sp)
                    Text("可以更换关键词或清除搜索", color = TextSecondary, fontSize = 11.sp)
                }
            }
        } else {
            val messageListState = rememberLazyListState()
            val messageListScope = rememberCoroutineScope()
            var showJumpToLatest by remember { mutableStateOf(false) }
            var observedMessageCount by remember { mutableStateOf(visibleMessages.size) }
            val lastMessageText = visibleMessages.lastOrNull()?.text
            LaunchedEffect(messageListState) {
                snapshotFlow {
                    val layoutInfo = messageListState.layoutInfo
                    val lastVisibleIndex = layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: -1
                    layoutInfo.totalItemsCount > 0 && lastVisibleIndex < layoutInfo.totalItemsCount - 2
                }.collect { awayFromLatest ->
                    showJumpToLatest = awayFromLatest
                }
            }
            LaunchedEffect(visibleMessages.size, client.busy, lastMessageText, normalizedMessageSearch) {
                if (visibleMessages.isNotEmpty()) {
                    val countChanged = visibleMessages.size != observedMessageCount
                    observedMessageCount = visibleMessages.size
                    val layoutInfo = messageListState.layoutInfo
                    val lastVisibleIndex = layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: -1
                    val nearBottom = layoutInfo.totalItemsCount == 0 ||
                        lastVisibleIndex >= layoutInfo.totalItemsCount - 2
                    // New messages should always be revealed. During streaming,
                    // only follow the response if the user was already near the
                    // bottom; otherwise a manual upward scroll must remain stable.
                    if (countChanged || nearBottom) {
                        val target = visibleMessages.lastIndex +
                            if (normalizedMessageSearch.isBlank() && client.busy && client.messages.lastOrNull()?.pending != true) 1 else 0
                        messageListState.animateScrollToItem(target)
                    }
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = messageListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visibleMessages) { MessageBubble(it) }
                    if (normalizedMessageSearch.isBlank() && client.busy && client.messages.lastOrNull()?.pending != true) item { WorkingIndicator() }
                }
                if (showJumpToLatest) {
                    TextButton(
                        onClick = {
                            messageListScope.launch {
                                val target = (visibleMessages.lastIndex +
                                    if (normalizedMessageSearch.isBlank() && client.busy && client.messages.lastOrNull()?.pending != true) 1 else 0)
                                    .coerceAtLeast(0)
                                messageListState.animateScrollToItem(target)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Violet.copy(alpha = 0.95f))
                            .border(1.dp, Violet, RoundedCornerShape(16.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                    ) {
                        Text("↓ 回到底部", color = Ink, fontSize = 11.sp)
                    }
                }
            }
        }
        Composer(
            value = draft,
            enabled = client.connected && !client.busy && !client.restoring,
            busy = client.busy,
            onValueChange = ::updateDraft,
            onVoiceInput = onStartVoiceInput,
            onStop = client::interrupt,
            onSend = {
                if (draft.isNotBlank()) {
                    client.sendUserMessage(draft)
                    updateDraft("")
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
    if (showMessageSearch) {
        AlertDialog(
            onDismissRequest = { showMessageSearch = false },
            title = { Text("查找当前对话") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("按消息内容筛选当前 Codex 对话", color = TextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = messageSearchQuery,
                        onValueChange = { messageSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("关键词") },
                        placeholder = { Text("例如：错误、测试、文件名") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { showMessageSearch = false }),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMessageSearch = false }) { Text("完成") }
            },
            dismissButton = {
                TextButton(onClick = { messageSearchQuery = "" }) { Text("清除") }
            },
            containerColor = PanelRaised,
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
            Column(
                modifier = Modifier.heightIn(max = mobileDialogMaxHeight(180)).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
    var historyQuery by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = historyQuery.trim()
    val filteredThreads = if (normalizedQuery.isBlank()) {
        client.threads
    } else {
        client.threads.filter { thread ->
            thread.title.contains(normalizedQuery, ignoreCase = true) ||
                thread.preview.contains(normalizedQuery, ignoreCase = true)
        }
    }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = historyQuery,
                    onValueChange = { historyQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("搜索历史对话") },
                    placeholder = { Text("标题或内容关键词") },
                    trailingIcon = {
                        if (historyQuery.isNotEmpty()) {
                            TextButton(
                                onClick = { historyQuery = "" },
                                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                            ) { Text("清除", fontSize = 10.sp) }
                        }
                    },
                )
                if (client.threads.isEmpty() && !client.loadingThreads) {
                    Text("暂无已保存对话。完成一次 Codex 对话后，这里会显示可继续的任务。", color = TextSecondary)
                } else if (filteredThreads.isEmpty() && !client.loadingThreads) {
                    Text("没有匹配的历史对话。可以换一个标题或内容关键词。", color = TextSecondary)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(mobileDialogMaxHeight(230)),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (client.loadingThreads) item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(Modifier.size(22.dp), color = Violet, strokeWidth = 2.dp)
                            }
                        }
                        items(filteredThreads, key = { it.id }) { thread ->
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
    val context = LocalContext.current
    val user = message.role == "user"
    val system = message.role == "system"
    fun copyMessage() {
        context.getSystemService(ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText("Codex 消息", message.text))
        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
    fun shareMessage() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, if (user) "Codex 提问" else "Codex 回复")
            putExtra(Intent.EXTRA_TEXT, message.text)
        }
        runCatching {
            context.startActivity(Intent.createChooser(shareIntent, "分享 Codex 消息"))
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(18.dp))
                .background(if (user) Color(0xFF282446) else if (system) Color(0xFF3B232A) else PanelRaised)
                .border(1.dp, if (system) Color(0xFF6B3440) else Line, RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (user) "你" else if (system) "系统" else "Codex",
                    color = if (user) Violet else if (system) Red else Cyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = ::copyMessage,
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp),
                ) { Text("复制", color = TextSecondary, fontSize = 10.sp) }
                IconButton(
                    onClick = ::shareMessage,
                    enabled = message.text.isNotBlank(),
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Rounded.Share, "分享消息", tint = TextSecondary, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            SelectionContainer {
                Text(message.text, color = TextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
            }
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
    onVoiceInput: () -> Unit,
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (enabled && value.isNotBlank()) onSend()
                },
            ),
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
            onClick = onVoiceInput,
            enabled = enabled,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (enabled) PanelRaised else Line),
        ) {
            Icon(Icons.Rounded.Mic, "语音输入", tint = if (enabled) Cyan else TextSecondary, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(6.dp))
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
    active: Boolean,
    health: LiveRuntimeState,
    harnessUrl: String?,
    textZoom: Int,
    showFrameChrome: Boolean,
    onCloseWeb: () -> Unit,
    onStartAndOpen: () -> Unit,
    onOpenTermux: () -> Unit,
    onPermission: () -> Unit,
    onDiagnostics: () -> Unit,
    onBatterySettings: () -> Unit,
    onRecover: () -> Unit,
    reloadRequest: Int,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
    onDownload: (WebDownload) -> Unit,
) {
    val context = LocalContext.current
    val url = harnessUrl
    if (url != null) {
        var pageError by remember(url) { mutableStateOf<String?>(null) }
        var pageLoading by remember(url) { mutableStateOf(true) }
        var pageProgress by remember(url) { mutableStateOf(0) }
        var reloadKey by remember(url) { mutableStateOf(0) }
        var autoRetryAttempt by remember(url) { mutableStateOf(0) }
        var rendererRecoveryAttempt by remember(url) { mutableStateOf(0) }
        var autoRetryGeneration by remember(url) { mutableStateOf(0) }
        var loadWatchdogGeneration by remember(url) { mutableStateOf(0) }
        var webViewRef by remember(url) { mutableStateOf<WebView?>(null) }
        var canGoBack by remember(url) { mutableStateOf(false) }
        var canGoForward by remember(url) { mutableStateOf(false) }
        var findVisible by remember(url) { mutableStateOf(false) }
        fun syncNavigation(view: WebView) {
            canGoBack = view.canGoBack()
            canGoForward = view.canGoForward()
        }
        fun scheduleAutoRetry(view: WebView, failedUrl: String?, finalMessage: String) {
            loadWatchdogGeneration++
            if (autoRetryAttempt >= 2) {
                pageLoading = false
                pageError = finalMessage
                return
            }
            val attempt = autoRetryAttempt + 1
            autoRetryAttempt = attempt
            val generation = autoRetryGeneration + 1
            autoRetryGeneration = generation
            val delayMs = attempt * 1_000L
            pageLoading = true
            pageError = "页面暂时无法连接，${delayMs / 1_000} 秒后自动重试（$attempt/2）"
            view.postDelayed({
                if (autoRetryGeneration == generation && view.isAttachedToWindow && (failedUrl == null || view.url == failedUrl)) {
                    pageError = null
                    view.reload()
                }
            }, delayMs)
        }
        fun armLoadWatchdog(view: WebView, pageUrl: String?) {
            val generation = loadWatchdogGeneration + 1
            loadWatchdogGeneration = generation
            view.postDelayed({
                if (
                    loadWatchdogGeneration == generation &&
                    pageLoading &&
                    pageError == null &&
                    view.isAttachedToWindow &&
                    (pageUrl == null || view.url == pageUrl)
                ) {
                    val message = "Harness 加载超时，请检查本地服务或稍后重试"
                    scheduleAutoRetry(view, pageUrl, message)
                    Log.w("HarnessWebView", "main frame load watchdog timed out: $pageUrl")
                }
            }, 15_000L)
        }
        fun manuallyReload() {
            autoRetryAttempt = 0
            rendererRecoveryAttempt = 0
            autoRetryGeneration++
            loadWatchdogGeneration++
            pageError = null
            webViewRef?.reload()
        }
        LaunchedEffect(reloadRequest) {
            if (reloadRequest > 0) {
                autoRetryAttempt = 0
                rendererRecoveryAttempt = 0
                autoRetryGeneration++
                loadWatchdogGeneration++
                pageError = null
                reloadKey++
            }
        }
        val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        BackHandler(enabled = active && showFrameChrome && !imeVisible) {
            val webView = webViewRef
            if (webView?.canGoBack() == true) webView.goBack() else onCloseWeb()
        }
        Column(modifier.background(Ink)) {
            if (showFrameChrome) {
                Row(Modifier.fillMaxWidth().height(44.dp).background(Panel).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Verified, null, tint = Green, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Harness", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
                    IconButton(
                        onClick = { webViewRef?.goBack() },
                        enabled = canGoBack,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "网页后退", tint = if (canGoBack) TextPrimary else Line, modifier = Modifier.size(17.dp))
                    }
                    IconButton(
                        onClick = { webViewRef?.goForward() },
                        enabled = canGoForward,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, "网页前进", tint = if (canGoForward) TextPrimary else Line, modifier = Modifier.size(17.dp))
                    }
                    IconButton(
                        onClick = ::manuallyReload,
                        enabled = webViewRef != null,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Rounded.Refresh, "刷新网页", tint = Cyan, modifier = Modifier.size(17.dp))
                    }
                    IconButton(
                        onClick = { findVisible = true },
                        enabled = webViewRef != null,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Rounded.Search, "查找网页内容", tint = TextSecondary, modifier = Modifier.size(17.dp))
                    }
                    TextButton(
                        onClick = onCloseWeb,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) { Text("返回", fontSize = 11.sp) }
                }
            }
            Box(Modifier.fillMaxSize()) {
                key(reloadKey) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                            webViewRef = this
                            setBackgroundColor(android.graphics.Color.rgb(9, 12, 18))
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.allowContentAccess = true
                        settings.allowFileAccess = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.setSupportZoom(true)
                        settings.textZoom = textZoom
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        isVerticalScrollBarEnabled = true
                        overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS
                        setOnTouchListener { view, event ->
                            val keepGesture = event.actionMasked != MotionEvent.ACTION_UP &&
                                event.actionMasked != MotionEvent.ACTION_CANCEL
                            view.parent?.requestDisallowInterceptTouchEvent(keepGesture)
                            if (event.actionMasked == MotionEvent.ACTION_UP) view.performClick()
                            false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, pageUrl: String?, favicon: android.graphics.Bitmap?) {
                                pageError = null
                                pageLoading = true
                                pageProgress = 5
                                armLoadWatchdog(view, pageUrl)
                                syncNavigation(view)
                                super.onPageStarted(view, pageUrl, favicon)
                            }

                            override fun onPageFinished(view: WebView, pageUrl: String?) {
                                super.onPageFinished(view, pageUrl)
                                loadWatchdogGeneration++
                                autoRetryAttempt = 0
                                rendererRecoveryAttempt = 0
                                autoRetryGeneration++
                                pageProgress = 100
                                pageLoading = false
                                syncNavigation(view)
                                view.evaluateJavascript(HarnessViewportFix, null)
                            }

                            override fun onReceivedError(
                                view: WebView,
                                request: android.webkit.WebResourceRequest,
                                error: android.webkit.WebResourceError,
                            ) {
                                if (request.isForMainFrame) {
                                    val message = "Harness 页面暂时无法连接：${error.description}"
                                    scheduleAutoRetry(view, request.url.toString(), message)
                                    Log.w("HarnessWebView", "main frame error ${error.errorCode}: ${error.description}")
                                }
                                super.onReceivedError(view, request, error)
                            }

                            override fun onReceivedHttpError(
                                view: WebView,
                                request: android.webkit.WebResourceRequest,
                                response: android.webkit.WebResourceResponse,
                            ) {
                                if (request.isForMainFrame && response.statusCode >= 400) {
                                    val message = "Harness 返回 HTTP ${response.statusCode}，请稍后重试"
                                    if (response.statusCode >= 500 || response.statusCode == 408 || response.statusCode == 429) {
                                        scheduleAutoRetry(view, request.url.toString(), message)
                                    } else {
                                        loadWatchdogGeneration++
                                        pageLoading = false
                                        pageError = message
                                    }
                                    Log.w("HarnessWebView", "main frame HTTP ${response.statusCode}: ${request.url}")
                                }
                                super.onReceivedHttpError(view, request, response)
                            }

                            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                                loadWatchdogGeneration++
                                pageLoading = false
                                val rendererMessage = if (detail.didCrash()) {
                                    "Harness 渲染进程异常退出"
                                } else {
                                    "Harness 渲染进程已被系统回收"
                                }
                                if (rendererRecoveryAttempt == 0) {
                                    rendererRecoveryAttempt = 1
                                    pageLoading = true
                                    pageError = "$rendererMessage，正在自动恢复…"
                                    view.postDelayed({
                                        if (webViewRef === view) {
                                            pageError = null
                                            reloadKey++
                                        }
                                    }, 600L)
                                } else {
                                    pageError = "$rendererMessage，请重新加载"
                                }
                                Log.e("HarnessWebView", "renderer gone crashed=${detail.didCrash()}")
                                return true
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                pageProgress = newProgress.coerceIn(0, 100)
                                if (newProgress >= 100) pageLoading = false
                                super.onProgressChanged(view, newProgress)
                            }

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
                        setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, _ ->
                            onDownload(WebDownload(downloadUrl, userAgent, contentDisposition, mimeType))
                        }
                            tag = "$url#$reloadKey"
                            doOnLayout { laidOutView ->
                                (laidOutView as? WebView)?.let { webView ->
                                    if (webView.url == null) webView.loadUrl(url)
                                }
                            }
                        }
                    },
                    update = { view ->
                        view.settings.textZoom = textZoom
                        val key = "$url#$reloadKey"
                        if (view.tag != key && view.width > 0 && view.height > 0) {
                            view.tag = key
                            view.loadUrl(url)
                        }
                        },
                        onRelease = { view ->
                            view.stopLoading()
                            view.webChromeClient = null
                            view.destroy()
                        },
                    )
                }
                if (pageLoading && pageError == null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(Panel.copy(alpha = 0.94f)),
                    ) {
                        LinearProgressIndicator(
                            progress = { pageProgress.coerceIn(0, 100) / 100f },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = Cyan,
                            trackColor = Line,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp, color = Cyan)
                            Spacer(Modifier.width(7.dp))
                            Text("正在打开 DeepSeek Harness… ${pageProgress.coerceIn(0, 100)}%", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
                pageError?.let { message ->
                    Card(
                        modifier = Modifier.align(Alignment.Center).padding(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PanelRaised),
                        border = BorderStroke(1.dp, Line),
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(message, color = TextPrimary, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = {
                                autoRetryAttempt = 0
                                rendererRecoveryAttempt = 0
                                autoRetryGeneration++
                                loadWatchdogGeneration++
                                pageError = null
                                reloadKey++
                            }) { Text("重新加载") }
                                TextButton(onClick = onRecover) { Text("恢复服务") }
                                TextButton(onClick = onDiagnostics) { Text("连接诊断") }
                            }
                            TextButton(onClick = {
                                copyWebErrorDetails(context, "DeepSeek Harness 页面错误", message, webViewRef?.url ?: url)
                            }) { Text("复制详情") }
                        }
                    }
                }
            }
        }
        if (findVisible) {
            WebFindDialog(
                webView = webViewRef,
                pageTitle = "DeepSeek Harness",
                onDismiss = { findVisible = false },
            )
        }
    } else {
        HarnessDashboard(modifier, health, onStartAndOpen, onOpenTermux, onPermission, onDiagnostics, onBatterySettings)
    }
}

@Composable
private fun HarnessDashboard(
    modifier: Modifier,
    health: LiveRuntimeState,
    onStartAndOpen: () -> Unit,
    onOpenTermux: () -> Unit,
    onPermission: () -> Unit,
    onDiagnostics: () -> Unit,
    onBatterySettings: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.background(Ink),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column {
                Text("DeepSeek Harness", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                 StatusRow(
                     Icons.Rounded.NetworkCheck,
                     "网络",
                     when {
                         !health.networkAvailable -> "无网络"
                         health.networkValidated -> "${health.networkTransport} · 已验证"
                         else -> "${health.networkTransport} · 未验证"
                     },
                     health.networkAvailable,
                 )
                 HorizontalDivider(color = Line)
                 StatusRow(Icons.Rounded.Memory, "Debian / ARM64", "Proot 本地环境", health.termuxInstalled)
                HorizontalDivider(color = Line)
                StatusRow(Icons.Rounded.Code, "Codex app-server", if (health.codexOnline) "运行中" else "已停止", health.codexOnline)
                HorizontalDivider(color = Line)
                StatusRow(Icons.Rounded.OpenInNew, "Codex WebUI", if (health.codexWebUiOnline) "运行中" else "未打开", health.codexWebUiOnline)
                HorizontalDivider(color = Line)
                StatusRow(Icons.Rounded.Language, "DeepSeek Harness", if (health.harnessOnline) "运行中" else "已停止", health.harnessOnline)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (health.termuxBatteryExempt) Color(0xFF10231D) else Color(0xFF241E14)),
                border = BorderStroke(1.dp, if (health.termuxBatteryExempt) Green.copy(alpha = 0.35f) else Amber.copy(alpha = 0.35f)),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Bolt, null, tint = if (health.termuxBatteryExempt) Green else Amber, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Termux 后台保活", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (health.termuxBatteryExempt) "已忽略电池优化，息屏后更不容易被系统回收。"
                            else "建议允许 Termux 后台运行，避免息屏后 DSH/Codex 被系统暂停。",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                    }
                    TextButton(
                        onClick = onBatterySettings,
                        enabled = health.termuxInstalled,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    ) { Text(if (health.termuxBatteryExempt) "查看设置" else "去设置", fontSize = 11.sp) }
                }
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
                    Text("打开 Termux")
                }
            }
        }
        item {
            OutlinedButton(
                onClick = onDiagnostics,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Line),
            ) {
                Icon(Icons.Rounded.NetworkCheck, null, Modifier.size(17.dp), tint = Cyan)
                Spacer(Modifier.width(6.dp))
                Text("检测网络、权限和本地连接")
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (online) Color(0xFF10231D) else Color(0xFF241E14)),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
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
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
    Row(Modifier.fillMaxWidth().background(Color(0xFF1B2230)).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Link, null, tint = Cyan, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) { Icon(Icons.Rounded.Close, "关闭", tint = TextSecondary) }
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
