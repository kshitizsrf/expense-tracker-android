package com.hisabkitab.core.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * A language the app is translated into.
 *
 * @property tag BCP-47 tag passed to the per-app locale API; empty means "follow the system".
 * @property nativeName the language's name in itself, so people can always find their own.
 * @property flag emoji flag; regional languages use their country's flag.
 */
data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String,
    val flag: String,
)

object AppLanguages {

    /** Offered first during onboarding. */
    const val PREFERRED_TAG = "hi"

    val SYSTEM = AppLanguage(tag = "", nativeName = "System default", englishName = "Use device language", flag = "📱")

    /** Indian languages first (Hindi leading), then widely spoken world languages. */
    val all: List<AppLanguage> = listOf(
        AppLanguage("hi", "हिन्दी", "Hindi", "🇮🇳"),
        AppLanguage("en", "English", "English", "🇬🇧"),
        AppLanguage("bn", "বাংলা", "Bengali", "🇮🇳"),
        AppLanguage("mr", "मराठी", "Marathi", "🇮🇳"),
        AppLanguage("te", "తెలుగు", "Telugu", "🇮🇳"),
        AppLanguage("ta", "தமிழ்", "Tamil", "🇮🇳"),
        AppLanguage("gu", "ગુજરાતી", "Gujarati", "🇮🇳"),
        AppLanguage("kn", "ಕನ್ನಡ", "Kannada", "🇮🇳"),
        AppLanguage("ml", "മലയാളം", "Malayalam", "🇮🇳"),
        AppLanguage("pa", "ਪੰਜਾਬੀ", "Punjabi", "🇮🇳"),
        AppLanguage("or", "ଓଡ଼ିଆ", "Odia", "🇮🇳"),
        AppLanguage("as", "অসমীয়া", "Assamese", "🇮🇳"),
        AppLanguage("ur", "اردو", "Urdu", "🇵🇰"),
        AppLanguage("ne", "नेपाली", "Nepali", "🇳🇵"),
        AppLanguage("es", "Español", "Spanish", "🇪🇸"),
        AppLanguage("fr", "Français", "French", "🇫🇷"),
        AppLanguage("de", "Deutsch", "German", "🇩🇪"),
        AppLanguage("pt-BR", "Português", "Portuguese", "🇧🇷"),
        AppLanguage("it", "Italiano", "Italian", "🇮🇹"),
        AppLanguage("ru", "Русский", "Russian", "🇷🇺"),
        AppLanguage("ar", "العربية", "Arabic", "🇸🇦"),
        AppLanguage("tr", "Türkçe", "Turkish", "🇹🇷"),
        AppLanguage("id", "Bahasa Indonesia", "Indonesian", "🇮🇩"),
        AppLanguage("vi", "Tiếng Việt", "Vietnamese", "🇻🇳"),
        AppLanguage("th", "ไทย", "Thai", "🇹🇭"),
        AppLanguage("zh-CN", "简体中文", "Chinese (Simplified)", "🇨🇳"),
        AppLanguage("ja", "日本語", "Japanese", "🇯🇵"),
        AppLanguage("ko", "한국어", "Korean", "🇰🇷"),
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
