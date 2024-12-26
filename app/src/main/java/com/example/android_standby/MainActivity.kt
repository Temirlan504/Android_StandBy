package com.example.android_standby

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var timeTextView: TextView
    private lateinit var dayDateTextView: TextView
    private lateinit var weatherTextView: TextView

    private val apiKey = "02d2bd06c796ffef517730a9198cd2fc"

    // Handler to schedule and execute periodic tasks on the main thread
    private val handler = Handler(Looper.getMainLooper())

    // Runnable to update the time every second
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            // Get the current time in the format "hh:mm"
            val currentTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            timeTextView.text = currentTime // Update the time TextView with the current time

            // Schedule the runnable to run again after 1 second
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    // Runnable to update the date at midnight
    private val updateDateRunnable = object : Runnable {
        override fun run() {
            updateDate()
            handler.postDelayed(this, calculateMidnightDelay()) // Schedule for the next midnight
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display for immersive UI
        setContentView(R.layout.activity_main) // Set the layout for the activity

        // Connect backend view variables with the frontend views
        timeTextView = findViewById(R.id.timeTextView)
        dayDateTextView = findViewById(R.id.dayDateTextView)
        weatherTextView = findViewById(R.id.weatherTextView)

        // Fetch weather for a city
        fetchWeather() // Replace with your preferred city

        // Display the initial date and start updating it
        updateDate()
        handler.post(updateDateRunnable)

        // Start updating the time
        handler.post(updateTimeRunnable)

        // Ensure app's UI does not overlap with the status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Prevent memory leaks by stopping the handlers
        handler.removeCallbacks(updateTimeRunnable)
        handler.removeCallbacks(updateDateRunnable)
    }

    // Weather API call
    private fun fetchWeather() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherApi = retrofit.create(WeatherAPI::class.java)

        // Replace with your lat, lon, and API key
        val call = weatherApi.getWeather("Mississauga", "02d2bd06c796ffef517730a9198cd2fc")

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val temperature = kotlin.math.round(it.main.temp).toInt()
                        val iconCode = it.weather[0].icon // Get the weather icon code

                        // Construct the icon URL
                        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

                        // Update UI
                        findViewById<TextView>(R.id.weatherTextView).text = "$temperature°C"

                        // Load the weather icon using Glide
                        Glide.with(this@MainActivity)
                            .load(iconUrl)
                            .into(findViewById<ImageView>(R.id.weatherIconImageView))
                    }
                }
            }

            // Error fetching weather API (Error 1)
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                findViewById<TextView>(R.id.weatherTextView).text = "Error 1"
            }
        })
    }

    // Update the current date and display it in the TextView
    private fun updateDate() {
        val dateFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        dayDateTextView.text = currentDate
    }

    // Calculate the delay until the next midnight
    private fun calculateMidnightDelay(): Long {
        val now = Calendar.getInstance()
        // Create a Calendar object representing the midnight
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1) // Schedule for the next day
        }
        return midnight.timeInMillis - now.timeInMillis
    }
}
