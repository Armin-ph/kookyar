package com.kookyar.app.core.music

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

/**
 * Music theory core of Kookyar.
 *
 * Supports 24-TET (quarter tones) so Persian microtonal pitches
 * (کرون = quarter-flat, سری = quarter-sharp) can be named and measured.
 *
 * A 24-TET "step" (q) runs 0..23 per octave:
 *   even q  -> standard semitone (q/2 = semitone index, C = 0)
 *   odd  q  -> quarter tone between the surrounding semitones
 *
 * Scientific pitch notation: A4 = q 18, octave 4  <=>  MIDI 69.
 */
object MusicTheory {

    const val MIN_A4 = 430.0
    const val MAX_A4 = 450.0
    const val DEFAULT_A4 = 440.0

    /** Tolerance for "in tune" verdicts. */
    const val IN_TUNE_CENTS = 3.0
    const val NEAR_CENTS = 10.0

    // Persian note names ----------------------------------------------------
    private val FA_NATURAL = listOf("دو", "ر", "می", "فا", "سل", "لا", "سی")
    private val FA_SHARP = listOf("دو دیز", "ر دیز", "فا دیز", "سل دیز", "لا دیز")

    /**
     * Full 24-step Persian name table (per octave, q = 0..23).
     * Koron (کرن) = quarter-flat, Sori (سری) = quarter-sharp.
     * Classical Persian usage: koron on می/لا/سی، سری on دو/فا/سل.
     */
    private val FA_24 = listOf(
        "دو",        // 0  C
        "دو سری",    // 1  C half-sharp
        "دو دیز",    // 2  C#
        "ر کرن",     // 3  D half-flat (rare)
        "ر",         // 4  D
        "ر سری",     // 5  D half-sharp (rare)
        "ر دیز",     // 6  D#
        "می کرن",    // 7  E half-flat  (classic)
        "می",        // 8  E
        "می سری",    // 9  E half-sharp (rare)
        "فا",        // 10 F
        "فا سری",    // 11 F half-sharp (classic)
        "فا دیز",    // 12 F#
        "سل کرن",    // 13 G half-flat
        "سل",        // 14 G
        "سل سری",    // 15 G half-sharp (classic)
        "سل دیز",    // 16 G#
        "لا کرن",    // 17 A half-flat (classic)
        "لا",        // 18 A
        "لا سری",    // 19 A half-sharp (rare)
        "لا دیز",    // 20 A#
        "سی کرن",    // 21 B half-flat (classic)
        "سی",        // 22 B
        "سی سری"     // 23 B half-sharp (rare)
    )

    private val EN_24 = listOf(
        "C", "C↑", "C♯", "D↓", "D", "D↑", "D♯", "E↓", "E", "E↑",
        "F", "F↑", "F♯", "G↓", "G", "G↑", "G♯", "A↓", "A", "A↑",
        "A♯", "B↓", "B", "B↑"
    )

    /** Persian name for a 24-TET step (q must be wrapped into 0..23). */
    fun faName(step: Int): String = FA_24[((step % 24) + 24) % 24]

    /** Latin name for a 24-TET step. */
    fun enName(step: Int): String = EN_24[((step % 24) + 24) % 24]

    /** True when the step is a quarter tone (odd index) i.e. کرن/سری. */
    fun isQuarterTone(step: Int): Boolean = ((step % 24) + 24) % 24 % 2 == 1

    // Frequency math ---------------------------------------------------------

    /** Frequency of a MIDI note for the given A4 reference. */
    fun frequency(midi: Double, a4: Double = DEFAULT_A4): Double =
        a4 * Math.pow(2.0, (midi - 69.0) / 12.0)

    /** Frequency of a 24-TET step in a given scientific octave. */
    fun frequency(step: Int, octave: Int, a4: Double = DEFAULT_A4): Double {
        val midi = (octave + 1) * 12.0 + step / 2.0
        return frequency(midi, a4)
    }

    /** Distance between two frequencies in cents. */
    fun cents(f1: Double, f2: Double): Double = 1200.0 * log2(f1 / f2)

    /** MIDI note number (fractional) of a frequency. */
    fun midiOf(freq: Double, a4: Double = DEFAULT_A4): Double =
        69.0 + 12.0 * log2(freq / a4)

    /**
     * Finds the nearest 24-TET step for a frequency.
     * [allowQuarter] = false snaps to 12-TET only.
     */
    fun nearestStep(freq: Double, a4: Double, allowQuarter: Boolean): NoteMatch {
        val midiFloat = midiOf(freq, a4)
        // continuous 24-step index: A4 (midi 69) is step 18 of octave 4
        val stepFloat = (midiFloat - 69.0) * 2.0 + 18.0
        val raw: Int = if (allowQuarter) {
            Math.round(stepFloat).toInt()
        } else {
            Math.round(stepFloat / 2.0).toInt() * 2
        }
        val step = ((raw % 24) + 24) % 24
        val octave = 4 + Math.floorDiv(raw, 24)
        val targetMidi = 69.0 + (raw - 18.0) / 2.0
        val cents = (midiFloat - targetMidi) * 100.0
        val targetFreq = frequency(targetMidi, a4)
        return NoteMatch(
            step = step,
            octave = octave,
            cents = cents,
            targetFrequency = targetFreq,
            detectedFrequency = freq,
            isQuarterTone = isQuarterTone(step)
        )
    }

    /** Percentage accuracy (0..100) from |cents| deviation. */
    fun accuracy(cents: Double): Int {
        val dev = abs(cents)
        val acc = when {
            dev <= IN_TUNE_CENTS -> 100.0
            dev >= 50.0 -> 0.0
            else -> 100.0 * (1.0 - (dev - IN_TUNE_CENTS) / (50.0 - IN_TUNE_CENTS))
        }
        return acc.roundToInt().coerceIn(0, 100)
    }

    /** Tempo word for a BPM value (Persian + Italian term, or just Italian for EN). */
    fun tempoWord(bpm: Int, lang: com.kookyar.app.core.i18n.AppLanguage = com.kookyar.app.core.i18n.AppLanguage.FA): String {
        val fa = lang.isFa
        return when {
            bpm < 40 -> if (fa) "گراو (Grave)" else "Grave"
            bpm < 60 -> if (fa) "لارگو (Largo)" else "Largo"
            bpm < 76 -> if (fa) "آداجیو (Adagio)" else "Adagio"
            bpm < 108 -> if (fa) "آندانته (Andante)" else "Andante"
            bpm < 120 -> if (fa) "مودراتو (Moderato)" else "Moderato"
            bpm < 156 -> if (fa) "آلگرو (Allegro)" else "Allegro"
            bpm < 176 -> if (fa) "ویواچه (Vivace)" else "Vivace"
            else -> if (fa) "پرِستو (Presto)" else "Presto"
        }
    }
}

/** Result of matching a detected frequency to the nearest note. */
data class NoteMatch(
    /** 24-TET step 0..23 (C=0 per octave). */
    val step: Int,
    /** Scientific octave (A4 = 4). */
    val octave: Int,
    /** Deviation from the target note in cents (-50..+50). */
    val cents: Double,
    /** Ideal frequency of the target note for the current A4. */
    val targetFrequency: Double,
    /** Measured frequency. */
    val detectedFrequency: Double,
    val isQuarterTone: Boolean
) {
    val faName: String get() = MusicTheory.faName(step)
    val enName: String get() = MusicTheory.enName(step)
    val accuracy: Int get() = MusicTheory.accuracy(cents)
    val inTune: Boolean get() = abs(cents) <= MusicTheory.IN_TUNE_CENTS
}
