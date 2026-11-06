package com.kookyar.app.ui.screens.ear

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.kookyar.app.ui.components.GlassCard
import com.kookyar.app.ui.components.OrnamentDivider
import com.kookyar.app.ui.components.SelectChip
import com.kookyar.app.ui.components.SparkleField
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum

@Composable
fun EarTrainingScreen(
    onOpenSettings: () -> Unit
) {
    val vm: EarTrainingViewModel = viewModel()
    val state by vm.ui.collectAsStateWithLifecycle()
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, s.settings, tint = palette.textDim)
            }
            Spacer(Modifier.weight(1f))
            Text(
                s.earTitle,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = palette.onBackground
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }
        OrnamentDivider(modifier = Modifier.padding(horizontal = 40.dp))

        Spacer(Modifier.height(14.dp))

        // ---------- level selector ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    s.difficulty,
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SelectChip("${3.toNum(lang)} ${s.optionsWord}", state.optionsCount == 3) { vm.setOptionsCount(3) }
                    SelectChip("${4.toNum(lang)} ${s.optionsWord}", state.optionsCount == 4) { vm.setOptionsCount(4) }
                    SelectChip("${6.toNum(lang)} ${s.optionsWord}", state.optionsCount == 6) { vm.setOptionsCount(6) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.cardStrong)
                    .clickable { vm.toggleReferenceFirst() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.playReferenceFirst) "✓" else "○",
                    color = if (state.playReferenceFirst) palette.mint else palette.textDim,
                    fontSize = 16.sp
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    s.playRefFirst,
                    fontFamily = Vazirmatn,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- score card ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreBox(s.scoreRound, state.round.toNum(lang), palette.cyan)
                ScoreBox(s.scorePoints, state.score.toNum(lang), palette.mint)
                ScoreBox(s.scoreStreak, state.streak.toNum(lang), palette.gold)
                ScoreBox(s.scoreBest, state.bestStreak.toNum(lang), palette.violet)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- play card ---------- //
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom))
                ),
            contentAlignment = Alignment.Center
        ) {
            SparkleField(modifier = Modifier.fillMaxSize())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.Hearing,
                    null,
                    tint = palette.mint,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    when {
                        state.round == 0 -> s.earStartPrompt
                        state.revealed && state.lastCorrect == true -> state.praise
                        state.revealed -> state.praise
                        else -> s.earListenPrompt
                    },
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (state.revealed && state.lastCorrect == true) palette.mint
                    else if (state.revealed) palette.coral
                    else palette.onSurface
                )
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        if (state.round == 0 || state.revealed) vm.startRound() else vm.playTarget()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = palette.primary,
                        contentColor = palette.onPrimary
                    )
                ) {
                    Icon(Icons.Rounded.MusicNote, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        when {
                            state.round == 0 -> s.btnStartRound
                            state.revealed -> s.btnNextRound
                            else -> s.btnReplayNote
                        },
                        fontFamily = Vazirmatn
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- answer options ---------- //
        if (state.round > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                state.options.chunked(2).forEach { rowOptions ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowOptions.forEach { step ->
                            val isTarget = step == state.targetStep
                            val isSelected = step == state.selected
                            val bg = when {
                                state.revealed && isTarget -> palette.mint.copy(alpha = 0.20f)
                                state.revealed && isSelected -> palette.coral.copy(alpha = 0.18f)
                                else -> palette.card
                            }
                            val border = when {
                                state.revealed && isTarget -> palette.mint
                                state.revealed && isSelected -> palette.coral
                                else -> palette.cardBorder
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(bg)
                                    .border(1.2.dp, border, RoundedCornerShape(18.dp))
                                    .clickable { vm.answer(step) }
                                    .padding(vertical = 22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    s.noteName(step),
                                    fontFamily = Vazirmatn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = when {
                                        state.revealed && isTarget -> palette.mint
                                        state.revealed && isSelected -> palette.coral
                                        else -> palette.onSurface
                                    }
                                )
                            }
                        }
                        if (rowOptions.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Replay, null, tint = palette.textDim, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text(
                    s.earHint,
                    fontFamily = Vazirmatn,
                    fontSize = 11.sp,
                    color = palette.textDim
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ScoreBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(value, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontFamily = Vazirmatn, fontSize = 11.sp, color = KookTheme.colors.textDim)
    }
}
