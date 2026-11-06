package com.kookyar.app.ui.screens.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kookyar.app.AppGraph
import com.kookyar.app.core.i18n.AppLanguage
import com.kookyar.app.core.metronome.ClickSynth
import com.kookyar.app.core.metronome.MetronomeEngine
import com.kookyar.app.data.model.MetronomePreset
import com.kookyar.app.utils.Haptics
import com.kookyar.app.core.music.MusicTheory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class MetronomeViewModel : ViewModel() {

    data class MetronomeUiState(
        val bpm: Int = 100,
        val sigBeats: Int = 4,
        val sigDen: Int = 4,
        val sigIndex: Int = 2,
        val customBeats: Int = 4,
        val subdivision: MetronomeEngine.Subdivision = MetronomeEngine.Subdivision.QUARTER,
        val accents: List<Int> = listOf(ACCENT_DEFAULTS[0], ACCENT_DEFAULTS[1], ACCENT_DEFAULTS[2], ACCENT_DEFAULTS[3]),
        val sound: ClickSynth.ClickSound = ClickSynth.ClickSound.WOOD,
        val volume: Float = 0.8f,
        val running: Boolean = false,
        val currentBeat: Int = -1,
        val pulseKey: Int = 0,
        val elapsedSeconds: Int = 0,
        val rampEnabled: Boolean = false,
        val rampTargetBpm: Int = 160,
        val rampMinutes: Int = 5,
        val presets: List<MetronomePreset> = emptyList(),
        val tapCount: Int = 0,
        val hapticsOn: Boolean = true
    )

    companion object {
        /** (beats, denominator) presets. */
        val TIME_SIGNATURES = listOf(
            2 to 4, 3 to 4, 4 to 4, 5 to 4, 6 to 8, 7 to 8, 9 to 8, 12 to 8
        )
        val ACCENT_DEFAULTS = listOf(2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val engine = MetronomeEngine(engineScope)

    private val _ui = MutableStateFlow(MetronomeUiState())
    val ui: StateFlow<MetronomeUiState> = _ui.asStateFlow()

    private var rampStartBpm = 100

    init {
        pushConfig()
        viewModelScope.launch {
            AppGraph.metronomeStore.presets.collect { presets ->
                _ui.update { it.copy(presets = presets) }
            }
        }
        viewModelScope.launch {
            AppGraph.settingsStore.settings.collect { s ->
                _ui.update { it.copy(hapticsOn = s.haptics) }
            }
        }
        viewModelScope.launch {
            engine.beats.collect { beat ->
                val wait = beat.audibleAtMs - System.currentTimeMillis()
                if (wait > 0) delay(wait)
                _ui.update {
                    it.copy(currentBeat = beat.beat, pulseKey = it.pulseKey + 1)
                }
                if (beat.level >= 2 && _ui.value.hapticsOn) {
                    Haptics.beat(AppGraph.appContext, strong = true)
                }
            }
        }
        // practice timer + progressive ramp
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val s = _ui.value
                if (s.running) {
                    val elapsed = s.elapsedSeconds + 1
                    var bpm = s.bpm
                    if (s.rampEnabled && s.rampMinutes > 0) {
                        val total = s.rampMinutes * 60
                        val target = s.rampTargetBpm
                        val progressed = rampStartBpm + ((target - rampStartBpm) * elapsed) / total
                        bpm = progressed.coerceIn(minOf(rampStartBpm, target), maxOf(rampStartBpm, target))
                    }
                    _ui.update { it.copy(elapsedSeconds = elapsed, bpm = bpm) }
                    pushConfig()
                }
            }
        }
    }

    fun start() {
        rampStartBpm = _ui.value.bpm
        engine.start()
        _ui.update { it.copy(running = true, currentBeat = -1) }
    }

    fun stop() {
        engine.stop()
        _ui.update { it.copy(running = false, currentBeat = -1) }
    }

    fun toggle() = if (_ui.value.running) stop() else start()

    fun resetSession() = _ui.update { it.copy(elapsedSeconds = 0) }

    fun setBpm(value: Int) {
        _ui.update { it.copy(bpm = value.coerceIn(30, 300)) }
        pushConfig()
    }

    fun nudgeBpm(delta: Int) = setBpm(_ui.value.bpm + delta)

    fun setTimeSignature(index: Int) {
        val (beats, den) = TIME_SIGNATURES[index]
        _ui.update {
            it.copy(
                sigIndex = index,
                sigBeats = beats,
                sigDen = den,
                customBeats = beats,
                accents = resizeAccents(it.accents, beats)
            )
        }
        pushConfig()
    }

    fun setCustomBeats(beats: Int) {
        val b = beats.coerceIn(1, 12)
        _ui.update {
            it.copy(
                sigIndex = -1,
                sigBeats = b,
                sigDen = 4,
                customBeats = b,
                accents = resizeAccents(it.accents, b)
            )
        }
        pushConfig()
    }

    fun setSubdivision(sub: MetronomeEngine.Subdivision) {
        _ui.update { it.copy(subdivision = sub) }
        pushConfig()
    }

    fun cycleAccent(beatIndex: Int) {
        _ui.update { s ->
            val accents = s.accents.toMutableList()
            val current = accents.getOrElse(beatIndex) { 0 }
            accents[beatIndex] = (current + 1) % 3
            s.copy(accents = accents)
        }
        pushConfig()
    }

    fun setSound(sound: ClickSynth.ClickSound) {
        _ui.update { it.copy(sound = sound) }
        pushConfig()
    }

    fun setVolume(value: Float) {
        _ui.update { it.copy(volume = value) }
        pushConfig()
    }

    fun setRamp(enabled: Boolean) = _ui.update { it.copy(rampEnabled = enabled) }
    fun setRampTarget(value: Int) = _ui.update { it.copy(rampTargetBpm = value.coerceIn(40, 300)) }
    fun setRampMinutes(value: Int) = _ui.update { it.copy(rampMinutes = value.coerceIn(1, 30)) }

    private var tapStamps: List<Long> = emptyList()

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapStamps = (listOf(now) + tapStamps).take(6)
        val windowed = tapStamps.takeWhile { now - it < 2500 }
        if (windowed.size >= 2) {
            val intervals = windowed.zipWithNext { a, b -> b - a }
            val avg = intervals.average()
            if (avg > 0) setBpm((60000.0 / avg).toInt().coerceIn(30, 300))
        }
        _ui.update { it.copy(tapCount = windowed.size) }
    }

    fun savePreset(name: String) {
        val s = _ui.value
        viewModelScope.launch {
            AppGraph.metronomeStore.save(
                MetronomePreset(
                    id = UUID.randomUUID().toString(),
                    name = name.ifBlank { "${s.bpm} BPM" },
                    bpm = s.bpm,
                    beatsPerBar = if (s.sigIndex >= 0) TIME_SIGNATURES[s.sigIndex].first else s.customBeats,
                    subdivision = s.subdivision.name,
                    accents = s.accents,
                    sound = s.sound.name
                )
            )
        }
    }

    fun applyPreset(preset: MetronomePreset) {
        val sub = runCatching { MetronomeEngine.Subdivision.valueOf(preset.subdivision) }
            .getOrDefault(MetronomeEngine.Subdivision.QUARTER)
        val sound = runCatching { ClickSynth.ClickSound.valueOf(preset.sound) }
            .getOrDefault(ClickSynth.ClickSound.WOOD)
        val sigIdx = TIME_SIGNATURES.indexOfFirst { it.first == preset.beatsPerBar && it.second == 4 }
        _ui.update {
            it.copy(
                bpm = preset.bpm,
                sigIndex = if (sigIdx >= 0) sigIdx else -1,
                sigBeats = preset.beatsPerBar,
                sigDen = 4,
                customBeats = preset.beatsPerBar,
                subdivision = sub,
                accents = resizeAccents(preset.accents, preset.beatsPerBar),
                sound = sound
            )
        }
        pushConfig()
    }

    fun deletePreset(id: String) {
        viewModelScope.launch { AppGraph.metronomeStore.delete(id) }
    }

    private fun resizeAccents(accents: List<Int>, beats: Int): List<Int> {
        val base = accents.take(beats).toMutableList()
        for (i in base.size until beats) base.add(ACCENT_DEFAULTS.getOrElse(i) { 0 })
        return base
    }

    private fun pushConfig() {
        val s = _ui.value
        val beats = if (s.sigIndex >= 0) TIME_SIGNATURES[s.sigIndex].first else s.customBeats
        engine.updateConfig(
            MetronomeEngine.Config(
                bpm = s.bpm,
                beatsPerBar = beats,
                subdivision = s.subdivision,
                accents = s.accents,
                sound = s.sound,
                volume = s.volume
            )
        )
    }

    fun tempoWord(lang: AppLanguage): String = MusicTheory.tempoWord(_ui.value.bpm, lang)

    override fun onCleared() {
        engine.stop()
        engineScope.cancel()
        super.onCleared()
    }
}
