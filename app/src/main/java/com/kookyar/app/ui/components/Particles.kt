package com.kookyar.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.kookyar.app.ui.theme.KookTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Celebration particle burst (fires whenever [burstKey] changes to a
 * positive value) plus a gentle ambient sparkle field.
 */
@Composable
fun ParticleBurst(
    burstKey: Int,
    modifier: Modifier = Modifier,
    particleCount: Int = 46
) {
    val palette = KookTheme.colors
    val colors = listOf(palette.mint, palette.cyan, palette.violet, palette.gold, Color.White)
    val progress = remember(burstKey) { Animatable(1f) }

    LaunchedEffect(burstKey) {
        if (burstKey > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(1300, easing = LinearEasing))
        }
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        if (p >= 1f) return@Canvas

        val rng = Random(burstKey * 7919 + 13)
        val radius = min(size.width, size.height) * 0.42f

        for (i in 0 until particleCount) {
            val angle = rng.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 0.45f + rng.nextFloat() * 0.55f
            val delay = rng.nextFloat() * 0.25f
            val t = ((p - delay) / (1f - delay)).coerceIn(0f, 1f)
            if (t <= 0f) continue

            // ease-out + slight gravity
            val eased = 1f - (1f - t) * (1f - t)
            val dist = radius * speed * eased
            val cx = size.width / 2f + cos(angle) * dist
            val cy = size.height / 2f + sin(angle) * dist + t * t * radius * 0.22f

            val alpha = (1f - t).coerceIn(0f, 1f)
            val dotRadius = (2.2f + rng.nextFloat() * 3.4f) * (1f - t * 0.55f)

            drawCircle(
                color = colors[i % colors.size].copy(alpha = alpha),
                radius = dotRadius,
                center = Offset(cx, cy)
            )
        }
    }
}

/** Tiny twinkling stars that make the dark pages feel alive. */
@Composable
fun SparkleField(
    modifier: Modifier = Modifier,
    dotCount: Int = 34,
    enabled: Boolean = true
) {
    val palette = KookTheme.colors
    val transition = rememberInfiniteTransition(label = "sparkle")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )

    Canvas(modifier = modifier) {
        if (!enabled) return@Canvas
        val rng = Random(7)
        repeat(dotCount) { i ->
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            val base = 0.12f + rng.nextFloat() * 0.5f
            val tw = 0.5f + 0.5f * sin((time + i) * Math.PI).toFloat()
            drawCircle(
                color = if (i % 5 == 0) palette.gold else Color.White,
                radius = (0.8f + rng.nextFloat() * 1.6f),
                center = Offset(x, y),
                alpha = base * tw
            )
        }
    }
}
