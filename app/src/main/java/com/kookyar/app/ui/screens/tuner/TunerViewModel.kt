package com.kookyar.app.ui.screens.tuner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kookyar.app.AppGraph
import com.kookyar.app.core.audio.TunerEngine
import com.kookyar.app.core.i18n.AppLanguage
import com.kookyar.app.core.i18n.stringsOf
import com.kookyar.app.core.metronome.TonePlayer
import com.kookyar.app.core.music.MusicTheory
import com.kookyar.app.core.music.NoteMatch
import com.kookyar.app.data.model.InstrumentDef
import com.kookyar.app.data.model.TuningDef
import com.kookyar.app.data.model.TuningRecord
import com.kookyar.app.data.repository.TuningsRepository
import com.kookyar.app.data.settings.AppSettings
import com.kookyar.app.utils.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

class TunerViewModel : ViewModel() {

    data class TunerUiState(
        val settings: AppSettings = AppSettings(),
        val instrument: InstrumentDef = TuningsRepository.all[1],
        val tuning: TuningDef = TuningsRepository.all[1].tunings.first(),
        val listening: Boolean = false,
        val permissionDenied: Boolean = false,
        val frequency: Double = 0.0,
        /** Smoothed frequency for display (Hz). */
        val displayFrequency: Double = 0.0,
        val clarity: Float = 0f,
        /** Down-sampled mic samples for the live oscilloscope view. */
        val waveform: FloatArray = FloatArray(0),
        val displayNote: String = "لا",
        val displayCents: Double = 0.0,
        val targetHz: Double = 0.0,
        val inTune: Boolean = false,
        val accuracy: Int = 0,
        val selectedString: Int? = null,
        val tunedStrings: Set<Int> = emptySet(),
        val burstKey: Int = 0,
        val hint: String = ""
    )

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val engine = TunerEngine(engineScope, ::onInTuneEdge)

    private val _ui = MutableStateFlow(TunerUiState())
    val ui: StateFlow<TunerUiState> = _ui.asStateFlow()

    @Volatile
    private var lastMatch: NoteMatch? = null

    @Volatile
    private var lastFrequency: Double = 0.0

    @Volatile
    private var lastDisplayFrequency: Double = 0.0

    @Volatile
    private var lang: AppLanguage = AppLanguage.FA

    init {
        viewModelScope.launch {
            AppGraph.settingsStore.settings.collect { s ->
                lang = s.language
                engine.configure(s.a4, s.quarterTones, s.sensitivity)
                val inst = TuningsRepository.byId(s.instrumentId) ?: TuningsRepository.all[1]
                val tuning = inst.tunings.firstOrNull { it.id == s.tuningId }
                    ?: TuningsRepository.defaultTuning(inst)
                _ui.update { it.copy(settings = s, instrument = inst, tuning = tuning) }
                recomputeDisplay()
            }
        }
        viewModelScope.launch {
            engine.state.collect { es ->
                lastMatch = es.match
                lastFrequency = es.frequency
                lastDisplayFrequency = es.displayFrequency
                _ui.update {
                    it.copy(
                        listening = es.listening,
                        permissionDenied = es.permissionDenied,
                        frequency = es.frequency,
                        displayFrequency = es.displayFrequency,
                        clarity = es.clarity,
                        waveform = es.scope
                    )
                }
                recomputeDisplay()
            }
        }
    }

    fun start(context: android.content.Context? = null) {
        engine.start(context ?: AppGraph.appContext)
    }

    fun stop() = engine.stop()

    fun selectInstrument(instrument: InstrumentDef) {
        viewModelScope.launch {
            AppGraph.settingsStore.setInstrument(instrument.id, TuningsRepository.defaultTuning(instrument).id)
        }
    }

    fun selectTuning(tuning: TuningDef) {
        viewModelScope.launch {
            AppGraph.settingsStore.setInstrument(_ui.value.instrument.id, tuning.id)
            _ui.update { it.copy(tunedStrings = emptySet(), selectedString = null) }
        }
    }

    /** Selects a string as the tuning target and plays its reference tone. */
    fun selectString(index: Int?) {
        val current = _ui.value
        val newSel = if (current.selectedString == index) null else index
        _ui.update { it.copy(selectedString = newSel) }
        if (newSel != null) {
            val spec = current.tuning.strings.getOrNull(newSel)
            if (spec != null) {
                playReference(spec.step, spec.octave)
            }
        }
    }

    fun playReference(step: Int, octave: Int) {
        val freq = MusicTheory.frequency(step, octave, _ui.value.settings.a4)
        TonePlayer.play(engineScope, freq, 1100, 0.8f)
    }

    fun playA4() {
        TonePlayer.play(engineScope, _ui.value.settings.a4, 1400, 0.8f)
    }

    private fun onInTuneEdge(match: NoteMatch) {
        val snapshot = _ui.value
        if (snapshot.settings.haptics) Haptics.preciseTune(AppGraph.appContext)
        if (snapshot.settings.confirmSound) {
            TonePlayer.play(engineScope, match.targetFrequency, 650, 0.45f)
        }
        viewModelScope.launch {
            _ui.update {
                it.copy(
                    burstKey = if (it.settings.particles) it.burstKey + 1 else it.burstKey,
                    tunedStrings = it.selectedString?.let { idx -> it.tunedStrings + idx } ?: it.tunedStrings
                )
            }
            AppGraph.historyStore.add(
                TuningRecord(
                    id = UUID.randomUUID().toString(),
                    at = System.currentTimeMillis(),
                    instrumentId = snapshot.instrument.id,
                    instrumentName = snapshot.instrument.faName,
                    tuningName = snapshot.tuning.name,
                    tuningId = snapshot.tuning.id,
                    step = match.step,
                    octave = match.octave,
                    cents = match.cents,
                    accuracy = match.accuracy,
                    a4 = snapshot.settings.a4
                )
            )
        }
    }

    private fun recomputeDisplay() {
        val s = _ui.value
        val strings = stringsOf(lang)
        val sel = s.selectedString
        val spec = if (sel != null) s.tuning.strings.getOrNull(sel) else null

        var note = s.displayNote
        var cents = s.displayCents
        var targetHz = s.targetHz
        var inTune = false
        var accuracy = 0

        if (spec != null) {
            note = strings.noteName(spec.step)
            targetHz = MusicTheory.frequency(spec.step, spec.octave, s.settings.a4)
            val f = lastFrequency
            if (f > 0) {
                var c = MusicTheory.cents(f, targetHz)
                c = ((c + 50.0) % 100.0 + 100.0) % 100.0 - 50.0
                cents = c
                inTune = abs(c) <= MusicTheory.IN_TUNE_CENTS
                accuracy = MusicTheory.accuracy(c)
            } else {
                cents = 0.0
            }
        } else {
            val m = lastMatch
            if (m != null) {
                note = strings.noteName(m.step)
                cents = m.cents
                targetHz = m.targetFrequency
                inTune = m.inTune
                accuracy = m.accuracy
            } else {
                // BUGFIX (needle stuck at ±50): when the sound stops we keep
                // the previous NOTE NAME (initial «لا» like the web, a giant
                // «؟» looked like a bug) but the cents MUST fall back to 0 -
                // otherwise the needle stayed pegged at the last ±50 reading
                // while the status said «داره گوش می‌دیم».
                cents = 0.0
                accuracy = 0
            }
        }

        val hint = when {
            !s.listening -> strings.hintMicOff
            s.frequency <= 0 -> strings.hintListening
            inTune -> strings.hintPerfect
            cents < 0 -> strings.hintFlat
            else -> strings.hintSharp
        }

        _ui.update {
            it.copy(
                displayNote = note,
                displayCents = cents,
                targetHz = targetHz,
                inTune = inTune,
                accuracy = accuracy,
                hint = hint
            )
        }
    }

    override fun onCleared() {
        engine.release()
        engineScope.cancel()
        super.onCleared()
    }
}
