package com.example.weatherapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("darkMode", false)
        
        // Use a more robust way to set the mode
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}