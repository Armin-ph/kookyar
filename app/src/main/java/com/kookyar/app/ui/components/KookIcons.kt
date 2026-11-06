package com.kookyar.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Custom brand icons that mirror the web preview's stroke icons
 * (tuning fork, guitar, metronome). Built from the same SVG path data,
 * rendered as 24dp stroked vectors so [androidx.compose.material3.Icon]
 * can tint them freely.
 */
private fun strokeIcon(name: String, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )
    for (d in paths) {
        builder.addPath(
            pathData = addPathNodes(d),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    return builder.build()
}

/** Tuning fork — the app's signature icon (center nav button). */
val KookTuningFork: ImageVector by lazy {
    strokeIcon(
        "KookTuningFork",
        "M7 3v7a5 5 0 0 0 10 0V3",
        "M12 15v5",
        "M9 21h6"
    )
}

/** Acoustic guitar — used for the «سازها» tab. */
val KookGuitar: ImageVector by lazy {
    strokeIcon(
        "KookGuitar",
        "M16.2 7.8 21 3",
        "M8.7 20.8a5.3 5.3 0 1 1 0-10.6 5.3 5.3 0 0 1 0 10.6z",
        "M13.4 12.8a3.5 3.5 0 1 1 0-7 3.5 3.5 0 0 1 0 7z",
        "M8.7 16.9a1.4 1.4 0 1 1 0-2.8 1.4 1.4 0 0 1 0 2.8z"
    )
}

/** Mechanical metronome — trapezoid body with pendulum. */
val KookMetronome: ImageVector by lazy {
    strokeIcon(
        "KookMetronome",
        "M9.2 4h5.6l2.7 17H6.5L9.2 4z",
        "M12 17V8.4",
        "M12 8.4 9.6 6.4",
        "M8.7 5.4a1.3 1.3 0 1 0 2.6 0 1.3 1.3 0 0 0-2.6 0"
    )
}

/** Ear-training icon — simple ear. */
val KookEar: ImageVector by lazy {
    strokeIcon(
        "KookEar",
        "M6 8.5a6 6 0 0 1 12 0c0 3-2.5 4.2-3.8 6-1 1.4-1 4.5-4.2 4.5-1.7 0-3-1-3.5-2.3",
        "M9.5 8.7a2.8 2.8 0 0 1 5.2 1.1c0 1.4-1.2 2-2.1 2.9"
    )
}
