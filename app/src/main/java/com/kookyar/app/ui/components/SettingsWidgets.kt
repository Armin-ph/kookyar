package com.kookyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn

/** Settings row with a colored icon chip, title, description and a switch. */
@Composable
fun ToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = KookTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconTint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textDim)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconTint,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.14f),
                uncheckedBorderColor = palette.cardBorder
            )
        )
    }
}

/** Statistic card used on the history page (icon, label, big value). */
@Composable
fun StatCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = iconTint
) {
    GlassCard(modifier = modifier, contentPadding = 14.dp) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Text(
            value,
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}

/** Theme preview card (mini gauge mockup) used in settings. */
@Composable
fun ThemePreviewCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accent: Color,
    bgTop: Color,
    bgBottom: Color,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = KookTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(palette.card)
            .border(
                1.4.dp,
                if (selected) accent else palette.cardBorder,
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onSelect)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
        ) {
            MiniGauge(
                modifier = Modifier
                    .size(78.dp)
                    .align(Alignment.Center),
                needleTint = accent
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == 2) accent else Color.White.copy(alpha = 0.25f)
                            )
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
        // reserve exactly two lines for the description so all three theme
        // cards stay the same height (short tags used to collapse a card)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textDim,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.heightIn(min = 34.dp)
        )
    }
}
