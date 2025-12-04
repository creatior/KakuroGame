package com.example.kakuro

import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val spinner: Spinner = findViewById(R.id.spinnerLanguage)

        val languages = listOf("Русский" to "ru", "English" to "en")
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, languages.map { it.first })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("app_language", "ru") ?: "ru"

        val selectedIndex = languages.indexOfFirst { it.second == savedLang }
        if (selectedIndex >= 0) spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val langCode = languages[position].second
                if (langCode != savedLang) {
                    setAppLocale(langCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", language).apply()

        recreate()
    }
}