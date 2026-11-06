package com.kookyar.app.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GetApp
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kookyar.app.BuildConfig
import com.kookyar.app.core.i18n.AppLanguage
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.data.settings.ThemeMode
import com.kookyar.app.ui.components.GlassCard
import com.kookyar.app.ui.components.OrnamentDivider
import com.kookyar.app.ui.components.SectionTitle
import com.kookyar.app.ui.components.SelectChip
import com.kookyar.app.ui.components.ToggleItem
import com.kookyar.app.ui.components.ThemePreviewCard
import com.kookyar.app.ui.screens.settings.SettingsViewModel.UpdateUi
import com.kookyar.app.ui.theme.GlassPalette
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.NeonPalette
import com.kookyar.app.ui.theme.PersianPalette
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.utils.toNum
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen() {
    val vm: SettingsViewModel = viewModel()
    val state by vm.settings.collectAsStateWithLifecycle()
    val updateState by vm.update.collectAsStateWithLifecycle()
    // bundled + remote (update.json) version history
    val changelog by vm.changelog.collectAsStateWithLifecycle()
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current
    val context = LocalContext.current
    var showChangelog by remember { mutableStateOf(false) }

    // leave no stale update state behind when the user leaves the page
    DisposableEffect(Unit) {
        onDispose { vm.resetUpdateState() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(s.settingsTitle, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        OrnamentDivider(modifier = Modifier.padding(horizontal = 60.dp, vertical = 6.dp))

        // ---------- language ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconChip(icon = Icons.Rounded.Language, tint = palette.gold)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        s.languageTitle,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        s.languageDesc,
                        fontFamily = Vazirmatn,
                        fontSize = 11.sp,
                        color = palette.textDim
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectChip(
                    label = s.languageFa,
                    selected = state.language == AppLanguage.FA
                ) { vm.setLanguage(AppLanguage.FA.code) }
                SelectChip(
                    label = s.languageEn,
                    selected = state.language == AppLanguage.EN
                ) { vm.setLanguage(AppLanguage.EN.code) }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- microphone sensitivity ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconChip(icon = Icons.Rounded.Mic, tint = palette.violet)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        s.micSensitivity,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        s.micSensitivityDesc,
                        fontFamily = Vazirmatn,
                        fontSize = 11.sp,
                        color = palette.textDim
                    )
                }
                Text(
                    "${state.sensitivity.toNum(lang, 1)}×",
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    color = palette.violet
                )
            }
            Slider(
                value = state.sensitivity,
                onValueChange = { vm.setSensitivity(it) },
                valueRange = 0.6f..1.6f,
                colors = SliderDefaults.colors(
                    thumbColor = palette.violet,
                    activeTrackColor = palette.violet,
                    inactiveTrackColor = palette.cardStrong
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        // ---------- behaviour toggles ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ToggleItem(
                    icon = Icons.Rounded.MusicNote,
                    iconTint = Color(0xFF2DD4BF),
                    title = s.quarterTones,
                    subtitle = s.quarterTonesDesc,
                    checked = state.quarterTones,
                    onCheckedChange = vm::setQuarterTones
                )
                ToggleItem(
                    icon = Icons.Rounded.Vibration,
                    iconTint = Color(0xFFE9B872),
                    title = s.hapticsTitle,
                    subtitle = s.hapticsDesc,
                    checked = state.haptics,
                    onCheckedChange = vm::setHaptics
                )
                ToggleItem(
                    icon = Icons.Rounded.AutoAwesome,
                    iconTint = Color(0xFF8F9DFF),
                    title = s.particlesTitle,
                    subtitle = s.particlesDesc,
                    checked = state.particles,
                    onCheckedChange = vm::setParticles
                )
                ToggleItem(
                    icon = Icons.Rounded.VolumeUp,
                    iconTint = palette.mint,
                    title = s.confirmSoundTitle,
                    subtitle = s.confirmSoundDesc,
                    checked = state.confirmSound,
                    onCheckedChange = vm::setConfirmSound
                )
                ToggleItem(
                    icon = Icons.Rounded.LightMode,
                    iconTint = palette.gold,
                    title = s.keepScreenOnTitle,
                    subtitle = s.keepScreenOnDesc,
                    checked = state.keepScreenOn,
                    onCheckedChange = vm::setKeepScreenOn
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- reference frequency ---------- //
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconChip(icon = Icons.Rounded.GraphicEq, tint = palette.cyan)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        s.refFreqTitle,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        s.refFreqRange,
                        fontFamily = Vazirmatn,
                        fontSize = 11.sp,
                        color = palette.textDim
                    )
                }
                Text(
                    "${state.a4.toNum(lang, 1)} ${s.hzSuffix}",
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = palette.cyan
                )
            }
            Slider(
                value = state.a4.toFloat(),
                onValueChange = { vm.setA4(it.toDouble()) },
                valueRange = 430f..450f,
                colors = SliderDefaults.colors(
                    thumbColor = palette.cyan,
                    activeTrackColor = palette.cyan,
                    inactiveTrackColor = palette.cardStrong
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(432.0, 440.0, 442.0, 443.0).forEach { hz ->
                    SelectChip(
                        label = hz.toNum(lang, 0),
                        selected = kotlin.math.abs(state.a4 - hz) < 0.6
                    ) { vm.setA4(hz) }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = vm::playReferenceA4) {
                    Icon(Icons.Rounded.VolumeUp, s.playReference, tint = palette.mint)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------- themes ---------- //
        SectionTitle(s.themesSection)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemePreviewCard(
                title = s.themeGlass,
                subtitle = s.themeGlassDesc,
                selected = state.theme == ThemeMode.GLASS,
                accent = GlassPalette.primary,
                bgTop = GlassPalette.bgTop,
                bgBottom = GlassPalette.bgBottom,
                onSelect = { vm.setTheme(ThemeMode.GLASS) },
                modifier = Modifier.weight(1f)
            )
            ThemePreviewCard(
                title = s.themePersian,
                subtitle = s.themePersianDesc,
                selected = state.theme == ThemeMode.PERSIAN,
                accent = PersianPalette.gold,
                bgTop = PersianPalette.bgTop,
                bgBottom = PersianPalette.bgBottom,
                onSelect = { vm.setTheme(ThemeMode.PERSIAN) },
                modifier = Modifier.weight(1f)
            )
            ThemePreviewCard(
                title = s.themeNeon,
                subtitle = s.themeNeonDesc,
                selected = state.theme == ThemeMode.NEON,
                accent = NeonPalette.primary,
                bgTop = NeonPalette.bgTop,
                bgBottom = NeonPalette.bgBottom,
                onSelect = { vm.setTheme(ThemeMode.NEON) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ---------- update ---------- //
        SectionTitle(s.updateSection)
        Spacer(Modifier.height(10.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconChip(icon = Icons.Rounded.SystemUpdateAlt, tint = palette.mint)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        s.checkForUpdate,
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        lastCheckText(state.lastUpdateCheck, lang),
                        fontFamily = Vazirmatn,
                        fontSize = 11.sp,
                        color = palette.textDim
                    )
                }
                if (updateState is UpdateUi.Checking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = palette.mint
                    )
                } else {
                    Button(
                        onClick = vm::checkForUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.mint.copy(alpha = 0.16f),
                            contentColor = palette.mint
                        )
                    ) {
                        Text(s.checkForUpdate, fontFamily = Vazirmatn, fontSize = 12.sp)
                    }
                }
            }

            // ---- status / available update panel ---- //
            when (val u = updateState) {
                is UpdateUi.Checking -> {
                    Spacer(Modifier.height(10.dp))
                    StatusLine(icon = Icons.Rounded.Schedule, tint = palette.textDim, text = s.checkingNow)
                }
                is UpdateUi.UpToDate -> {
                    Spacer(Modifier.height(10.dp))
                    StatusLine(icon = Icons.Rounded.CheckCircle, tint = palette.mint, text = s.upToDate)
                }
                is UpdateUi.Failed -> {
                    Spacer(Modifier.height(10.dp))
                    StatusLine(icon = Icons.Rounded.ErrorOutline, tint = palette.coral, text = s.updateFailed)
                }
                is UpdateUi.Available -> {
                    Spacer(Modifier.height(12.dp))
                    UpdateAvailablePanel(
                        versionName = u.manifest.versionName,
                        items = u.manifest.changelogFor(lang.isFa),
                        onBazaar = { vm.openBazaar(context, u.manifest) },
                        onMyket = { vm.openMyket(context, u.manifest) },
                        onPlay = { vm.openGooglePlay(context, u.manifest) },
                        showApk = u.manifest.apkUrl != null,
                        onDownloadApk = { vm.downloadUpdate(context, u.manifest) }
                    )
                }
                is UpdateUi.Downloading -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        s.downloading + if (u.progress >= 0) " ${u.progress.toNum(lang)}${s.percentSuffix}" else "",
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { if (u.progress >= 0) u.progress / 100f else 0.3f },
                        modifier = Modifier.fillMaxWidth(),
                        color = palette.mint,
                        trackColor = palette.cardStrong
                    )
                }
                is UpdateUi.ReadyToInstall -> {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { vm.installDownloaded(context, u.file, u.versionName) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.mint,
                            contentColor = Color(0xFF06281F)
                        )
                    ) {
                        Icon(Icons.Rounded.InstallMobile, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.installNow, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateUi.DownloadFailed -> {
                    Spacer(Modifier.height(10.dp))
                    StatusLine(icon = Icons.Rounded.ErrorOutline, tint = palette.coral, text = s.updateFailed)
                }
                else -> {}
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------- version & changelog ---------- //
        SectionTitle(s.versionSection)
        Spacer(Modifier.height(10.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(palette.gold.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎻", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "کوک‌یار • Kookyar",
                        fontFamily = Vazirmatn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "${s.versionWord} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE.toNum(lang)})",
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim
                    )
                }
                IconButton(onClick = { showChangelog = !showChangelog }) {
                    Icon(
                        if (showChangelog) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        if (showChangelog) s.hideChangelog else s.showChangelog,
                        tint = palette.gold
                    )
                }
            }

            AnimatedVisibility(visible = showChangelog) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    changelog.forEach { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.cardStrong)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "v${entry.versionName}",
                                    fontFamily = Vazirmatn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = palette.gold
                                )
                                Spacer(Modifier.weight(1f))
                                if (entry.date.isNotBlank()) {
                                    Text(
                                        entry.date.toNum(lang),
                                        fontFamily = Vazirmatn,
                                        fontSize = 11.sp,
                                        color = palette.textDim
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            val items = if (lang.isFa) entry.fa else entry.en
                            items.forEach { item ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        "•  ",
                                        fontFamily = Vazirmatn,
                                        fontSize = 12.sp,
                                        color = palette.cyan
                                    )
                                    Text(
                                        item,
                                        fontFamily = Vazirmatn,
                                        fontSize = 12.sp,
                                        color = palette.textDim
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.cardStrong)
                    .padding(10.dp)
            ) {
                Text(
                    s.privacyNote,
                    fontFamily = Vazirmatn,
                    fontSize = 11.sp,
                    color = palette.textDim
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            s.madeWithLove,
            fontFamily = Vazirmatn,
            fontSize = 12.sp,
            color = palette.textDim
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun lastCheckText(epochMs: Long, lang: AppLanguage): String {
    val s = LocalStrings.current
    return if (epochMs <= 0L) {
        s.neverChecked
    } else {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd  HH:mm")
        val text = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).format(formatter)
        "${s.lastCheck}: ${text.toNum(lang)}"
    }
}

@Composable
private fun StatusLine(icon: ImageVector, tint: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text, fontFamily = Vazirmatn, fontSize = 12.sp, color = tint)
    }
}

@Composable
private fun UpdateAvailablePanel(
    versionName: String,
    items: List<String>,
    onBazaar: () -> Unit,
    onMyket: () -> Unit,
    onPlay: () -> Unit,
    showApk: Boolean,
    onDownloadApk: () -> Unit
) {
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.mint.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CloudDownload, null, tint = palette.mint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "${s.newVersion} $versionName ${s.available}",
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = palette.mint
            )
        }
        if (items.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            items.take(5).forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•  ", fontFamily = Vazirmatn, fontSize = 12.sp, color = palette.mint)
                    Text(
                        item,
                        fontFamily = Vazirmatn,
                        fontSize = 12.sp,
                        color = palette.textDim
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            s.updateMarkets,
            fontFamily = Vazirmatn,
            fontSize = 11.sp,
            color = palette.textDim
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SelectChip(s.marketBazaar, selected = false, onClick = onBazaar)
            SelectChip(s.marketMyket, selected = false, onClick = onMyket)
            SelectChip(s.marketPlay, selected = false, onClick = onPlay)
        }
        if (showApk) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onDownloadApk,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.mint,
                    contentColor = Color(0xFF06281F)
                )
            ) {
                Icon(Icons.Rounded.GetApp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(s.downloadApk, fontFamily = Vazirmatn, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun IconChip(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp))
    }
}
