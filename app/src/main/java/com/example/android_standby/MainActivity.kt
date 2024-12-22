package com.example.android_standby

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*
import androidx.work.*
import java.util.concurrent.TimeUnit

class UpdateDateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val dateFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Save the date in SharedPreferences
        val sharedPreferences = applicationContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("current_date", currentDate).apply()

        return Result.success()
    }
}


class MainActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())

    // Runnable to update time every minute
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            // Get current time in 12-hour format without AM/PM
            val currentTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
            timeTextView.text = currentTime
            handler.postDelayed(this, 1000) // Update every minute
        }
    }

    // In your Activity or Application class
    private fun scheduleDateUpdate() {
        val workRequest = PeriodicWorkRequestBuilder<UpdateDateWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateMidnightDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "UpdateDateWork",
            ExistingPeriodicWorkPolicy.UPDATE, // Use UPDATE instead of REPLACE
            workRequest
        )
    }

    private fun calculateMidnightDelay(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1) // Schedule for the next midnight
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        timeTextView = findViewById(R.id.timeTextView)
        val dayDateTextView = findViewById<TextView>(R.id.dayDateTextView)

        // Get current date and format it to Day (EEE) and Day of the month (dd)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)

        // Set the formatted date to the TextView
        dayDateTextView.text = dateString

        // Start the runnable to update the time
        handler.post(updateTimeRunnable)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop updating time when activity is destroyed to prevent memory leaks
        handler.removeCallbacks(updateTimeRunnable)
    }
}
