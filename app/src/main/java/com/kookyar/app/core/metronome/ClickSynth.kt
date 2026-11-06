package com.kookyar.app.core.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Sample-level synthesis of metronome clicks and reference tones.
 * Everything is generated locally - no audio assets, tiny APK.
 */
object ClickSynth {

    const val SAMPLE_RATE = 44100

    enum class ClickSound(val faName: String, val enName: String) {
        WOOD("چوبی", "Wood"),
        CLASSIC("کلاسیک", "Classic"),
        DIGITAL("دیجیتال", "Digital"),
        KICK("بیس", "Kick"),
        TOMBAK("تمپک", "Tombak")
    }

    /** Accent level of a click. */
    const val LEVEL_WEAK = 0
    const val LEVEL_MEDIUM = 1
    const val LEVEL_STRONG = 2

    private val noise = Random(42)

    /**
     * Renders one click as 16-bit PCM.
     *
     * @param level [LEVEL_WEAK], [LEVEL_MEDIUM] or [LEVEL_STRONG].
     */
    fun renderClick(sound: ClickSound, level: Int, volume: Float): ShortArray {
        val base = when (level) {
            LEVEL_STRONG -> 1.0f
            LEVEL_MEDIUM -> 0.66f
            else -> 0.42f
        } * volume.coerceIn(0f, 1f)

        val out = when (sound) {
            ClickSound.WOOD -> wood(base, level)
            ClickSound.CLASSIC -> tick(base, level)
            ClickSound.DIGITAL -> digital(base, level)
            ClickSound.KICK -> kick(base)
            ClickSound.TOMBAK -> tombak(base, level)
        }
        return out
    }

    private fun alloc(ms: Int) = FloatArray(SAMPLE_RATE * ms / 1000)

    private fun toPcm(f: FloatArray): ShortArray {
        val out = ShortArray(f.size)
        for (i in f.indices) out[i] = (f[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        return out
    }

    private fun wood(level: Float, lv: Int): ShortArray {
        val buf = alloc(90)
        val freq = when (lv) {
            2 -> 1750.0
            1 -> 1500.0
            else -> 1250.0
        }
        for (i in buf.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t / 0.011).toFloat()
            var s = sin(2 * PI * freq * t).toFloat() * env * 0.8f
            // attack transient: short noise burst
            if (i < 64) s += (noise.nextFloat() * 2f - 1f) * (1f - i / 64f) * 0.5f
            buf[i] = s * level
        }
        return toPcm(buf)
    }

    private fun tick(level: Float, lv: Int): ShortArray {
        val buf = alloc(60)
        val freq = if (lv == 2) 2400.0 else 1900.0
        for (i in buf.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t / 0.007).toFloat()
            buf[i] = (sin(2 * PI * freq * t).toFloat() * 0.9f + sin(2 * PI * freq * 2 * t).toFloat() * 0.2f) * env * level
        }
        return toPcm(buf)
    }

    private fun digital(level: Float, lv: Int): ShortArray {
        val buf = alloc(100)
        val freq = when (lv) {
            2 -> 1000.0
            1 -> 840.0
            else -> 700.0
        }
        for (i in buf.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = min(1f, i / 220f)
            val env = exp(-t / 0.028).toFloat() * attack
            buf[i] = (sin(2 * PI * freq * t).toFloat() + 0.35f * sin(4 * PI * freq * t).toFloat()) * env * level
        }
        return toPcm(buf)
    }

    private fun kick(level: Float): ShortArray {
        val buf = alloc(140)
        var phase = 0.0
        for (i in buf.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 150.0 * exp(-t * 18.0) + 52.0
            phase += 2 * PI * freq / SAMPLE_RATE
            val env = exp(-t / 0.045).toFloat()
            buf[i] = sin(phase).toFloat() * env * level
        }
        return toPcm(buf)
    }

    private fun tombak(level: Float, lv: Int): ShortArray {
        // Persian tombak: bass "tom" for strong, dry skin snap for weak
        val buf = alloc(130)
        if (lv == 2) {
            var phase = 0.0
            for (i in buf.indices) {
                val t = i.toDouble() / SAMPLE_RATE
                val freq = 200.0 * exp(-t * 10.0) + 95.0
                phase += 2 * PI * freq / SAMPLE_RATE
                val env = exp(-t / 0.05).toFloat()
                buf[i] = sin(phase).toFloat() * env * level
            }
        } else {
            var lp = 0f
            for (i in buf.indices) {
                val t = i.toDouble() / SAMPLE_RATE
                val env = exp(-t / 0.014).toFloat()
                lp += 0.35f * ((noise.nextFloat() * 2f - 1f) - lp)
                buf[i] = lp * env * level
            }
        }
        return toPcm(buf)
    }

    /**
     * Renders a natural piano-like note (additive synthesis):
     *  - 10 partials with a ~1/n^1.3 roll-off like a real string spectrum
     *  - each partial dies out at its own rate (highs fade faster)
     *  - slight inharmonicity so it reads as a struck string, not a beep
     *  - soft low-passed hammer thump in the first milliseconds
     * Used by the ear trainer, the string reference tones and the
     * tune-confirmation ping.
     */
    fun renderTone(freqHz: Double, durationMs: Int, volume: Float): ShortArray {
        val buf = alloc(durationMs)
        val attack = SAMPLE_RATE * 6 / 1000
        val release = SAMPLE_RATE * 55 / 1000
        val nyq = SAMPLE_RATE * 0.45

        // per-partial amplitude + decay rate
        val maxPartial = 10
        val amp = DoubleArray(maxPartial + 1)
        val decay = DoubleArray(maxPartial + 1)
        var norm = 0.0
        val register = (freqHz / 440.0).coerceIn(0.55, 2.6)
        for (n in 1..maxPartial) {
            val fn = freqHz * n
            if (fn > nyq) break
            amp[n] = 1.0 / Math.pow(n.toDouble(), 1.3)
            decay[n] = (1.4 + 1.6 * (n - 1)) * register
            norm += amp[n]
        }

        // soft hammer thump: low-passed noise burst over the first ~15 ms
        val thump = FloatArray(SAMPLE_RATE * 15 / 1000)
        var lp = 0f
        for (i in thump.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            lp += 0.28f * ((noise.nextFloat() * 2f - 1f) - lp)
            thump[i] = (lp * exp(-t * 240.0) * 2.1f).toFloat()
        }

        for (i in buf.indices) {
            val t = i.toDouble() / SAMPLE_RATE
            var env = 1f
            if (i < attack) env = i.toFloat() / attack
            val tail = buf.size - i
            if (tail < release) env *= tail.toFloat() / release

            var v = 0.0
            for (n in 1..maxPartial) {
                if (amp[n] <= 0.0) break
                // real strings run slightly sharp on higher partials
                val fn = freqHz * n * (1.0 + INHARMONICITY * n * n)
                v += amp[n] * exp(-t * decay[n]) * sin(2 * PI * fn * t + n * PHASE_STEP)
            }
            if (i < thump.size) v += thump[i]

            buf[i] = ((v / norm) * 1.55 * env * 0.5 * volume.coerceIn(0f, 1f)).toFloat()
        }
        return toPcm(buf)
    }

    private const val INHARMONICITY = 0.00035
    private const val PHASE_STEP = 0.4
}

/**
 * One-shot tone player (ear training, confirmation ping).
 * Each call plays on its own AudioTrack; the previous one is cut off.
 */
object TonePlayer {

    private var current: AudioTrack? = null
    private var job: Job? = null

    fun play(scope: CoroutineScope, freqHz: Double, durationMs: Int = 900, volume: Float = 0.9f) {
        stop()
        job = scope.launch(Dispatchers.IO) {
            val pcm = ClickSynth.renderTone(freqHz, durationMs, volume)
            val track = buildTrack(pcm.size)
            current = track
            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                // wait for playback to finish, then release
                Thread.sleep(durationMs + 60L)
            } catch (_: Throwable) {
            } finally {
                try {
                    track.stop()
                } catch (_: Throwable) {
                }
                track.release()
                if (current === track) current = null
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        try {
            current?.pause()
            current?.flush()
        } catch (_: Throwable) {
        }
        current = null
    }

    private fun buildTrack(frames: Int): AudioTrack {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(ClickSynth.SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val minBuf = AudioTrack.getMinBufferSize(
            ClickSynth.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        return AudioTrack(attrs, format, maxOf(minBuf, frames * 2), AudioTrack.MODE_STATIC, android.media.AudioManager.AUDIO_SESSION_ID_GENERATE)
    }
}
