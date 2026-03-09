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
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

data class DailyForecast(
    val date: String,
    val tempMax: Int,
    val tempMin: Int,
    val description: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherApi: WeatherApi
    private val API_KEY = "8e98a19f9805c1b99f7828d30cfeeae8"

    private val cities = listOf(
        City("Erbil (Hewlêr)", 36.19, 44.01),
        City("Sulaymaniyah (Silêmanî)", 35.55, 45.43),
        City("Duhok (Dihok)", 36.86, 42.98),
        City("Halabja", 35.17, 46.00),
        City("Zakho", 37.14, 42.68),
        City("Akre (Akrê)", 36.74, 43.87),
        City("Ranya", 36.25, 44.91),
        City("Chamchamal", 35.53, 44.82),
        City("Kalar", 34.62, 45.31),
        City("Shaqlawa", 36.40, 44.32),
        City("Soran", 36.84, 44.54),
        City("Rawandiz", 36.61, 44.52),
        City("Qaladiza", 36.12, 45.12),
        City("Darbandikhan", 35.14, 45.98),
        City("Penjwen", 35.60, 46.10),
        City("Koy Sanjaq", 36.08, 44.63),
        City("Simele", 36.85, 42.84),
        City("Ankawa", 36.22, 44.02),
        City("Amedi (Amêdî)", 37.09, 43.49),
        City("Bamarni", 37.02, 43.40),
        City("Batifa", 37.13, 42.74),
        City("Sanandaj (Sine)", 35.31, 46.99),
        City("Kermanshah (Kirmaşan)", 34.31, 47.06),
        City("Marivan", 35.52, 46.17),
        City("Baneh", 35.99, 45.88),
        City("Saqqez", 36.24, 46.26),
        City("Qorveh", 35.16, 47.80),
        City("Kamyaran", 34.79, 46.93),
        City("Bijar", 35.86, 47.60),
        City("Divandarreh", 35.91, 47.01),
        City("Dehgolan", 35.18, 47.25),
        City("Diyarbakır (Amed)", 37.91, 40.23),
        City("Van (Wan)", 38.49, 43.38),
        City("Şırnak (Şirnex)", 37.51, 42.46),
        City("Hakkâri (Colemêrg)", 37.57, 43.74),
        City("Mardin (Mêrdîn)", 37.32, 40.72),
        City("Batman", 37.88, 41.12),
        City("Siirt (Sêrt)", 37.93, 41.94),
        City("Muş (Mûş)", 38.74, 41.50),
        City("Bitlis (Bedlîs)", 38.40, 42.10),
        City("Tunceli (Dêrsim)", 39.10, 39.55),
        City("Qamishli (Qamişlo)", 37.05, 41.22),
        City("Kobanî (Ayn al-Arab)", 36.89, 38.35),
        City("Afrin (Efrîn)", 36.51, 36.86),
        City("Hasakah (Heseke)", 36.50, 40.05)
    )

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
                Math.abs(hour - 15)
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