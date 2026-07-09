package de.disaai.chathilfe.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.disaai.chathilfe.R
import de.disaai.chathilfe.ai.AiClient
import de.disaai.chathilfe.ai.ParseResult
import de.disaai.chathilfe.chat.WhatsAppChatParser
import de.disaai.chathilfe.detection.ForegroundAppDetector
import de.disaai.chathilfe.model.ReplyMode
import de.disaai.chathilfe.model.ReplyRequest
import de.disaai.chathilfe.model.ReplySuggestion
import de.disaai.chathilfe.model.RetryInstruction
import de.disaai.chathilfe.model.ToneOption
import de.disaai.chathilfe.model.WritingStyleSettings
import de.disaai.chathilfe.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Hosts the overlay bubble's runtime. Started only from a visible user action
 * (the Settings overlay toggle) - never from boot or a broadcast. Runs no AI
 * requests and reads no clipboard in the background.
 *
 * As of Phase 4, the bubble is no longer shown immediately on start. A
 * [ForegroundAppDetector] polls the foreground app; the bubble is attached only
 * while WhatsApp is in the foreground and removed when it leaves, without stopping
 * the service. Position is persisted across show/hide cycles via [SettingsStore].
 *
 * As of Phase 5, tapping the bubble opens a narrow [InputBarView]; starting from it shows a
 * dummy-data [ResultPanelView]. Both are torn down (without stopping the service) as soon as
 * WhatsApp leaves the foreground, so no overlay content is ever left orphaned.
 */
class OverlayService : Service() {

    private enum class OverlayState { HIDDEN, BUBBLE, INPUT_BAR, RESULT_PANEL }

    private lateinit var controller: OverlayController
    private lateinit var settingsStore: SettingsStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val aiClient = AiClient()
    private var requestJob: Job? = null

    private var detector: ForegroundAppDetector? = null
    private var overlayState = OverlayState.HIDDEN
    private var isWhatsappForeground = false

    // Transient, in-memory only for the current Input-Bar -> Result-Panel round trip.
    // Never logged or persisted.
    private var pendingText: String = ""
    private var pendingMode: ReplyMode = ReplyMode.REPLY
    private var pendingTone: ToneOption = ToneOption.DEFAULT
    private var pendingStyle: WritingStyleSettings = WritingStyleSettings()
    private var pendingIntentHint: String? = null

    private val bubbleListener = object : FloatingBubbleView.BubbleListener {
        override fun onDragMove(newX: Int, newY: Int) {
            controller.updatePosition(newX, newY)
        }

        override fun onDragEnd(finalX: Int, finalY: Int) {
            scope.launch { settingsStore.setBubblePosition(finalX, finalY) }
        }

        override fun onTap() {
            openInputBar()
        }
    }

    private val inputBarListener = object : InputBarView.Listener {
        override fun onModeSelected(mode: ReplyMode) {
            pendingMode = visibleModeOrDefault(mode)
            scope.launch { settingsStore.setLastMode(pendingMode.name) }
        }

        override fun onStart(text: String, mode: ReplyMode, intentHint: String?) {
            pendingText = text
            pendingMode = visibleModeOrDefault(mode)
            pendingIntentHint = intentHint
            runInitialRequest()
        }

        override fun onStyleChanged(style: WritingStyleSettings) {
            pendingStyle = style
            // Persist eagerly so the next overlay open picks up the last values.
            scope.launch { settingsStore.setWritingStyle(style) }
        }

        override fun onToneSelected(tone: ToneOption) {
            pendingTone = tone
            // Remember the last preferred tone so the next overlay open restores it (Issue #22).
            scope.launch { settingsStore.setPreferredTone(tone.internalValue) }
        }

        override fun onClose() {
            closeContent()
        }
    }

    private val resultPanelListener = object : ResultPanelView.Listener {
        override fun onClose() {
            closeContent()
        }

        override fun onRetry(chips: Set<RetryInstruction>) {
            runRetry(chips)
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
            hideEverything()
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
            ForegroundAppDetector.State.NoUsageAccess -> {
                isWhatsappForeground = false
                hideEverything()
            }
            is ForegroundAppDetector.State.WhatsappForeground -> {
                isWhatsappForeground = state.isForeground
                if (state.isForeground) {
                    if (overlayState == OverlayState.HIDDEN) showBubble()
                } else {
                    hideEverything()
                }
            }
        }
    }

    private fun showBubble() {
        if (overlayState != OverlayState.HIDDEN) return
        scope.launch {
            if (overlayState != OverlayState.HIDDEN) return@launch
            val settings = settingsStore.settings.first()
            val (defaultX, defaultY) = FloatingBubbleView.defaultPosition(applicationContext)
            controller.show(
                settings.bubbleX ?: defaultX,
                settings.bubbleY ?: defaultY,
                bubbleListener,
            )
            if (controller.isAttached) overlayState = OverlayState.BUBBLE
        }
    }

    private fun openInputBar() {
        controller.remove()
        scope.launch {
            val settings = settingsStore.settings.first()
            val mode = settings.lastMode
                ?.let { runCatching { ReplyMode.valueOf(it) }.getOrNull() }
                ?.let(::visibleModeOrDefault)
                ?: ReplyMode.REPLY
            pendingMode = mode
            pendingTone = ToneOption.fromInternalValue(settings.preferredTone)
            pendingStyle = settings.writingStyle
            pendingIntentHint = null
            controller.showInputBar(mode, inputBarListener, pendingStyle, pendingTone)
            overlayState = OverlayState.INPUT_BAR
        }
    }

    /**
     * Starts the first AI request from the Input-Bar. The pending mode decides how the entered
     * text is mapped (Antworten→copiedMessage, Schreiben→userIntent); see [buildRequest].
     * Disables the bar + shows a loading hint; on success opens the
     * Result-Panel, on error shows a compact message and keeps the Input-Bar open.
     * At most one request runs at a time.
     */
    private fun runInitialRequest() {
        requestJob?.cancel()
        controller.setInputBarError(null)
        controller.setInputBarLoading(true)
        requestJob = scope.launch {
            val request = buildRequest(pendingTone, retryInstructions = emptySet())
            val result = aiClient.request(request, pendingStyle)
            controller.setInputBarLoading(false)
            val suggestions = when (result) {
                is ParseResult.Success -> result.suggestions
                is ParseResult.Partial -> result.suggestions
                is ParseResult.Error -> null
            }
            if (suggestions != null) {
                controller.showResultPanel(suggestions, pendingText, pendingTone, resultPanelListener)
                overlayState = OverlayState.RESULT_PANEL
            } else {
                controller.setInputBarError((result as ParseResult.Error).message)
            }
        }
    }

    /**
     * Retries with optional change chips. Builds a fresh request from the stored text/tone + the
     * active chips, shows a compact loading hint on the existing Result-Panel. On success the
     * suggestions are swapped; on error the previous suggestions stay visible with a short error.
     * Chip selection is cleared only on success, so a failed retry keeps the user's choice.
     */
    private fun runRetry(chips: Set<RetryInstruction>) {
        requestJob?.cancel()
        controller.setResultPanelError(null)
        controller.setResultPanelLoading(true)
        requestJob = scope.launch {
            val request = buildRequest(pendingTone, retryInstructions = chips)
            val result = aiClient.request(request, pendingStyle)
            controller.setResultPanelLoading(false)
            val suggestions = when (result) {
                is ParseResult.Success -> result.suggestions
                is ParseResult.Partial -> result.suggestions
                is ParseResult.Error -> null
            }
            if (suggestions != null) {
                controller.replaceResultSuggestions(suggestions)
            } else {
                controller.setResultPanelError((result as ParseResult.Error).message)
            }
        }
    }

    /**
     * Builds a [ReplyRequest] from the pending text/mode, mapping the entered text to the
     * field the active mode expects: Antworten(REPLY)→copiedMessage, Formulieren(COMPOSE)→
     * userIntent. An optional intent hint (reply chips) is prepended to the userIntent field.
     * Tone is taken from the transient [pendingTone], not read from [SettingsStore] each time.
     * In REPLY mode, a pasted WhatsApp dialog block (Issue #19) is structured first: the
     * counterpart's latest message becomes the copied reply trigger, the rest is passed as
     * transient conversation context. Never logs or persists the text.
     */
    private fun buildRequest(tone: ToneOption, retryInstructions: Set<RetryInstruction>): ReplyRequest {
        val text = pendingText
        return when (pendingMode) {
            ReplyMode.REPLY -> {
                // If the pasted text is a WhatsApp dialog block (Issue #19), use the counterpart's
                // latest message as the reply trigger and pass the rest as transient context.
                // Otherwise the parser returns null and the plain single-text flow is kept.
                val parsed = WhatsAppChatParser.parse(text)
                ReplyRequest(
                    mode = ReplyMode.REPLY,
                    userIntent = buildUserIntentWithHint(pendingIntentHint),
                    tone = tone,
                    retryInstructions = retryInstructions,
                    copiedMessage = parsed?.latestOtherMessage?.text ?: text,
                    conversationContext = parsed?.formatContext(),
                )
            }
            ReplyMode.COMPOSE -> ReplyRequest(
                mode = ReplyMode.COMPOSE,
                userIntent = text,
                tone = tone,
                retryInstructions = retryInstructions,
            )
            ReplyMode.REWRITE -> ReplyRequest(
                mode = ReplyMode.REWRITE,
                userIntent = buildUserIntentWithHint(pendingIntentHint),
                tone = tone,
                retryInstructions = retryInstructions,
                originalText = text,
            )
        }
    }

    /** Prepends an intent hint (reply chip) to a base user intent if both are present. */
    private fun buildUserIntentWithHint(hint: String?): String =
        if (hint != null) "$hint." else ""

    private fun visibleModeOrDefault(mode: ReplyMode): ReplyMode =
        if (mode == ReplyMode.REPLY || mode == ReplyMode.COMPOSE) mode else ReplyMode.REPLY

    /** Closes Input-Bar/Result-Panel content and returns to the bubble (or hides fully). */
    private fun closeContent() {
        requestJob?.cancel()
        requestJob = null
        controller.hideContent()
        pendingText = ""
        pendingMode = ReplyMode.REPLY
        pendingIntentHint = null
        overlayState = OverlayState.HIDDEN
        if (isWhatsappForeground) showBubble()
    }

    /** Removes the bubble and any Input-Bar/Result-Panel content. Idempotent. */
    private fun hideEverything() {
        requestJob?.cancel()
        requestJob = null
        controller.hideContent()
        controller.remove()
        pendingText = ""
        pendingMode = ReplyMode.REPLY
        pendingIntentHint = null
        overlayState = OverlayState.HIDDEN
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
        hideEverything()
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
