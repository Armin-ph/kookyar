package com.kookyar.app.utils

import com.kookyar.app.core.i18n.AppLanguage

/**
 * Persian digit helpers.
 *
 * IMPORTANT: Only [faDigits] lives inside this object. The `toFa()` extensions
 * MUST be declared at file top level (see below) — if they are declared inside
 * `object Fa` they become "member extensions", which cannot be imported or
 * called outside the object (that was the source of the
 * "Unresolved reference 'toFa'" errors).
 */
object Fa {
    private val digits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /** Converts every Latin digit in [s] to its Persian equivalent. */
    fun faDigits(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s) {
            sb.append(if (c in '0'..'9') digits[c - '0'] else c)
        }
        return sb.toString()
    }
}

/* ------------------------------------------------------------------ */
/* Top-level Persian-digit extensions — importable everywhere via:     */
/*   import com.kookyar.app.utils.toFa                                 */
/* ------------------------------------------------------------------ */

fun Int.toFa(): String = Fa.faDigits(toString())

fun Long.toFa(): String = Fa.faDigits(toString())

fun Double.toFa(decimals: Int = 1): String = Fa.faDigits(String.format("%.${decimals}f", this))

fun Float.toFa(decimals: Int = 1): String = toDouble().toFa(decimals)

/* ------------------------------------------------------------------ */
/* Language-aware helpers — Persian digits for FA, Latin digits for EN */
/* Import via: import com.kookyar.app.utils.toNum                      */
/* ------------------------------------------------------------------ */

fun Int.toNum(lang: AppLanguage): String = if (lang.isFa) toFa() else toString()

fun Long.toNum(lang: AppLanguage): String = if (lang.isFa) toFa() else toString()

fun Double.toNum(lang: AppLanguage, decimals: Int = 1): String =
    if (lang.isFa) toFa(decimals) else String.format("%.${decimals}f", this)

fun Float.toNum(lang: AppLanguage, decimals: Int = 1): String = toDouble().toNum(lang, decimals)

fun String.toNum(lang: AppLanguage): String = if (lang.isFa) Fa.faDigits(this) else this
