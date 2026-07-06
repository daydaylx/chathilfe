package de.disaai.chathilfe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.disaai.chathilfe.settings.SettingsScreen
import de.disaai.chathilfe.settings.SettingsStore
import de.disaai.chathilfe.ui.theme.ChatHilfeTheme

class MainActivity : ComponentActivity() {
    private lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settingsStore = SettingsStore(applicationContext)
        setContent {
            ChatHilfeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SettingsScreen(settingsStore = settingsStore)
                }
            }
        }
    }
}
