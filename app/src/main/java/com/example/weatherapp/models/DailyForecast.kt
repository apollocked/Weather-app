package com.example.weatherapp.models

data class DailyForecast(
    val date: String,
    val tempMax: Int,
    val tempMin: Int,
    val description: String,
    val iconReference: String
)