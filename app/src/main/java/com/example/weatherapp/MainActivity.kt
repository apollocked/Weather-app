package com.example.weatherapp
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Utilis.cities
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.models.DailyForecast
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


    data class City(val name: String, val lat: Double, val lon: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRetrofit()
        setupSpinner()
        setupRecyclerView()
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
                    apiKey = API_KEY
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

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: WeatherResponse) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = inputFormat.format(Date())

        // 1. Update Current Weather (Top Card)
        if (data.list.isNotEmpty()) {
            val current = data.list[0]

            binding.tvCityName.text = data.city.name
            binding.tvTemp.text = "${current.main.temp.toInt()}°C"

            val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            binding.tvDate.text = "Today, ${sdf.format(Date(current.dt * 1000))}"

            val desc = current.weather.firstOrNull()?.description ?: "N/A"
            binding.tvDesc.text = desc.replaceFirstChar { it.uppercase() }

            binding.tvHumidity.text = "Humidity: ${current.main.humidity}%"
            binding.tvWind.text = "Wind: ${current.wind.speed} m/s"

            // --- NEW: Calculate Today's High and Low ---
            // Filter the list to get only today's entries
            val todayItems = data.list.filter {
                inputFormat.format(Date(it.dt * 1000)) == todayStr
            }

            if (todayItems.isNotEmpty()) {
                val todayMax = todayItems.maxOf { it.main.tempMax }.toInt()
                val todayMin = todayItems.minOf { it.main.tempMin }.toInt()

                binding.tvTempMax.text = "H: $todayMax°"
                binding.tvTempMin.text = "L: $todayMin°"
            } else {
                // Fallback if for some reason no data for today exists
                binding.tvTempMax.text = "H: ${current.main.tempMax.toInt()}°"
                binding.tvTempMin.text = "L: ${current.main.tempMin.toInt()}°"
            }
        }

        // 2. Process Forecast List
        val dailyForecasts = processForecastData(data.list)
        val adapter = ForecastAdapter(dailyForecasts)
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
            // Skip today, as it's shown in the top card
            if (dateStr == todayStr) continue

            val tempMax = itemsInDay.maxOf { it.main.tempMax }.toInt()
            val tempMin = itemsInDay.minOf { it.main.tempMin }.toInt()

            val midDayItem = itemsInDay.minByOrNull {
                val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(it.dt * 1000)).toInt()
                abs(hour - 15)
            }

            val description = midDayItem?.weather?.firstOrNull()?.description ?: "N/A"
            val displayDate = outputFormat.format(Date(itemsInDay.first().dt * 1000))

            dailyList.add(
                DailyForecast(
                    date = displayDate,
                    tempMax = tempMax,
                    tempMin = tempMin,
                    description = description.replaceFirstChar { it.uppercase() }
                )
            )
        }

        return dailyList.take(5)
    }
}