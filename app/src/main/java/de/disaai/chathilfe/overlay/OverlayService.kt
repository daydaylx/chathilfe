package de.disaai.chathilfe.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.detection.ForegroundAppDetector
import de.disaai.chathilfe.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Hosts the overlay bubble's runtime. Started only from a visible user action
 * (the Settings overlay toggle) - never from boot or a broadcast. Runs no AI
 * requests and reads no clipboard.
 *
 * As of Phase 4, the bubble is no longer shown immediately on start. A
 * [ForegroundAppDetector] polls the foreground app; the bubble is attached only
 * while WhatsApp is in the foreground and removed when it leaves, without stopping
 * the service. Position is persisted across show/hide cycles via [SettingsStore].
 */
class OverlayService : Service() {

    private lateinit var controller: OverlayController
    private lateinit var settingsStore: SettingsStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var detector: ForegroundAppDetector? = null
    private var isBubbleShown = false

    private val bubbleListener = object : FloatingBubbleView.BubbleListener {
        override fun onDragMove(newX: Int, newY: Int) {
            controller.updatePosition(newX, newY)
        }

        override fun onDragEnd(finalX: Int, finalY: Int) {
            scope.launch { settingsStore.setBubblePosition(finalX, finalY) }
        }

        override fun onTap() {
            Toast.makeText(applicationContext, getString(R.string.bubble_tap_toast), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        controller = OverlayController(applicationContext)
        settingsStore = SettingsStore(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopDetection()
            hideBubble()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        startDetection()
        return START_NOT_STICKY
    }

    private fun startDetection() {
        if (detector != null) return
        detector = ForegroundAppDetector(applicationContext, scope)
            .also { it.start(::onDetectionState) }
    }

    private fun stopDetection() {
        detector?.stop()
        detector = null
    }

    /** Runs on [scope] (Main). Show/hide are idempotent and guarded against double views. */
    private fun onDetectionState(state: ForegroundAppDetector.State) {
        when (state) {
            ForegroundAppDetector.State.NoUsageAccess -> hideBubble()
            is ForegroundAppDetector.State.WhatsappForeground -> {
                if (state.isForeground) showBubble() else hideBubble()
            }
        }
    }

    private fun showBubble() {
        if (isBubbleShown) return
        scope.launch {
            if (isBubbleShown) return@launch
            val settings = settingsStore.settings.first()
            val (defaultX, defaultY) = FloatingBubbleView.defaultPosition(applicationContext)
            controller.show(
                settings.bubbleX ?: defaultX,
                settings.bubbleY ?: defaultY,
                bubbleListener,
            )
            isBubbleShown = controller.isAttached
        }
    }

    private fun hideBubble() {
        if (!isBubbleShown) return
        controller.remove()
        isBubbleShown = false
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification_bubble)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopDetection()
        hideBubble()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val ACTION_START = "de.disaai.chathilfe.overlay.action.START"
        private const val ACTION_STOP = "de.disaai.chathilfe.overlay.action.STOP"
        private const val CHANNEL_ID = "chathilfe_overlay_channel"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, OverlayService::class.java).setAction(ACTION_STOP))
        }
    }
}
