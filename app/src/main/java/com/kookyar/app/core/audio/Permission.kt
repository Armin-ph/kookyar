package com.kookyar.app.core.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** Small indirection so UI code never touches android.Manifest directly. */
object Permission {
    fun canRecordAudio(context: Context?): Boolean {
        if (context == null) return false
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }
}
