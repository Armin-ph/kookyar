package com.kookyar.app.ui.screens.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.data.repository.TuningsRepository
import com.kookyar.app.ui.components.GlassCard
import com.kookyar.app.ui.components.OrnamentDivider
import com.kookyar.app.ui.components.StatCard
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    onOpenSettings: () -> Unit
) {
    val vm: HistoryViewModel = viewModel()
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
                s.historyTitle,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = palette.onBackground
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = vm::clearAll) {
                Icon(Icons.Rounded.Delete, s.clearHistory, tint = palette.textDim, modifier = Modifier.size(20.dp))
            }
        }
        OrnamentDivider(modifier = Modifier.padding(horizontal = 40.dp))

        Spacer(Modifier.height(12.dp))

        // ---------- weekly chart ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        s.chartTitle,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        s.chartSubtitle,
                        fontFamily = Vazirmatn,
                        fontSize = 11.sp,
                        color = palette.textDim
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(palette.cardStrong)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(s.last7Days, fontFamily = Vazirmatn, fontSize = 11.sp, color = palette.textDim)
                }
            }
            Spacer(Modifier.height(12.dp))
            WeeklyChart(
                data = state.week.map { it.accuracy },
                labels = state.week.map { it.label },
                modifier = Modifier.fillMaxWidth().height(190.dp)
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                LegendLine(palette.mint, s.legendDaily)
                LegendLine(palette.cyan, s.legendWeekAvg)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- stat cards ---------- //
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                icon = Icons.Rounded.EventNote,
                iconTint = palette.cyan,
                label = s.statWeekCount,
                value = state.weekCount.toNum(lang),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Rounded.Star,
                iconTint = palette.gold,
                label = s.statBestDay,
                value = state.bestDay,
                modifier = Modifier.weight(1f),
                valueColor = palette.gold
            )
            StatCard(
                icon = Icons.Rounded.TrackChanges,
                iconTint = palette.mint,
                label = s.statAvgAcc,
                value = "${state.avgAccuracy.toNum(lang)}${s.percentSuffix}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // ---------- trend ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(palette.gold.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎼", fontSize = 24.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        s.trendTitle,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        when {
                            state.records.isEmpty() -> s.trendEmpty
                            state.trendUp -> s.trendUp(state.trendDelta.toNum(lang))
                            else -> s.trendDown((-state.trendDelta).toNum(lang))
                        },
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim
                    )
                }
                Text(
                    if (state.trendUp) "📈" else "📉",
                    fontSize = 22.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- records ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    s.recentTunings,
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (state.records.isEmpty()) {
                    Text(
                        s.nothingYet,
                        fontFamily = Vazirmatn,
                        fontSize = 11.sp,
                        color = palette.textDim
                    )
                }
            }
            if (state.records.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.records, key = { it.id }) { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .background(palette.cardStrong)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (rec.accuracy >= 90) palette.mint.copy(alpha = 0.15f)
                                        else palette.gold.copy(alpha = 0.13f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    s.noteName(rec.step),
                                    fontFamily = Vazirmatn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (rec.accuracy >= 90) palette.mint else palette.gold
                                )
                            }
                            Spacer(Modifier.size(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val inst = TuningsRepository.byId(rec.instrumentId)
                                val instName = inst?.localizedName(lang) ?: rec.instrumentName
                                val tunName = inst?.tunings?.firstOrNull { it.id == rec.tuningId }
                                    ?.localizedName(lang) ?: rec.tuningName
                                Text(
                                    "$instName • $tunName",
                                    fontFamily = Vazirmatn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    formatTime(rec.at),
                                    fontFamily = Vazirmatn,
                                    fontSize = 10.sp,
                                    color = palette.textDim
                                )
                            }
                            Text(
                                "${rec.accuracy.toNum(lang)}${s.percentSuffix}",
                                fontFamily = Vazirmatn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (rec.accuracy >= 90) palette.mint else palette.gold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun WeeklyChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val palette = KookTheme.colors
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val leftPad = 34.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad

        // grid + y labels 0..100
        for (v in 0..100 step 20) {
            val y = chartH - chartH * v / 100f
            drawLine(
                color = palette.cardBorder,
                start = Offset(leftPad, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = chartW / (data.size - 1)
        fun point(i: Int, v: Int) = Offset(leftPad + stepX * i, chartH - chartH * v / 100f)

        // daily line (mint) + fill
        val dailyPath = Path()
        data.indices.forEach { i -> val p = point(i, data[i]); if (i == 0) dailyPath.moveTo(p.x, p.y) else dailyPath.lineTo(p.x, p.y) }
        drawPath(
            brush = Brush.verticalGradient(listOf(palette.mint.copy(alpha = 0.25f), Color.Transparent), startY = 0f, endY = chartH),
            path = Path().apply {
                addPath(dailyPath)
                lineTo(leftPad + chartW, chartH)
                lineTo(leftPad, chartH)
                close()
            }
        )
        drawPath(dailyPath, color = palette.mint, style = Stroke(2.5.dp.toPx()))

        // week average line (cyan)
        val avg = data.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
        val avgY = chartH - chartH * avg / 100f
        drawLine(
            color = palette.cyan,
            start = Offset(leftPad, avgY),
            end = Offset(size.width, avgY),
            strokeWidth = 2.dp.toPx()
        )

        // dots
        data.indices.forEach { i ->
            val p = point(i, data[i])
            drawCircle(color = palette.mint, radius = 4.dp.toPx(), center = p)
            drawCircle(color = Color.White, radius = 1.6.dp.toPx(), center = p)
        }

        // x labels handled by caller (canvas text is complex); draw tick dots
        data.indices.forEach { i ->
            val x = leftPad + stepX * i
            drawLine(
                color = palette.cardBorder,
                start = Offset(x, chartH),
                end = Offset(x, chartH + 4.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
    // weekday labels under the chart
    Row(modifier = Modifier.fillMaxWidth().padding(start = 34.dp)) {
        labels.forEach { label ->
            Text(
                label,
                fontSize = 10.sp,
                fontFamily = Vazirmatn,
                color = palette.textDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LegendLine(color: Color, label: String) {
    val palette = KookTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontFamily = Vazirmatn, fontSize = 11.sp, color = palette.textDim)
    }
}

private fun formatTime(epochMs: Long): String {
    val zoned = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
    return zoned.format(DateTimeFormatter.ofPattern("HH:mm"))
}
