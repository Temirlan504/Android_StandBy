package com.example.android_standby

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<WeatherResponse>
}
