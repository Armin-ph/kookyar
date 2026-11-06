package com.kookyar.app.data.model

import com.kookyar.app.core.i18n.AppLanguage
import kotlinx.serialization.Serializable

/** One course/string of an instrument: a 24-TET step (0..23) + scientific octave. */
@Serializable
data class StringSpec(val step: Int, val octave: Int)

/** A named tuning of an instrument. Empty [strings] means chromatic mode. */
@Serializable
data class TuningDef(
    val id: String,
    val name: String,
    val latinName: String,
    val strings: List<StringSpec>
) {
    /** Localized display name (Persian or Latin). */
    fun localizedName(lang: AppLanguage): String = if (lang.isFa) name else latinName.ifBlank { name }
}

enum class InstrumentFamily { PERSIAN, WESTERN, UNIVERSAL }

@Serializable
data class InstrumentDef(
    val id: String,
    val faName: String,
    val enName: String,
    val emoji: String,
    val family: InstrumentFamily,
    val tunings: List<TuningDef>
) {
    val isChromatic: Boolean get() = tunings.all { it.strings.isEmpty() }

    /** Localized display name (Persian or English). */
    fun localizedName(lang: AppLanguage): String = if (lang.isFa) faName else enName
}

/** A logged tuning session (for the history page). */
@Serializable
data class TuningRecord(
    val id: String,
    val at: Long,
    val instrumentId: String,
    val instrumentName: String,
    val tuningName: String,
    /** Id of the used tuning (empty for legacy records) - for localized display. */
    val tuningId: String = "",
    /** 24-TET step that was tuned. */
    val step: Int,
    val octave: Int,
    val cents: Double,
    val accuracy: Int,
    val a4: Double
)

@Serializable
data class MetronomePreset(
    val id: String,
    val name: String,
    val bpm: Int,
    val beatsPerBar: Int,
    val subdivision: String,
    val accents: List<Int>,
    val sound: String
)
