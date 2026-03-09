package com.example.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.models.DailyForecast

class ForecastAdapter(private val items: List<DailyForecast>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTempHigh: TextView = view.findViewById(R.id.tvTempHigh)
        val tvTempLow: TextView = view.findViewById(R.id.tvTempLow)
        val tvDesc: TextView = view.findViewById(R.id.tvDescItem)
        val ivIcon: ImageView = view.findViewById(R.id.ivForecastIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = items[position]

        // --- CHANGE: Check if it is the first item (Tomorrow) ---
        if (position == 0) {
            holder.tvDate.text = "Tomorrow"
        } else {
            holder.tvDate.text = item.date
        }

        holder.tvTempHigh.text = "${item.tempMax}°"
        holder.tvTempLow.text = "${item.tempMin}°"
        holder.tvDesc.text = item.description

        // --- ICON LOGIC ---
        val weatherIcon = when (item.iconReference) {
            "Clear" -> R.drawable.ic_sunny
            "Clouds" -> R.drawable.ic_cloudy
            "Rain", "Drizzle" -> R.drawable.ic_rainy
            "Thunderstorm" -> R.drawable.ic_thunderstorm
            "Snow" -> R.drawable.ic_snowy
            "Mist", "Fog", "Haze", "Smoke", "Dust", "Sand" -> R.drawable.ic_foggy
            else -> R.drawable.ic_weather_default
        }
        holder.ivIcon.setImageResource(weatherIcon)
    }

    override fun getItemCount() = items.size
}