package com.kookyar.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kookyar.app.data.settings.ThemeMode

/**
 * Extended palette: carries the brand accents and the glass surfaces that
 * Material's [ColorScheme] cannot express.
 */
data class KookPalette(
    // material mapping
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,

    // brand accents
    val mint: Color,
    val cyan: Color,
    val violet: Color,
    val gold: Color,
    val coral: Color,

    // glass surfaces
    val card: Color,
    val cardStrong: Color,
    val cardBorder: Color,
    val textDim: Color,

    // page background gradient (top -> bottom)
    val bgTop: Color,
    val bgBottom: Color,

    // decorative glow used behind the note display
    val glow: Color,

    val isPersian: Boolean = false
) {
    fun toScheme(): ColorScheme = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = cyan,
        onSecondary = Color(0xFF06202A),
        tertiary = violet,
        onTertiary = Color(0xFF140B2E),
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        error = coral,
        onError = Color(0xFF2B0A12)
    )
}

val GlassPalette = KookPalette(
    primary = Color(0xFF34E1B5),
    onPrimary = Color(0xFF04231A),
    background = Color(0xFF070B14),
    onBackground = Color(0xFFF2F6FA),
    surface = Color(0xFF0C1220),
    onSurface = Color(0xFFF2F6FA),
    surfaceVariant = Color(0xFF131B2C),
    onSurfaceVariant = Color(0xFF9AA7BA),
    outline = Color(0x1AFFFFFF),
    mint = Color(0xFF34E1B5),
    cyan = Color(0xFF38C7E8),
    violet = Color(0xFF9F8CFF),
    gold = Color(0xFFE9B872),
    coral = Color(0xFFFB7185),
    card = Color(0x0DFFFFFF),
    cardStrong = Color(0x14FFFFFF),
    cardBorder = Color(0x1FFFFFFF),
    textDim = Color(0xFF9AA7BA),
    bgTop = Color(0xFF0A101E),
    bgBottom = Color(0xFF05070D),
    glow = Color(0xFF34E1B5)
)

val PersianPalette = KookPalette(
    primary = Color(0xFFE3B764),
    onPrimary = Color(0xFF2B1D07),
    background = Color(0xFF141009),
    onBackground = Color(0xFFF6EFDD),
    surface = Color(0xFF1D160C),
    onSurface = Color(0xFFF6EFDD),
    surfaceVariant = Color(0xFF261D10),
    onSurfaceVariant = Color(0xFFC0AE8C),
    outline = Color(0x29E3B764),
    mint = Color(0xFF49C9A6),
    cyan = Color(0xFF3FB4C9),
    violet = Color(0xFFB08CDE),
    gold = Color(0xFFE3B764),
    coral = Color(0xFFD96A50),
    card = Color(0x14E9D9B0),
    cardStrong = Color(0x1FE9D9B0),
    cardBorder = Color(0x33E3B764),
    textDim = Color(0xFFBBA987),
    bgTop = Color(0xFF191208),
    bgBottom = Color(0xFF0C0906),
    glow = Color(0xFFE3B764),
    isPersian = true
)

val NeonPalette = KookPalette(
    primary = Color(0xFFFF4FD8),
    onPrimary = Color(0xFF2B0423),
    background = Color(0xFF05060F),
    onBackground = Color(0xFFFAFAFF),
    surface = Color(0xFF0B0D1C),
    onSurface = Color(0xFFFAFAFF),
    surfaceVariant = Color(0xFF141731),
    onSurfaceVariant = Color(0xFF9B9FC0),
    outline = Color(0x24FFFFFF),
    mint = Color(0xFF57F2C0),
    cyan = Color(0xFF2BD9FF),
    violet = Color(0xFF8A63FF),
    gold = Color(0xFFFFC94D),
    coral = Color(0xFFFF5C7A),
    card = Color(0x0FFFFFFF),
    cardStrong = Color(0x16FFFFFF),
    cardBorder = Color(0x29FFFFFF),
    textDim = Color(0xFF9B9FC0),
    bgTop = Color(0xFF0A0718),
    bgBottom = Color(0xFF04040A),
    glow = Color(0xFFFF4FD8)
)

fun paletteFor(mode: ThemeMode): KookPalette = when (mode) {
    ThemeMode.GLASS -> GlassPalette
    ThemeMode.PERSIAN -> PersianPalette
    ThemeMode.NEON -> NeonPalette
}

val LocalKookColors = staticCompositionLocalOf { GlassPalette }
