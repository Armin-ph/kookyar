package com.kookyar.app.ui.screens.ear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kookyar.app.AppGraph
import com.kookyar.app.core.i18n.AppLanguage
import com.kookyar.app.core.i18n.stringsOf
import com.kookyar.app.core.metronome.TonePlayer
import com.kookyar.app.core.music.MusicTheory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class EarTrainingViewModel : ViewModel() {

    data class EarUiState(
        val optionsCount: Int = 4,
        val playReferenceFirst: Boolean = true,
        val round: Int = 0,
        val score: Int = 0,
        val streak: Int = 0,
        val bestStreak: Int = 0,
        val targetStep: Int = -1,
        val options: List<Int> = emptyList(),
        val selected: Int = -1,
        val revealed: Boolean = false,
        val lastCorrect: Boolean? = null,
        val praise: String = ""
    )

    private val toneScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var lang: AppLanguage = AppLanguage.FA

    init {
        viewModelScope.launch {
            AppGraph.settingsStore.settings.collect { lang = it.language }
        }
    }

    private val _ui = MutableStateFlow(EarUiState())
    val ui: StateFlow<EarUiState> = _ui.asStateFlow()

    companion object {
        /** Diatonic 24-TET steps of one octave: C D E F G A B. */
        val NATURAL_STEPS = listOf(0, 4, 8, 10, 14, 18, 22)
    }

    fun setOptionsCount(count: Int) {
        _ui.update { it.copy(optionsCount = count.coerceIn(3, 6)) }
    }

    fun toggleReferenceFirst() {
        _ui.update { it.copy(playReferenceFirst = !it.playReferenceFirst) }
    }

    fun playTarget() {
        val s = _ui.value
        if (s.targetStep < 0) return
        val freq = MusicTheory.frequency(s.targetStep, 4)
        if (s.playReferenceFirst) {
            TonePlayer.play(toneScope, MusicTheory.DEFAULT_A4, 700, 0.5f)
            viewModelScope.launch {
                delay(850)
                TonePlayer.play(toneScope, freq, 1200, 0.9f)
            }
        } else {
            TonePlayer.play(toneScope, freq, 1200, 0.9f)
        }
    }

    fun startRound() {
        val s = _ui.value
        val target = NATURAL_STEPS.random()
        val others = NATURAL_STEPS.filter { it != target }.shuffled(Random(System.nanoTime()))
        val options = (listOf(target) + others.take(s.optionsCount - 1)).shuffled(Random(System.nanoTime()))
        _ui.update {
            it.copy(
                round = it.round + 1,
                targetStep = target,
                options = options,
                selected = -1,
                revealed = false,
                lastCorrect = null,
                praise = ""
            )
        }
        // small delay so the UI settles before the note sounds
        viewModelScope.launch {
            delay(350)
            playTarget()
        }
    }

    fun answer(step: Int) {
        val s = _ui.value
        if (s.revealed || s.targetStep < 0) return
        val correct = step == s.targetStep
        val newStreak = if (correct) s.streak + 1 else 0
        val strings = stringsOf(lang)
        _ui.update {
            it.copy(
                selected = step,
                revealed = true,
                lastCorrect = correct,
                score = if (correct) it.score + 1 else it.score,
                streak = newStreak,
                bestStreak = maxOf(it.bestStreak, newStreak),
                praise = if (correct) strings.praiseList.random()
                else strings.wrongAnswer(strings.noteName(it.targetStep))
            )
        }
    }

    override fun onCleared() {
        TonePlayer.stop()
        super.onCleared()
    }
}
