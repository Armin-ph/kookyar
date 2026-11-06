package com.kookyar.app.core.updater

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.kookyar.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight in-app update infrastructure for Kookyar's multi-market
 * distribution (Cafe Bazaar, Myket, Google Play + direct APK).
 *
 * How it works:
 *  1. Host a small JSON file (schema below) anywhere reachable - e.g. GitHub
 *     raw, your own CDN. Put its URL in [UPDATE_JSON_URL].
 *  2. `check()` downloads it and compares `version_code` with the installed app.
 *  3. The UI then offers store buttons (bazaar/myket/play intents) and, when
 *     `apk_url` is present, a direct APK download that installs via the
 *     system package installer.
 *
 * JSON schema (see store-assets/update.json for a ready template):
 * ```json
 * {
 *   "version_code": 2,
 *   "version_name": "1.1.0",
 *   "date": "2026-11-03",
 *   "min_version_code": 1,
 *   "apk_url": "https://example.com/kookyar-1.1.0.apk",
 *   "changelog": { "fa": ["..."], "en": ["..."] },
 *   "markets": {
 *     "bazaar": "https://cafebazaar.ir/app/com.kookyar.app",
 *     "myket": "https://myket.ir/app/com.kookyar.app",
 *     "google_play": "https://play.google.com/store/apps/details?id=com.kookyar.app"
 *   }
 * }
 * ```
 *
 * The `changelog` (+ `date`) fields are ALSO merged into Settings →
 * «نسخه و تغییرات» at runtime (see [com.kookyar.app.core.about.Changelog]),
 * so release notes for future versions can be published without shipping
 * a new APK.
 */
object UpdateChecker {

    /**
     * Remote update manifest URL. Replace with your own hosted file before
     * publishing (a template lives in store-assets/update.json).
     */
    const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/kookyar/app/main/update.json"

    @Serializable
    data class UpdateManifest(
        @SerialName("version_code") val versionCode: Int,
        @SerialName("version_name") val versionName: String,
        @SerialName("min_version_code") val minVersionCode: Int = 0,
        @SerialName("apk_url") val apkUrl: String? = null,
        /** ISO release date - shown in Settings → «نسخه و تغییرات» (optional). */
        val date: String = "",
        val changelog: Map<String, List<String>> = emptyMap(),
        val markets: Markets? = null
    ) {
        fun changelogFor(fa: Boolean): List<String> =
            changelog[if (fa) "fa" else "en"] ?: changelog["en"] ?: emptyList()
    }

    @Serializable
    data class Markets(
        val bazaar: String? = null,
        val myket: String? = null,
        @SerialName("google_play") val googlePlay: String? = null
    )

    private val json = Json { ignoreUnknownKeys = true }

    /** True when the manifest offers something newer than the installed build. */
    fun isNewer(manifest: UpdateManifest): Boolean =
        manifest.versionCode > BuildConfig.VERSION_CODE

    /**
     * Fetches and parses the update manifest.
     * @return Result with the manifest or a network/parse failure.
     */
    suspend fun check(url: String = UPDATE_JSON_URL): Result<UpdateManifest> =
        withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 8_000
                conn.readTimeout = 8_000
                conn.setRequestProperty("Cache-Control", "no-cache")
                try {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    json.decodeFromString(UpdateManifest.serializer(), body)
                } finally {
                    conn.disconnect()
                }
            }
        }

    // ---- market intents ----------------------------------------------------

    /** Opens the app page on Cafe Bazaar, falling back to the web link. */
    fun openBazaar(context: Context, webUrl: String?) = openMarket(
        context,
        deepLink = "bazaar://details?id=${BuildConfig.APPLICATION_ID}",
        fallback = webUrl ?: "https://cafebazaar.ir/app/${BuildConfig.APPLICATION_ID}"
    )

    /** Opens the app page on Myket, falling back to the web link. */
    fun openMyket(context: Context, webUrl: String?) = openMarket(
        context,
        deepLink = "myket://details?id=${BuildConfig.APPLICATION_ID}",
        fallback = webUrl ?: "https://myket.ir/app/${BuildConfig.APPLICATION_ID}"
    )

    /** Opens the app page on Google Play, falling back to the web link. */
    fun openGooglePlay(context: Context, webUrl: String?) = openMarket(
        context,
        deepLink = "market://details?id=${BuildConfig.APPLICATION_ID}",
        fallback = webUrl ?: "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    )

    private fun openMarket(context: Context, deepLink: String, fallback: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(fallback))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Throwable) {
            }
        }
    }

    // ---- direct APK download & install --------------------------------------

    /**
     * Downloads [apkUrl] and suspends until it finishes.
     * [onProgress] reports 0..100 percent (or -1 when the size is unknown).
     * Returns the downloaded file or null on failure.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        versionName: String,
        onProgress: (Int) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates"
            ).apply { mkdirs() }
            val name = "kookyar-$versionName.apk"
            val out = File(dir, name)
            if (out.exists()) out.delete()

            val conn = URL(apkUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            try {
                val total = conn.contentLengthLong
                val tmp = File(dir, "$name.part")
                conn.inputStream.use { input ->
                    tmp.outputStream().use { output ->
                        val buf = ByteArray(64 * 1024)
                        var readTotal = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n < 0) break
                            output.write(buf, 0, n)
                            readTotal += n
                            if (total > 0) {
                                onProgress(((readTotal * 100) / total).toInt().coerceIn(0, 100))
                            } else {
                                onProgress(-1)
                            }
                        }
                    }
                }
                tmp.renameTo(out)
                out
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }

    /** Installs an APK via the system installer (FileProvider URI). */
    fun installApk(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, "${BuildConfig.APPLICATION_ID}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    /** True when the user has allowed installing unknown apps for this package. */
    fun canInstall(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    /** Opens the system page where the user can allow unknown-app installs. */
    fun requestInstallPermission(context: Context) {
        runCatching {
            context.startActivity(
                Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData(Uri.parse("package:${BuildConfig.APPLICATION_ID}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
