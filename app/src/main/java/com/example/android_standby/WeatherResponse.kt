package com.example.android_standby

data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val sys: Sys,
    val name: String
)

data class Coord(val lon: Double, val lat: Double)
data class Weather(val main: String, val description: String, val icon: String)
data class Main(val temp: Double, val feels_like: Double, val humidity: Int)
data class Wind(val speed: Double, val deg: Int)
data class Sys(val country: String, val sunrise: Long, val sunset: Long)
