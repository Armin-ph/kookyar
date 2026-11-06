package com.kookyar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kookyar.app.ui.theme.KookTheme

/** Rounded frosted-glass card used across every page. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    borderColor: Color? = null,
    contentPadding: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val palette = KookTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        color = palette.card,
        border = BorderStroke(1.dp, borderColor ?: palette.cardBorder),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/** «تنظیمات ❋» style section header with a small diamond ornament. */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = KookTheme.colors.gold,
    ornament: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (ornament) {
            Text("✦", color = color, style = MaterialTheme.typography.titleSmall)
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        if (ornament) {
            Text("✦", color = color, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** Thin gradient divider with a ❋ in the middle (Persian ornament). */
@Composable
fun OrnamentDivider(
    modifier: Modifier = Modifier,
    color: Color = KookTheme.colors.gold
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, color.copy(alpha = 0.55f))),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.5.dp.toPx()
            )
        }
        Text("❋", color = color, style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(
                brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent)),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}

/** Soft radial glow painted behind hero elements. */
@Composable
fun GlowBackdrop(
    modifier: Modifier = Modifier,
    color: Color = KookTheme.colors.glow,
    alpha: Float = 0.22f
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), Color.Transparent)
            ),
            radius = size.minDimension / 2f,
            center = center
        )
    }
}
