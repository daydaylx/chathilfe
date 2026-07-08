package de.disaai.chathilfe.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.disaai.chathilfe.model.AnswerLength
import de.disaai.chathilfe.model.CapitalizationStyle
import de.disaai.chathilfe.model.EmojiUsage
import de.disaai.chathilfe.model.Naturalness
import de.disaai.chathilfe.model.PunctuationStyle
import de.disaai.chathilfe.model.WritingStyleSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chathilfe_settings")

private object Keys {
    val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    val PREFERRED_TONE = stringPreferencesKey("preferred_tone")
    val LAST_MODE = stringPreferencesKey("last_mode")
    val BUBBLE_X = intPreferencesKey("bubble_x")
    val BUBBLE_Y = intPreferencesKey("bubble_y")

    // Writing-style values (Issue #8): enum internalValue strings only. No user text, no persona.
    val STYLE_LENGTH = stringPreferencesKey("style_length")
    val STYLE_EMOJI = stringPreferencesKey("style_emoji")
    val STYLE_PUNCTUATION = stringPreferencesKey("style_punctuation")
    val STYLE_CAPITALIZATION = stringPreferencesKey("style_capitalization")
    val STYLE_NATURALNESS = stringPreferencesKey("style_naturalness")
}

data class Settings(
    val overlayEnabled: Boolean = false,
    val preferredTone: String? = null,
    val lastMode: String? = null,
    val bubbleX: Int? = null,
    val bubbleY: Int? = null,
    val writingStyle: WritingStyleSettings = WritingStyleSettings(),
)

/**
 * Stores only UI/overlay preferences. Never stores the API key - that stays in
 * BuildConfig, sourced at build time from local.properties (see docs/API_KEY_STRATEGY.md).
 */
class SettingsStore(private val context: Context) {

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            overlayEnabled = prefs[Keys.OVERLAY_ENABLED] ?: false,
            preferredTone = prefs[Keys.PREFERRED_TONE],
            lastMode = prefs[Keys.LAST_MODE],
            bubbleX = prefs[Keys.BUBBLE_X],
            bubbleY = prefs[Keys.BUBBLE_Y],
            writingStyle = WritingStyleSettings(
                length = AnswerLength.fromInternalValue(prefs[Keys.STYLE_LENGTH]),
                emojiUsage = EmojiUsage.fromInternalValue(prefs[Keys.STYLE_EMOJI]),
                punctuation = PunctuationStyle.fromInternalValue(prefs[Keys.STYLE_PUNCTUATION]),
                capitalization = CapitalizationStyle.fromInternalValue(prefs[Keys.STYLE_CAPITALIZATION]),
                naturalness = Naturalness.fromInternalValue(prefs[Keys.STYLE_NATURALNESS]),
            ),
        )
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
    }

    suspend fun setPreferredTone(tone: String?) {
        context.dataStore.edit { prefs ->
            if (tone == null) prefs.remove(Keys.PREFERRED_TONE) else prefs[Keys.PREFERRED_TONE] = tone
        }
    }

    suspend fun setLastMode(mode: String?) {
        context.dataStore.edit { prefs ->
            if (mode == null) prefs.remove(Keys.LAST_MODE) else prefs[Keys.LAST_MODE] = mode
        }
    }

    // Reserved for Phase 3 (floating button drag position). Not called from any Phase 2 UI.
    suspend fun setBubblePosition(x: Int, y: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BUBBLE_X] = x
            prefs[Keys.BUBBLE_Y] = y
        }
    }

    /**
     * Persists the user's writing-style values (Issue #8). Stores only enum internalValue
     * strings — never user text, suggestions or a persona. The fixed app voice is a static
     * prompt rule (D-013), not a stored setting.
     */
    suspend fun setWritingStyle(style: WritingStyleSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STYLE_LENGTH] = style.length.internalValue
            prefs[Keys.STYLE_EMOJI] = style.emojiUsage.internalValue
            prefs[Keys.STYLE_PUNCTUATION] = style.punctuation.internalValue
            prefs[Keys.STYLE_CAPITALIZATION] = style.capitalization.internalValue
            prefs[Keys.STYLE_NATURALNESS] = style.naturalness.internalValue
        }
    }
}
