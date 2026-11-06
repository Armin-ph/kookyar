package com.kookyar.app.core.audio

import com.kookyar.app.core.music.MusicTheory
import com.kookyar.app.core.music.NoteMatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Glues the recorder + pitch detector together and produces a smooth,
 * UI-ready tuning state.
 *
 * Readability pipeline (why the needle does NOT jitter and the note name
 * no longer flickers between several notes):
 *  1. NOISE GATE: post-gain RMS must clear SILENCE_RMS -> room noise,
 *     footsteps and far-away voices never reach the detector at all
 *  2. microphone gain is clamped to a sane range -> no noise amplification
 *  3. ONSET CONFIRMATION: after silence, a note is only published after
 *     ONSET_FRAMES consecutive valid frames - a single noisy burst can no
 *     longer flash a note/needle (the «هر صدایی یه نت تشخیص میده» bug)
 *  4. clarity floor MIN_CLARITY -> only confident periodic signals pass
 *  5. median-of-5 on raw cents  -> rejects single-frame spikes / octave jumps
 *  6. slow exponential moving average (ALPHA 0.12) -> glides instead of jumping
 *  7. dead-band near zero (< 1.2 cents snaps to 0) -> stable "perfect" readout
 *  8. NOTE HYSTERESIS: a different note must repeat 3 consecutive frames
 *     before the readout switches - one noisy frame can no longer flip the
 *     name between neighbours (the "چندین نت در لحظه" bug)
 *  9. UI-side animation (see ArcGauge) adds a final critically-damped glide
 */
class TunerEngine(
    private val scope: CoroutineScope,
    private val onInTuneEdge: (match: NoteMatch) -> Unit = {}
) {

    data class TunerState(
        val listening: Boolean = false,
        val permissionDenied: Boolean = false,
        val frequency: Double = 0.0,
        /** Smoothed frequency for display purposes (Hz). */
        val displayFrequency: Double = 0.0,
        val clarity: Float = 0f,
        val rms: Float = 0f,
        val match: NoteMatch? = null,
        /** Down-sampled microphone samples for the live oscilloscope view. */
        val scope: FloatArray = FloatArray(0)
    )

    private val recorder = AudioRecorder()
    private val detector = PitchDetector()

    private val _state = MutableStateFlow(TunerState())
    val state: StateFlow<TunerState> = _state.asStateFlow()

    private var job: kotlinx.coroutines.Job? = null
    private var gain = 1.0f
    private var allowQuarter = true
    private var a4 = MusicTheory.DEFAULT_A4

    // smoothing buffers
    private val recentCents = ArrayDeque<Double>(MEDIAN_WINDOW)
    private var smoothCents = 0.0
    private var smoothFrequency = 0.0
    private var lastStep = Int.MIN_VALUE

    // note-change hysteresis state
    private var pendingStep = Int.MIN_VALUE
    private var pendingCount = 0

    // onset confirmation: consecutive valid frames after silence
    private var signalStreak = 0

    // in-tune edge trigger state
    private var wasInTune = false
    private var lastEdgeAt = 0L

    fun configure(a4: Double, quarterTones: Boolean, sensitivity: Float) {
        this.a4 = a4
        this.allowQuarter = quarterTones
        // Balanced mic gain: the old 0.5..3.0 range let users crank the gain
        // so high that room noise entered the detector and the needle/note
        // went haywire. 0.6..1.8 keeps quiet instruments usable without
        // feeding the YIN detector pure noise.
        this.gain = sensitivity.coerceIn(0.6f, 1.8f)
    }

    fun start(context: android.content.Context? = null) {
        if (recorder.isRunning) return
        resetSmoothing()
        val ok = recorder.start(scope, context, ::onFrame)
        _state.value = _state.value.copy(listening = ok, permissionDenied = !ok)
    }

    fun stop() {
        recorder.stop()
        _state.value = _state.value.copy(
            listening = false, match = null, frequency = 0.0, displayFrequency = 0.0
        )
    }

    fun release() = stop()

    private fun resetSmoothing() {
        recentCents.clear()
        smoothCents = 0.0
        smoothFrequency = 0.0
        lastStep = Int.MIN_VALUE
        pendingStep = Int.MIN_VALUE
        pendingCount = 0
        signalStreak = 0
        wasInTune = false
    }

    /** Median of the last [MEDIAN_WINDOW] raw cents - robust against spikes. */
    private fun medianCents(): Double {
        val sorted = recentCents.sorted()
        return sorted[sorted.size / 2]
    }

    private fun onFrame(frame: AudioRecorder.AudioFrame) {
        val gained = FloatArray(frame.samples.size) { i ->
            (frame.samples[i] * gain).coerceIn(-1f, 1f)
        }
        val rms = PitchDetector.rmsOf(gained)

        // live oscilloscope buffer (refreshed every frame, even during
        // silence, so the wave view reacts instantly like the web preview)
        val scopeWave = run {
            val n = frame.samples.size
            if (n <= SCOPE_POINTS) frame.samples
            else FloatArray(SCOPE_POINTS) { i -> frame.samples[i * n / SCOPE_POINTS] }
        }

        val result = if (rms < SILENCE_RMS) null else detector.detect(gained, PitchDetector.TARGET_RATE)

        if (result == null || result.clarity < MIN_CLARITY) {
            // frame is silence or not confident enough -> resets the onset
            // streak so one ambient burst can never flash a note
            signalStreak = 0
            pendingStep = Int.MIN_VALUE
            pendingCount = 0
            _state.value = _state.value.copy(
                rms = rms, scope = scopeWave, frequency = 0.0, displayFrequency = 0.0, clarity = 0f, match = null
            )
            wasInTune = false
            return
        }

        // onset confirmation: a note is only published after ONSET_FRAMES
        // consecutive valid frames (~93 ms) - ambient blips stay invisible
        signalStreak++
        if (signalStreak < ONSET_FRAMES) {
            _state.value = _state.value.copy(
                rms = rms, scope = scopeWave, frequency = 0.0, displayFrequency = 0.0, clarity = 0f, match = null
            )
            return
        }

        val raw = MusicTheory.nearestStep(result.frequency, a4, allowQuarter)
        val stable = _state.value.match

        val reported: NoteMatch = if (stable == null || raw.step == lastStep) {
            // same note as before (or very first detection) -> normal smoothing
            lastStep = raw.step
            pendingStep = Int.MIN_VALUE
            pendingCount = 0
            recentCents.addLast(raw.cents)
            if (recentCents.size > MEDIAN_WINDOW) recentCents.removeFirst()
            val median = medianCents()
            // slow EMA over the median -> calm, readable needle
            smoothCents = smoothCents * (1 - ALPHA) + median * ALPHA
            // gentle pull to exactly 0 inside the dead-band for a stable readout
            if (abs(smoothCents) < DEADBAND_CENTS) smoothCents = 0.0
            raw.copy(cents = smoothCents)
        } else if (raw.step == pendingStep) {
            pendingCount++
            if (pendingCount >= NOTE_CONFIRM_FRAMES) {
                // the new note repeated enough frames -> commit the switch
                lastStep = raw.step
                pendingStep = Int.MIN_VALUE
                pendingCount = 0
                recentCents.clear()
                recentCents.addLast(raw.cents)
                smoothCents = raw.cents
                raw.copy(cents = smoothCents)
            } else {
                // not confirmed yet: keep reporting the PREVIOUS note and let
                // the needle glide towards it - no name flickering
                val c = MusicTheory.cents(result.frequency, stable.targetFrequency)
                stable.copy(cents = c, detectedFrequency = result.frequency)
            }
        } else {
            // first sighting of a different note - start the confirmation window
            pendingStep = raw.step
            pendingCount = 1
            val c = MusicTheory.cents(result.frequency, stable.targetFrequency)
            stable.copy(cents = c, detectedFrequency = result.frequency)
        }

        // light EMA on frequency so the Hz caption stops flickering too
        smoothFrequency = if (smoothFrequency <= 0.0) result.frequency
        else smoothFrequency * (1 - FREQ_ALPHA) + result.frequency * FREQ_ALPHA

        val match = reported
        val inTune = abs(match.cents) <= MusicTheory.IN_TUNE_CENTS

        val now = System.currentTimeMillis()
        if (inTune && !wasInTune && now - lastEdgeAt > EDGE_COOLDOWN_MS) {
            wasInTune = true
            lastEdgeAt = now
            onInTuneEdge(match)
        } else if (!inTune) {
            wasInTune = false
        }

        _state.value = _state.value.copy(
            listening = true,
            frequency = result.frequency,
            displayFrequency = smoothFrequency,
            clarity = result.clarity,
            rms = rms,
            scope = scopeWave,
            match = match
        )
    }

    companion object {
        /** Post-gain RMS floor (web gate: 0.008). Room noise on phone mics
         *  sits around 0.002-0.006, a played instrument well above 0.02. */
        private const val SILENCE_RMS = 0.010f

        /** YIN clarity floor - real plucked strings score 0.85-0.98,
         *  ambient noise rarely passes 0.6. */
        private const val MIN_CLARITY = 0.70f

        /** Consecutive valid frames required after silence before a note is
         *  shown (~93 ms at 21.5 fps) - kills single-burst false triggers. */
        private const val ONSET_FRAMES = 2

        /** EMA factor: lower = calmer. 0.12 ≈ full response in ~15 frames (≈1.4 s). */
        private const val ALPHA = 0.12

        /** Frequency caption smoothing (kept slightly faster than the needle). */
        private const val FREQ_ALPHA = 0.2

        /** Median window for spike rejection. */
        private const val MEDIAN_WINDOW = 5

        /** Below this deviation the readout snaps to a stable 0.0 cents. */
        private const val DEADBAND_CENTS = 1.2

        private const val EDGE_COOLDOWN_MS = 1200L

        /** Points in the live oscilloscope buffer handed to the UI. */
        private const val SCOPE_POINTS = 512

        /** A different note must be seen this many consecutive frames
         *  (~140 ms at 21.5 fps) before the readout switches to it. */
        private const val NOTE_CONFIRM_FRAMES = 3
    }
}
