package de.disaai.chathilfe.detection

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import de.disaai.chathilfe.settings.PermissionState
import de.disaai.chathilfe.settings.checkUsageAccessPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Detects whether WhatsApp ([WHATSAPP_PACKAGE]) is the current foreground app using
 * [UsageStatsManager.queryEvents].
 *
 * Runs a polling loop on the provided [scope]; the [start] callback is invoked on that
 * scope's dispatcher (Main in [de.disaai.chathilfe.overlay.OverlayService]).
 *
 * Constraints (docs/ANDROID_CONSTRAINTS.md "WhatsApp-Erkennung", docs/ARCHITECTURE.md
 * "ForegroundAppDetector"):
 * - Only [UsageStatsManager.queryEvents] is used. No Accessibility fallback, no
 *   NotificationListener, no screen capture, no WhatsApp database access.
 * - Poll interval starts at [POLL_INTERVAL_MS]; do not poll more aggressively unless the
 *   UX is visibly too slow (max 500 ms). UsageStats is not real-time; delays are expected
 *   and accepted.
 *
 * Detection model: a sliding window from the previous poll to now catches foreground
 * switches as [UsageEvents.Event.ACTIVITY_RESUMED] events. The very first poll looks back
 * [BOOTSTRAP_WINDOW_MS] so the current foreground app is known even if the service starts
 * while WhatsApp is already open. [lastForegroundPackage] is kept across polls for continuity.
 */
class ForegroundAppDetector(
    private val context: Context,
    private val scope: CoroutineScope,
) {

    sealed interface State {
        /**
         * PACKAGE_USAGE_STATS is not granted; the overlay cannot detect the foreground app.
         * The caller (service) should keep the bubble hidden. Reported defensively; the
         * Settings toggle is expected to gate on Usage Access before starting the service.
         */
        data object NoUsageAccess : State

        /** Result of a poll: whether WhatsApp is currently in the foreground. */
        data class WhatsappForeground(val isForeground: Boolean) : State
    }

    private val usageStatsManager: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    private var job: Job? = null
    private var lastCheckTimeMs: Long = 0L
    private var lastForegroundPackage: String? = null

    fun start(onState: (State) -> Unit) {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                onState(poll())
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun poll(): State = withContext(Dispatchers.Default) {
        if (checkUsageAccessPermission(context) == PermissionState.MISSING) {
            State.NoUsageAccess
        } else {
            State.WhatsappForeground(isWhatsappInForeground())
        }
    }

    private fun isWhatsappInForeground(): Boolean {
        val manager = usageStatsManager
        val now = System.currentTimeMillis()
        val begin = if (lastCheckTimeMs == 0L) now - BOOTSTRAP_WINDOW_MS else lastCheckTimeMs

        val events: UsageEvents? = try {
            manager?.queryEvents(begin, now)
        } catch (e: Exception) {
            lastCheckTimeMs = now
            return lastForegroundPackage == WHATSAPP_PACKAGE
        }
        if (events == null) {
            lastCheckTimeMs = now
            return lastForegroundPackage == WHATSAPP_PACKAGE
        }

        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundPackage = event.packageName
            }
        }
        lastCheckTimeMs = now
        return lastForegroundPackage == WHATSAPP_PACKAGE
    }

    companion object {
        const val WHATSAPP_PACKAGE = "com.whatsapp"

        /** Polling interval (ms). Start value per docs/ANDROID_CONSTRAINTS.md. */
        private const val POLL_INTERVAL_MS = 1000L

        /** First-poll look-back window to bootstrap the current foreground app. */
        private const val BOOTSTRAP_WINDOW_MS = 30_000L
    }
}
