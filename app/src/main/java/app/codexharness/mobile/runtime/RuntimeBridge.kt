package app.codexharness.mobile.runtime

/**
 * Boundary between the Android UI and the Termux/Debian runtime.
 *
 * The production logic lives in TermuxRuntimeManager, which dispatches Termux
 * RUN_COMMAND jobs and probes the local Codex/Harness services. This
 * small protocol model remains useful to keep UI-facing runtime concepts
 * independent from the concrete Termux transport.
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
