package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.WeatherApi
import com.example.weatherapp.models.WeatherResponse
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherApi: WeatherApi

    // Replace with your actual key
    private val apiKey = "8e98a19f9805c1b99f7828d30cfeeae8"

    // Data class to hold city info
    data class City(val name: String, val lat: Double, val lon: Double)

    // List of Kurdistan Region Cities
    private val cities = listOf(
        City("Erbil (Hewlêr)", 36.19, 44.01),
        City("Sulaymaniyah (Silêmanî)", 35.55, 45.43),
        City("Duhok (Dihok)", 36.86, 42.98),
        City("Kirkuk (Kerkûk)", 35.46, 44.38),
        City("Halabja", 35.17, 46.00)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRetrofit()
        setupSpinner()
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApi::class.java)
    }

    private fun setupSpinner() {
        val cityNames = cities.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityNames)
        binding.spinnerCities.adapter = adapter

        binding.spinnerCities.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCity = cities[position]
                fetchWeather(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchWeather(city: City) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = weatherApi.getWeather(
                    lat = city.lat,
                    lon = city.lon,
                    apiKey = apiKey
                )
                updateUI(response)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateUI(data: WeatherResponse) {
        binding.apply {
            tvCityName.text = data.name
            tvTemp.text = "${data.main.temp.toInt()}°C"

            // Capitalize first letter of description
            val desc = data.weather[0].description
            tvDesc.text = desc.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            tvHumidity.text = "Humidity: ${data.main.humidity}%"
            tvWind.text = "Wind: ${data.wind.speed} m/s"
        }
    }
}