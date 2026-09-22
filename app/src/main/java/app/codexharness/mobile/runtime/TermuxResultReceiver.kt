package app.codexharness.mobile.runtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object HarnessBridgeState {
    var url by mutableStateOf<String?>(null)
}

object CodexDesktopBridgeState {
    var url by mutableStateOf<String?>(null)
}

object TermuxCommandState {
    var lastLabel by mutableStateOf<String?>(null)
    var lastOutput by mutableStateOf<String?>(null)
    var lastSucceeded by mutableStateOf<Boolean?>(null)
    var completedAt by mutableStateOf<Long?>(null)
}

/**
 * Receives the result of a Termux RUN_COMMAND invocation.
 *
 * This receiver must be exported: the broadcast is delivered from Termux' own
 * uid via the PendingIntent the app hands to RunCommandService. It deliberately
 * declares no android:permission, because a required permission is checked
 * against the SENDER, and Termux does not request ours -- that silently drops
 * the broadcast while `am broadcast` still reports success.
 *
 * Safety comes from what is accepted: only a loopback URL on the two known
 * local-service ports is ever turned into a page load. This receiver does not
 * claim to authenticate arbitrary broadcast senders; the callback is expected
 * to arrive through the PendingIntent owned by the app.
 */
class TermuxResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Collect every string the sender provided: Termux puts the command's
        // combined stdout+stderr under its result bundle, but we scan the whole
        // intent so a change on their side cannot silently break us.
        val values = mutableListOf<String>()
        fun collect(bundle: Bundle?) {
            if (bundle == null) return
            for (key in bundle.keySet()) {
                when (val v = bundle.get(key)) {
                    is String -> values += v
                    is CharSequence -> values += v.toString()
                }
            }
        }
        collect(intent.getBundleExtra(TERMUX_BUNDLE_RESULT_KEY))
        collect(intent.getBundleExtra("result"))
        collect(intent.extras)

        val output = values.joinToString("\n")
        val url = URL_PATTERN.find(output)?.value?.trimEnd(',', '.', ')', ']', '"', '\'')
        val safeOutput = redactSecrets(output)

        val bridgeKind = intent.getStringExtra(BRIDGE_KIND_EXTRA)
        TermuxCommandState.lastOutput = safeOutput.take(8_000).ifBlank { null }
        TermuxCommandState.lastSucceeded = if (bridgeKind == "logs") safeOutput.isNotBlank() else url != null
        TermuxCommandState.completedAt = System.currentTimeMillis()

        Log.i(TAG, "result: kind=$bridgeKind url=${url?.let(::redactSecrets)} output=${safeOutput.take(400)}")

        when (bridgeKind) {
            "codex" -> {
                if (url != null) CodexDesktopBridgeState.url = url
                else Log.w(TAG, "codex command returned no loopback URL; keeping the current window")
            }
            "logs" -> Unit
            else -> {
                if (url != null) HarnessBridgeState.url = url
                else Log.w(TAG, "harness command returned no loopback URL; keeping the current window")
            }
        }
    }

    private companion object {
        const val TAG = "TermuxResultReceiver"
        const val BRIDGE_KIND_EXTRA = "app.codexharness.mobile.BRIDGE_KIND"
        const val TERMUX_BUNDLE_RESULT_KEY = "result"
        val URL_PATTERN = Regex(
            "https?://(?:127\\.0\\.0\\.1|localhost):(3080|3200)(?:/[^\\s\"']*)?",
            RegexOption.IGNORE_CASE,
        )

        fun redactSecrets(value: String): String = value.replace(
            Regex("(?i)(token=)[^\\s&]+"),
            "$1…",
        )
    }
}
