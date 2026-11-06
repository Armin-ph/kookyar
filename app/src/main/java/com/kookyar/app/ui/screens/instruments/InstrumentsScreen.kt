package com.kookyar.app.ui.screens.instruments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kookyar.app.R
import com.kookyar.app.core.i18n.LocalAppLang
import com.kookyar.app.core.i18n.LocalStrings
import com.kookyar.app.data.model.InstrumentDef
import com.kookyar.app.ui.components.GlassCard
import com.kookyar.app.ui.components.KookTuningFork
import com.kookyar.app.ui.components.OrnamentDivider
import com.kookyar.app.ui.components.SelectChip
import com.kookyar.app.ui.theme.KookTheme
import com.kookyar.app.ui.theme.Vazirmatn
import com.kookyar.app.ui.screens.tuner.TunerViewModel

/** Bundled photos (same images as the web preview) — null = icon fallback. */
private fun imageFor(id: String): Int? = when (id) {
    "guitar", "bass" -> R.drawable.inst_guitar
    "tar" -> R.drawable.inst_tar
    "setar" -> R.drawable.inst_setar
    "santur" -> R.drawable.inst_santur
    "kamancheh" -> R.drawable.inst_kamancheh
    "violin" -> R.drawable.inst_violin
    "ukulele" -> R.drawable.inst_ukulele
    "piano" -> R.drawable.inst_piano
    else -> null // chromatic → tuning-fork artwork like the web
}

@Composable
fun InstrumentsScreen(
    onDone: () -> Unit
) {
    val vm: TunerViewModel = viewModel()
    val state by vm.ui.collectAsStateWithLifecycle()
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                s.pickInstrument,
                fontFamily = Vazirmatn,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = palette.onBackground
            )
            OrnamentDivider(modifier = Modifier.padding(horizontal = 60.dp, vertical = 6.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
        ) {
            items(com.kookyar.app.data.repository.TuningsRepository.all) { instrument ->
                InstrumentCard(
                    instrument = instrument,
                    selected = instrument.id == state.instrument.id,
                    onClick = {
                        vm.selectInstrument(instrument)
                        onDone()
                    }
                )
            }
        }

        // tunings of the selected instrument
        if (state.instrument.tunings.size > 1) {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    s.tuningsOf(state.instrument.localizedName(lang)),
                    fontFamily = Vazirmatn,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = palette.textDim
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.instrument.tunings.forEach { tuning ->
                        SelectChip(
                            label = tuning.localizedName(lang),
                            selected = tuning.id == state.tuning.id
                        ) { vm.selectTuning(tuning) }
                    }
                }
                if (state.tuning.strings.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        state.tuning.strings.joinToString(" – ") { s.noteName(it.step) },
                        fontFamily = Vazirmatn,
                        fontSize = 13.sp,
                        color = palette.primary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InstrumentCard(
    instrument: InstrumentDef,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = KookTheme.colors
    val s = LocalStrings.current
    val lang = LocalAppLang.current
    val photo = imageFor(instrument.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) palette.primary.copy(alpha = 0.10f) else palette.card)
            .border(
                1.4.dp,
                if (selected) palette.primary else palette.cardBorder,
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // photo area (or tuning-fork artwork for chromatic, like the web)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.25f)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surfaceVariant)
        ) {
            if (photo != null) {
                Image(
                    painter = painterResource(photo),
                    contentDescription = instrument.localizedName(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        KookTuningFork,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = palette.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            instrument.localizedName(lang),
            fontFamily = Vazirmatn,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (selected) palette.primary else palette.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            if (instrument.isChromatic) s.chromaticAll else s.standardTuning,
            fontFamily = Vazirmatn,
            fontSize = 11.sp,
            color = palette.textDim,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
    }
}
