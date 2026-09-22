package app.codexharness.mobile.runtime

/**
 * Boundary between the Android UI and the Termux/Debian runtime.
 *
 * The sample UI uses mocked state for now. The real implementation will call
 * Termux RUN_COMMAND (or a small local runner) and expose health checks for
 * codex app-server and DeepSeek Harness.
 */
interface RuntimeBridge {
    suspend fun startCodex(): Result<Unit>
    suspend fun startHarness(): Result<Unit>
    suspend fun stopAll(): Result<Unit>
    suspend fun health(): RuntimeHealth
}

data class RuntimeHealth(
    val termux: ServiceState = ServiceState.UNKNOWN,
    val debian: ServiceState = ServiceState.UNKNOWN,
    val codex: ServiceState = ServiceState.UNKNOWN,
    val harness: ServiceState = ServiceState.UNKNOWN,
    val message: String? = null,
)

enum class ServiceState { ONLINE, STARTING, OFFLINE, UNKNOWN }
