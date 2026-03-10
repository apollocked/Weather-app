package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.weatherapp.Utilis.cities
import com.example.weatherapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)

        // Setup Spinner
        val cityNames = cities.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityNames)
        binding.spinnerCitiesSettings.adapter = adapter

        // Set current selection
        val savedCity = sharedPref.getString("selectedCity", cities[0].name)
        val cityIndex = cities.indexOfFirst { it.name == savedCity }
        if (cityIndex != -1) {
            binding.spinnerCitiesSettings.setSelection(cityIndex)
        }

        // Setup Units
        val savedUnit = sharedPref.getString("tempUnit", "metric")
        if (savedUnit == "imperial") {
            binding.rbFahrenheit.isChecked = true
        } else {
            binding.rbCelsius.isChecked = true
        }

        // Setup Dark Mode
        val isDarkMode = sharedPref.getBoolean("darkMode", false)
        binding.switchDarkMode.isChecked = isDarkMode

        binding.btnSave.setOnClickListener {
            val selectedCityName = cities[binding.spinnerCitiesSettings.selectedItemPosition].name
            val selectedUnit = if (binding.rbFahrenheit.isChecked) "imperial" else "metric"
            val darkMode = binding.switchDarkMode.isChecked

            with(sharedPref.edit()) {
                putString("selectedCity", selectedCityName)
                putString("tempUnit", selectedUnit)
                putBoolean("darkMode", darkMode)
                apply()
            }

            // Apply Dark Mode immediately and globally
            if (darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Return to MainActivity and refresh
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}