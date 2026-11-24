package com.drdisagree.glasslayer.ui.keyboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.glasslayer.data.source.PrefKeys
import com.drdisagree.glasslayer.data.source.PrefsStorage
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import java.io.BufferedReader
import java.io.InputStreamReader

private val Foreground = Color(0xFFF4F7FC)
private val Secondary = Color(0xFFB5C5D2)
private val AccentBlue = Color(0xFF168FFF)
private val AccentGreen = Color(0xFF30D158)
private val GlassFill = Color(0x1FFFFFFF)

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
    var panelTint by remember { mutableFloatStateOf(backgroundTint.coerceIn(0f, 100f)) }
    var keyTintValue by remember { mutableFloatStateOf(keyTint.coerceIn(0f, 100f)) }
    var accentTintValue by remember { mutableFloatStateOf(accentTint.coerceIn(0f, 100f)) }
    var restarting by remember { mutableStateOf(false) }
    var restartStatus by remember { mutableStateOf<String?>(null) }

    val backdrop = rememberLayerBackdrop()

    Box(Modifier.fillMaxSize()) {
        // The backdrop records this atmospheric layer; every glass surface below samples it.
        Box(
            Modifier.fillMaxSize()
                .layerBackdrop(backdrop)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF07191F), Color(0xFF0D2B3E), Color(0xFF0A2823))
                    )
                )
        ) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 105.dp, end = 12.dp)
                    .size(190.dp).background(
                        Brush.radialGradient(listOf(Color(0x6639A9C7), Color.Transparent)),
                        CircleShape
                    )
            )
            Box(
                Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                    .size(170.dp).background(
                        Brush.radialGradient(listOf(Color(0x4430D158), Color.Transparent)),
                        CircleShape
                    )
            )
        }

        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Header(backdrop)

            LiquidSurface(backdrop, modifier = Modifier.fillMaxWidth(), radius = 34.dp, tint = Color(0x332A9BCB)) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Liquid glass", color = Foreground, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("Glass effects for Gboard", color = Secondary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    LiquidSwitch(master, backdrop) {
                        master = it
                        onEnabledChange(it)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LiquidSurface(backdrop, Modifier.weight(1f), radius = 30.dp) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        KeyboardPreview(backdrop, Modifier.fillMaxWidth().height(94.dp))
                        Text("Glass style", color = Foreground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Blur ${blurValue.toInt()} dp", color = Secondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                LiquidSurface(backdrop, Modifier.weight(1f), radius = 30.dp) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.fillMaxWidth().height(94.dp), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(54.dp).background(
                                    Brush.radialGradient(listOf(Color(0xFFBCEBFF), AccentBlue, Color(0xFF07518D)),
                                    center = androidx.compose.ui.geometry.Offset.Unspecified),
                                    CircleShape
                                )
                            )
                        }
                        Text("Keyboard", color = Foreground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(if (keyboard) "Enabled" else "Disabled", color = Secondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            SectionTitle("⌘", "Quick settings")
            LiquidSurface(backdrop) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    SettingSwitch("Keyboard", "Glass Gboard keys and panel", keyboard, backdrop) {
                        keyboard = it
                        onKeyboardEnabledChange(it)
                    }
                }
            }

            SectionTitle("◈", "Tint")
            LiquidSurface(backdrop) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSlider("Panel tint", panelTint, "%", 0f..100f, backdrop, { panelTint = it }, { onBackgroundTintChange(panelTint) })
                    GlassDivider()
                    LiquidSlider("Key tint", keyTintValue, "%", 0f..100f, backdrop, { keyTintValue = it }, { onKeyTintChange(keyTintValue) })
                    GlassDivider()
                    LiquidSlider("Accent key tint", accentTintValue, "%", 0f..100f, backdrop, { accentTintValue = it }, { onAccentTintChange(accentTintValue) })
                }
            }

            SectionTitle("◉", "Glass")
            LiquidSurface(backdrop, tint = Color(0x222BC58A)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSlider("Blur", blurValue, " dp", 0f..32f, backdrop, { blurValue = it }, { onBlurChange(blurValue) })
                }
            }

            SectionTitle("⟲", "Gboard controls")
            LiquidSurface(backdrop) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Restart Gboard", color = Foreground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Manually force-stop Gboard with root access. Android will start it again the next time an input field requests the keyboard.",
                        color = Secondary, fontSize = 14.sp, lineHeight = 20.sp
                    )
                    LiquidActionButton(
                        text = if (restarting) "Restarting…" else "Restart Gboard",
                        backdrop = backdrop,
                        enabled = !restarting
                    ) {
                        restarting = true
                        restartStatus = "Requesting root permission…"
                        Thread {
                            val result = forceStopGboard()
                            Handler(Looper.getMainLooper()).post {
                                restartStatus = result
                                restarting = false
                            }
                        }.start()
                    }
                    restartStatus?.let {
                        Text(it, color = if (it.contains("success", true)) AccentGreen else Secondary, fontSize = 13.sp)
                    }
                }
            }

            Text(
                "Settings changes are saved immediately. For the module to take effect, enable GlassLayer in your Xposed manager and keep its scope limited to Gboard.",
                color = Secondary.copy(alpha = 0.82f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun forceStopGboard(): String {
    return try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop com.google.android.inputmethod.latin"))
        val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            "Gboard force-stopped successfully. Open a text field to start it again."
        } else {
            "Root command failed (exit $exitCode). ${stderr.ifBlank { stdout }.take(180)}"
        }
    } catch (error: Exception) {
        "Could not run the root command: ${error.message ?: "unknown error"}"
    }
}

@Composable
private fun Header(backdrop: LayerBackdrop) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Glass Layer", color = Foreground, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.8).sp)
            Text("GBOARD EDITION", color = Secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
        Box(
            Modifier.size(52.dp).drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = { blur(8.dp.toPx()); lens(10.dp.toPx(), 18.dp.toPx()) },
                highlight = { Highlight.Ambient },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.07f)) }
            ),
            contentAlignment = Alignment.Center
        ) {
            Text("⋮", color = Foreground, fontSize = 28.sp)
        }
    }
}

@Composable
private fun SectionTitle(icon: String, title: String) {
    Row(Modifier.padding(start = 12.dp, top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, color = Secondary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text(title, color = Secondary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun LiquidSurface(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier.fillMaxWidth(),
    radius: androidx.compose.ui.unit.Dp = 32.dp,
    tint: Color = GlassFill,
    content: @Composable () -> Unit
) {
    val shape: Shape = RoundedCornerShape(radius)
    Box(
        modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(7.dp.toPx())
                lens(12.dp.toPx(), 24.dp.toPx(), chromaticAberration = true)
            },
            highlight = { Highlight.Ambient },
            shadow = { Shadow(radius = 18.dp, color = Color.Black.copy(alpha = 0.16f)) },
            onDrawSurface = {
                drawRect(tint)
                drawRect(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.075f), Color.White.copy(alpha = 0.012f))
                    )
                )
            }
        )
    ) {
        content()
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    backdrop: LayerBackdrop,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Foreground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Secondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        LiquidSwitch(checked, backdrop, onCheckedChange)
    }
}

@Composable
private fun LiquidSwitch(checked: Boolean, backdrop: LayerBackdrop, onCheckedChange: (Boolean) -> Unit) {
    val trackColor = if (checked) AccentGreen.copy(alpha = 0.92f) else Color(0x667A8791)
    Box(
        Modifier.size(width = 68.dp, height = 38.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = { blur(5.dp.toPx()); lens(6.dp.toPx(), 12.dp.toPx()) },
                highlight = { Highlight.Ambient },
                onDrawSurface = { drawRect(trackColor) }
            )
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            Modifier.size(30.dp).drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = { blur(3.dp.toPx()); lens(5.dp.toPx(), 10.dp.toPx()) },
                highlight = { Highlight.Ambient },
                shadow = { Shadow(radius = 4.dp, color = Color.Black.copy(alpha = 0.18f)) },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.98f)) }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiquidSlider(
    title: String,
    value: Float,
    suffix: String,
    range: ClosedFloatingPointRange<Float>,
    backdrop: LayerBackdrop,
    onValueChange: (Float) -> Unit,
    onFinished: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Foreground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("${value.toInt()}$suffix", color = Secondary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            onValueChangeFinished = onFinished,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AccentBlue,
                inactiveTrackColor = Color(0x557C8995),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            thumb = {
                Box(
                    Modifier.size(40.dp, 27.dp).drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(50) },
                        effects = { blur(7.dp.toPx()); lens(9.dp.toPx(), 16.dp.toPx()) },
                        highlight = { Highlight.Ambient },
                        shadow = { Shadow(radius = 5.dp, color = Color.Black.copy(alpha = 0.16f)) },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.98f)) }
                    )
                )
            }
        )
    }
}

@Composable
private fun LiquidActionButton(
    text: String,
    backdrop: LayerBackdrop,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth().height(54.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(20.dp) },
                effects = { vibrancy(); blur(4.dp.toPx()); lens(10.dp.toPx(), 18.dp.toPx()) },
                highlight = { Highlight.Ambient },
                shadow = { Shadow(radius = 8.dp, color = Color.Black.copy(alpha = 0.15f)) },
                onDrawSurface = { drawRect(if (enabled) AccentBlue.copy(alpha = 0.78f) else Color.Gray.copy(alpha = 0.4f)) }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.12f)))
}

@Composable
private fun KeyboardPreview(backdrop: LayerBackdrop, modifier: Modifier = Modifier) {
    Box(
        modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { RoundedCornerShape(22.dp) },
            effects = { blur(6.dp.toPx()); lens(8.dp.toPx(), 14.dp.toPx()) },
            highlight = { Highlight.Ambient },
            onDrawSurface = { drawRect(Color(0xB52A3038)) }
        ).padding(9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) {
                        Box(
                            Modifier.weight(1f).height(20.dp).drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(7.dp) },
                                effects = { blur(3.dp.toPx()); lens(4.dp.toPx(), 7.dp.toPx()) },
                                highlight = { Highlight.Ambient },
                                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.12f)) }
                            )
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.weight(1f).height(23.dp).drawBackdrop(backdrop, { RoundedCornerShape(7.dp) }, effects = { blur(3.dp.toPx()); lens(5.dp.toPx(), 8.dp.toPx()) }, highlight = { Highlight.Ambient }, onDrawSurface = { drawRect(AccentBlue) }))
                Box(Modifier.weight(3f).height(23.dp).drawBackdrop(backdrop, { RoundedCornerShape(7.dp) }, effects = { blur(3.dp.toPx()); lens(5.dp.toPx(), 8.dp.toPx()) }, highlight = { Highlight.Ambient }, onDrawSurface = { drawRect(Color.White.copy(alpha = 0.11f)) }))
                Box(Modifier.weight(1f).height(23.dp).drawBackdrop(backdrop, { RoundedCornerShape(7.dp) }, effects = { blur(3.dp.toPx()); lens(5.dp.toPx(), 8.dp.toPx()) }, highlight = { Highlight.Ambient }, onDrawSurface = { drawRect(AccentBlue) }))
            }
        }
    }
}
