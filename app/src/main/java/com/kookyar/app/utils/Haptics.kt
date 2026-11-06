package com.kookyar.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Haptic feedback helpers (used on precise-tune and metronome accents). */
object Haptics {

    fun preciseTune(context: Context) {
        vibrate(context, 45L, 160)
    }

    fun light(context: Context) {
        vibrate(context, 18L, 90)
    }

    fun beat(context: Context, strong: Boolean) {
        vibrate(context, if (strong) 32L else 14L, if (strong) 140 else 80)
    }

    private fun vibrate(context: Context, ms: Long, amplitude: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vibrator.hasVibrator()) return
            vibrator.vibrate(VibrationEffect.createOneShot(ms, amplitude.coerceIn(1, 255)))
        } catch (_: Throwable) {
        }
    }
}
