package com.example.testapp2

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.AlarmManager
import android.app.PendingIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // Declare UI elements and variables
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private val notificationPermissionCode = 1001
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        // Initialize notification helper
        notificationHelper = NotificationHelper(this)

        // Check if the user is already logged in
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("logged_in", false)) {
            navigateToQuestionsActivity()
        }

        // Check and store the first launch date
        val firstLaunchDate = sharedPreferences.getString("first_launch_date", null)
        if (firstLaunchDate == null) {
            // First time launch, set the first launch date
            val editor = sharedPreferences.edit()
            val currentDate = getCurrentDate() // Get the current date dynamically
            editor.putString("first_launch_date", currentDate)
            editor.apply()
            Log.d("MainActivity", "First launch, setting first launch date: $currentDate")
        } else {
            // Calculate days since first launch
            val daysSinceFirstLaunch = calculateDaysSinceFirstLaunch(firstLaunchDate)
            Log.d("MainActivity", "Days since first launch: $daysSinceFirstLaunch")
            Toast.makeText(this, "Day $daysSinceFirstLaunch", Toast.LENGTH_SHORT).show()
        }

        // Request notification permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                notificationPermissionCode
            )
        }

        // Set click listener for login button
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            Log.d("MainActivity", "Login button clicked")

            // Check if username and password are not empty
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Validate username and password
                if (isValidUser(username, password)) {
                    // Save login state and username in shared preferences
                    with(sharedPreferences.edit()) {
                        putBoolean("logged_in", true)
                        putString("username", username)
                        apply()
                    }
                    navigateToQuestionsActivity()
                    checkAndScheduleNotifications() // Check permission and schedule notifications
                } else {
                    Log.d("MainActivity", "Invalid username or password")
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("MainActivity", "Username or password is empty")
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Validate username and password
    private fun isValidUser(username: String, password: String): Boolean {
        val userPrefix = "ID24"
        if (username.startsWith(userPrefix) && username.length == 7) {
            val userNumber = username.substring(4).toIntOrNull()
            if (userNumber != null && userNumber in 1..100) {
                val expectedPassword = "$username$$"
                return password == expectedPassword
            }
        }
        return false
    }

    // Navigate to QuestionActivity
    private fun navigateToQuestionsActivity() {
        val intent = Intent(this, QuestionActivity::class.java)
        startActivity(intent)
        finish()
        Log.d("MainActivity", "Navigating to QuestionActivity")
    }

    // Check notification permission and schedule notifications
    private fun checkAndScheduleNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Notification permission is required to send notifications",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            scheduleDailyNotifications() // Schedule the notifications after login
        }
    }

    // Schedule daily notifications at specific times
    private fun scheduleDailyNotifications() {
        val times = listOf(17 to 0, 19 to 0, 21 to 0) // 5 PM, 7 PM, 9 PM
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for ((hour, minute) in times) {
            val intent = Intent(this, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                hour * 100 + minute,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            // Set inexact repeating alarm
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == notificationPermissionCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, schedule notifications
                scheduleDailyNotifications()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission is required to send notifications",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Helper function to get the current date in a specific format
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Helper function to calculate days since first launch
    private fun calculateDaysSinceFirstLaunch(firstLaunchDate: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val firstDate: Date = sdf.parse(firstLaunchDate) ?: return 0
        val currentDate: Date = sdf.parse(getCurrentDate()) ?: return 0

        val diffInMillis: Long = currentDate.time - firstDate.time
        return TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1 // Add 1 to count the first day as "Day 1"
    }
}