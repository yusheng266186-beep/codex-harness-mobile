package app.codexharness.mobile.runtime

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

data class CodexUiMessage(
    val role: String,
    val text: String,
    val pending: Boolean = false,
)

data class PendingApproval(
    val requestId: Any,
    val command: String,
    val reason: String,
)

data class CodexRunSettings(
    val model: String = "gpt-5.6-terra",
    val effort: String = "medium",
    val approvalPolicy: String = "on-request",
    val sandboxMode: String = "workspaceWrite",
    val cwd: String = "",
)

data class CodexThreadSummary(val id: String, val title: String, val preview: String)

class CodexWebSocketClient(context: Context) {
    private val preferences = context.getSharedPreferences("codex_mobile_settings", Context.MODE_PRIVATE)
    private val main = Handler(Looper.getMainLooper())
    private val http = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var socket: WebSocket? = null
    private var threadId: String? = null
    private var queuedText: String? = null
    private val ids = AtomicInteger(10)

    var connected by mutableStateOf(false)
        private set
    var connecting by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var activeTurnId by mutableStateOf<String?>(null)
        private set
    var statusText by mutableStateOf("未连接")
        private set
    var approval by mutableStateOf<PendingApproval?>(null)
        private set
    var settings by mutableStateOf(
        CodexRunSettings(
            model = preferences.getString("model", "gpt-5.6-terra") ?: "gpt-5.6-terra",
            effort = preferences.getString("effort", "medium") ?: "medium",
            approvalPolicy = preferences.getString("approvalPolicy", "on-request") ?: "on-request",
            sandboxMode = preferences.getString("sandboxMode", "workspaceWrite") ?: "workspaceWrite",
            cwd = preferences.getString("cwd", "") ?: "",
        ),
    )
        private set
    val messages = mutableStateListOf<CodexUiMessage>()
    val threads = mutableStateListOf<CodexThreadSummary>()
    var loadingThreads by mutableStateOf(false)
        private set

    fun connect() {
        if (connected || connecting) return
        connecting = true
        statusText = "正在连接 Codex…"
        socket = http.newWebSocket(
            Request.Builder().url("ws://127.0.0.1:4500").build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    sendJson(
                        JSONObject()
                            .put("method", "initialize")
                            .put("id", 0)
                            .put(
                                "params",
                                JSONObject().put(
                                    "clientInfo",
                                    JSONObject()
                                        .put("name", "codex_harness_mobile")
                                        .put("title", "Codex Harness 移动端")
                                        .put("version", "0.2.0"),
                                ),
                            ),
                    )
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    main.post { handleMessage(text) }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    main.post {
                        connecting = false
                        connected = false
                        busy = false
                        activeTurnId = null
                        statusText = "连接已关闭"
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    main.post {
                        connecting = false
                        connected = false
                        busy = false
                        activeTurnId = null
                        statusText = "连接失败：${t.message ?: "服务未启动"}"
                    }
                }
            },
        )
    }

    fun disconnect() {
        socket?.close(1000, "用户断开")
        socket = null
        connected = false
        connecting = false
        busy = false
        activeTurnId = null
        statusText = "未连接"
    }

    fun newConversation() {
        threadId = null
        queuedText = null
        approval = null
        activeTurnId = null
        messages.clear()
        statusText = if (connected) "已连接，可开始新对话" else "未连接"
    }

    fun loadThreads() {
        if (!connected || loadingThreads) return
        loadingThreads = true
        sendJson(
            JSONObject()
                .put("method", "thread/list")
                .put("id", 20)
                .put(
                    "params",
                    JSONObject()
                        .put("limit", 30)
                        .put("sortKey", "updated_at")
                        .put("sortDirection", "desc"),
                ),
        )
    }

    fun resumeThread(summary: CodexThreadSummary) {
        if (!connected || busy) return
        loadingThreads = false
        messages.clear()
        approval = null
        threadId = summary.id
        sendJson(
            JSONObject()
                .put("method", "thread/resume")
                .put("id", 21)
                .put("params", JSONObject().put("threadId", summary.id)),
        )
        statusText = "正在恢复：${summary.title}"
    }

    fun updateSettings(next: CodexRunSettings) {
        settings = next
        preferences.edit()
            .putString("model", next.model)
            .putString("effort", next.effort)
            .putString("approvalPolicy", next.approvalPolicy)
            .putString("sandboxMode", next.sandboxMode)
            .putString("cwd", next.cwd)
            .apply()
        if (threadId != null) statusText = "配置已更新；新一轮开始时生效"
    }

    fun sendUserMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || busy) return
        if (!connected) {
            statusText = "请先启动并连接 Codex"
            return
        }
        messages += CodexUiMessage("user", clean)
        busy = true
        queuedText = clean
        if (threadId == null) {
            val params = JSONObject()
                .put("model", settings.model)
                .put("approvalPolicy", settings.approvalPolicy)
                .put("sandbox", settings.sandboxMode)
                .put("serviceName", "codex_harness_mobile")
            if (settings.cwd.isNotBlank()) params.put("cwd", settings.cwd)
            sendJson(
                JSONObject()
                    .put("method", "thread/start")
                    .put("id", 1)
                    .put("params", params),
            )
        } else {
            startTurn(clean)
        }
    }

    fun respondToApproval(allow: Boolean) {
        val pending = approval ?: return
        val decision = if (allow) "accept" else "decline"
        sendJson(
            JSONObject()
                .put("id", pending.requestId)
                .put("result", JSONObject().put("decision", decision)),
        )
        approval = null
    }

    fun interrupt() {
        val thread = threadId ?: return
        val turn = activeTurnId ?: return
        sendJson(
            JSONObject()
                .put("method", "turn/interrupt")
                .put("id", ids.incrementAndGet())
                .put("params", JSONObject().put("threadId", thread).put("turnId", turn)),
        )
        statusText = "正在停止…"
    }

    private fun startTurn(text: String) {
        val id = threadId ?: return
        queuedText = null
        val params = JSONObject()
            .put("threadId", id)
            .put("model", settings.model)
            .put("effort", settings.effort)
            .put("approvalPolicy", settings.approvalPolicy)
            .put("sandboxPolicy", sandboxPolicy())
            .put(
                "input",
                org.json.JSONArray().put(
                    JSONObject().put("type", "text").put("text", text),
                ),
            )
        if (settings.cwd.isNotBlank()) params.put("cwd", settings.cwd)
        sendJson(JSONObject().put("method", "turn/start").put("id", ids.incrementAndGet()).put("params", params))
        statusText = "Codex 正在工作…"
    }

    private fun sandboxPolicy(): JSONObject = JSONObject().apply {
        put("type", settings.sandboxMode)
        if (settings.sandboxMode == "workspaceWrite" && settings.cwd.isNotBlank()) {
            put("writableRoots", org.json.JSONArray().put(settings.cwd))
        }
        if (settings.sandboxMode == "workspaceWrite") put("networkAccess", true)
    }

    private fun handleMessage(raw: String) {
        runCatching {
            val json = JSONObject(raw)
            when {
                json.has("id") && json.has("error") -> {
                    busy = false
                    if (json.optInt("id", -1) == 20) loadingThreads = false
                    val message = json.optJSONObject("error")?.optString("message").orEmpty()
                    messages += CodexUiMessage(
                        "system",
                        if (message.isBlank()) "Codex 请求失败" else message,
                    )
                    statusText = "请求失败"
                }

                json.optInt("id", -1) == 0 && json.has("result") -> {
                    connected = true
                    connecting = false
                    statusText = "Codex 已连接"
                    sendJson(JSONObject().put("method", "initialized").put("params", JSONObject()))
                    loadThreads()
                }

                json.optInt("id", -1) == 1 && json.has("result") -> {
                    threadId = json.getJSONObject("result").getJSONObject("thread").getString("id")
                    queuedText?.let(::startTurn)
                }

                json.optInt("id", -1) == 20 && json.has("result") -> {
                    threads.clear()
                    val data = json.optJSONObject("result")?.optJSONArray("data") ?: JSONArray()
                    for (index in 0 until data.length()) {
                        val item = data.optJSONObject(index) ?: continue
                        val id = jsonText(item, "id")
                        if (id.isBlank()) continue
                        val preview = jsonText(item, "preview").ifBlank { jsonText(item, "firstUserMessage") }
                        val title = jsonText(item, "name")
                            .ifBlank { jsonText(item, "title") }
                            .ifBlank { preview.take(28).ifBlank { "未命名对话" } }
                        threads += CodexThreadSummary(id, title, preview)
                    }
                    loadingThreads = false
                }

                json.optInt("id", -1) == 21 && json.has("result") -> {
                    sendJson(
                        JSONObject()
                            .put("method", "thread/read")
                            .put("id", 22)
                            .put("params", JSONObject().put("threadId", threadId)),
                    )
                    statusText = "正在加载历史消息…"
                }

                json.optInt("id", -1) == 22 && json.has("result") -> {
                    restoreThreadMessages(json.optJSONObject("result") ?: JSONObject())
                    statusText = "已恢复，可继续对话"
                }

                json.optString("method") == "item/agentMessage/delta" -> {
                    val delta = json.optJSONObject("params")?.optString("delta").orEmpty()
                    appendAssistantDelta(delta)
                }

                json.optString("method") == "turn/completed" -> {
                    busy = false
                    activeTurnId = null
                    statusText = "已完成"
                    markAssistantComplete()
                }

                json.optString("method") == "turn/started" -> {
                    activeTurnId = json.optJSONObject("params")?.optJSONObject("turn")?.optString("id")
                }

                json.optString("method") == "turn/interrupt" -> {
                    busy = false
                    activeTurnId = null
                    statusText = "已停止"
                    markAssistantComplete()
                }

                json.optString("method") == "item/commandExecution/outputDelta" -> {
                    val delta = json.optJSONObject("params")?.optString("delta").orEmpty()
                    if (delta.isNotBlank()) appendSystemProgress(delta)
                }

                json.optString("method") == "item/started" -> {
                    val item = json.optJSONObject("params")?.optJSONObject("item") ?: JSONObject()
                    when (item.optString("type")) {
                        "commandExecution" -> appendSystemProgress("正在执行命令…")
                        "fileChange" -> appendSystemProgress("正在修改文件…")
                    }
                }

                json.optString("method") == "error" -> {
                    busy = false
                    val message = json.optJSONObject("params")
                        ?.optJSONObject("error")
                        ?.optString("message")
                        .orEmpty()
                    messages += CodexUiMessage("system", if (message.isBlank()) "Codex 返回了错误" else message)
                    statusText = "执行失败"
                }

                json.optString("method").endsWith("requestApproval") && json.has("id") -> {
                    val params = json.optJSONObject("params") ?: JSONObject()
                    approval = PendingApproval(
                        requestId = json.get("id"),
                        command = params.optString("command", "需要执行受控操作"),
                        reason = params.optString("reason", "Codex 请求你的确认"),
                    )
                }
            }
        }.onFailure {
            statusText = "收到无法解析的服务消息"
        }
    }

    private fun appendAssistantDelta(delta: String) {
        if (delta.isEmpty()) return
        val last = messages.lastOrNull()
        if (last?.role == "assistant" && last.pending) {
            messages[messages.lastIndex] = last.copy(text = last.text + delta)
        } else {
            messages += CodexUiMessage("assistant", delta, pending = true)
        }
    }

    private fun appendSystemProgress(text: String) {
        val compact = text.trim().takeLast(2_000)
        if (compact.isBlank()) return
        val last = messages.lastOrNull()
        if (last?.role == "system" && last.pending) {
            messages[messages.lastIndex] = last.copy(text = (last.text + compact).takeLast(4_000))
        } else {
            messages += CodexUiMessage("system", compact, pending = true)
        }
    }

    private fun markAssistantComplete() {
        val last = messages.lastOrNull() ?: return
        if (last.role == "assistant" && last.pending) {
            messages[messages.lastIndex] = last.copy(pending = false)
        }
    }

    private fun restoreThreadMessages(result: JSONObject) {
        val restored = mutableListOf<CodexUiMessage>()
        val thread = result.optJSONObject("thread") ?: result
        val turns = thread.optJSONArray("turns")
        if (turns != null) {
            for (turnIndex in 0 until turns.length()) {
                val turn = turns.optJSONObject(turnIndex) ?: continue
                val items = turn.optJSONArray("items") ?: continue
                for (itemIndex in 0 until items.length()) {
                    val item = items.optJSONObject(itemIndex) ?: continue
                    val type = jsonText(item, "type").lowercase()
                    val role = when {
                        type.contains("user") -> "user"
                        type.contains("agent") || type.contains("assistant") -> "assistant"
                        type.contains("command") || type.contains("file") -> "system"
                        else -> ""
                    }
                    if (role.isNotBlank()) {
                        val text = extractText(item).trim()
                        if (text.isNotBlank()) restored += CodexUiMessage(role, text)
                    }
                }
            }
        }
        if (restored.isNotEmpty()) {
            messages.clear()
            messages.addAll(restored.takeLast(120))
        }
    }

    private fun extractText(value: JSONObject): String {
        val direct = listOf("text", "message", "content").firstNotNullOfOrNull { key ->
            val raw = value.opt(key)
            when (raw) {
                null, JSONObject.NULL -> null
                is String -> raw
                is JSONObject -> extractText(raw)
                is JSONArray -> buildString {
                    for (index in 0 until raw.length()) {
                        val entry = raw.opt(index)
                        when (entry) {
                            is String -> append(entry).append('\n')
                            is JSONObject -> append(extractText(entry)).append('\n')
                        }
                    }
                }
                else -> raw.toString()
            }?.takeIf { it.isNotBlank() }
        }
        return direct.orEmpty()
    }

    private fun jsonText(value: JSONObject, key: String): String {
        val raw = value.opt(key)
        return if (raw == null || raw == JSONObject.NULL) "" else raw.toString().trim().take(8_000)
    }

    private fun sendJson(json: JSONObject) {
        socket?.send(json.toString())
    }
}
