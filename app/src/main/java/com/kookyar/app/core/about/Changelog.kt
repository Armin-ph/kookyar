package com.kookyar.app.core.about

import com.kookyar.app.core.updater.UpdateChecker

/**
 * Version history of Kookyar shown in Settings → «نسخه و تغییرات».
 *
 * The list is fed from TWO sources:
 *  1. this bundled list - ships inside the APK, always available offline;
 *  2. the hosted update.json ([UpdateChecker]) - fetched at runtime and
 *     merged by [mergedWith]. A remote entry with the same [Entry.versionCode]
 *     REPLACES the bundled one, so fixing a typo in a release note or adding
 *     a brand-new release's notes never requires shipping a new build.
 *
 * Keep the newest entry on top and always add an entry for every release;
 * it is what users in Cafe Bazaar / Myket / Play read before updating.
 */
object Changelog {

    data class Entry(
        val versionCode: Int,
        val versionName: String,
        /** ISO date of the release. */
        val date: String,
        val fa: List<String>,
        val en: List<String>
    )

    val entries: List<Entry> = listOf(
        Entry(
            versionCode = 3,
            versionName = "1.1.1",
            date = "2026-11-08",
            fa = listOf(
                "عکس تار سه تار سنتور و کمانچه با عکس واقعی سازها جایگزین شد",
                "رفع خطای «بررسی ناموفق بود» هنگام چک کردن به روزرسانی",
                "پیام های خطای بروزرسانی رفع شد"
            ),
            en = listOf(
                "Replaced Tar, Setar, Santur and Kamancheh pictures with real photos of the instruments",
                "Fixed the «Check failed» error when checking for updates",
                "Fixed update error messages"
            )
        ),

        Entry(
            versionCode = 2,
            versionName = "1.1.0",
            date = "2026-11-03",
            fa = listOf(
                "زبان انگلیسی اضافه شد؛ اپلیکیشن دو زبانه شد (فارسی / English)",
                "کلید تغییر زبان در صفحه تیونر، کنار دکمه تم",
                "واکنش عقربه کوک خیلی روان‌تر و خواناتر شد",
                "بخش «نسخه و تغییرات» در تنظیمات",
                "بررسی و دریافت به‌روزرسانی از مارکت‌ها یا فایل نصبی در تنظیمات",
                "رفع گیر کردن عقربهٔ تیونر؛ عقربه دقیق و بدون تپش روی نت می‌ایستد",
                "تشخیص نت در محیط‌های شلوغ و پرسروصدا مطمئن‌تر شد؛ نویز محیط به‌عنوان نت ثبت نمی‌شود",
                "اصلاح اندازهٔ فونت و فاصله‌گذاری نمایش نت"
            ),
            en = listOf(
                "Added English - the app is now fully bilingual (فارسی / English)",
                "Language switch button on the tuner page, next to the theme button",
                "Much smoother, easier-to-read tuning needle",
                "New \"Version & changes\" section in Settings",
                "In-app update check & download from stores or APK in Settings",
                "Fixed the tuning needle getting stuck - it settles precisely on the note",
                "More reliable note detection in noisy environments; background noise is not detected as a note",
                "Fixed note display font size and spacing"
            )
        ),
        Entry(
            versionCode = 1,
            versionName = "1.0.0",
            date = "2026-10-01",
            fa = listOf(
                "نسخه اولیه کوک‌یار",
                "تیونر دقیق با پشتیبانی ربع‌پرده (سری و کرن)",
                "کوک‌های آماده برای تار، سه‌تار، سنتور، کمانچه، گیتار، ویولن و…",
                "مترونوم حرفه‌ای با تاکید، تقسیم‌بندی و Tap Tempo",
                "تمرین گوش، تاریخچه کوک و سه تم زیبا"
            ),
            en = listOf(
                "First release of Kookyar",
                "Precise tuner with Persian quarter tones (Sori & Koron)",
                "Ready tunings for Tar, Setar, Santur, Kamancheh, Guitar, Violin and more",
                "Pro metronome with accents, subdivisions and Tap Tempo",
                "Ear training, tuning history and three beautiful themes"
            )
        )
    )

    /**
     * Merges the bundled entries with the remote changelog hosted in
     * update.json. Rules:
     *  - `remote == null` (fetch failed / no changelog field) → bundled list;
     *  - a remote entry with the same versionCode overrides the bundled one
     *    (remote notes win), inheriting the bundled date when the manifest
     *    has no `date` field;
     *  - anything else is appended and the result is sorted newest-first.
     */
    fun mergedWith(remote: Entry?): List<Entry> {
        if (remote == null) return entries
        val bundled = entries.firstOrNull { it.versionCode == remote.versionCode }
        val entry = if (bundled != null && remote.date.isBlank()) {
            remote.copy(date = bundled.date)
        } else {
            remote
        }
        return (listOf(entry) + entries.filterNot { it.versionCode == remote.versionCode })
            .sortedByDescending { it.versionCode }
    }
}
