package com.kookyar.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum
import kotlin.math.abs
import kotlin.math.sin

/**
 * The hero note glyph with glow + the flowing ambient waves behind it
 * (first reference screen).
 */
@Composable
fun NoteHero(
    noteName: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    active: Boolean = true,
    glowColor: Color = KookTheme.colors.glow,
    waves: Boolean = true
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (waves && active) {
            AmbientWave(modifier = Modifier.fillMaxSize())
        }
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // web parity: ~8px between the note and the frequency caption
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Unified halo behind the whole label - ONE continuous radial
                // glow. (The old approach blurred a copy of the text, but blur
                // renders per glyph-run, so RTL labels like «ر دیز» showed the
                // light cut into separate rectangles.) This ellipse is drawn
                // as a single shape behind the text and can never fragment.
                if (active) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        // IDENTICAL recipe to the web preview's halo: ONE
                        // ellipse-shaped radial gradient - 30% alpha in the
                        // core, 12% at 55% of the radius, fully transparent
                        // at 78%. Because the gradient dies INSIDE the shape
                        // it can never show a hard rim (the old drawOval kept
                        // ~18% alpha at its top/bottom edge and rendered as a
                        // solid purple blob).
                        val rx = size.width * 0.78f
                        val ry = size.height * 1.15f
                        val haloBrush = Brush.radialGradient(
                            0f to glowColor.copy(alpha = 0.30f),
                            0.55f to glowColor.copy(alpha = 0.12f),
                            0.78f to Color.Transparent,
                            center = Offset(cx, cy),
                            radius = 1f
                        )
                        scale(scaleX = rx, scaleY = ry, pivot = Offset(cx, cy)) {
                            drawCircle(brush = haloBrush, radius = 1f, center = Offset(cx, cy))
                        }
                    }
                }
                // web parity (text-7xl = 72px): big but never overflowing
                // the layout like the old 110sp did
                Text(
                    noteName,
                    style = TextStyle(
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Black,
                        fontSize = 72.sp,
                        color = Color.White,
                        shadow = Shadow(glowColor.copy(alpha = 0.55f), blurRadius = 26f)
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = TextStyle(
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = glowColor
                    )
                )
            }
        }
    }
}

/** Flowing multi-wave ribbons with sparkle dots. */
@Composable
fun AmbientWave(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val palette = KookTheme.colors
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Restart),
        label = "phase"
    )
    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(13000), RepeatMode.Restart),
        label = "phase2"
    )

    Canvas(modifier = modifier) {
        val waves = listOf(
            Triple(palette.mint, 0.5f, phase),
            Triple(palette.cyan, 0.7f, phase2),
            Triple(palette.violet, 0.9f, phase * 0.7f + phase2 * 0.3f)
        )
        waves.forEach { (color, alphaMul, ph) ->
            val path = wavePath(
                width = size.width,
                height = size.height,
                phase = ph * 2f,
                amplitude = size.height * 0.10f * intensity,
                cycles = 2.2f
            )
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, color.copy(alpha = 0.55f * alphaMul * intensity), Color.Transparent)
                ),
                style = Stroke(width = 2.5.dp.toPx())
            )
            // soft double stroke for glow feel
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, color.copy(alpha = 0.16f * alphaMul * intensity), Color.Transparent)
                ),
                style = Stroke(width = 9.dp.toPx())
            )
        }
    }
}

/**
 * Live oscilloscope trace - the raw microphone waveform drawn as ONE
 * glowing gradient line with a faded mirror beneath, exactly like the web
 * preview's WaveCanvas. Auto-gain keeps quiet playing clearly visible and
 * peaks soft-clipped; when the mic is idle a slow breathing sine takes
 * over. Fed directly from the engine's down-sampled PCM samples, so it
 * reacts to every vibration in real time.
 * (Replaces the RMS bar chart that never matched the preview.)
 */
@Composable
fun WaveTrace(
    samples: FloatArray,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    startColor: Color = KookTheme.colors.violet,
    endColor: Color = KookTheme.colors.mint
) {
    val transition = rememberInfiniteTransition(label = "scope")
    val idlePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400), RepeatMode.Restart),
        label = "idlePhase"
    )
    val anim = remember { ScopeAnim() }
    val live = active && samples.size > 8

    Canvas(modifier = modifier.height(56.dp)) {
        val mid = size.height / 2f
        if (live) {
            // auto gain (EMA) - quiet playing stays visible, loud soft-clips
            var peak = 0.001f
            for (v in samples) {
                val a = abs(v)
                if (a > peak) peak = a
            }
            val targetGain = (0.72f / peak).coerceIn(1f, 6f)
            anim.gain += (targetGain - anim.gain) * 0.08f
            val g = anim.gain
            val amp = size.height * 0.42f
            val stepX = size.width / (samples.size - 1)
            val grad = Brush.horizontalGradient(
                listOf(startColor, endColor, endColor),
                startX = 0f,
                endX = size.width
            )

            fun trace(mirror: Boolean): Path = Path().apply {
                for (i in samples.indices) {
                    val v = (samples[i] * g).coerceIn(-1f, 1f)
                    val y = if (mirror) mid + v * amp else mid - v * amp
                    if (i == 0) moveTo(0f, y) else lineTo(i * stepX, y)
                }
            }

            // soft glow pass under the main line
            drawPath(
                path = trace(false),
                brush = grad,
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                alpha = 0.16f
            )
            // main line
            drawPath(
                path = trace(false),
                brush = grad,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            // faded mirror
            drawPath(
                path = trace(true),
                brush = grad,
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
                alpha = 0.25f
            )
        } else {
            anim.gain = 1f
            val path = Path()
            var first = true
            var x = 0f
            while (x <= size.width) {
                val t = x / size.width
                val env = sin(t * Math.PI).toFloat()
                val y = mid + sin(t * 9f + idlePhase * 6.28f) * env * size.height * 0.10f
                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
                x += 3f
            }
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(startColor, endColor)),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
                alpha = 0.5f
            )
        }
    }
}

/** Plain (non-snapshot) holder for the scope's auto-gain - mutated per frame. */
private class ScopeAnim {
    var gain = 1f
}

/** Frequency + hint line under the note glyph. */
@Composable
fun FrequencyCaption(
    frequency: Double,
    hint: String?,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (frequency > 0) {
            Text(
                "${frequency.toInt().toNum(LocalAppLang.current)} ${LocalStrings.current.hzSuffix}",
                fontFamily = Vazirmatn,
                fontSize = 15.sp,
                color = KookTheme.colors.mint
            )
        }
        if (hint != null) {
            Text(
                hint,
                fontFamily = Vazirmatn,
                fontSize = 12.sp,
                color = KookTheme.colors.textDim
            )
        }
    }
}
