package com.kookyar.app.data.repository

import com.kookyar.app.data.model.InstrumentDef
import com.kookyar.app.data.model.InstrumentFamily
import com.kookyar.app.data.model.StringSpec
import com.kookyar.app.data.model.TuningDef

/**
 * Built-in instrument & tuning library.
 *
 * 24-TET steps: C=0, D=4, E=8, F=10, G=14, A=18, B=22 (odd = quarter tone).
 * Scientific octaves: A4 = step 18 / octave 4.
 */
object TuningsRepository {

    private fun s(step: Int, octave: Int) = StringSpec(step, octave)

    val all: List<InstrumentDef> = listOf(
        InstrumentDef(
            id = "chromatic", faName = "کروماتیک", enName = "Chromatic",
            emoji = "🎯", family = InstrumentFamily.UNIVERSAL,
            tunings = listOf(TuningDef("all", "همه نت‌ها", "All notes", emptyList()))
        ),
        InstrumentDef(
            id = "guitar", faName = "گیتار", enName = "Guitar",
            emoji = "🎸", family = InstrumentFamily.WESTERN,
            tunings = listOf(
                TuningDef(
                    "std", "استاندارد", "Standard",
                    listOf(s(8, 2), s(18, 2), s(4, 3), s(14, 3), s(22, 3), s(8, 4))
                ),
                TuningDef(
                    "dropd", "دراپ رِ", "Drop D",
                    listOf(s(4, 2), s(18, 2), s(4, 3), s(14, 3), s(22, 3), s(8, 4))
                ),
                TuningDef(
                    "halfdown", "نیم پرده پایین‌تر", "Half-step down",
                    listOf(s(6, 2), s(16, 2), s(2, 3), s(12, 3), s(20, 3), s(6, 4))
                ),
                TuningDef(
                    "openg", "اوپن جی", "Open G",
                    listOf(s(4, 2), s(14, 2), s(4, 3), s(14, 3), s(22, 3), s(4, 4))
                ),
                TuningDef(
                    "dadgad", "دادگاد", "DADGAD",
                    listOf(s(4, 2), s(18, 2), s(4, 3), s(14, 3), s(18, 3), s(4, 4))
                )
            )
        ),
        InstrumentDef(
            id = "bass", faName = "گیتار بیس", enName = "Bass",
            emoji = "🎸", family = InstrumentFamily.WESTERN,
            tunings = listOf(
                TuningDef(
                    "std", "استاندارد", "Standard",
                    listOf(s(8, 1), s(18, 1), s(4, 2), s(14, 2))
                ),
                TuningDef(
                    "dropd", "دراپ رِ", "Drop D",
                    listOf(s(4, 1), s(18, 1), s(4, 2), s(14, 2))
                )
            )
        ),
        InstrumentDef(
            id = "tar", faName = "تار", enName = "Tar",
            emoji = "🪕", family = InstrumentFamily.PERSIAN,
            tunings = listOf(
                TuningDef(
                    "std", "کوک دو – سل – دو", "C–G–C",
                    listOf(s(0, 3), s(14, 3), s(0, 4))
                ),
                TuningDef(
                    "low", "یک پرده پایین‌تر", "Half-step down",
                    listOf(s(20, 2), s(10, 3), s(20, 3))
                )
            )
        ),
        InstrumentDef(
            id = "setar", faName = "سه‌تار", enName = "Setar",
            emoji = "🪕", family = InstrumentFamily.PERSIAN,
            tunings = listOf(
                TuningDef(
                    "std", "کوک دو – سل – دو", "C–G–C",
                    listOf(s(0, 3), s(14, 3), s(0, 4))
                ),
                TuningDef(
                    "low", "یک پرده پایین‌تر", "Half-step down",
                    listOf(s(20, 2), s(10, 3), s(20, 3))
                )
            )
        ),
        InstrumentDef(
            id = "santur", faName = "سنتور", enName = "Santur",
            emoji = "🎼", family = InstrumentFamily.PERSIAN,
            tunings = listOf(
                TuningDef(
                    "g", "کوک سل", "G tuning",
                    listOf(s(14, 3), s(0, 4), s(0, 4), s(14, 4), s(0, 5))
                ),
                TuningDef(
                    "a", "کوک لا", "A tuning",
                    listOf(s(18, 3), s(4, 4), s(4, 4), s(18, 4), s(4, 5))
                )
            )
        ),
        InstrumentDef(
            id = "kamancheh", faName = "کمانچه", enName = "Kamancheh",
            emoji = "🎻", family = InstrumentFamily.PERSIAN,
            tunings = listOf(
                TuningDef(
                    "std", "کوک لا – ر – سل – دو", "A–D–G–C",
                    listOf(s(18, 2), s(4, 3), s(14, 3), s(0, 4))
                ),
                TuningDef(
                    "low", "یک پرده پایین‌تر", "Half-step down",
                    listOf(s(16, 2), s(2, 3), s(12, 3), s(20, 3))
                )
            )
        ),
        InstrumentDef(
            id = "violin", faName = "ویولن", enName = "Violin",
            emoji = "🎻", family = InstrumentFamily.WESTERN,
            tunings = listOf(
                TuningDef(
                    "std", "استاندارد", "Standard",
                    listOf(s(14, 3), s(4, 4), s(18, 4), s(8, 5))
                )
            )
        ),
        InstrumentDef(
            id = "ukulele", faName = "یوکلیلی", enName = "Ukulele",
            emoji = "🪕", family = InstrumentFamily.WESTERN,
            tunings = listOf(
                TuningDef(
                    "std", "استاندارد سل – دو – می – لا", "G–C–E–A",
                    listOf(s(14, 4), s(0, 4), s(8, 4), s(18, 4))
                )
            )
        ),
        InstrumentDef(
            id = "piano", faName = "پیانو", enName = "Piano",
            emoji = "🎹", family = InstrumentFamily.WESTERN,
            tunings = listOf(TuningDef("all", "همه نت‌ها", "All notes", emptyList()))
        )
    )

    fun byId(id: String): InstrumentDef? = all.firstOrNull { it.id == id }

    fun defaultTuning(instrument: InstrumentDef): TuningDef =
        instrument.tunings.firstOrNull { it.strings.isNotEmpty() } ?: instrument.tunings.first()
}
