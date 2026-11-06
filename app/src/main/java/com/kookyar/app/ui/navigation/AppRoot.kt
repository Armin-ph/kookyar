package com.kookyar.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kookyar.app.AppGraph
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.data.settings.AppSettings
import com.kookyar.app.data.settings.ThemeMode
import com.kookyar.app.ui.components.KookBottomBar
import com.kookyar.app.ui.components.KookEar
import com.kookyar.app.ui.components.KookGuitar
import com.kookyar.app.ui.components.KookMetronome
import com.kookyar.app.ui.components.KookTuningFork
import com.kookyar.app.ui.components.NavItem
import com.kookyar.app.ui.components.SparkleField
import com.kookyar.app.ui.screens.ear.EarTrainingScreen
import com.kookyar.app.ui.screens.history.HistoryScreen
import com.kookyar.app.ui.screens.instruments.InstrumentsScreen
import com.kookyar.app.ui.screens.metronome.MetronomeScreen
import com.kookyar.app.ui.screens.settings.SettingsScreen
import com.kookyar.app.ui.screens.tuner.TunerScreen
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.KookyarTheme
import kotlinx.coroutines.launch

enum class Routes { TUNER, METRONOME, INSTRUMENTS, EAR, HISTORY, SETTINGS }

@Composable
fun KookyarApp() {
    val settings by AppGraph.settingsStore.settings
        .collectAsStateWithLifecycle(initialValue = AppSettings())
    KookyarTheme(mode = settings.theme, language = settings.language) {
        AppRoot(themeMode = settings.theme)
    }
}

@Composable
private fun AppRoot(themeMode: ThemeMode) {
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current
    var tab by rememberSaveable { mutableStateOf(Routes.TUNER.name) }
    val scope = rememberCoroutineScope()

    val navItems = remember(lang) {
        listOf(
            NavItem(Routes.EAR.name, s.tabEar, KookEar),
            NavItem(Routes.HISTORY.name, s.tabHistory, Icons.Rounded.ShowChart),
            NavItem(Routes.TUNER.name, s.tabTuner, KookTuningFork),
            NavItem(Routes.INSTRUMENTS.name, s.tabInstruments, KookGuitar),
            NavItem(Routes.METRONOME.name, s.tabMetronome, KookMetronome)
        )
    }

    // FIX: give every Text outside a Surface the theme's onBackground color.
    // Without this, Compose's default LocalContentColor is black, which made
    // page titles (مترونوم، تاریخچه کوک، انتخاب ساز، ...) invisible on the dark
    // gradient background.
    CompositionLocalProvider(LocalContentColor provides palette.onBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom)))
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SparkleField(modifier = Modifier.fillMaxSize(), enabled = true)
                when (Routes.valueOf(tab)) {
                    Routes.TUNER -> TunerScreen(
                        onOpenSettings = { tab = Routes.SETTINGS.name },
                        onOpenInstruments = { tab = Routes.INSTRUMENTS.name },
                        onCycleTheme = {
                            val next = when (themeMode) {
                                ThemeMode.GLASS -> ThemeMode.PERSIAN
                                ThemeMode.PERSIAN -> ThemeMode.NEON
                                ThemeMode.NEON -> ThemeMode.GLASS
                            }
                            scope.launch { AppGraph.settingsStore.setTheme(next) }
                        },
                        onToggleLanguage = {
                            val next = if (lang.isFa) com.kookyar.app.core.i18n.AppLanguage.EN
                            else com.kookyar.app.core.i18n.AppLanguage.FA
                            scope.launch { AppGraph.settingsStore.setLanguage(next) }
                        }
                    )
                    Routes.METRONOME -> MetronomeScreen(onOpenSettings = { tab = Routes.SETTINGS.name })
                    Routes.INSTRUMENTS -> InstrumentsScreen(onDone = { tab = Routes.TUNER.name })
                    Routes.EAR -> EarTrainingScreen(onOpenSettings = { tab = Routes.SETTINGS.name })
                    Routes.HISTORY -> HistoryScreen(onOpenSettings = { tab = Routes.SETTINGS.name })
                    Routes.SETTINGS -> SettingsScreen()
                }
            }
            KookBottomBar(
                items = navItems,
                centerRoute = Routes.TUNER.name,
                selectedRoute = tab,
                onSelect = { route -> tab = route }
            )
        }
    }
}
