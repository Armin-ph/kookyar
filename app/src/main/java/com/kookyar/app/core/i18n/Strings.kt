package com.kookyar.app.core.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Supported UI languages of Kookyar.
 *
 * The app ships fully bilingual (فارسی / English). Strings live in code
 * (not in res/) so switching is instant, works on every device and does
 * not require an activity restart.
 */
enum class AppLanguage(val code: String) {
    FA("fa"),
    EN("en");

    val isFa: Boolean get() = this == FA

    /** Label of the OTHER language - shown on the switch button. */
    val switchLabel: String get() = if (isFa) "EN" else "فا"

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: FA
    }
}

/** CompositionLocal for the active language. */
val LocalAppLang = staticCompositionLocalOf { AppLanguage.FA }

/**
 * Every user-facing string of the app, resolved for the active language.
 * Access via `LocalStrings.current` inside composables.
 */
@Suppress("MemberVisibilityCanBePrivate")
class Strings(private val lang: AppLanguage) {

    private val fa = lang.isFa

    // ---- bottom navigation -------------------------------------------------
    val tabTuner get() = if (fa) "تیونر" else "Tuner"
    val tabMetronome get() = if (fa) "مترونوم" else "Metronome"
    val tabInstruments get() = if (fa) "سازها" else "Instruments"
    val tabEar get() = if (fa) "تمرین گوش" else "Ear training"
    val tabHistory get() = if (fa) "تاریخچه" else "History"

    // ---- shared ------------------------------------------------------------
    val settings get() = if (fa) "تنظیمات" else "Settings"
    val hzSuffix get() = if (fa) "هرتز" else "Hz"
    val percentSuffix get() = if (fa) "٪" else "%"
    val minutesWord get() = if (fa) "دقیقه" else "min"
    val save get() = if (fa) "ذخیره" else "Save"
    val cancel get() = if (fa) "لغو" else "Cancel"
    val delete get() = if (fa) "حذف" else "Delete"
    val instrumentWord get() = if (fa) "ساز" else "Instrument"

    // ---- tuner -------------------------------------------------------------
    val tunerTitle get() = if (fa) "تیونر" else "Tuner"
    val tunerPrompt get() = if (fa) "یه نت بزن تا کوک کنیم" else "Play a note to tune it"
    val tunerPerfect get() = if (fa) "کوک دقیق شد!" else "Perfectly tuned!"
    val tunerSubtitle get() = if (fa) "نت کامل، دقیق و هماهنگ" else "Pure, precise and in harmony"
    val statusPerfect get() = if (fa) "کوک عالی!" else "Perfect!"
    val statusAnalyzing get() = if (fa) "در حال تحلیل صدا" else "Analyzing sound"
    val statusReady get() = if (fa) "آماده کوک" else "Ready to tune"
    val centsUnit get() = if (fa) "سنت" else "cents"
    val lockedCenter get() = if (fa) "کوک شد" else "In tune!"
    val micPermissionNeeded get() = if (fa) "برای کوک کردن، اجازه میکروفون لازم است" else "Microphone permission is needed for tuning"
    val grantPermission get() = if (fa) "اجازه می‌دهم" else "Grant permission"
    val stringsTitle get() = if (fa) "سیم‌ها" else "Strings"
    val targetWord get() = if (fa) "هدف" else "Target"
    val waveOn get() = if (fa) "موج صوتی" else "Sound wave"
    val waveOff get() = if (fa) "خاموش" else "Off"
    val quickEar get() = if (fa) "تمرین گوش" else "Ear training"
    val quickHistory get() = if (fa) "تاریخچه کوک" else "Tuning history"
    val unknownNote get() = if (fa) "؟" else "?"

    // tuner hints (TunerViewModel)
    val hintMicOff get() = if (fa) "میکروفون خاموش است" else "Microphone is off"
    val hintListening get() = if (fa) "داره گوش می‌ده… یه نت بزن" else "Listening… play a note"
    val hintPerfect get() = if (fa) "کوک عالی! هارمونی کامل" else "In tune! Perfect harmony"
    val hintFlat get() = if (fa) "یه‌کم پایین‌تره؛ محکم‌تر بکش" else "A bit flat; tighten the string"
    val hintSharp get() = if (fa) "یه‌کم بالاست؛ شل‌تر کن" else "A bit sharp; loosen the string"

    // cents ruler
    val centsMinus50 get() = if (fa) "۵۰-" else "-50"
    val centsPlus50 get() = if (fa) "۵۰+" else "+50"
    val centsZero get() = if (fa) "۰" else "0"
    fun deviation(cents: String) = if (fa) "اختلاف: $cents سنت" else "Deviation: $cents cents"

    // ---- settings ----------------------------------------------------------
    val settingsTitle get() = if (fa) "تنظیمات" else "Settings"
    val micSensitivity get() = if (fa) "حساسیت میکروفون" else "Microphone sensitivity"
    val micSensitivityDesc get() = if (fa) "برای سازهای خفگ‌تر کمی بیشترش کن" else "Raise it a bit for quieter instruments"
    val quarterTones get() = if (fa) "پشتیبانی ربع‌پرده (سری و کرن)" else "Quarter tones (Sori & Koron)"
    val quarterTonesDesc get() = if (fa) "فعال‌سازی فواصل ربع‌پرده در موسیقی ایرانی" else "Enable Persian quarter-tone intervals"
    val hapticsTitle get() = if (fa) "لرزش هنگام کوک دقیق" else "Vibrate when in tune"
    val hapticsDesc get() = if (fa) "لرزش ملایم وقتی به کوک دقیق می‌رسی" else "A gentle buzz when you hit perfect pitch"
    val particlesTitle get() = if (fa) "انیمیشن ذرات" else "Particle animation"
    val particlesDesc get() = if (fa) "جشن ذرات هنگام کوک دقیق" else "A particle celebration on perfect tuning"
    val confirmSoundTitle get() = if (fa) "صدای تأیید کوک" else "Tuned confirmation sound"
    val confirmSoundDesc get() = if (fa) "پس از کوک دقیق، نت به‌صورت ملایم پخش می‌شود" else "Softly plays the note once you are in tune"
    val keepScreenOnTitle get() = if (fa) "روشن ماندن صفحه" else "Keep screen on"
    val keepScreenOnDesc get() = if (fa) "جلوگیری از خاموش‌شدن صفحه در حین کوک" else "Prevent the screen from sleeping while tuning"
    val refFreqTitle get() = if (fa) "فرکانس مرجع (لا / A4)" else "Reference frequency (A4)"
    val refFreqRange get() = if (fa) "بین ۴۳۰ تا ۴۵۰ هرتز" else "Between 430 and 450 Hz"
    val playReference get() = if (fa) "پخش نت مرجع" else "Play reference note"
    val themesSection get() = if (fa) "پیش‌نمایش تم‌ها" else "Theme preview"
    val themeGlass get() = if (fa) "تاریک شیشه‌ای" else "Glass dark"
    val themeGlassDesc get() = if (fa) "مدرن • شفاف • عمق" else "Modern • Clear • Deep"
    val themePersian get() = if (fa) "سنتی ایرانی" else "Persian classic"
    val themePersianDesc get() = if (fa) "اصیل • هنری • ایرانی" else "Authentic • Artistic • Persian"
    val themeNeon get() = if (fa) "نئون" else "Neon"
    val themeNeonDesc get() = if (fa) "پرجلوه • درخشان • شبانه" else "Vivid • Glowing • Night"
    val madeWithLove get() = if (fa) "ساخته‌شده با ❤ برای موسیقی ایرانی" else "Made with ❤ for Persian music"
    val privacyNote get() = if (fa)
        "🔒 حریم خصوصی: هیچ صدایی ذخیره یا ارسال نمی‌شود؛ همه پردازش‌ها روی خود گوشی انجام می‌شود."
    else
        "🔒 Privacy: no audio is ever stored or sent; all processing happens on your device."

    // language section
    val languageTitle get() = if (fa) "زبان برنامه" else "App language"
    val languageDesc get() = if (fa) "فارسی یا English — همان لحظه اعمال می‌شود" else "Persian or English — applied instantly"
    val languageFa get() = "فارسی"
    val languageEn get() = "English"

    // version & changelog
    val versionSection get() = if (fa) "نسخه و تغییرات" else "Version & changes"
    val versionWord get() = if (fa) "نسخه" else "Version"
    val whatsNew get() = if (fa) "تغییرات نسخه‌ها" else "What's new"
    val showChangelog get() = if (fa) "نمایش تغییرات" else "Show changelog"
    val hideChangelog get() = if (fa) "بستن تغییرات" else "Hide changelog"

    // update section
    val updateSection get() = if (fa) "به‌روزرسانی برنامه" else "App updates"
    val checkForUpdate get() = if (fa) "بررسی به‌روزرسانی" else "Check for updates"
    val checkingNow get() = if (fa) "در حال بررسی…" else "Checking…"
    val lastCheck get() = if (fa) "آخرین بررسی" else "Last check"
    val neverChecked get() = if (fa) "هنوز بررسی نشده" else "Never checked"
    val upToDate get() = if (fa) "آخرین نسخه را داری ✓" else "You're up to date ✓"
    val updateFailed get() = if (fa) "بررسی ناموفق بود؛ اینترنت را چک کن" else "Check failed; check your connection"
    val updateNotFound get() = if (fa) "فایل به‌روزرسانی روی سرور پیدا نشد (۴۰۴)" else "Update file not found on the server (404)"
    val updateServerError get() = if (fa) "سرور به‌روزرسانی پاسخ نداد؛ کمی بعد دوباره امتحان کن" else "Update server didn't respond; try again later"
    val newVersion get() = if (fa) "نسخه جدید" else "New version"
    val available get() = if (fa) "موجود است" else "available"
    val updateMarkets get() = if (fa) "از مارکت به‌روزرسانی کن:" else "Update from a store:"
    val marketBazaar get() = "کافه‌بازار"
    val marketMyket get() = "مایکت"
    val marketPlay get() = "Google Play"
    val downloadApk get() = if (fa) "دریافت فایل نصبی" else "Download APK"
    val downloading get() = if (fa) "در حال دریافت…" else "Downloading…"
    val readyToInstall get() = if (fa) "دریافت شد — نصب کن" else "Downloaded — install"
    val installNow get() = if (fa) "نصب" else "Install"
    val installPermissionNeeded get() = if (fa)
        "برای نصب، اجازه «نصب برنامه‌های ناشناس» لازم است"
    else
        "Allow \"install unknown apps\" to continue"

    // ---- metronome ---------------------------------------------------------
    val metronomeTitle get() = if (fa) "مترونوم" else "Metronome"
    val slower get() = if (fa) "آهسته‌تر" else "Slower"
    val faster get() = if (fa) "تندتر" else "Faster"
    val playStop get() = if (fa) "پخش یا توقف" else "Play or stop"
    val subdivisionTitle get() = if (fa) "تقسیم‌بندی" else "Subdivision"
    val timeSigTitle get() = if (fa) "میزان" else "Time signature"
    val beatCountWord get() = if (fa) "تعداد ضرب" else "Beats"
    val less get() = if (fa) "کمتر" else "Less"
    val more get() = if (fa) "بیشتر" else "More"
    val accentPattern get() = if (fa) "الگوی تاکید" else "Accent pattern"
    val accentStrong get() = if (fa) "قوی" else "Strong"
    val accentMedium get() = if (fa) "ملایم" else "Medium"
    val accentNone get() = if (fa) "بدون تاکید" else "None"
    val tapTitle get() = if (fa) "ضربه بزن (TAP)" else "Tap tempo"
    val tapLast get() = if (fa) "آخرین" else "Last"
    val tapHint get() = if (fa) "با ریتم آهنگ ضربه بزن" else "Tap along with the music"
    val practiceTime get() = if (fa) "زمان تمرین" else "Practice time"
    val resetWord get() = if (fa) "صفر کردن" else "Reset"
    val autoRamp get() = if (fa) "تندشونده خودکار" else "Auto speed-up"
    val targetWord2 get() = if (fa) "هدف" else "Target"
    val targetBpm get() = if (fa) "BPM هدف" else "Target BPM"
    val soundType get() = if (fa) "نوع صدا" else "Sound"
    val presetsTitle get() = if (fa) "پیش‌تنظیم‌ها" else "Presets"
    val presetsEmpty get() = if (fa) "تنظیم فعلی را با اسم دلخواه ذخیره کن" else "Save your current setup with a custom name"
    val beatsSuffix get() = if (fa) "ضرب" else "beats"
    val savePresetTitle get() = if (fa) "ذخیره پیش‌تنظیم" else "Save preset"
    val presetNameHint get() = if (fa) "مثلاً: تمرین ۶/۸" else "e.g. 6/8 practice"
    val customSig get() = if (fa) "دلخواه" else "Custom"
    val customSigTitle get() = if (fa) "میزان دلخواه" else "Custom time signature"
    val customSigHint get() = if (fa) "تعداد ضرب‌ها را انتخاب کن" else "Choose the number of beats"
    val applyWord get() = if (fa) "اعمال" else "Apply"

    // ---- ear training ------------------------------------------------------
    val earTitle get() = if (fa) "تمرین گوش" else "Ear training"
    val difficulty get() = if (fa) "سطح سختی" else "Difficulty"
    val optionsWord get() = if (fa) "گزینه" else "options"
    val playRefFirst get() = if (fa) "پخش نت مرجع (لا) قبل از هر تمرین" else "Play the reference (A) before each round"
    val scoreRound get() = if (fa) "راند" else "Round"
    val scorePoints get() = if (fa) "امتیاز" else "Score"
    val scoreStreak get() = if (fa) "زنجیره" else "Streak"
    val scoreBest get() = if (fa) "رکورد" else "Best"
    val earStartPrompt get() = if (fa) "شروع کن و گوشت را بیازما" else "Start and test your ear"
    val earListenPrompt get() = if (fa) "گوش کن و نت درست را انتخاب کن" else "Listen and pick the right note"
    val btnStartRound get() = if (fa) "شروع تمرین" else "Start"
    val btnNextRound get() = if (fa) "راند بعدی" else "Next round"
    val btnReplayNote get() = if (fa) "پخش دوباره نت" else "Replay note"
    val earHint get() = if (fa) "هر راند یک نت پخش می‌شود؛ انتخاب کن و ببین درست بود یا نه" else "Each round plays one note; guess it and see if you were right"
    val praiseList get() = if (fa)
        listOf("آفرین!", "عالی بود!", "دقیق بود!", "همینه!", "دارت عالی می‌شی!")
    else
        listOf("Well done!", "Great!", "Spot on!", "That's it!", "You're on fire!")
    fun wrongAnswer(note: String) = if (fa) "نشد! جواب: $note" else "Not quite! It was $note"

    // ---- history -----------------------------------------------------------
    val historyTitle get() = if (fa) "تاریخچه کوک" else "Tuning history"
    val clearHistory get() = if (fa) "پاک کردن تاریخچه" else "Clear history"
    val chartTitle get() = if (fa) "دقت کوک در طول روزها" else "Tuning accuracy over days"
    val chartSubtitle get() = if (fa) "میزان دقت کوک شما نسبت به نت مرجع" else "Your accuracy against the reference pitch"
    val last7Days get() = if (fa) "۷ روز اخیر" else "Last 7 days"
    val legendDaily get() = if (fa) "دقت روزانه" else "Daily accuracy"
    val legendWeekAvg get() = if (fa) "میانگین هفته" else "Week average"
    val statWeekCount get() = if (fa) "کوک این هفته" else "This week"
    val statBestDay get() = if (fa) "بهترین روز" else "Best day"
    val statAvgAcc get() = if (fa) "دقت متوسط" else "Avg accuracy"
    val trendTitle get() = if (fa) "روندی کلی" else "Overall trend"
    val trendEmpty get() = if (fa) "هنوز کوکی ثبت نشده؛ اولین نت را کوک کن!" else "Nothing recorded yet; tune your first note!"
    fun trendUp(delta: String) = if (fa)
        "دقت کوک شما $delta٪ بهتر از هفته پیش بوده. عالی! ادامه بده."
    else
        "Your tuning accuracy is $delta% better than last week. Keep it up!"
    fun trendDown(delta: String) = if (fa)
        "دقت کوک $delta٪ کمتر از هفته پیش شده؛ کمی تمرین گوش کمکت می‌کند."
    else
        "Accuracy dropped $delta% versus last week; some ear training will help."
    val recentTunings get() = if (fa) "کوک‌های اخیر" else "Recent tunings"
    val nothingYet get() = if (fa) "هنوز چیزی نیست" else "Nothing yet"

    fun weekday(javaDay: java.time.DayOfWeek): String = when {
        fa -> when (javaDay) {
            java.time.DayOfWeek.SATURDAY -> "شنبه"
            java.time.DayOfWeek.SUNDAY -> "یکشنبه"
            java.time.DayOfWeek.MONDAY -> "دوشنبه"
            java.time.DayOfWeek.TUESDAY -> "سه‌شنبه"
            java.time.DayOfWeek.WEDNESDAY -> "چهارشنبه"
            java.time.DayOfWeek.THURSDAY -> "پنجشنبه"
            java.time.DayOfWeek.FRIDAY -> "جمعه"
        }
        else -> when (javaDay) {
            java.time.DayOfWeek.SATURDAY -> "Sat"
            java.time.DayOfWeek.SUNDAY -> "Sun"
            java.time.DayOfWeek.MONDAY -> "Mon"
            java.time.DayOfWeek.TUESDAY -> "Tue"
            java.time.DayOfWeek.WEDNESDAY -> "Wed"
            java.time.DayOfWeek.THURSDAY -> "Thu"
            java.time.DayOfWeek.FRIDAY -> "Fri"
        }
    }

    // ---- instruments -------------------------------------------------------
    val pickInstrument get() = if (fa) "انتخاب ساز" else "Choose instrument"
    fun tuningsOf(name: String) = if (fa) "کوک‌های $name" else "$name tunings"
    val chromaticAll get() = if (fa) "همه نت‌ها" else "All notes"
    val standardTuning get() = if (fa) "کوک استاندارد" else "Standard tuning"

    /** Localized 24-TET note name (دو/ر/… or C/D/…). */
    fun noteName(step: Int): String = if (fa)
        com.kookyar.app.core.music.MusicTheory.faName(step)
    else
        com.kookyar.app.core.music.MusicTheory.enName(step)
}

/** CompositionLocal holding the active [Strings]. */
val LocalStrings = staticCompositionLocalOf { Strings(AppLanguage.FA) }

/** Returns the [Strings] for a language (used by ViewModels / non-composable code). */
fun stringsOf(lang: AppLanguage): Strings = Strings(lang)
