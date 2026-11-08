package com.kookyar.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kookyar.app.AppGraph
import com.kookyar.app.core.about.Changelog
import com.kookyar.app.core.metronome.TonePlayer
import com.kookyar.app.core.updater.UpdateChecker
import com.kookyar.app.data.settings.AppSettings
import com.kookyar.app.data.settings.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel : ViewModel() {

    /** Lifecycle of the in-app update flow (Settings → به‌روزرسانی). */
    sealed interface UpdateUi {
        data object Idle : UpdateUi
        data object Checking : UpdateUi
        data class Available(val manifest: UpdateChecker.UpdateManifest) : UpdateUi
        data object UpToDate : UpdateUi
        data class Failed(val reason: FailReason) : UpdateUi
        data class Downloading(val progress: Int) : UpdateUi
        data class ReadyToInstall(val file: File, val versionName: String) : UpdateUi
        data object DownloadFailed : UpdateUi

        /** Why a check failed — drives the exact message shown to the user. */
        enum class FailReason {
            /** No/weak connectivity on the device. */
            NETWORK,
            /** HTTP 404 — update.json is not hosted at the configured URL. */
            NOT_FOUND,
            /** Any other HTTP error (5xx, 403, …). */
            SERVER
        }
    }

    private val toneScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val settings: StateFlow<AppSettings> = AppGraph.settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _update = MutableStateFlow<UpdateUi>(UpdateUi.Idle)
    val update: StateFlow<UpdateUi> = _update.asStateFlow()

    /** Bundled + remote (update.json) version history for «نسخه و تغییرات». */
    private val _changelog = MutableStateFlow(Changelog.entries)
    val changelog: StateFlow<List<Changelog.Entry>> = _changelog.asStateFlow()

    init {
        // silently merge the hosted changelog into the settings list -
        // release notes for versions published after THIS build appear
        // without any app update
        refreshChangelog()
    }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { AppGraph.settingsStore.setTheme(mode) }
    fun setHaptics(v: Boolean) = viewModelScope.launch { AppGraph.settingsStore.setHaptics(v) }
    fun setQuarterTones(v: Boolean) = viewModelScope.launch { AppGraph.settingsStore.setQuarterTones(v) }
    fun setKeepScreenOn(v: Boolean) = viewModelScope.launch { AppGraph.settingsStore.setKeepScreenOn(v) }
    fun setParticles(v: Boolean) = viewModelScope.launch { AppGraph.settingsStore.setParticles(v) }
    fun setConfirmSound(v: Boolean) = viewModelScope.launch { AppGraph.settingsStore.setConfirmSound(v) }
    fun setSensitivity(v: Float) = viewModelScope.launch { AppGraph.settingsStore.setSensitivity(v) }
    fun setA4(v: Double) = viewModelScope.launch { AppGraph.settingsStore.setA4(v) }
    fun setLanguage(code: String) = viewModelScope.launch {
        AppGraph.settingsStore.setLanguage(com.kookyar.app.core.i18n.AppLanguage.fromCode(code))
    }

    fun playReferenceA4() {
        TonePlayer.play(toneScope, settings.value.a4, 1500, 0.8f)
    }

    // ---- update flow --------------------------------------------------------

    fun resetUpdateState() {
        if (_update.value !is UpdateUi.Downloading) _update.value = UpdateUi.Idle
    }

    fun checkForUpdate() {
        if (_update.value is UpdateUi.Checking || _update.value is UpdateUi.Downloading) return
        _update.value = UpdateUi.Checking
        viewModelScope.launch {
            val result = UpdateChecker.check()
            AppGraph.settingsStore.setLastUpdateCheck(System.currentTimeMillis())
            result.fold(
                onSuccess = { manifest ->
                    manifest.toChangelogEntry()?.let { remote ->
                        _changelog.value = Changelog.mergedWith(remote)
                    }
                    _update.value =
                        if (UpdateChecker.isNewer(manifest)) UpdateUi.Available(manifest)
                        else UpdateUi.UpToDate
                },
                onFailure = { e ->
                    _update.value = UpdateUi.Failed(
                        when (e) {
                            is UpdateChecker.UpdateHttpException ->
                                if (e.code == 404) UpdateUi.FailReason.NOT_FOUND
                                else UpdateUi.FailReason.SERVER
                            else -> UpdateUi.FailReason.NETWORK
                        }
                    )
                }
            )
        }
    }

    /**
     * Fetches update.json and merges its changelog into the version list.
     * Silent by design: on any failure the bundled [Changelog.entries]
     * stays as-is, so the section never breaks offline.
     */
    fun refreshChangelog() {
        viewModelScope.launch {
            UpdateChecker.check().onSuccess { manifest ->
                manifest.toChangelogEntry()?.let { remote ->
                    _changelog.value = Changelog.mergedWith(remote)
                }
            }
        }
    }

    /** Maps the update manifest to a changelog entry (null when it has no notes). */
    private fun UpdateChecker.UpdateManifest.toChangelogEntry(): Changelog.Entry? {
        val fa = changelogFor(fa = true)
        val en = changelogFor(fa = false)
        if (fa.isEmpty() && en.isEmpty()) return null
        return Changelog.Entry(
            versionCode = versionCode,
            versionName = versionName,
            date = date,
            fa = fa,
            en = en
        )
    }

    fun openBazaar(context: Context, manifest: UpdateChecker.UpdateManifest?) =
        UpdateChecker.openBazaar(context, manifest?.markets?.bazaar)

    fun openMyket(context: Context, manifest: UpdateChecker.UpdateManifest?) =
        UpdateChecker.openMyket(context, manifest?.markets?.myket)

    fun openGooglePlay(context: Context, manifest: UpdateChecker.UpdateManifest?) =
        UpdateChecker.openGooglePlay(context, manifest?.markets?.googlePlay)

    /** Downloads the APK (when the manifest exposes apk_url) then prepares install. */
    fun downloadUpdate(context: Context, manifest: UpdateChecker.UpdateManifest) {
        val url = manifest.apkUrl ?: return
        _update.value = UpdateUi.Downloading(progress = 0)
        viewModelScope.launch {
            val file = UpdateChecker.downloadApk(
                context, url, manifest.versionName
            ) { p -> _update.value = UpdateUi.Downloading(progress = p) }
            _update.value =
                if (file != null) {
                    if (UpdateChecker.canInstall(context)) {
                        UpdateChecker.installApk(context, file)
                        UpdateUi.ReadyToInstall(file, manifest.versionName)
                    } else {
                        // send the user to allow "install unknown apps" first
                        UpdateChecker.requestInstallPermission(context)
                        UpdateUi.ReadyToInstall(file, manifest.versionName)
                    }
                } else UpdateUi.DownloadFailed
        }
    }

    /** (Re)tries installing an already-downloaded file. */
    fun installDownloaded(context: Context, file: File, versionName: String) {
        if (UpdateChecker.canInstall(context)) {
            UpdateChecker.installApk(context, file)
        } else {
            UpdateChecker.requestInstallPermission(context)
        }
    }

    override fun onCleared() {
        TonePlayer.stop()
        super.onCleared()
    }
}
