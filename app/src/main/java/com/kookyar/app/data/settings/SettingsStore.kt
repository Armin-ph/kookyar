package com.kookyar.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kookyar.app.core.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "kookyar_settings")

enum class ThemeMode(val faName: String) {
    GLASS("تاریک شیشه‌ای"),
    PERSIAN("سنتی ایرانی"),
    NEON("نئون")
}

data class AppSettings(
    val theme: ThemeMode = ThemeMode.GLASS,
    val a4: Double = 440.0,
    val haptics: Boolean = true,
    val quarterTones: Boolean = true,
    val keepScreenOn: Boolean = true,
    val particles: Boolean = true,
    val confirmSound: Boolean = true,
    val sensitivity: Float = 1.0f,
    val instrumentId: String = "guitar",
    val tuningId: String = "std",
    val language: AppLanguage = AppLanguage.FA,
    val lastUpdateCheck: Long = 0L
)

/** Persistent app settings backed by Jetpack DataStore. */
class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val A4 = doublePreferencesKey("a4")
        val HAPTICS = booleanPreferencesKey("haptics")
        val QUARTER = booleanPreferencesKey("quarter_tones")
        val SCREEN = booleanPreferencesKey("keep_screen")
        val PARTICLES = booleanPreferencesKey("particles")
        val CONFIRM = booleanPreferencesKey("confirm_sound")
        val GAIN = floatPreferencesKey("sensitivity")
        val INSTRUMENT = stringPreferencesKey("instrument_id")
        val TUNING = stringPreferencesKey("tuning_id")
        val LANG = stringPreferencesKey("language")
        val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { p ->
        AppSettings(
            theme = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.GLASS,
            a4 = p[Keys.A4] ?: 440.0,
            haptics = p[Keys.HAPTICS] ?: true,
            quarterTones = p[Keys.QUARTER] ?: true,
            keepScreenOn = p[Keys.SCREEN] ?: true,
            particles = p[Keys.PARTICLES] ?: true,
            confirmSound = p[Keys.CONFIRM] ?: true,
            sensitivity = p[Keys.GAIN] ?: 1.0f,
            instrumentId = p[Keys.INSTRUMENT] ?: "guitar",
            tuningId = p[Keys.TUNING] ?: "std",
            language = AppLanguage.fromCode(p[Keys.LANG]),
            lastUpdateCheck = p[Keys.LAST_UPDATE_CHECK] ?: 0L
        )
    }

    suspend fun setTheme(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setA4(value: Double) = edit { it[Keys.A4] = value.coerceIn(430.0, 450.0) }
    suspend fun setHaptics(value: Boolean) = edit { it[Keys.HAPTICS] = value }
    suspend fun setQuarterTones(value: Boolean) = edit { it[Keys.QUARTER] = value }
    suspend fun setKeepScreenOn(value: Boolean) = edit { it[Keys.SCREEN] = value }
    suspend fun setParticles(value: Boolean) = edit { it[Keys.PARTICLES] = value }
    suspend fun setConfirmSound(value: Boolean) = edit { it[Keys.CONFIRM] = value }
    suspend fun setSensitivity(value: Float) = edit { it[Keys.GAIN] = value.coerceIn(0.5f, 3f) }
    suspend fun setInstrument(id: String, tuningId: String) = edit {
        it[Keys.INSTRUMENT] = id
        it[Keys.TUNING] = tuningId
    }
    suspend fun setLanguage(lang: AppLanguage) = edit { it[Keys.LANG] = lang.code }
    suspend fun setLastUpdateCheck(at: Long) = edit { it[Keys.LAST_UPDATE_CHECK] = at }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.settingsDataStore.edit { block(it) }
    }
}
