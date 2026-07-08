package de.disaai.chathilfe.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.disaai.chathilfe.BuildConfig
import de.disaai.chathilfe.R
import de.disaai.chathilfe.model.AnswerLength
import de.disaai.chathilfe.model.CapitalizationStyle
import de.disaai.chathilfe.model.EmojiUsage
import de.disaai.chathilfe.model.Naturalness
import de.disaai.chathilfe.model.PunctuationStyle
import de.disaai.chathilfe.model.ToneOption
import de.disaai.chathilfe.model.WritingStyleSettings
import de.disaai.chathilfe.overlay.OverlayService
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionStatus by remember { mutableStateOf(currentPermissionStatus(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        permissionStatus = currentPermissionStatus(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = currentPermissionStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val settings by settingsStore.settings.collectAsState(initial = Settings())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(id = R.string.settings_intro),
            style = MaterialTheme.typography.bodyMedium,
        )

        PermissionCard(
            title = stringResource(id = R.string.permission_overlay_title),
            description = stringResource(id = R.string.permission_overlay_description),
            state = permissionStatus.overlay,
            onOpenSettings = { startSettingsActivity(context, overlayPermissionSettingsIntent(context)) },
        )

        PermissionCard(
            title = stringResource(id = R.string.permission_usage_title),
            description = stringResource(id = R.string.permission_usage_description),
            state = permissionStatus.usageAccess,
            onOpenSettings = { startSettingsActivity(context, usageAccessSettingsIntent()) },
        )

        PermissionCard(
            title = stringResource(id = R.string.permission_notification_title),
            description = stringResource(id = R.string.permission_notification_description),
            state = permissionStatus.notification,
            onOpenSettings = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        )

        InfoCard(
            title = stringResource(id = R.string.foreground_service_title),
            statusText = stringResource(
                id = if (settings.overlayEnabled) {
                    R.string.foreground_service_state_active
                } else {
                    R.string.foreground_service_state_inactive
                },
            ),
            description = stringResource(id = R.string.foreground_service_description),
        )

        val isApiKeyConfigured = BuildConfig.OPENROUTER_API_KEY != "replace_me_locally"
        InfoCard(
            title = stringResource(id = R.string.api_key_title),
            statusText = stringResource(
                id = if (isApiKeyConfigured) R.string.api_key_configured else R.string.api_key_not_configured,
            ),
            description = stringResource(id = R.string.api_key_description),
        )

        val currentTone = ToneOption.fromInternalValue(settings.preferredTone)
        StyleCard(
            tone = currentTone,
            style = settings.writingStyle,
            onToneChange = { tone ->
                coroutineScope.launch { settingsStore.setPreferredTone(tone.internalValue) }
            },
            onStyleChange = { updated ->
                coroutineScope.launch { settingsStore.setWritingStyle(updated) }
            },
        )

        OverlayToggleCard(
            checked = settings.overlayEnabled,
            onCheckedChange = { enabled ->
                if (enabled) {
                    when {
                        permissionStatus.overlay != PermissionState.GRANTED -> {
                            Toast.makeText(
                                context,
                                R.string.overlay_permission_required_toast,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                        permissionStatus.usageAccess != PermissionState.GRANTED -> {
                            Toast.makeText(
                                context,
                                R.string.usage_permission_required_toast,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                        else -> {
                            coroutineScope.launch { settingsStore.setOverlayEnabled(true) }
                            OverlayService.start(context)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                permissionStatus.notification != PermissionState.GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                } else {
                    coroutineScope.launch { settingsStore.setOverlayEnabled(false) }
                    OverlayService.stop(context)
                }
            },
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    state: PermissionState,
    onOpenSettings: () -> Unit,
) {
    val statusText = stringResource(
        id = if (state == PermissionState.GRANTED) {
            R.string.permission_state_granted
        } else {
            R.string.permission_state_missing
        },
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = statusText, style = MaterialTheme.typography.labelLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            if (state == PermissionState.MISSING) {
                Button(onClick = onOpenSettings) {
                    Text(text = stringResource(id = R.string.action_open_settings))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    statusText: String,
    description: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = statusText, style = MaterialTheme.typography.labelLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun OverlayToggleCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = stringResource(id = R.string.overlay_toggle_title), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(id = R.string.overlay_toggle_description), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

/**
 * Combined Stil-Card with Tone + Writing Style dimensions (Issue #8).
 * Tone and style are stored via [SettingsStore]; never user text or a persona (D-013).
 */
@Composable
private fun StyleCard(
    tone: ToneOption,
    style: WritingStyleSettings,
    onToneChange: (ToneOption) -> Unit,
    onStyleChange: (WritingStyleSettings) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.settings_style_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(id = R.string.settings_style_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Tone selection
            ChipRow(label = stringResource(id = R.string.settings_style_tone)) {
                ToneOption.entries.forEach { option ->
                    FilterChipItem(option.label, option == tone) { onToneChange(option) }
                }
            }
            // Writing-style dimensions
            ChipRow(label = stringResource(id = R.string.settings_style_length)) {
                AnswerLength.entries.forEach { option ->
                    FilterChipItem(option.label, option == style.length) { onStyleChange(style.copy(length = option)) }
                }
            }
            ChipRow(label = stringResource(id = R.string.settings_style_emoji)) {
                EmojiUsage.entries.forEach { option ->
                    FilterChipItem(option.label, option == style.emojiUsage) { onStyleChange(style.copy(emojiUsage = option)) }
                }
            }
            ChipRow(label = stringResource(id = R.string.settings_style_punctuation)) {
                PunctuationStyle.entries.forEach { option ->
                    FilterChipItem(option.label, option == style.punctuation) { onStyleChange(style.copy(punctuation = option)) }
                }
            }
            ChipRow(label = stringResource(id = R.string.settings_style_capitalization)) {
                CapitalizationStyle.entries.forEach { option ->
                    FilterChipItem(option.label, option == style.capitalization) { onStyleChange(style.copy(capitalization = option)) }
                }
            }
            ChipRow(label = stringResource(id = R.string.settings_style_naturalness)) {
                Naturalness.entries.forEach { option ->
                    FilterChipItem(option.label, option == style.naturalness) { onStyleChange(style.copy(naturalness = option)) }
                }
            }
        }
    }
}

@Composable
private fun ChipRow(label: String, content: @Composable FlowRowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onSelect: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text(label) },
    )
}

private fun startSettingsActivity(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.action_open_settings), Toast.LENGTH_SHORT).show()
    }
}
