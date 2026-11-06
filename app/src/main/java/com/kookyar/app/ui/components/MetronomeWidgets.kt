package com.kookyar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.metronome.ClickSynth
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum
import kotlin.math.cos
import kotlin.math.sin

/** Accent levels for metronome beats. */
const val ACCENT_WEAK = 0
const val ACCENT_MEDIUM = 1
const val ACCENT_STRONG = 2

fun accentColor(level: Int): Color = when (level) {
    ACCENT_STRONG -> Color(0xFFF87171)
    ACCENT_MEDIUM -> Color(0xFF2DD4BF)
    else -> Color(0xFF8B93A7)
}

/**
 * Ring of beat dots that lights up in sync with the audible click.
 * [centerContent] is drawn inside the ring (the big BPM number, like the
 * web design).
 */
@Composable
fun BeatRing(
    beatsPerBar: Int,
    currentBeat: Int,
    accents: List<Int>,
    playing: Boolean,
    modifier: Modifier = Modifier,
    ringSize: Dp = 240.dp,
    centerContent: (@Composable () -> Unit)? = null
) {
    val palette = KookTheme.colors
    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f - 18.dp.toPx()
            // dashed guide circle (like the web svg ring)
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = radius,
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f))
                )
            )
            for (i in 0 until beatsPerBar) {
                val angle = -90.0 + i * (360.0 / beatsPerBar)
                val rad = Math.toRadians(angle)
                val cx = size.width / 2 + radius * cos(rad)
                val cy = size.height / 2 + radius * sin(rad)
                val level = accents.getOrElse(i) { 0 }
                val isActive = playing && i == currentBeat
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            accentColor(level).copy(alpha = if (isActive) 0.40f else 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(cx.toFloat(), cy.toFloat()),
                        radius = 30.dp.toPx()
                    ),
                    radius = 30.dp.toPx(),
                    center = Offset(cx.toFloat(), cy.toFloat())
                )
                drawCircle(
                    color = if (isActive) palette.primary else accentColor(level),
                    radius = (if (isActive) 13f else if (level == ACCENT_STRONG) 10f else 8f) *
                        (size.minDimension / 240.dp.toPx()),
                    center = Offset(cx.toFloat(), cy.toFloat()),
                    alpha = if (isActive) 1f else if (level == ACCENT_WEAK) 0.40f else 0.80f
                )
            }
        }
        if (centerContent != null) centerContent()
    }
}

/** One accent cell in the pattern editor (number + colored dot). */
@Composable
fun AccentCell(
    index: Int,
    level: Int,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = when {
            active -> accentColor(level).copy(alpha = 0.85f)
            level == ACCENT_STRONG -> accentColor(level).copy(alpha = 0.14f)
            level == ACCENT_MEDIUM -> accentColor(level).copy(alpha = 0.10f)
            else -> KookTheme.colors.card
        },
        label = "bg"
    )
    val borderColor = when {
        active -> Color.Transparent
        level == ACCENT_STRONG -> accentColor(level).copy(alpha = 0.60f)
        level == ACCENT_MEDIUM -> accentColor(level).copy(alpha = 0.50f)
        else -> KookTheme.colors.cardBorder
    }
    val dot = if (active) Color.White else accentColor(level)
    val textColor = when {
        active -> Color.White
        level == ACCENT_STRONG || level == ACCENT_MEDIUM -> accentColor(level)
        else -> KookTheme.colors.textDim
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            (index + 1).toNum(LocalAppLang.current),
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = textColor
        )
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else dot)
        )
    }
}

/** Round glass stepper button (+/-) used around the BPM display. */
@Composable
fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(KookTheme.colors.card)
            .border(1.dp, KookTheme.colors.cardBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = KookTheme.colors.onSurface, modifier = Modifier.size(20.dp))
    }
}

/**
 * Slim custom slider (thin 5dp track + round thumb) that matches the web
 * `app-range` look — Material 3's default slider draws a thick track.
 * RTL-aware: in Persian the fill grows from the right and dragging left
 * increases the value, exactly like the web input[type=range].
 *
 * BUG FIX (metronome tempo lever): the fill/thumb used explicit
 * `if (rtl) CenterEnd else CenterStart` placement, which mirrored the
 * geometry only for the drawing but NOT for the hit-test mapping, so in
 * RTL the thumb jumped to the opposite side on touch and then dragged
 * AGAINST the finger. Alignment.CenterStart/End are already
 * layout-direction-aware, so the same code is correct for both RTL and
 * LTR: fill anchored at the visual start, thumb riding its inner edge,
 * finger and thumb always moving in the same direction.
 */
@Composable
fun KookSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = KookTheme.colors.primary,
    trackColor: Color = KookTheme.colors.cardStrong
) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var widthPx by remember { mutableFloatStateOf(0f) }
    val fraction = value.coerceIn(0f, 1f)

    fun update(x: Float) {
        if (widthPx <= 0f) return
        val raw = (x / widthPx).coerceIn(0f, 1f)
        onValueChange(if (rtl) 1f - raw else raw)
    }

    Box(
        modifier = modifier
            .height(30.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(Unit) { detectTapGestures { update(it.x) } }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    update(change.position.x)
                }
            }
    ) {
        // base track
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        )
        // active fill + thumb riding its inner edge
        // (direction-aware: start = visual right in RTL, left in LTR)
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(30.dp)
                .align(Alignment.CenterStart)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(3.dp))
                    .background(activeColor)
            )
            Box(
                Modifier
                    .size(18.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(4.dp, activeColor, CircleShape)
            )
        }
    }
}

/** Rounded selectable chip (subdivision, sound type, tunings...). */
@Composable
fun SelectChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) KookTheme.colors.primary else KookTheme.colors.card,
        label = "chipBg"
    )
    val fg = if (selected) KookTheme.colors.onPrimary else KookTheme.colors.textDim
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(
                1.dp,
                if (selected) Color.Transparent else KookTheme.colors.cardBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = fg, modifier = Modifier.size(16.dp))
        }
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

/** BPM big display with optional scale pulse on each beat. */
@Composable
fun BpmDisplay(
    bpm: Int,
    pulse: Boolean,
    modifier: Modifier = Modifier,
    fontSize: Int = 56,
    color: Color = KookTheme.colors.onSurface
) {
    val scale by animateFloatAsState(
        targetValue = if (pulse) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 700f),
        label = "bpmPulse"
    )
    Column(modifier = modifier.scale(scale), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            bpm.toNum(LocalAppLang.current),
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            color = color
        )
        Text(
            "BPM",
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = KookTheme.colors.textDim
        )
    }
}

/** Sound type label helper (legacy; use ClickSound.faName/enName). */
fun ClickSynth.ClickSound.label(): String = faName
