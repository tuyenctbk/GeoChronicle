package com.example.data.model

data class TourLanguage(
    val code: String,
    val displayName: String,
    val flagEmoji: String
)

object SupportedLanguages {
    val LANGUAGES = listOf(
        TourLanguage("en", "English", "🇺🇸"),
        TourLanguage("es", "Español", "🇪🇸"),
        TourLanguage("fr", "Français", "🇫🇷"),
        TourLanguage("de", "Deutsch", "🇩🇪"),
        TourLanguage("it", "Italiano", "🇮🇹"),
        TourLanguage("ja", "日本語", "🇯🇵"),
        TourLanguage("zh", "中文 (简体)", "🇨🇳"),
        TourLanguage("pt", "Português", "🇵🇹"),
        TourLanguage("vi", "Tiếng Việt", "🇻🇳"),
        TourLanguage("ar", "العربية", "🇪🇬")
    )

    val DEFAULT = LANGUAGES[0]
}
