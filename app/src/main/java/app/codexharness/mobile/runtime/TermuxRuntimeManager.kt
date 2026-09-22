package app.codexharness.mobile.runtime

import android.annotation.SuppressLint
import android.Manifest
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
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
    val termuxBatteryExempt: Boolean = false,
    val networkTransport: String = "无活动网络",
    val networkAvailable: Boolean = false,
    val networkValidated: Boolean = false,
    val codexOnline: Boolean = false,
    val codexDesktopOnline: Boolean = false,
    val harnessOnline: Boolean = false,
    val checking: Boolean = false,
    val detail: String = "尚未检测",
)

data class DiagnosticProbe(
    val name: String,
    val endpoint: String,
    val ok: Boolean,
    val statusCode: Int? = null,
    val latencyMs: Long? = null,
    val detail: String,
)

data class RuntimeDiagnostics(
    val checkedAt: Long,
    val networkTransport: String,
    val networkAvailable: Boolean,
    val networkValidated: Boolean,
    val termuxInstalled: Boolean,
    val commandPermission: Boolean,
    val termuxBatteryExempt: Boolean = false,
    val lastCommand: String? = null,
    val lastCommandResult: String? = null,
    val lastCommandSucceeded: Boolean? = null,
    val probes: List<DiagnosticProbe> = emptyList(),
) {
    val allLocalServicesHealthy: Boolean
        get() = probes.filter { it.endpoint.startsWith("http://127.0.0.1") }.all { it.ok }
}

@SuppressLint("SdCardPath")
class TermuxRuntimeManager(private val context: Context) {
    companion object {
        const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"
        private const val TERMUX_PACKAGE = "com.termux"
        private const val TERMUX_RUNNER = "com.termux.app.RunCommandService"
        private const val BASH = "/data/data/com.termux/files/usr/bin/bash"
        private const val HOME = "/data/data/com.termux/files/home"
        private const val TAG = "TermuxRuntimeManager"
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

    @Volatile
    private var lastHealthSignature: String? = null

    @Volatile
    private var lastCommandLabel: String? = null

    fun openTermux(): Result<Unit> = runCatching {
        val launch = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
            ?: error("没有找到 Termux")
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launch)
    }

    fun isTermuxBatteryExempt(): Boolean {
        if (!isTermuxInstalled()) return false
        val power = context.getSystemService(PowerManager::class.java)
        return power?.isIgnoringBatteryOptimizations(TERMUX_PACKAGE) == true
    }

    fun openTermuxBatterySettings(): Result<Unit> = runCatching {
        check(isTermuxInstalled()) { "请先安装 Termux" }
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$TERMUX_PACKAGE")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val settingsIntent = if (direct.resolveActivity(context.packageManager) != null) direct else fallback
        check(settingsIntent.resolveActivity(context.packageManager) != null) { "系统没有可用的电池优化设置页面" }
        context.startActivity(settingsIntent)
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
              nohup setsid proot-distro login debian -- bash -lc 'exec codex app-server --listen ws://127.0.0.1:4500' </dev/null >"${'$'}HOME/.codex-harness-mobile/codex.log" 2>&1 &
              echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/codex.pid"
            fi
        """.trimIndent()
        runTermux(script, "启动 Codex 服务")
    }

    fun restartCodexDesktopAndOpen(forceRestart: Boolean = false): Result<Unit> = runCatching {
        requireReady()
        val script = """
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            if [ "${'$'}forceRestart" != "true" ] && /system/bin/toybox nc -z -w 1 127.0.0.1 3200 >/dev/null 2>&1; then
              printf '%s\n' 'http://127.0.0.1:3200'
              exit 0
            fi
            kill_tree() {
              pid="${'$'}1"
              for child in ${'$'}(pgrep -P "${'$'}pid" 2>/dev/null); do kill_tree "${'$'}child"; done
              kill "${'$'}pid" >/dev/null 2>&1 || true
            }
            # A stale/missing pid file must not leave a cdesktop process
            # holding port 3200 during an explicit restart.
            for old_pid in ${'$'}(pgrep -f '/cdesktop' 2>/dev/null); do
              if [ "${'$'}old_pid" != "${'$'}${'$'}" ]; then kill_tree "${'$'}old_pid"; fi
            done
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
              nohup setsid proot-distro login debian -- env HOST=127.0.0.1 PORT=3200 "${'$'}CD" </dev/null >"${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>&1 &
            elif command -v cdesktop >/dev/null 2>&1; then
              nohup setsid proot-distro login debian -- env HOST=127.0.0.1 PORT=3200 cdesktop </dev/null >"${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>&1 &
            else
              nohup setsid proot-distro login debian -- env HOST=127.0.0.1 PORT=3200 npx --yes cdesktop@0.2.3 </dev/null >"${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>&1 &
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
        """.trimIndent().replace("${'$'}forceRestart", forceRestart.toString())
        runTermux(script, "启动 Codex 成熟工作台", returnResult = true, bridgeKind = "codex")
    }

    fun startHarness(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            if ! /system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1; then
              nohup setsid proot-distro login debian -- bash -lc 'exec node --expose-internals /usr/bin/dsh web --no-open --port 3080' </dev/null >"${'$'}HOME/.codex-harness-mobile/harness.log" 2>&1 &
              echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/harness.pid"
            fi
        """.trimIndent()
        runTermux(script, "启动 DeepSeek Harness")
    }

    fun restartHarnessAndOpen(forceRestart: Boolean = false): Result<Unit> = runCatching {
        requireReady()
        val script = """
            mkdir -p "${'$'}HOME/.codex-harness-mobile"
            # Keep every pipeline on one physical line: a trailing backslash does
            # not survive the trip through Intent extras reliably, and a broken
            # continuation makes bash fail to parse the whole script.
            url=${'$'}(grep -a 'dsh web: http' "${'$'}HOME/.codex-harness-mobile/harness.log" 2>/dev/null | tail -n 1 | sed 's/.*dsh web: //')
            if [ "${'$'}forceRestart" != "true" ] && /system/bin/toybox nc -z -w 1 127.0.0.1 3080 >/dev/null 2>&1 && [ -n "${'$'}url" ]; then
              printf '%s\n' "${'$'}url"
              exit 0
            fi
            kill_tree() {
              pid="${'$'}1"
              [ -n "${'$'}pid" ] || return 0
              for child in ${'$'}(pgrep -P "${'$'}pid" 2>/dev/null); do kill_tree "${'$'}child"; done
              kill "${'$'}pid" >/dev/null 2>&1 || true
            }
            # The pid file can be stale after Termux has been closed. In that
            # case the old node process may survive its proot parent and keep
            # 3080 busy. Kill only processes whose command line is the DSH web
            # entrypoint; skip this shell itself because the command text also
            # contains that entrypoint.
            for old_pid in ${'$'}(pgrep -f '/usr/bin/dsh web' 2>/dev/null); do
              if [ "${'$'}old_pid" != "${'$'}${'$'}" ]; then kill_tree "${'$'}old_pid"; fi
            done
            if [ -s "${'$'}HOME/.codex-harness-mobile/harness.pid" ]; then
              kill_tree "${'$'}(cat "${'$'}HOME/.codex-harness-mobile/harness.pid")"
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
            nohup setsid proot-distro login debian -- bash -lc 'exec node --expose-internals /usr/bin/dsh web --no-open --port 3080' </dev/null >"${'$'}HOME/.codex-harness-mobile/harness.log" 2>&1 &
            echo ${'$'}! >"${'$'}HOME/.codex-harness-mobile/harness.pid"
            for attempt in ${'$'}(seq 1 90); do
              url=${'$'}(grep -a 'dsh web: http' "${'$'}HOME/.codex-harness-mobile/harness.log" 2>/dev/null | tail -n 1 | sed 's/.*dsh web: //')
              if [ -n "${'$'}url" ]; then
                printf '%s\n' "${'$'}url"
                exit 0
              fi
              sleep 1
            done
            echo 'Harness 启动超时，请打开 Termux 查看 harness.log' >&2
            exit 41
        """.trimIndent().replace("${'$'}forceRestart", forceRestart.toString())
        runTermux(script, "打开 Harness 图形界面", returnResult = true, bridgeKind = "harness")
    }

    fun stopAll(): Result<Unit> = runCatching {
        requireReady()
        val script = """
            kill_tree() {
              pid="${'$'}1"
              [ -n "${'$'}pid" ] || return 0
              for child in ${'$'}(pgrep -P "${'$'}pid" 2>/dev/null); do kill_tree "${'$'}child"; done
              kill "${'$'}pid" >/dev/null 2>&1 || true
            }
            for service in codex harness cdesktop; do
              pid_file="${'$'}HOME/.codex-harness-mobile/${'$'}service.pid"
              if [ -s "${'$'}pid_file" ]; then
                pid="${'$'}(cat "${'$'}pid_file")"
                kill_tree "${'$'}pid"
                rm -f "${'$'}pid_file"
              fi
            done
            for pattern in '/usr/bin/dsh web' 'codex app-server' '/cdesktop'; do
              for old_pid in ${'$'}(pgrep -f "${'$'}pattern" 2>/dev/null); do
                if [ "${'$'}old_pid" != "${'$'}${'$'}" ]; then kill_tree "${'$'}old_pid"; fi
              done
            done
        """.trimIndent()
        runTermux(script, "停止本地服务")
    }

    fun readRuntimeLogs(): Result<Unit> = runCatching {
        requireReady()
        // Keep the command on one physical line: Termux receives it through an
        // Intent extra, and the log callback only needs a bounded recent tail.
        val script = """printf '%s\n' '--- harness.log ---'; tail -n 80 "${'$'}HOME/.codex-harness-mobile/harness.log" 2>/dev/null || true; printf '%s\n' '--- cdesktop.log ---'; tail -n 80 "${'$'}HOME/.codex-harness-mobile/cdesktop.log" 2>/dev/null || true; printf '%s\n' '--- codex.log ---'; tail -n 80 "${'$'}HOME/.codex-harness-mobile/codex.log" 2>/dev/null || true"""
        runTermux(script, "读取最近运行日志", returnResult = true, bridgeKind = "logs")
    }

    suspend fun health(): LiveRuntimeState = coroutineScope {
        val termux = isTermuxInstalled()
        val permission = hasCommandPermission()
        val batteryExempt = isTermuxBatteryExempt()
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity?.activeNetwork
        val capabilities = network?.let { connectivity.getNetworkCapabilities(it) }
        val networkAvailable = network != null && capabilities != null
        val networkValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val networkTransport = when {
            capabilities == null -> "无活动网络"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi‑Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动网络"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "有线网络"
            else -> "其他网络"
        }
        val codex = async { probe("http://127.0.0.1:4500/readyz") }
        val codexDesktop = async { probe("http://127.0.0.1:3200/") }
        val harness = async { probe("http://127.0.0.1:3080/") }
        val c = codex.await()
        val d = codexDesktop.await()
        val h = harness.await()
        val serviceDetail = when {
            !termux -> "尚未安装 Termux"
            !permission -> "需要授予 Termux 命令权限"
            c && d && h -> "Codex、工作台与 Harness 均已就绪"
            c && h -> "Codex 与 Harness 已就绪，工作台待打开"
            c && d -> "Codex 与工作台已就绪，Harness 未启动"
            d && h -> "工作台与 Harness 已就绪，Codex 服务未启动"
            c -> "Codex 已就绪，Harness 未启动"
            h -> "Harness 已就绪，Codex 未启动"
            d -> "Codex 工作台已就绪，其他服务未启动"
            else -> "后端服务未启动"
        }
        val state = LiveRuntimeState(
            termuxInstalled = termux,
            commandPermission = permission,
            termuxBatteryExempt = batteryExempt,
            networkTransport = networkTransport,
            networkAvailable = networkAvailable,
            networkValidated = networkValidated,
            codexOnline = c,
            codexDesktopOnline = d,
            harnessOnline = h,
            detail = if ((c || d || h) && !networkAvailable) {
                "$serviceDetail · 当前无外网"
            } else if ((c || d || h) && !networkValidated) {
                "$serviceDetail · 网络未验证"
            } else {
                serviceDetail
            },
        )
        val signature = "${state.termuxInstalled}/${state.commandPermission}/${state.termuxBatteryExempt}/${state.networkTransport}/${state.networkAvailable}/${state.networkValidated}/${state.codexOnline}/${state.codexDesktopOnline}/${state.harnessOnline}"
        if (signature != lastHealthSignature) {
            lastHealthSignature = signature
            Log.i(TAG, "health=$signature detail=${state.detail}")
        }
        state
    }

    suspend fun diagnostics(): RuntimeDiagnostics = coroutineScope {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity?.activeNetwork
        val capabilities = network?.let { connectivity.getNetworkCapabilities(it) }
        val networkAvailable = network != null && capabilities != null
        val networkValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val networkTransport = when {
            capabilities == null -> "无活动网络"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi‑Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动网络"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "有线网络"
            else -> "其他网络"
        }
        val endpoints = listOf(
            "Codex app-server" to "http://127.0.0.1:4500/readyz",
            "Codex 工作台" to "http://127.0.0.1:3200/",
            "DeepSeek Harness" to "http://127.0.0.1:3080/",
            "公网 HTTPS" to "https://www.gstatic.com/generate_204",
        )
        val probes = endpoints.map { (name, endpoint) ->
            async(Dispatchers.IO) { probeDetailed(name, endpoint) }
        }.map { it.await() }
        RuntimeDiagnostics(
            checkedAt = System.currentTimeMillis(),
            networkTransport = networkTransport,
            networkAvailable = networkAvailable,
            networkValidated = networkValidated,
            termuxInstalled = isTermuxInstalled(),
            commandPermission = hasCommandPermission(),
            termuxBatteryExempt = isTermuxBatteryExempt(),
            lastCommand = lastCommandLabel,
            lastCommandResult = TermuxCommandState.lastOutput,
            lastCommandSucceeded = TermuxCommandState.lastSucceeded,
            probes = probes,
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
        lastCommandLabel = label
        TermuxCommandState.lastLabel = label
        TermuxCommandState.lastOutput = null
        TermuxCommandState.lastSucceeded = null
        TermuxCommandState.completedAt = null
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
        Log.i(TAG, "dispatch label=$label returnResult=$returnResult bridge=$bridgeKind")
        context.startService(intent)
    }

    private fun probeDetailed(name: String, address: String): DiagnosticProbe {
        val startedAt = System.nanoTime()
        var connection: HttpURLConnection? = null
        return try {
            connection = URL(address).openConnection() as HttpURLConnection
            connection.connectTimeout = 2_500
            connection.readTimeout = 2_500
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            val status = connection.responseCode
            val elapsed = (System.nanoTime() - startedAt) / 1_000_000L
            val authRequired = address.startsWith("http://127.0.0.1") && status in 401..403
            val reachable = status in 200..399 || authRequired
            DiagnosticProbe(
                name = name,
                endpoint = address,
                ok = reachable,
                statusCode = status,
                latencyMs = elapsed,
                detail = when {
                    status in 200..399 -> "连接正常"
                    authRequired -> "服务可达，需要访问令牌"
                    else -> "HTTP $status"
                },
            )
        } catch (error: Exception) {
            val elapsed = (System.nanoTime() - startedAt) / 1_000_000L
            DiagnosticProbe(
                name = name,
                endpoint = address,
                ok = false,
                latencyMs = elapsed,
                detail = error.message?.take(72) ?: error.javaClass.simpleName,
            )
        } finally {
            connection?.disconnect()
        }
    }

    private suspend fun probe(address: String): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(address).openConnection() as HttpURLConnection
            connection.connectTimeout = 1200
            connection.readTimeout = 1200
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connect()
            connection.responseCode in 100..599
        } catch (_: Exception) {
            false
        } finally {
            connection?.disconnect()
        }
    }
}
