package com.kookyar.app.core.audio

import kotlin.math.max
import kotlin.math.min

/**
 * YIN fundamental frequency estimator.
 *
 * Chosen for its stability on plucked/bowed string instruments and its
 * sub-sample precision (parabolic interpolation), which gives us the
 * sub-cent resolution the tuner needs.
 *
 * Input buffers are expected to be down-sampled to ~22.05 kHz mono float
 * in [-1..1]. With N = 2048 that covers 21.5 Hz .. 11 kHz, comfortably
 * including every supported instrument.
 */
class PitchDetector(
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE
) {

    data class PitchResult(
        val frequency: Double,
        /** YIN aperiodicity -> clarity in 0..1 (higher = more confident). */
        val clarity: Float
    )

    private val tauMax = bufferSize / 2
    private val window = bufferSize / 2
    private val diff = DoubleArray(tauMax)
    private val cmnd = DoubleArray(tauMax)

    /**
     * Detects the fundamental frequency of [samples].
     * Returns null when the buffer is silent or no periodicity is found.
     */
    fun detect(samples: FloatArray, sampleRate: Int): PitchResult? {
        val n = min(samples.size, bufferSize)
        if (n < bufferSize) return null

        // silence gate
        var energy = 0.0
        for (i in 0 until n) energy += samples[i] * samples[i]
        val rms = kotlin.math.sqrt(energy / n)
        if (rms < 1e-4) return null

        // 1) squared difference function
        for (tau in 0 until tauMax) {
            var sum = 0.0
            for (j in 0 until window) {
                val delta = samples[j] - samples[j + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // 2) cumulative mean normalized difference
        cmnd[0] = 1.0
        var running = 0.0
        for (tau in 1 until tauMax) {
            running += diff[tau]
            cmnd[tau] = if (running != 0.0) diff[tau] * tau / running else 1.0
        }

        // 3) absolute threshold -> first local minimum below it
        var tauEstimate = -1
        var tau = 2
        while (tau < tauMax - 1) {
            if (cmnd[tau] < THRESHOLD) {
                while (tau + 1 < tauMax && cmnd[tau + 1] < cmnd[tau]) tau++
                tauEstimate = tau
                break
            }
            tau++
        }
        if (tauEstimate == -1) {
            // fall back to the global minimum
            var best = 2
            for (t in 2 until tauMax) if (cmnd[t] < cmnd[best]) best = t
            if (cmnd[best] > 0.85) return null
            tauEstimate = best
        }

        // 4) parabolic interpolation for sub-sample precision
        val betterTau = parabolic(tauEstimate) ?: tauEstimate.toDouble()
        val freq = sampleRate / betterTau
        if (freq <= 0 || freq.isNaN() || freq > sampleRate / 2.2) return null

        val clarity = (1.0 - cmnd[tauEstimate]).toFloat().coerceIn(0f, 1f)
        if (clarity < 0.30f) return null
        return PitchResult(freq, clarity)
    }

    private fun parabolic(tau: Int): Double? {
        if (tau < 1 || tau >= tauMax - 1) return null
        val s0 = cmnd[tau - 1]
        val s1 = cmnd[tau]
        val s2 = cmnd[tau + 1]
        val denom = 2.0 * (2.0 * s1 - s2 - s0)
        if (denom == 0.0) return null
        return tau + (s2 - s0) / denom
    }

    companion object {
        const val DEFAULT_BUFFER_SIZE = 2048
        /** Analysis rate after decimation (covers 21.5 Hz .. 11 kHz). */
        const val TARGET_RATE = 22050
        private const val THRESHOLD = 0.15

        /** Decimates a higher-rate frame to [toRate] by block averaging. */
        fun downsample(input: FloatArray, fromRate: Int, toRate: Int): FloatArray {
            if (fromRate <= toRate) return input
            val factor = max(1, Math.round(fromRate.toDouble() / toRate.toDouble()).toInt())
            val outSize = input.size / factor
            if (outSize <= 0) return FloatArray(0)
            val out = FloatArray(outSize)
            for (i in 0 until outSize) {
                var acc = 0f
                for (k in 0 until factor) acc += input[i * factor + k]
                out[i] = acc / factor
            }
            return out
        }

        fun rmsOf(samples: FloatArray): Float {
            var acc = 0.0
            for (s in samples) acc += s * s
            return min(1.0, kotlin.math.sqrt(acc / max(1, samples.size))).toFloat()
        }
    }
}
