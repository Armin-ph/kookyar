package com.kookyar.app.ui.screens.metronome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.core.metronome.ClickSynth
import com.kookyar.app.core.metronome.MetronomeEngine
import com.kookyar.app.ui.components.AccentCell
import com.kookyar.app.ui.components.BeatRing
import com.kookyar.app.ui.components.BpmDisplay
import com.kookyar.app.ui.components.GlassCard
import com.kookyar.app.ui.components.KookSlider
import com.kookyar.app.ui.components.OrnamentDivider
import com.kookyar.app.ui.components.SelectChip
import com.kookyar.app.ui.components.StepperButton
import com.kookyar.app.ui.components.accentColor
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetronomeScreen(
    onOpenSettings: () -> Unit
) {
    val vm: MetronomeViewModel = viewModel()
    val state by vm.ui.collectAsStateWithLifecycle()
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current
    var showSaveDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    fun subdivisionLabel(sub: MetronomeEngine.Subdivision) = if (lang.isFa) sub.faName else sub.enName
    fun soundLabel(sound: ClickSynth.ClickSound) = if (lang.isFa) sound.faName else sound.enName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------- header ---------- //
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, s.settings, tint = palette.textDim)
            }
            Spacer(Modifier.weight(1f))
            Text(
                s.metronomeTitle,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = palette.onBackground
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }
        OrnamentDivider(modifier = Modifier.padding(horizontal = 60.dp))

        Spacer(Modifier.height(12.dp))

        // ---------- main beat card (ring + bpm inside + slider + play/tap) ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            BeatRing(
                beatsPerBar = if (state.sigIndex >= 0)
                    MetronomeViewModel.TIME_SIGNATURES[state.sigIndex].first else state.customBeats,
                currentBeat = state.currentBeat,
                accents = state.accents,
                playing = state.running,
                ringSize = 240.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BpmDisplay(
                        bpm = state.bpm,
                        pulse = state.running && state.pulseKey % 2 == 1,
                        fontSize = 56,
                        color = if (state.running) palette.primary else palette.onSurface
                    )
                    Text(
                        vm.tempoWord(lang),
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // bpm slider: [−] ——— [+]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StepperButton(Icons.Rounded.Remove, s.slower) { vm.nudgeBpm(-1) }
                KookSlider(
                    value = ((state.bpm - 30) / 220f).coerceIn(0f, 1f),
                    onValueChange = { f -> vm.setBpm(30 + (f * 220f).roundToInt()) },
                    modifier = Modifier.weight(1f)
                )
                StepperButton(Icons.Rounded.Add, s.faster) { vm.nudgeBpm(1) }
            }

            Spacer(Modifier.height(14.dp))

            // play + tap (side by side, like the web)
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (state.running) {
                                Modifier
                                    .background(palette.cardStrong)
                                    .border(1.dp, palette.cardBorder, RoundedCornerShape(18.dp))
                            } else {
                                Modifier.background(
                                    Brush.horizontalGradient(listOf(palette.primary, palette.mint))
                                )
                            }
                        )
                        .clickable { vm.toggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (state.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = s.playStop,
                            tint = if (state.running) palette.onSurface else palette.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            if (state.running) (if (lang.isFa) "توقف" else "Stop")
                            else (if (lang.isFa) "پخش" else "Play"),
                            fontFamily = Vazirmatn,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (state.running) palette.onSurface else palette.onPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.card)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(18.dp))
                        .clickable { vm.tapTempo() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Rounded.TouchApp,
                            contentDescription = s.tapTitle,
                            tint = palette.violet,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            s.tapTitle,
                            fontFamily = Vazirmatn,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = palette.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- time signature (full width, wrapped chips) ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("♫", color = palette.primary, fontSize = 14.sp)
                Text(
                    s.timeSigTitle,
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = palette.onSurface
                )
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetronomeViewModel.TIME_SIGNATURES.forEachIndexed { i, (b, d) ->
                    SelectChip("${b.toNum(lang)}/${d.toNum(lang)}", state.sigIndex == i) {
                        vm.setTimeSignature(i)
                    }
                }
                SelectChip(s.customSig, state.sigIndex == -1) { showCustomDialog = true }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- subdivision (full width, wrapped chips) ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Text(
                s.subdivisionTitle,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = palette.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetronomeEngine.Subdivision.entries.forEach { sub ->
                    SelectChip(subdivisionLabel(sub), state.subdivision == sub) {
                        vm.setSubdivision(sub)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- accent pattern ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Text(
                s.accentPattern,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = palette.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (lang.isFa) "روی هر ضرب بزنید تا قوی ← ملایم ← بدون تاکید تغییر کند"
                else "Tap any beat to cycle strong → medium → none",
                fontFamily = Vazirmatn,
                fontSize = 10.sp,
                color = palette.textDim,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val beats = if (state.sigIndex >= 0)
                    MetronomeViewModel.TIME_SIGNATURES[state.sigIndex].first else state.customBeats
                for (i in 0 until beats) {
                    AccentCell(
                        index = i,
                        level = state.accents.getOrElse(i) { 0 },
                        active = state.running && state.currentBeat == i,
                        onClick = { vm.cycleAccent(i) },
                        modifier = Modifier.size(width = 62.dp, height = 60.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendDot(accentColor(2), s.accentStrong)
                LegendDot(accentColor(1), s.accentMedium)
                LegendDot(accentColor(0), s.accentNone)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- practice timer + auto ramp ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(palette.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Timer, null, tint = palette.primary, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatDuration(state.elapsedSeconds, lang),
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = palette.cyan
                    )
                    Text(
                        s.practiceTime,
                        fontFamily = Vazirmatn,
                        fontSize = 10.sp,
                        color = palette.textDim
                    )
                }
                TextButton(onClick = vm::resetSession) {
                    Text(s.resetWord, color = palette.textDim, fontFamily = Vazirmatn)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ramp toggle (own row — no more clipped «هدف» text)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.cardStrong)
                    .clickable { vm.setRamp(!state.rampEnabled) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.rampEnabled) "✓" else "○",
                    color = if (state.rampEnabled) palette.mint else palette.textDim,
                    fontSize = 15.sp
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    s.autoRamp,
                    fontFamily = Vazirmatn,
                    fontSize = 13.sp,
                    color = palette.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.rampEnabled) {
                Spacer(Modifier.height(8.dp))
                // target bpm stepper row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StepperButtonSmall(Icons.Rounded.Remove, "-") { vm.setRampTarget(state.rampTargetBpm - 5) }
                    Text(
                        "${s.targetWord2}: ${state.rampTargetBpm.toNum(lang)} BPM",
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim,
                        modifier = Modifier.weight(1f)
                    )
                    StepperButtonSmall(Icons.Rounded.Add, "+") { vm.setRampTarget(state.rampTargetBpm + 5) }
                }
                Spacer(Modifier.height(6.dp))
                // minutes stepper row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StepperButtonSmall(Icons.Rounded.Remove, "-") { vm.setRampMinutes(state.rampMinutes - 1) }
                    Text(
                        "${s.minutesWord}: ${state.rampMinutes.toNum(lang)}",
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim,
                        modifier = Modifier.weight(1f)
                    )
                    StepperButtonSmall(Icons.Rounded.Add, "+") { vm.setRampMinutes(state.rampMinutes + 1) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- sound + volume ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Text(
                s.soundType,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = palette.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClickSynth.ClickSound.entries.forEach { sound ->
                    SelectChip(soundLabel(sound), state.sound == sound) { vm.setSound(sound) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Rounded.VolumeUp,
                    null,
                    tint = palette.textDim,
                    modifier = Modifier.size(18.dp)
                )
                KookSlider(
                    value = state.volume.coerceIn(0f, 1f),
                    onValueChange = { vm.setVolume(it) },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(state.volume * 100).toInt().toNum(lang)}${s.percentSuffix}",
                    fontFamily = Vazirmatn,
                    fontSize = 12.sp,
                    color = palette.textDim
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- presets ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Timer, null, tint = palette.primary, modifier = Modifier.size(16.dp))
                Text(
                    s.presetsTitle,
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = palette.onSurface
                )
                Spacer(Modifier.weight(1f))
                SelectChip(s.save, selected = false, leadingIcon = Icons.Rounded.Save) {
                    showSaveDialog = true
                }
            }
            Spacer(Modifier.height(10.dp))
            if (state.presets.isEmpty()) {
                Text(
                    s.presetsEmpty,
                    fontFamily = Vazirmatn,
                    fontSize = 12.sp,
                    color = palette.textDim,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.presets.take(8).forEach { preset ->
                        PresetChip(
                            name = preset.name,
                            bpm = preset.bpm.toNum(lang),
                            onApply = { vm.applyPreset(preset) },
                            onDelete = { vm.deletePreset(preset.id) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }

    // ---------- custom time signature dialog ---------- //
    if (showCustomDialog) {
        var draft by remember { mutableIntStateOf(state.customBeats.coerceIn(2, 12)) }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text(s.customSigTitle, fontFamily = Vazirmatn, color = palette.onSurface) },
            text = {
                Column {
                    Text(
                        s.customSigHint,
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${s.beatCountWord}: ${draft.toNum(lang)}",
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    KookSlider(
                        value = ((draft - 2) / 10f).coerceIn(0f, 1f),
                        onValueChange = { f -> draft = 2 + (f * 10f).roundToInt() }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.setCustomBeats(draft)
                        showCustomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = palette.onPrimary
                    )
                ) { Text(s.applyWord, fontFamily = Vazirmatn) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text(s.cancel, fontFamily = Vazirmatn, color = palette.textDim)
                }
            }
        )
    }

    // ---------- save preset dialog ---------- //
    if (showSaveDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text(s.savePresetTitle, fontFamily = Vazirmatn, color = palette.onSurface) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(s.presetNameHint, fontFamily = Vazirmatn) }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.savePreset(name)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = palette.onPrimary
                    )
                ) { Text(s.save, fontFamily = Vazirmatn) }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(s.cancel, fontFamily = Vazirmatn, color = palette.textDim)
                }
            }
        )
    }
}

/** Preset chip with an inline delete (web style). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetChip(
    name: String,
    bpm: String,
    onApply: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = KookTheme.colors
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(palette.card)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
                .clickable(onClick = onApply)
                .padding(start = 14.dp, end = 30.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, color = palette.onSurface, style = MaterialTheme.typography.labelLarge)
            Text(
                " $bpm",
                fontFamily = Vazirmatn,
                fontSize = 9.sp,
                color = palette.textDim
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(26.dp)
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = LocalStrings.current.delete,
                tint = palette.textDim,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val palette = KookTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(label, fontFamily = Vazirmatn, fontSize = 11.sp, color = palette.textDim)
    }
}

@Composable
private fun StepperButtonSmall(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(KookTheme.colors.card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, desc, tint = KookTheme.colors.onSurface, modifier = Modifier.size(16.dp))
    }
}

private fun formatDuration(seconds: Int, lang: com.kookyar.app.core.i18n.AppLanguage): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toNum(lang)}:${s.toString().padStart(2, '0').toNum(lang)}"
}
