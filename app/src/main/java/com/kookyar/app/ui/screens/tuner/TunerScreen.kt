package com.kookyar.app.ui.screens.tuner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kookyar.app.core.audio.Permission
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.ui.components.ArcGauge
import com.kookyar.app.ui.components.CentsRuler
import com.kookyar.app.ui.components.GlassCard
import com.kookyar.app.ui.components.NoteHero
import com.kookyar.app.ui.components.ParticleBurst
import com.kookyar.app.ui.components.StatusPill
import com.kookyar.app.ui.components.StringPill
import com.kookyar.app.ui.components.StringPillRow
import com.kookyar.app.ui.components.WaveTrace
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum

@Composable
fun TunerScreen(
    onOpenSettings: () -> Unit,
    onOpenInstruments: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    val vm: TunerViewModel = viewModel()
    val state by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.start(context)
    }

    DisposableEffect(Unit) {
        if (Permission.canRecordAudio(context)) {
            vm.start(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        onDispose { vm.stop() }
    }

    DisposableEffect(state.settings.keepScreenOn) {
        val window = context.findActivity()?.window
        if (state.settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------- top bar ---------- //
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, s.settings, tint = palette.textDim)
            }
            Spacer(Modifier.weight(1f))
            StatusPill(
                text = "${s.instrumentWord}: ${state.instrument.localizedName(lang)} • ${state.tuning.localizedName(lang)}",
                tint = palette.textDim,
                onClick = onOpenInstruments
            )
            Spacer(Modifier.weight(1f))
            // language switch - right next to the theme button
            IconButton(onClick = onToggleLanguage) {
                Text(
                    lang.switchLabel,
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = palette.gold
                )
            }
            IconButton(onClick = onCycleTheme) {
                Text("✨", fontSize = 20.sp)
            }
        }

        // ---------- headline ---------- //
        Text(
            text = when {
                state.inTune -> s.tunerPerfect
                state.listening -> s.tunerPrompt
                else -> s.tunerTitle
            },
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = if (state.inTune) palette.mint else palette.onBackground
        )
        Text(
            s.tunerSubtitle,
            fontFamily = Vazirmatn,
            fontSize = 12.sp,
            color = palette.textDim
        )

        Spacer(Modifier.height(4.dp))

        // ---------- hero: note + particles ---------- //
        // (web parity: the ONLY glow is the unified halo inside NoteHero;
        //  the box hugs its content - the old fixed 240.dp height added
        //  ~45 dp of dead air above and below the note, the big red-boxed
        //  gaps from the user's screenshot)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (state.settings.particles) {
                ParticleBurst(
                    burstKey = state.burstKey,
                    modifier = Modifier.fillMaxSize()
                )
            }
            NoteHero(
                noteName = state.displayNote,
                // web parity: no decorative ribbons behind the note
                waves = false,
                subtitle = run {
                    // web parity: show the target frequency when nothing is
                    // detected yet (selected string target, or A4 in chromatic)
                    val sel = state.selectedString
                    val targetHz = when {
                        state.frequency > 0 -> state.displayFrequency
                        sel != null -> state.tuning.strings.getOrNull(sel)
                            ?.let { com.kookyar.app.core.music.MusicTheory.frequency(it.step, it.octave, state.settings.a4) }
                            ?: state.settings.a4
                        else -> state.settings.a4
                    }
                    if (targetHz > 0) "${targetHz.toInt().toNum(lang)} ${s.hzSuffix}"
                    else state.tuning.localizedName(lang)
                },
                active = state.listening && state.frequency > 0,
                glowColor = if (state.inTune) palette.mint else palette.glow
            )
        }

        // ---------- gauge + ruler ---------- //
        ArcGauge(
            cents = state.displayCents,
            active = state.listening && state.frequency > 0,
            gaugeSize = 250.dp
        ) {
            // center readout - same content as the web gauge
            val live = state.listening && state.frequency > 0
            if (live && state.inTune) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(palette.mint.copy(alpha = 0.15f))
                            .border(2.dp, palette.mint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = palette.mint, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        s.lockedCenter,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = palette.mint
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val shown = state.displayCents.coerceIn(-50.0, 50.0)
                    Text(
                        (if (shown >= 0) "+" else "\u2212") +
                            kotlin.math.abs(shown).toNum(lang, 1),
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = palette.onBackground
                    )
                    Text(
                        s.centsUnit,
                        fontFamily = Vazirmatn,
                        fontSize = 10.sp,
                        color = palette.textDim
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            live -> state.hint
                            state.listening -> s.hintListening
                            else -> s.statusReady
                        },
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (live) palette.mint else palette.textDim,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 36.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        CentsRuler(
            cents = state.displayCents,
            active = state.listening && state.frequency > 0
        )

        Spacer(Modifier.height(14.dp))

        // ---------- status card ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.inTune) palette.mint.copy(alpha = 0.18f) else palette.cardStrong
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            null,
                            tint = if (state.inTune) palette.mint else palette.textDim,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            when {
                                state.inTune -> s.statusPerfect
                                state.listening -> s.statusAnalyzing
                                else -> s.statusReady
                            },
                            fontFamily = Vazirmatn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (state.inTune) palette.mint else palette.onSurface
                        )
                        Text(
                            state.hint,
                            fontFamily = Vazirmatn,
                            fontSize = 12.sp,
                            color = palette.textDim
                        )
                    }
                }
                AccuracyRing(accuracy = state.accuracy, active = state.listening && state.frequency > 0)
            }
        }

        // ---------- permission card ---------- //
        if (state.permissionDenied && !state.listening) {
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    s.micPermissionNeeded,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textDim
                )
                Spacer(Modifier.height(8.dp))
                StatusPill(
                    text = s.grantPermission,
                    tint = palette.mint,
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
            }
        }

        // ---------- strings ---------- //
        if (state.tuning.strings.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "🪕 ${s.stringsTitle}",
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = palette.textDim,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                StringPillRow(
                    pills = state.tuning.strings.map { spec ->
                        StringPill(
                            label = s.noteName(spec.step),
                            targetHz = com.kookyar.app.core.music.MusicTheory.frequency(
                                spec.step, spec.octave, state.settings.a4
                            ),
                            step = spec.step,
                            octave = spec.octave
                        )
                    },
                    selectedIndex = state.selectedString,
                    tunedIndexes = state.tunedStrings,
                    onSelect = vm::selectString
                )
                state.selectedString?.let { idx ->
                    val spec = state.tuning.strings.getOrNull(idx)
                    if (spec != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${s.targetWord}: ${s.noteName(spec.step)} • " +
                                "${com.kookyar.app.core.music.MusicTheory.frequency(spec.step, spec.octave, state.settings.a4).toInt().toNum(lang)} ${s.hzSuffix}",
                            fontFamily = Vazirmatn,
                            fontSize = 12.sp,
                            color = palette.mint
                        )
                    }
                }
            }
        }

        // ---------- waveform ---------- //
        Spacer(Modifier.height(14.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state.listening) palette.mint else palette.textDim.copy(alpha = 0.4f))
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    if (state.listening) s.waveOn else s.waveOff,
                    fontFamily = Vazirmatn,
                    fontSize = 12.sp,
                    color = palette.textDim
                )
            }
            WaveTrace(
                samples = state.waveform,
                modifier = Modifier.fillMaxWidth(),
                active = state.listening
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AccuracyRing(accuracy: Int, active: Boolean) {
    val palette = KookTheme.colors
    val s = LocalStrings.current
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val stroke = 6.dp.toPx()
            drawCircle(color = palette.cardStrong, style = Stroke(stroke))
            if (active && accuracy > 0) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(palette.cyan, palette.mint, palette.mint)),
                    startAngle = -90f,
                    sweepAngle = 360f * (accuracy / 100f),
                    useCenter = false,
                    style = Stroke(stroke)
                )
            }
        }
        Text(
            if (active) "${accuracy.toNum(LocalAppLang.current)}${s.percentSuffix}" else "–",
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (accuracy >= 95) palette.mint else palette.textDim
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
