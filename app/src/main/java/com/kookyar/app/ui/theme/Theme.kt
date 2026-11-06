package com.kookyar.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.kookyar.app.core.i18n.AppLanguage
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.core.i18n.Strings
import com.kookyar.app.data.settings.ThemeMode

/** Convenience accessor for the extended palette. */
object KookTheme {
    val colors: KookPalette
        @Composable get() = LocalKookColors.current
}

@Composable
fun KookyarTheme(
    mode: ThemeMode,
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(mode)
    val strings = Strings(language)
    val dir = if (language.isFa) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(
        LocalKookColors provides palette,
        LocalAppLang provides language,
        LocalStrings provides strings,
        LocalLayoutDirection provides dir
    ) {
        MaterialTheme(
            colorScheme = palette.toScheme(),
            typography = KookTypography,
            content = content
        )
    }
}
