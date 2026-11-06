package com.kookyar.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kookyar.app.ui.theme.KookTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Web-preview parity gauge (the «آمپر»): a ±135° dial carrying ALL the
 * details of the reference design -
 *  - 28 decorative outer segments
 *  - dim base track + violet near zones (±20c) + mint hot zone (±5c)
 *  - animated gradient progress arc with a soft glow pass
 *  - 21 ticks (every 5 cents, major every 10)
 *  - thin needle with a glowing mint tip + hub
 *  - a [center] slot for the cents readout / status content.
 *
 * The needle angle is ANIMATED with a ~340 ms critically-damped tween
 * (matching the web transition) so raw ~21 Hz readings glide into place.
 */
@Composable
fun ArcGauge(
    cents: Double,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    gaugeSize: Dp = 250.dp,
    center: @Composable () -> Unit = {}
) {
    val palette = KookTheme.colors
    val clamped = cents.coerceIn(-50.0, 50.0)

    val needleDeg by animateFloatAsState(
        targetValue = (clamped / 50.0 * SPAN_DEG).toFloat(),
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
        label = "needleDeg"
    )

    Box(modifier = modifier.size(gaugeSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // the web viewBox is 300x240 - every web px scales by [u]
            val u = min(size.width / 300f, size.height / 240f)
            val cx = size.width / 2f
            val cy = size.height * 0.615f
            val rArc = RADIUS * u
            val accent = palette.mint      // web --accent
            val accent2 = palette.violet   // web --accent-2
            val accent3 = palette.mint     // web --accent-3
            val dim = palette.cardBorder   // web --gauge-dim
            val muted = palette.textDim
            val needleColor = palette.onBackground

            fun polar(r: Float, deg: Float): Offset {
                val rad = Math.toRadians(deg.toDouble())
                return Offset((cx + r * sin(rad)).toFloat(), (cy - r * cos(rad)).toFloat())
            }

            fun arcPath(r: Float, a0: Float, a1: Float): Path = Path().apply {
                arcTo(
                    rect = Rect(cx - r, cy - r, cx + r, cy + r),
                    startAngleDegrees = a0 - 90f, // web angles start at 12 o'clock
                    sweepAngleDegrees = a1 - a0,
                    forceMoveTo = true
                )
            }

            // 1) decorative outer segments
            for (i in 0 until 28) {
                val a = -SPAN_DEG + i * (SPAN_DEG * 2f / 27f)
                drawLine(
                    color = dim,
                    start = polar(OUTER_R * u, a - 2.2f),
                    end = polar(OUTER_R * u, a + 2.2f),
                    strokeWidth = 2.4f * u,
                    cap = StrokeCap.Round,
                    alpha = 0.55f
                )
            }

            // 2) base track
            drawPath(
                path = arcPath(rArc, -SPAN_DEG, SPAN_DEG),
                color = dim,
                style = Stroke(width = 13f * u, cap = StrokeCap.Round),
                alpha = 0.9f
            )

            // 3) near zones (±20 cents)
            val nearZones = listOf(-54f to -13.5f, 13.5f to 54f)
            for ((a0, a1) in nearZones) {
                drawPath(
                    path = arcPath(rArc, a0, a1),
                    color = accent2,
                    style = Stroke(width = 13f * u, cap = StrokeCap.Round),
                    alpha = 0.3f
                )
            }

            // 4) hot zone (±5 cents)
            drawPath(
                path = arcPath(rArc, -13.5f, 13.5f),
                color = accent,
                style = Stroke(width = 13f * u, cap = StrokeCap.Round),
                alpha = 0.85f
            )

            // 5) progress arc with glow (only while listening)
            if (active) {
                val progressGrad = Brush.horizontalGradient(
                    listOf(accent2, accent, accent3),
                    startX = cx - rArc,
                    endX = cx + rArc
                )
                drawPath(
                    path = arcPath(rArc, -SPAN_DEG, needleDeg),
                    brush = progressGrad,
                    style = Stroke(width = 19f * u, cap = StrokeCap.Round),
                    alpha = 0.16f
                )
                drawPath(
                    path = arcPath(rArc, -SPAN_DEG, needleDeg),
                    brush = progressGrad,
                    style = Stroke(width = 13f * u, cap = StrokeCap.Round)
                )
            }

            // 6) ticks: every 5 cents, major every 10
            var c = -50
            while (c <= 50) {
                val deg = (c / 50f) * SPAN_DEG
                val major = c % 10 == 0
                drawLine(
                    color = muted,
                    start = polar(rArc - 12f * u, deg),
                    end = polar(rArc - (if (major) 20f else 17f) * u, deg),
                    strokeWidth = (if (major) 2f else 1f) * u,
                    cap = StrokeCap.Round,
                    alpha = if (major) 0.7f else 0.35f
                )
                c += 5
            }

            // 7) needle: thin line + glowing mint tip
            drawLine(
                color = needleColor,
                start = polar(34f * u, needleDeg),
                end = polar(92f * u, needleDeg),
                strokeWidth = 3.6f * u,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = accent.copy(alpha = 0.25f),
                radius = 9f * u,
                center = polar(92f * u, needleDeg)
            )
            drawCircle(color = accent, radius = 5f * u, center = polar(92f * u, needleDeg))

            // 8) hub
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(palette.cardStrong, palette.card),
                    center = Offset(cx, cy),
                    radius = 9f * u
                ),
                radius = 9f * u,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = palette.cardBorder,
                radius = 9f * u,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f * u)
            )
        }

        // center readout (cents / status) - same proportions as the web:
        // content area = top 30% .. bottom 12% of the box, centered
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(gaugeSize)
                .padding(top = gaugeSize * 0.30f, bottom = gaugeSize * 0.12f),
            contentAlignment = Alignment.Center
        ) {
            center()
        }
    }
}

/** Compact circular dial used inside the theme preview cards. */
@Composable
fun MiniGauge(
    modifier: Modifier = Modifier,
    needleTint: Color = KookTheme.colors.gold
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.09f
        val radius = size.minDimension / 2f - stroke
        drawArc(
            color = Color.White.copy(alpha = 0.25f),
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(needleTint.copy(alpha = 0.15f), needleTint, needleTint.copy(alpha = 0.15f))),
            startAngle = 240f,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // needle tilted right like the mockups
        val rad = Math.toRadians(285.0)
        drawLine(
            color = needleTint,
            start = Offset(
                (size.width / 2 + (radius - stroke) * cos(rad)).toFloat(),
                (size.height / 2 + (radius - stroke) * sin(rad)).toFloat()
            ),
            end = Offset(
                (size.width / 2 + (radius - stroke * 3) * cos(rad)).toFloat(),
                (size.height / 2 + (radius - stroke * 3) * sin(rad)).toFloat()
            ),
            strokeWidth = stroke * 0.5f,
            cap = StrokeCap.Round
        )
    }
}

/** Total needle sweep in degrees (web SPAN constant). */
private const val SPAN_DEG = 135f

/** Arc radius in web viewBox units. */
private const val RADIUS = 108f

/** Decorative outer segments radius in web viewBox units. */
private const val OUTER_R = 132f
