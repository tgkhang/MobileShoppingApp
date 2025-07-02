package com.example.shopapp.localization

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

enum class Language(val code: String) {
    ENGLISH("en"),
    VIETNAMESE("vi")
}

object LanguageManager {
    private const val PREFS_NAME = "LanguagePrefs"
    private const val KEY_LANGUAGE = "selected_language"
    private val _currentLanguage: MutableState<Language> = mutableStateOf(Language.ENGLISH)

    // Khởi tạo ngôn ngữ từ SharedPreferences khi ứng dụng bắt đầu
    fun initialize(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLanguageCode = sharedPreferences.getString(KEY_LANGUAGE, Language.ENGLISH.code)
        _currentLanguage.value = when (savedLanguageCode) {
            Language.VIETNAMESE.code -> Language.VIETNAMESE
            else -> Language.ENGLISH
        }
    }

    fun setLanguage(language: Language, context: Context? = null) {
        _currentLanguage.value = language
        // Lưu ngôn ngữ vào SharedPreferences
        context?.let {
            val sharedPreferences = it.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(KEY_LANGUAGE, language.code)
                apply()
            }
        }
    }

    fun getCurrentLanguage(): Language = _currentLanguage.value

    @Composable
    fun getString(resId: Int): String {
        _currentLanguage.value
        val context = LocalContext.current
        val locale = Locale(_currentLanguage.value.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.resources.getString(resId)
    }

    @Composable
    fun getString(resId: Int, vararg formatArgs: Any): String {
        _currentLanguage.value
        val context = LocalContext.current
        val locale = Locale(_currentLanguage.value.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.resources.getString(resId, *formatArgs)
    }
}