package com.kookyar.app

import android.app.Application
import android.content.Context

/** Tiny service locator - no DI framework needed for this app size. */
object AppGraph {
    @Volatile
    private var appContextRef: Context? = null

    val appContext: Context get() = appContextRef ?: error("AppGraph not initialised")

    val settingsStore by lazy { com.kookyar.app.data.settings.SettingsStore(appContext) }
    val historyStore by lazy { com.kookyar.app.data.history.HistoryStore(appContext) }
    val metronomeStore by lazy { com.kookyar.app.data.history.MetronomeStore(appContext) }

    fun init(context: Context) {
        if (appContextRef == null) appContextRef = context.applicationContext
    }
}

class TunerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
