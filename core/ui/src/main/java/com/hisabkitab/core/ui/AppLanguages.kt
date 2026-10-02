package com.hisabkitab.core.ui

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hisabkitab.core.common.money.MoneyFormatter

/**
 * A language the app is translated into.
 *
 * @property tag BCP-47 tag passed to the per-app locale API; empty means "follow the system".
 * @property nativeName the language's name in itself, so people can always find their own.
 * @property flag emoji flag; regional languages use their country's flag.
 * @property currencyCode the currency of the language's main country, used as the default
 *   currency; null where the language spans many currencies (English), so the device region decides.
 */
data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String,
    val flag: String,
    val currencyCode: String? = null,
) {
    /** Default currency for someone choosing this language. */
    // The device locale, not the app locale: an app-only "en" carries no country to infer from.
    fun defaultCurrencyCode(): String =
        currencyCode ?: MoneyFormatter.defaultCurrencyCode(Resources.getSystem().configuration.locales[0])
}

object AppLanguages {

    /** Offered first during onboarding. */
    const val PREFERRED_TAG = "hi"

    val SYSTEM = AppLanguage(tag = "", nativeName = "System default", englishName = "Use device language", flag = "📱")

    /** Indian languages first (Hindi leading), then widely spoken world languages. */
    val all: List<AppLanguage> = listOf(
        AppLanguage("hi", "हिन्दी", "Hindi", "🇮🇳", "INR"),
        AppLanguage("en", "English", "English", "🇬🇧"),
        AppLanguage("bn", "বাংলা", "Bengali", "🇮🇳", "INR"),
        AppLanguage("mr", "मराठी", "Marathi", "🇮🇳", "INR"),
        AppLanguage("te", "తెలుగు", "Telugu", "🇮🇳", "INR"),
        AppLanguage("ta", "தமிழ்", "Tamil", "🇮🇳", "INR"),
        AppLanguage("gu", "ગુજરાતી", "Gujarati", "🇮🇳", "INR"),
        AppLanguage("kn", "ಕನ್ನಡ", "Kannada", "🇮🇳", "INR"),
        AppLanguage("ml", "മലയാളം", "Malayalam", "🇮🇳", "INR"),
        AppLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi", "🇮🇳", "INR"),
        AppLanguage("or", "ଓଡ଼ିଆ", "Odia", "🇮🇳", "INR"),
        AppLanguage("as", "অসমীয়া", "Assamese", "🇮🇳", "INR"),
        AppLanguage("ur", "اردو", "Urdu", "🇵🇰", "PKR"),
        AppLanguage("ne", "नेपाली", "Nepali", "🇳🇵", "NPR"),
        AppLanguage("es", "Español", "Spanish", "🇪🇸", "EUR"),
        AppLanguage("fr", "Français", "French", "🇫🇷", "EUR"),
        AppLanguage("de", "Deutsch", "German", "🇩🇪", "EUR"),
        AppLanguage("pt-BR", "Português", "Portuguese", "🇧🇷", "BRL"),
        AppLanguage("it", "Italiano", "Italian", "🇮🇹", "EUR"),
        AppLanguage("ru", "Русский", "Russian", "🇷🇺", "RUB"),
        AppLanguage("ar", "العربية", "Arabic", "🇸🇦", "SAR"),
        AppLanguage("tr", "Türkçe", "Turkish", "🇹🇷", "TRY"),
        AppLanguage("id", "Bahasa Indonesia", "Indonesian", "🇮🇩", "IDR"),
        AppLanguage("vi", "Tiếng Việt", "Vietnamese", "🇻🇳", "VND"),
        AppLanguage("th", "ไทย", "Thai", "🇹🇭", "THB"),
        AppLanguage("zh-CN", "简体中文", "Chinese (Simplified)", "🇨🇳", "CNY"),
        AppLanguage("ja", "日本語", "Japanese", "🇯🇵", "JPY"),
        AppLanguage("ko", "한국어", "Korean", "🇰🇷", "KRW"),
    )

    /** The language currently applied to the app, or [SYSTEM]. */
    fun current(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return SYSTEM
        val tag = locales[0]?.toLanguageTag() ?: return SYSTEM
        return all.firstOrNull { it.tag.equals(tag, ignoreCase = true) }
            ?: all.firstOrNull { tag.substringBefore('-').equals(it.tag.substringBefore('-'), ignoreCase = true) }
            ?: SYSTEM
    }

    /** Applies [language] app-wide; the activity is recreated in the new language. */
    fun apply(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            if (language.tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(language.tag),
        )
    }
}
