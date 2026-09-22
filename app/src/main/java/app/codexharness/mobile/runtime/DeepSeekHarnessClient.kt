package app.codexharness.mobile.runtime

/**
 * DSH integration boundary. v0.1 will host the official local Web UI in a
 * WebView; this interface leaves room for a native session client once the
 * upstream protocol is stable enough to depend on directly.
 */
interface DeepSeekHarnessClient {
    fun webUiUrl(): String = "http://127.0.0.1:3080"
    suspend fun isHealthy(): Boolean
}
