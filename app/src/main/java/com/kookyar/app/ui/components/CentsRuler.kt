package com.kookyar.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.utils.toNum
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The horizontal ±50 cents ruler with the glowing teardrop needle
 * from the reference design. The marker glides with the same 300 ms
 * tween as the arc gauge so both stay perfectly readable.
 */
@Composable
fun CentsRuler(
    cents: Double,
    modifier: Modifier = Modifier,
    active: Boolean = true
) {
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current
    val clamped = cents.coerceIn(-50.0, 50.0)

    val markerRatio by animateFloatAsState(
        targetValue = (((clamped + 50.0) / 100.0).toFloat()),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rulerMarker"
    )

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(s.centsMinus50, color = palette.textDim, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.weight(1f))
            Text(s.centsPlus50, color = palette.textDim, style = MaterialTheme.typography.labelMedium)
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            val y = size.height * 0.62f
            val left = size.width * 0.04f
            val right = size.width * 0.96f
            val width = right - left

            // base line
            drawLine(
                color = palette.cardBorder,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 2.dp.toPx()
            )

            // ticks: long at -50/0/+50, short between
            val tickCount = 21
            for (i in 0 until tickCount) {
                val t = i / (tickCount - 1).toFloat()
                val x = left + width * t
                val major = i % 5 == 0
                drawLine(
                    color = if (major) palette.textDim else palette.cardBorder,
                    start = Offset(x, y - if (major) 10.dp.toPx() else 5.dp.toPx()),
                    end = Offset(x, y + if (major) 10.dp.toPx() else 5.dp.toPx()),
                    strokeWidth = if (major) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            if (active) {
                val x = left + width * markerRatio
                val needleColor = if (Math.abs(clamped) <= 3.0) palette.mint else palette.cyan

                // teardrop: stem + glowing head
                drawLine(
                    color = needleColor,
                    start = Offset(x, y - 6.dp.toPx()),
                    end = Offset(x, y - 26.dp.toPx()),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(needleColor.copy(alpha = 0.35f), Color.Transparent),
                        radius = 16.dp.toPx()
                    ),
                    radius = 16.dp.toPx(),
                    center = Offset(x, y - 30.dp.toPx())
                )
                drawCircle(
                    color = needleColor,
                    radius = 6.5.dp.toPx(),
                    center = Offset(x, y - 30.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.4.dp.toPx(),
                    center = Offset(x, y - 30.dp.toPx())
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                s.centsZero, color = palette.textDim,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.weight(1f))
            Text(
                s.deviation(clamped.roundToInt().toNum(lang)),
                color = palette.textDim,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight(1f))
            Text(
                s.centsZero, color = Color.Transparent,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/** Draws a smooth bezier path - used by the ambient wave. */
internal fun wavePath(
    width: Float,
    height: Float,
    phase: Float,
    amplitude: Float,
    cycles: Float
): Path {
    val path = Path()
    val steps = 60
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val x = width * t
        val y = height / 2f +
            sin((t * cycles + phase) * 2f * Math.PI).toFloat() * amplitude +
            sin((t * cycles * 2.3f + phase * 1.7f) * Math.PI).toFloat() * amplitude * 0.35f
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}
