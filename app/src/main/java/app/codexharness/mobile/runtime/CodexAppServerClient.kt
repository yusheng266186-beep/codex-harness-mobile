package app.codexharness.mobile.runtime

/**
 * Versioned client boundary for the official Codex app-server protocol.
 *
 * The generated schema must be kept alongside the installed Codex version;
 * this class is intentionally small until a real device is attached and the
 * exact app-server version/transport are confirmed.
 */
interface CodexAppServerClient {
    suspend fun connect(endpoint: String, capabilityToken: String? = null): Result<Unit>
    suspend fun disconnect()
    suspend fun listThreads(): Result<List<CodexThread>>
    suspend fun startTurn(threadId: String, text: String): Result<Unit>
    suspend fun respondToApproval(requestId: String, allow: Boolean): Result<Unit>
}

data class CodexThread(
    val id: String,
    val title: String,
    val updatedAt: Long,
)
