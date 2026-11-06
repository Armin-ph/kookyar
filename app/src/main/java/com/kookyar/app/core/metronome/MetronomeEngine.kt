package com.kookyar.app.core.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.roundToLong

/**
 * Sample-accurate streaming metronome.
 *
 * A producer coroutine continuously renders 50 ms PCM chunks and mixes
 * scheduled clicks into them. Beat events are emitted with the wall-clock
 * timestamp at which the click will actually be heard (derived from the
 * AudioTrack playback head), so the UI ring lights up in sync with the
 * sound instead of with the write time.
 */
class MetronomeEngine(private val scope: CoroutineScope) {

    enum class Subdivision(val perBeat: Int, val faName: String, val enName: String) {
        QUARTER(1, "نت", "Quarter"),
        EIGHTH(2, "چنگ", "Eighth"),
        TRIPLET(3, "سه‌تایی", "Triplet"),
        SIXTEENTH(4, "شانزدهم", "16th")
    }

    data class Config(
        val bpm: Int = 100,
        val beatsPerBar: Int = 4,
        val subdivision: Subdivision = Subdivision.QUARTER,
        /** One accent level (0..2) per beat. */
        val accents: List<Int> = listOf(2, 0, 1, 0),
        val sound: ClickSynth.ClickSound = ClickSynth.ClickSound.WOOD,
        val volume: Float = 0.8f
    )

    data class BeatEvent(
        val bar: Int,
        val beat: Int,
        val subBeat: Int,
        val level: Int,
        /** Wall-clock epoch ms at which this click becomes audible. */
        val audibleAtMs: Long
    )

    @Volatile
    var config: Config = Config()
        private set

    private val running = AtomicBoolean(false)
    private var job: Job? = null
    private var track: AudioTrack? = null

    private val _beats = MutableSharedFlow<BeatEvent>(
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val beats: SharedFlow<BeatEvent> = _beats.asSharedFlow()

    val isRunning: Boolean get() = running.get()

    fun updateConfig(newConfig: Config) {
        config = newConfig
    }

    fun start() {
        if (running.get()) return
        running.set(true)
        job = scope.launch(Dispatchers.IO) { runLoop() }
    }

    fun stop() {
        running.set(false)
        job?.cancel()
        job = null
        val t = track
        track = null
        try {
            t?.pause()
            t?.flush()
            t?.release()
        } catch (_: Throwable) {
        }
    }

    // ------------------------------------------------------------------ //

    private fun buildTrack(): AudioTrack? {
        return try {
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
            AudioTrack(
                attrs, format,
                max(minBuf, CHUNK_FRAMES * 4),
                AudioTrack.MODE_STREAM,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        } catch (t: Throwable) {
            null
        }
    }

    private suspend fun runLoop() {
        val audio = buildTrack() ?: run { running.set(false); return }
        track = audio
        audio.play()

        val chunk = FloatArray(CHUNK_FRAMES)
        var totalWritten = 0L
        var nextClickFrame = 0L
        var clickIndex = 0L
        val clickPool = HashMap<String, ShortArray>()

        try {
            while (kotlin.coroutines.coroutineContext.isActive && running.get()) {
                val cfg = config
                val subs = cfg.subdivision.perBeat
                val beatFrames = 60.0 / cfg.bpm * ClickSynth.SAMPLE_RATE
                val subFrames = beatFrames / subs

                java.util.Arrays.fill(chunk, 0f)
                val chunkStart = totalWritten
                val chunkEnd = totalWritten + CHUNK_FRAMES

                // schedule every click that starts inside this chunk
                while (nextClickFrame < chunkEnd) {
                    val beatInBar = ((clickIndex / subs) % cfg.beatsPerBar).toInt()
                    val sub = (clickIndex % subs).toInt()
                    val level = if (sub == 0) {
                        (cfg.accents.getOrNull(beatInBar) ?: 0).coerceIn(0, 2)
                    } else 0

                    val key = "${cfg.sound.name}_$level"
                    val pcm = clickPool.getOrPut(key) { ClickSynth.renderClick(cfg.sound, level, 1f) }

                    val offset = (nextClickFrame - chunkStart).toInt()
                    if (offset >= 0 && offset < CHUNK_FRAMES) {
                        for (i in pcm.indices) {
                            val idx = offset + i
                            if (idx >= CHUNK_FRAMES) break
                            chunk[idx] += (pcm[i] / Short.MAX_VALUE.toFloat()) * cfg.volume
                        }
                    }

                    // audible time = write time + queued audio duration
                    val queuedFrames = totalWritten - audio.playbackHeadPosition
                    val audibleAt = System.currentTimeMillis() +
                        (max(0, queuedFrames) * 1000.0 / ClickSynth.SAMPLE_RATE).roundToLong()

                    _beats.tryEmit(
                        BeatEvent(
                            bar = ((clickIndex / subs / cfg.beatsPerBar)).toInt(),
                            beat = beatInBar,
                            subBeat = sub,
                            level = level,
                            audibleAtMs = audibleAt
                        )
                    )

                    nextClickFrame += max(1L, subFrames.roundToLong())
                    clickIndex++
                }

                // float -> pcm16 and write
                val pcmOut = ShortArray(CHUNK_FRAMES)
                for (i in 0 until CHUNK_FRAMES) {
                    pcmOut[i] = (chunk[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                }
                var written = 0
                while (written < CHUNK_FRAMES && running.get()) {
                    written += audio.write(pcmOut, written, CHUNK_FRAMES - written)
                }
                totalWritten += CHUNK_FRAMES
            }
        } catch (_: Throwable) {
        } finally {
            try {
                audio.pause()
                audio.flush()
                audio.release()
            } catch (_: Throwable) {
            }
            if (track === audio) track = null
        }
    }

    companion object {
        private const val CHUNK_FRAMES = 2205 // 50 ms @ 44.1 kHz
    }
}
