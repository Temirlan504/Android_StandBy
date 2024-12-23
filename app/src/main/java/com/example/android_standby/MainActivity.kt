package com.example.android_standby

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var timeTextView: TextView
    private lateinit var dayDateTextView: TextView
    private val handler = Handler(Looper.getMainLooper())

    // Runnable to update the time every second
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            timeTextView.text = currentTime
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        timeTextView = findViewById(R.id.timeTextView)
        dayDateTextView = findViewById(R.id.dayDateTextView)

        // Display the initial date and start updating it
        updateDate()
        handler.post(updateDateRunnable)

        // Start updating the time
        handler.post(updateTimeRunnable)

        // Handle system bar insets
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

    // Update the current date and display it in the TextView
    private fun updateDate() {
        val dateFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        dayDateTextView.text = currentDate
    }

    // Calculate the delay until the next midnight
    private fun calculateMidnightDelay(): Long {
        val now = Calendar.getInstance()
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
