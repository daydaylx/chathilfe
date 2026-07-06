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
 * requests, reads no clipboard, and does no app/WhatsApp detection.
 */
class OverlayService : Service() {

    private lateinit var controller: OverlayController
    private lateinit var settingsStore: SettingsStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        controller = OverlayController(applicationContext)
        settingsStore = SettingsStore(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            controller.remove()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        if (!controller.isAttached) {
            scope.launch { showBubble() }
        }
        return START_NOT_STICKY
    }

    private suspend fun showBubble() {
        val settings = settingsStore.settings.first()
        val (defaultX, defaultY) = FloatingBubbleView.defaultPosition(applicationContext)
        val x = settings.bubbleX ?: defaultX
        val y = settings.bubbleY ?: defaultY

        controller.show(
            x,
            y,
            object : FloatingBubbleView.BubbleListener {
                override fun onDragMove(newX: Int, newY: Int) {
                    controller.updatePosition(newX, newY)
                }

                override fun onDragEnd(finalX: Int, finalY: Int) {
                    scope.launch { settingsStore.setBubblePosition(finalX, finalY) }
                }

                override fun onTap() {
                    Toast.makeText(applicationContext, getString(R.string.bubble_tap_toast), Toast.LENGTH_SHORT).show()
                }
            },
        )
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
        controller.remove()
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
