package app.codexharness.mobile.runtime

import android.annotation.SuppressLint
import android.Manifest
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

data class LiveRuntimeState(
    val termuxInstalled: Boolean = false,
    val commandPermission: Boolean = false,
    val codexOnline: Boolean = false,
    val harnessOnline: Boolean = false,
    val checking: Boolean = false,
    val detail: String = "尚未检测",
)

@SuppressLint("SdCardPath")
class TermuxRuntimeManager(private val context: Context) {
    companion object {
        const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"
        private const val TERMUX_PACKAGE = "com.termux"
        private const val TERMUX_RUNNER = "com.termux.app.RunCommandService"
        private const val BASH = "/data/data/com.termux/files/usr/bin/bash"
        private const val HOME = "/data/data/com.termux/files/home"
        private val callbackIds = AtomicInteger(5000)
    }

    fun isTermuxInstalled(): Boolean = try {
        context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    fun hasCommandPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        RUN_COMMAND_PERMISSION,
    ) == PackageManager.PERMISSION_GRANTED

    fun openTermux(): Result<Unit> = runCatching {
        val launch = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
            ?: error("没有找到 Termux")
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }

    fun startAll(): Result<Unit> = runCatching {
        requireReady()
        startCodex().getOrThrow()
        startHarness().getOrThrow()
    }

    fun startCodex(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            if ! command -v proot-distro >/dev/null 2>&1; then exit 31; fi
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            if ! /system/bin/toybox nc -z -w 1 127.0.0.1 4500 >/dev/null 2>&1; then
              nohup proot-distro login debian -- bash -lc 'exec codex app-server --listen ws://127.0.0.1:4500' >"${'$'}HOME/.codex-harness-mobile/codex.log" 2>&1 &
              echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/codex.pid"
            fi
        """.trimIndent()
        runTermux(script, "启动 Codex 服务")
    }

    fun restartCodexDesktopAndOpen(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            kill_tree() {
              pid="${'$'}1"
              for child in ${'$'}(pgrep -P "${'$'}pid" 2>/dev/null); do kill_tree "${'$'}child"; done
              kill "${'$'}pid" >/dev/null 2>&1 || true
            }
            if [ -s "${'$'}HOME/.codex-harness-mobile/cdesktop.pid" ]; then
              kill_tree "${'$'}(cat "${'$'}HOME/.codex-harness-mobile/cdesktop.pid")"
              rm -f "${'$'}HOME/.codex-harness-mobile/cdesktop.pid"
            fi
            for attempt in 1 2 3 4 5 6 7 8; do
              if ! /system/bin/toybox nc -z -w 1 127.0.0.1 3200 >/dev/null 2>&1; then break; fi
              sleep 1
            done
            rm -f "${'$'}HOME/.codex-harness-mobile/cdesktop.log"
            # The ARM64 runtime is already staged in the wrapper's cache (see
            # .tools/do-stage.sh), so run the real binary directly. Going through
            # `npx cdesktop` would re-resolve the package on every launch, and its
            # own ~49 MB binary download stalls on this phone's network.
            # All one line: a trailing backslash does not survive Intent extras.
            CD="${'$'}HOME/.cdesktop/bin/v0.2.3-20260519022845/linux-arm64/cdesktop"
            if [ -x "${'$'}CD" ]; then
              nohup proot-distro login debian -- env HOST=127.0.0.1 PORT=3200 "${'$'}CD" >"${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>&1 &
            elif command -v cdesktop >/dev/null 2>&1; then
              nohup proot-distro login debian -- env HOST=127.0.0.1 PORT=3200 cdesktop >"${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>&1 &
            else
              nohup proot-distro login debian -- env HOST=127.0.0.1 PORT=3200 npx --yes cdesktop@0.2.3 >"${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>&1 &
            fi
            echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/cdesktop.pid"
            # cdesktop has to boot a Rust backend and bind the port; over mobile
            # data with the binaries already cached this still needs well over 30s.
            for attempt in ${'$'}(seq 1 120); do
              if /system/bin/toybox nc -z -w 1 127.0.0.1 3200 >/dev/null 2>&1; then
                printf '%s\n' 'http://127.0.0.1:3200'
                exit 0
              fi
              sleep 1
            done
            echo 'Codex 工作台启动超时，请查看 cdesktop.log' >&2
            exit 42
        """.trimIndent()
        runTermux(script, "启动 Codex 成熟工作台", returnResult = true, bridgeKind = "codex")
    }

    fun startHarness(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            if ! /system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1; then
              nohup proot-distro login debian -- bash -lc 'exec node --expose-internals /usr/bin/dsh web --no-open --port 3080' >"${'$'}HOME/.codex-harness-mobile/harness.log" 2>&1 &
              echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/harness.pid"
            fi
        """.trimIndent()
        runTermux(script, "启动 DeepSeek Harness")
    }

    fun restartHarnessAndOpen(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            # Keep every pipeline on one physical line: a trailing backslash does
            # not survive the trip through Intent extras reliably, and a broken
            # continuation makes bash fail to parse the whole script.
            url=${'$'}(grep -a 'dsh web: http' "${'$'}HOME/.codex-harness-mobile/harness.log" 2>/dev/null | tail -n 1 | sed 's/.*dsh web: //')
            if /system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1 && [ -n "${'$'}url" ]; then
              printf '%s\n' "${'$'}url"
              exit 0
            fi
            if [ -s "${'$'}HOME/.codex-harness-mobile/harness.pid" ]; then
              kill "${'$'}(cat "${'$'}HOME/.codex-harness-mobile/harness.pid")" >/dev/null 2>&1 || true
              rm -f "${'$'}HOME/.codex-harness-mobile/harness.pid"
            fi
            for attempt in 1 2 3 4 5 6 7 8; do
              if ! /system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1; then
                break
              fi
              sleep 1
            done
            rm -f "${'$'}HOME/.codex-harness-mobile/harness.log"
            # DSH >= 0.1.6 boots a shim for the native node-addon-require-builtin
            # addon, which has no Android build; the shim needs Node internals
            # exposed. NODE_OPTIONS refuses --expose-internals, so the flag has to
            # be passed straight to node.
            nohup proot-distro login debian -- bash -lc 'exec node --expose-internals /usr/bin/dsh web --no-open --port 3080' >"${'$'}HOME/.codex-harness-mobile/harness.log" 2>&1 &
            echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/harness.pid"
            for attempt in ${'$'}(seq 1 90); do
              url=${'$'}(grep -a 'dsh web: http' "${'$'}HOME/.codex-harness-mobile/harness.log" 2>/dev/null | tail -n 1 | sed 's/.*dsh web: //')
              if [ -n "${'$'}url" ]; then
                printf '%s\n' "${'$'}url"
                exit 0
              fi
              sleep 1
            done
            echo 'Harness 启动超时，请点“终端日志”查看 harness.log' >&2
            exit 41
        """.trimIndent()
        runTermux(script, "打开 Harness 图形界面", returnResult = true, bridgeKind = "harness")
    }

    fun stopAll(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            for service in codex harness cdesktop; do
              pid_file="${'$'}HOME/.codex-harness-mobile/${'$'}service.pid"
              if [ -s "${'$'}pid_file" ]; then
                pid="${'$'}(cat "${'$'}pid_file")"
                for child in ${'$'}(pgrep -P "${'$'}pid" 2>/dev/null); do kill "${'$'}child" >/dev/null 2>&1 || true; done
                kill "${'$'}pid" >/dev/null 2>&1 || true
                rm -f "${'$'}pid_file"
              fi
            done
        """.trimIndent()
        runTermux(script, "停止本地服务")
    }

    suspend fun health(): LiveRuntimeState = coroutineScope {
        val termux = isTermuxInstalled()
        val permission = hasCommandPermission()
        val codex = async { probe("http://127.0.0.1:4500/readyz") }
        val harness = async { probe("http://127.0.0.1:3080/") }
        val c = codex.await()
        val h = harness.await()
        LiveRuntimeState(
            termuxInstalled = termux,
            commandPermission = permission,
            codexOnline = c,
            harnessOnline = h,
            detail = when {
                !termux -> "尚未安装 Termux"
                !permission -> "需要授予 Termux 命令权限"
                c && h -> "Codex 与 Harness 均已就绪"
                c -> "Codex 已就绪，Harness 未启动"
                h -> "Harness 已就绪，Codex 未启动"
                else -> "后端服务未启动"
            },
        )
    }

    private fun requireReady() {
        check(isTermuxInstalled()) { "请先安装 Termux" }
        check(hasCommandPermission()) { "请授予“在 Termux 中运行命令”权限" }
    }

    private fun runTermux(
        script: String,
        label: String,
        returnResult: Boolean = false,
        bridgeKind: String? = null,
    ) {
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            component = ComponentName(TERMUX_PACKAGE, TERMUX_RUNNER)
            putExtra("com.termux.RUN_COMMAND_PATH", BASH)
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-lc", script))
            putExtra("com.termux.RUN_COMMAND_WORKDIR", HOME)
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            putExtra("com.termux.RUN_COMMAND_COMMAND_LABEL", label)
            if (returnResult) {
                val callback = Intent(context, TermuxResultReceiver::class.java).apply {
                    action = "app.codexharness.mobile.TERMUX_RESULT"
                    bridgeKind?.let { putExtra("app.codexharness.mobile.BRIDGE_KIND", it) }
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    callbackIds.incrementAndGet(),
                    callback,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                )
                putExtra("com.termux.RUN_COMMAND_PENDING_INTENT", pendingIntent)
            }
        }
        context.startService(intent)
    }

    private suspend fun probe(address: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = URL(address).openConnection() as HttpURLConnection
            connection.connectTimeout = 1200
            connection.readTimeout = 1200
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connect()
            connection.responseCode in 100..599
        } catch (_: Exception) {
            false
        }
    }
}
