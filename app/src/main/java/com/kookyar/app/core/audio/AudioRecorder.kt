package com.kookyar.app.core.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Microphone capture built on [AudioRecord].
 *
 * Streams mono 16-bit frames at 44.1 kHz, converts them to float and
 * decimates to ~22.05 kHz before handing them to consumers. Nothing is
 * ever written to disk - privacy friendly and market friendly.
 */
class AudioRecorder {

    data class AudioFrame(
        /** Down-sampled samples in [-1..1] (~22.05 kHz). */
        val samples: FloatArray,
        val rms: Float
    )

    private val running = AtomicBoolean(false)
    private var job: Job? = null
    private var audioRecord: AudioRecord? = null

    val isRunning: Boolean get() = running.get()

    /**
     * Starts emitting [AudioFrame]s through [onFrame].
     * Returns false when permission is missing or the recorder cannot open.
     */
    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope, context: Context?, onFrame: (AudioFrame) -> Unit): Boolean {
        if (running.get()) return true
        if (!Permission.canRecordAudio(context)) return false

        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) return false

        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuf, READ_CHUNK * 4)
            )
        } catch (t: Throwable) {
            return false
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return false
        }

        audioRecord = record
        running.set(true)
        record.startRecording()

        job = scope.launch(Dispatchers.IO) {
            val shortBuf = ShortArray(READ_CHUNK)
            val floatBuf = FloatArray(READ_CHUNK)
            var prev = FloatArray(0)
            while (isActive && running.get()) {
                val read = try {
                    record.read(shortBuf, 0, READ_CHUNK)
                } catch (t: Throwable) {
                    break
                }
                if (read <= 0) continue
                for (i in 0 until read) floatBuf[i] = shortBuf[i] / 32768f

                val down = PitchDetector.downsample(
                    floatBuf.copyOf(read), SAMPLE_RATE, PitchDetector.TARGET_RATE
                )
                val rms = PitchDetector.rmsOf(down)

                // The wave view refreshes per read (≈21 fps) while pitch
                // detection still runs on a full 2048-sample window built
                // from the previous + current chunk (50% overlap, standard
                // practice). Halving READ_CHUNK alone would have broken
                // bass notes (tauMax too small for E1).
                val analysis: FloatArray = if (prev.isNotEmpty()) {
                    FloatArray(prev.size + down.size).also {
                        System.arraycopy(prev, 0, it, 0, prev.size)
                        System.arraycopy(down, 0, it, prev.size, down.size)
                    }
                } else {
                    down
                }
                prev = down.copyOf()

                try {
                    onFrame(AudioFrame(analysis, rms))
                } catch (_: Throwable) {
                }
            }
        }
        return true
    }

    fun stop() {
        running.set(false)
        job?.cancel()
        job = null
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }
        try {
            audioRecord?.release()
        } catch (_: Throwable) {
        }
        audioRecord = null
    }

    companion object {
        const val SAMPLE_RATE = 44100

        /** 2048 frames @ 44.1 kHz -> 1024 samples @ 22.05 kHz per read.
         *  Two consecutive reads are concatenated into the 2048-sample
         *  analysis window the pitch detector expects, and the waveform
         *  refreshes every read (~21 fps) for a lively response. */
        const val READ_CHUNK = 2048
    }
}
