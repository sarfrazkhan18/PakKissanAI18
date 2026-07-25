package com.example.utils

/**
 * Numeral normalization.
 *
 * The app standardises on Western digits (0-9): mandi rate boards, fertiliser bags, and
 * telecom SMS all use them, and younger operators (who often hold the phone for an elder)
 * read them fastest. LLM output and pasted content can mix Eastern Arabic-Indic (٠-٩) and
 * Persian/Urdu (۰-۹) digits, so normalise numeric-heavy strings — market rates, doses,
 * dates — before display for a consistent look.
 *
 * Intentionally not applied to free Urdu prose wholesale; use it where numbers must be
 * unambiguous (rates, quantities).
 */
object NumeralUtils {

    // Arabic-Indic ٠١٢٣٤٥٦٧٨٩ and Persian/Urdu ۰۱۲۳۴۵۶۷۸۹ -> Western 0-9
    private val easternToWestern: Map<Char, Char> = buildMap {
        val western = "0123456789"
        val arabicIndic = "٠١٢٣٤٥٦٧٨٩"
        val persianUrdu = "۰۱۲۳۴۵۶۷۸۹"
        for (i in 0..9) {
            put(arabicIndic[i], western[i])
            put(persianUrdu[i], western[i])
        }
    }

    /** Convert any Eastern digits in the string to Western digits; other chars untouched. */
    fun toWesternDigits(input: String): String =
        input.map { easternToWestern[it] ?: it }.joinToString("")
}
