package com.example.weatherapp
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Utilis.cities
import com.example.weatherapp.Utilis.City
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.DailyForecast
import com.example.weatherapp.adaptor.ForecastAdapter
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherApi: WeatherApi
    private val API_KEY = "8e98a19f9805c1b99f7828d30cfeeae8"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply Dark Mode before super.onCreate
        val sharedPref = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("darkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRetrofit()
        setupRecyclerView()

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadSavedSettingsAndFetch()
    }

    private fun loadSavedSettingsAndFetch() {
        val sharedPref = getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val savedCityName = sharedPref.getString("selectedCity", cities[0].name)
        val savedUnit = sharedPref.getString("tempUnit", "metric")

        val city = cities.find { it.name == savedCityName } ?: cities[0]
        fetchWeather(city, savedUnit ?: "metric")
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApi::class.java)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewForecast.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchWeather(city: City, unit: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = weatherApi.getWeather(
                    lat = city.lat,
                    lon = city.lon,
                    apiKey = API_KEY,
                    units = unit
                )
                updateUI(response, unit)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: WeatherResponse, unit: String) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = inputFormat.format(Date())
        val unitSymbol = if (unit == "imperial") "°F" else "°C"

        // 1. Update Current Weather (Top Card)
        if (data.list.isNotEmpty()) {
            val current = data.list[0]
            val weatherIcon = when (current.weather.firstOrNull()?.main) {
                "Clear" -> R.drawable.ic_sunny
                "Clouds" -> R.drawable.ic_cloudy
                "Rain", "Drizzle" -> R.drawable.ic_rainy
                "Thunderstorm" -> R.drawable.ic_thunderstorm
                "Snow" -> R.drawable.ic_snowy
                "Mist", "Fog", "Haze", "Smoke", "Dust", "Sand" -> R.drawable.ic_foggy
                else -> R.drawable.ic_weather_default
            }

            binding.ivWeatherIcon.setImageResource(weatherIcon)
            binding.tvCityName.text = data.city.name
            binding.tvTemp.text = "${current.main.temp.toInt()}${unitSymbol}"

            val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            binding.tvDate.text = "Today, ${sdf.format(Date(current.dt * 1000))}"

            val desc = current.weather.firstOrNull()?.description ?: "N/A"
            binding.tvDesc.text = desc.replaceFirstChar { it.uppercase() }

            binding.tvHumidity.text = "${current.main.humidity}%"
            binding.tvWind.text = "${current.wind.speed} ${if(unit == "imperial") "mph" else "m/s"}"

            val todayItems = data.list.filter {
                inputFormat.format(Date(it.dt * 1000)) == todayStr
            }

            if (todayItems.isNotEmpty()) {
                val todayMax = todayItems.maxOf { it.main.tempMax }.toInt()
                val todayMin = todayItems.minOf { it.main.tempMin }.toInt()

                binding.tvTempMax.text = "H: $todayMax°"
                binding.tvTempMin.text = "L: $todayMin°"
            } else {
                binding.tvTempMax.text = "H: ${current.main.tempMax.toInt()}°"
                binding.tvTempMin.text = "L: ${current.main.tempMin.toInt()}°"
            }
        }

        // 2. Process Forecast List
        val dailyForecasts = processForecastData(data.list)
        val adapter = ForecastAdapter(dailyForecasts, unitSymbol)
        binding.recyclerViewForecast.adapter = adapter
    }


    private fun processForecastData(list: List<ForecastItem>): List<DailyForecast> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val todayStr = inputFormat.format(Date())

        val groupedByDay = list.groupBy { item ->
            inputFormat.format(Date(item.dt * 1000))
        }

        val dailyList = mutableListOf<DailyForecast>()

        for ((dateStr, itemsInDay) in groupedByDay) {
            if (dateStr == todayStr) continue

            val tempMax = itemsInDay.maxOf { it.main.tempMax }.toInt()
            val tempMin = itemsInDay.minOf { it.main.tempMin }.toInt()

            val midDayItem = itemsInDay.minByOrNull {
                val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(it.dt * 1000)).toInt()
                abs(hour - 15)
            }

            val mainWeather = midDayItem?.weather?.firstOrNull()?.main ?: "Clear"
            val description = midDayItem?.weather?.firstOrNull()?.description ?: "N/A"
            val displayDate = outputFormat.format(Date(itemsInDay.first().dt * 1000))

            dailyList.add(
                DailyForecast(
                    date = displayDate,
                    tempMax = tempMax,
                    tempMin = tempMin,
                    description = description.replaceFirstChar { it.uppercase() },
                    iconReference = mainWeather
                )
            )
        }

        return dailyList.take(5)
    }
}