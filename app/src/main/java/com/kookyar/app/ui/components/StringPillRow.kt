package com.kookyar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn

data class StringPill(
    val label: String,
    val targetHz: Double,
    val step: Int,
    val octave: Int
)

/**
 * Row of string targets (e.g. E A D G B E) with green check badges when
 * the string has been tuned - mirrors the reference design.
 */
@Composable
fun StringPillRow(
    pills: List<StringPill>,
    selectedIndex: Int?,
    tunedIndexes: Set<Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = KookTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        pills.forEachIndexed { i, pill ->
            val selected = i == selectedIndex
            val tuned = i in tunedIndexes
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) palette.primary.copy(alpha = 0.16f) else palette.card)
                    .border(
                        1.dp,
                        when {
                            selected -> palette.primary
                            tuned -> palette.mint.copy(alpha = 0.55f)
                            else -> palette.cardBorder
                        },
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelect(i) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    pill.label,
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (selected) palette.primary else palette.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (tuned) palette.mint else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (tuned) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFF04231A),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Small rounded status pill (e.g. «کوک استاندارد ~»). */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = KookTheme.colors.textDim,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(KookTheme.colors.card)
            .border(1.dp, KookTheme.colors.cardBorder, RoundedCornerShape(50))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(text, color = tint, fontSize = 12.sp, fontFamily = Vazirmatn)
    }
}
