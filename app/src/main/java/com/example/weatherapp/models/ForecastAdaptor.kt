package com.example.weatherapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R

class ForecastAdapter(private val items: List<DailyForecast>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTempHigh: TextView = view.findViewById(R.id.tvTempHigh)
        val tvTempLow: TextView = view.findViewById(R.id.tvTempLow)
        val tvDescItem: TextView = view.findViewById(R.id.tvDescItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = items[position]

        holder.tvDate.text = item.date

        // Show High and Low
        holder.tvTempHigh.text = "↑ ${item.tempMax}°"
        holder.tvTempLow.text = "↓ ${item.tempMin}°"

        holder.tvDescItem.text = item.description
    }

    override fun getItemCount(): Int = items.size
}