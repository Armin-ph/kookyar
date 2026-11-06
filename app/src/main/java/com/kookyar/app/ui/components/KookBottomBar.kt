package com.kookyar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Floating glassy bottom bar with a raised circular center button —
 * mirrors the web preview's navigation (center = tuner, sides = 2 + 2).
 */
@Composable
fun KookBottomBar(
    items: List<NavItem>,
    centerRoute: String,
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = KookTheme.colors
    val center = items.first { it.route == centerRoute }
    val sides = items.filter { it.route != centerRoute }
    val startSide = sides.take(2)
    val endSide = sides.drop(2)

    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        // pill background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .shadow(14.dp, RoundedCornerShape(26.dp), spotColor = Color.Black)
                .background(palette.cardStrong)
                .border(1.dp, palette.cardBorder, RoundedCornerShape(26.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* swallow clicks on the bar itself */ }
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                startSide.forEach { NavButton(it, it.route == selectedRoute) { onSelect(it.route) } }
            }
            Spacer(Modifier.width(68.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                endSide.forEach { NavButton(it, it.route == selectedRoute) { onSelect(it.route) } }
            }
        }

        // raised center button
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val active = center.route == selectedRoute
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) Brush.linearGradient(listOf(palette.primary, palette.mint))
                        else Brush.linearGradient(listOf(palette.cardStrong, palette.cardStrong))
                    )
                    .border(
                        1.4.dp,
                        if (active) Color.Transparent else palette.cardBorder,
                        CircleShape
                    )
                    .clickable { onSelect(center.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    center.icon,
                    center.label,
                    tint = if (active) palette.onPrimary else palette.textDim,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                center.label,
                fontFamily = Vazirmatn,
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = if (active) palette.primary else palette.textDim
            )
        }
    }
}

@Composable
private fun NavButton(item: NavItem, active: Boolean, onClick: () -> Unit) {
    val palette = KookTheme.colors
    val tint by animateColorAsState(
        if (active) palette.primary else palette.textDim,
        label = "navTint"
    )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(item.icon, item.label, tint = tint, modifier = Modifier.size(23.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .width(20.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (active) palette.primary else Color.Transparent)
        )
    }
}
