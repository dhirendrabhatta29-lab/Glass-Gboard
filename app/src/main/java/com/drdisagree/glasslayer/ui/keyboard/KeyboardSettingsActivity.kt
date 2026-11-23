package com.drdisagree.glasslayer.ui.keyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.glasslayer.data.source.PrefKeys
import com.drdisagree.glasslayer.data.source.PrefsStorage

class KeyboardSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PrefsStorage.of(this)
        setContent {
            MaterialTheme {
                KeyboardSettingsScreen(
                    enabled = prefs.getBoolean(PrefKeys.ENABLED, PrefKeys.DEFAULT_ENABLED),
                    keyboardEnabled = prefs.getBoolean(PrefKeys.KEYBOARD, PrefKeys.DEFAULT_MODULE),
                    blur = prefs.getFloat(PrefKeys.BLUR_RADIUS, PrefKeys.DEFAULT_BLUR_RADIUS),
                    backgroundTint = prefs.getFloat(PrefKeys.KEYBOARD_TINT, PrefKeys.DEFAULT_KEYBOARD_TINT),
                    keyTint = prefs.getFloat(PrefKeys.KEYBOARD_KEY_TINT, PrefKeys.DEFAULT_KEYBOARD_KEY_TINT),
                    accentTint = prefs.getFloat(PrefKeys.KEYBOARD_ACCENT_TINT, PrefKeys.DEFAULT_KEYBOARD_ACCENT_TINT),
                    onEnabledChange = { prefs.edit().putBoolean(PrefKeys.ENABLED, it).apply() },
                    onKeyboardEnabledChange = { prefs.edit().putBoolean(PrefKeys.KEYBOARD, it).apply() },
                    onBlurChange = { prefs.edit().putFloat(PrefKeys.BLUR_RADIUS, it).apply() },
                    onBackgroundTintChange = { prefs.edit().putFloat(PrefKeys.KEYBOARD_TINT, it).apply() },
                    onKeyTintChange = { prefs.edit().putFloat(PrefKeys.KEYBOARD_KEY_TINT, it).apply() },
                    onAccentTintChange = { prefs.edit().putFloat(PrefKeys.KEYBOARD_ACCENT_TINT, it).apply() }
                )
            }
        }
    }
}

@Composable
private fun KeyboardSettingsScreen(
    enabled: Boolean,
    keyboardEnabled: Boolean,
    blur: Float,
    backgroundTint: Float,
    keyTint: Float,
    accentTint: Float,
    onEnabledChange: (Boolean) -> Unit,
    onKeyboardEnabledChange: (Boolean) -> Unit,
    onBlurChange: (Float) -> Unit,
    onBackgroundTintChange: (Float) -> Unit,
    onKeyTintChange: (Float) -> Unit,
    onAccentTintChange: (Float) -> Unit
) {
    var master by remember { mutableStateOf(enabled) }
    var keyboard by remember { mutableStateOf(keyboardEnabled) }
    var blurValue by remember { mutableFloatStateOf(blur.coerceIn(0f, 32f)) }
    var backgroundValue by remember { mutableFloatStateOf(backgroundTint.coerceIn(0f, 100f)) }
    var keyValue by remember { mutableFloatStateOf(keyTint.coerceIn(0f, 100f)) }
    var accentValue by remember { mutableFloatStateOf(accentTint.coerceIn(0f, 100f)) }

    Scaffold { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("GlassLayer for Gboard", style = MaterialTheme.typography.headlineMedium)
            Text("A focused glass effect for Google Keyboard.", style = MaterialTheme.typography.bodyMedium)

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SwitchRow("Enable module", "Master switch for the Gboard effect.", master) {
                        master = it
                        onEnabledChange(it)
                    }
                    SwitchRow("Keyboard glass", "Apply the glass surface to Gboard.", keyboard) {
                        keyboard = it
                        onKeyboardEnabledChange(it)
                    }
                }
            }

            SettingsCard(title = "Blur") {
                ValueSlider("Blur radius", blurValue, "${blurValue.toInt()} dp", 0f..32f, onValueChange = { blurValue = it }, onValueChangeFinished = { onBlurChange(blurValue) })
            }

            SettingsCard(title = "Tint") {
                ValueSlider("Keyboard background", backgroundValue, "${backgroundValue.toInt()}%", 0f..100f, onValueChange = { backgroundValue = it }, onValueChangeFinished = { onBackgroundTintChange(backgroundValue) })
                ValueSlider("Keys", keyValue, "${keyValue.toInt()}%", 0f..100f, onValueChange = { keyValue = it }, onValueChangeFinished = { onKeyTintChange(keyValue) })
                ValueSlider("Accent", accentValue, "${accentValue.toInt()}%", 0f..100f, onValueChange = { accentValue = it }, onValueChangeFinished = { onAccentTintChange(accentValue) })
            }
            Text("Changes are applied to Gboard automatically. Make sure this module is enabled in your Xposed manager and Gboard is in its scope.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun ValueSlider(
    title: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(valueLabel, style = MaterialTheme.typography.labelLarge)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range, onValueChangeFinished = onValueChangeFinished)
    }
}
