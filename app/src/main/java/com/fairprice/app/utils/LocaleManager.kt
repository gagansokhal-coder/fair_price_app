package com.fairprice.app.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * LocaleManager — Manages runtime language switching for the app.
 *
 * Supports: English (en), Hindi (hi), Punjabi (pa)
 *
 * Usage:
 *   - Call [setLocale] from the language selector in ProfileScreen
 *   - Override [attachBaseContext] in both Application and Activity
 *     using [wrapContext] so that all getString() calls respect the chosen locale
 *   - Persist the selected language code via SessionManager
 */
object LocaleManager {

    const val LANG_ENGLISH = "en"
    const val LANG_HINDI = "hi"
    const val LANG_PUNJABI = "pa"

    /**
     * Set the app locale and persist it.
     * The calling Activity should recreate() after this.
     */
    fun setLocale(context: Context, languageCode: String) {
        SessionManager.getInstance(context).saveLanguage(languageCode)
    }

    /**
     * Get the currently saved language code (defaults to English).
     */
    fun getLanguage(context: Context): String {
        return SessionManager.getInstance(context).getLanguage()
    }

    /**
     * Wrap the base context with the user's preferred locale.
     * Call this from attachBaseContext() in both Application and Activity.
     *
     * IMPORTANT: Reads SharedPreferences directly (not via SessionManager)
     * because applicationContext is null during Application.attachBaseContext().
     */
    fun wrapContext(context: Context): Context {
        val lang = try {
            context.getSharedPreferences("FairPriceSession", Context.MODE_PRIVATE)
                .getString("app_language", LANG_ENGLISH) ?: LANG_ENGLISH
        } catch (_: Exception) {
            LANG_ENGLISH
        }
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Returns the display name for the given language code.
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANG_HINDI -> "हिन्दी"
            LANG_PUNJABI -> "ਪੰਜਾਬੀ"
            else -> "English"
        }
    }

    /**
     * Returns all supported language entries.
     */
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            LANG_ENGLISH to "English",
            LANG_HINDI to "हिन्दी",
            LANG_PUNJABI to "ਪੰਜਾਬੀ",
        )
    }
}
