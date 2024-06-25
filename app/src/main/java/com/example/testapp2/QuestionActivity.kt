package com.example.testapp2


import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class QuestionActivity : AppCompatActivity() {

    // UI elements
    private lateinit var currentDateTimeTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var answer2aValue: TextView
    private lateinit var answer2bValue: TextView
    private lateinit var answer2cValue: TextView
    private lateinit var dayCountTextView: TextView

    // Constants for database columns
    private val TABLE_NAME = "questionnaire"
    private val COLUMN_USERNAME = "username"
    private val COLUMN_ANSWER1 = "answer1"
    private val COLUMN_ANSWER2A = "answer2a"
    private val COLUMN_ANSWER2B = "answer2b"
    private val COLUMN_ANSWER2C = "answer2c"
    private val COLUMN_ANSWER3 = "answer3"
    private val COLUMN_ANSWER4 = "answer4"
    private val COLUMN_ANSWER5 = "answer5"
    private val COLUMN_TIMESTAMP = "timestamp"
    private val COLUMN_SYNCED = "synced"
    private val COLUMN_DISPLAY_DATE = "displayDate"

    // Flag to prevent multiple submissions
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        try {
            // Initialize UI elements
            currentDateTimeTextView = findViewById(R.id.currentDateTime)
            submitButton = findViewById(R.id.submitButton)
            sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            answer2aValue = findViewById(R.id.answer2aValue)
            answer2bValue = findViewById(R.id.answer2bValue)
            answer2cValue = findViewById(R.id.answer2cValue)

            // Initialize SeekBars and their values
            val answer2a = findViewById<SeekBar>(R.id.answer2a)
            val answer2b = findViewById<SeekBar>(R.id.answer2b)
            val answer2c = findViewById<SeekBar>(R.id.answer2c)
            setupSeekBar(answer2a, answer2aValue)
            setupSeekBar(answer2b, answer2bValue)
            setupSeekBar(answer2c, answer2cValue)

            // Display the current date
            val currentDate = SimpleDateFormat("EEEE, yyyy-MM-dd", Locale.getDefault()).format(Date())
            currentDateTimeTextView.text = currentDate


            // Get username from shared preferences
            val username = sharedPreferences.getString("username", null)

            // Check if the user has already submitted today
            val lastSubmissionDate = sharedPreferences.getString("lastSubmissionDate", "")
            if (currentDate == lastSubmissionDate) {
                disableOptions()
                submitButton.isEnabled = false
                Toast.makeText(this, "You have already submitted today", Toast.LENGTH_LONG).show()
            } else {
                submitButton.isEnabled = true
            }

            // Initialize the day count TextView
            dayCountTextView = findViewById(R.id.dayCountTextView)


            // Check if it's the first submission
            val isFirstSubmission = sharedPreferences.getBoolean("isFirstSubmission", true)
            if (isFirstSubmission) {
                val firstSubmissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                with(sharedPreferences.edit()) {
                    putString("firstSubmissionDate", firstSubmissionDate)
                    putBoolean("isFirstSubmission", false)
                    apply()
                }
            }


            // Set up the submit button click listener
            submitButton.setOnClickListener {
                try {
                    if (isSubmitting) return@setOnClickListener // Prevent multiple submissions

                    isSubmitting = true // Set the flag to true when starting the submission

                    // Get selected answers from UI elements
                    val selectedAnswer1 = findViewById<RadioGroup>(R.id.answers1).checkedRadioButtonId
                    val answer2aProgress = answer2a.progress
                    val answer2bProgress = answer2b.progress
                    val answer2cProgress = answer2c.progress
                    val selectedAnswer3 = findViewById<RadioGroup>(R.id.answers3).checkedRadioButtonId
                    val selectedAnswer4 = findViewById<RadioGroup>(R.id.answers4).checkedRadioButtonId
                    val selectedAnswer5 = findViewById<RadioGroup>(R.id.answers5).checkedRadioButtonId


                    // Check if all questions are answered
                    if (selectedAnswer1 != -1 && selectedAnswer3 != -1 && selectedAnswer4 != -1 && selectedAnswer5 != -1) {
                        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        currentDateTimeTextView = findViewById(R.id.currentDateTime)
                        currentDateTimeTextView.text = currentDateTime
                        val displayDate = currentDateTimeTextView.text.toString() // Capture the displayed date
                        with(sharedPreferences.edit()) {
                            putString("lastSubmissionDate", currentDate)
                            apply()
                        }
                        Toast.makeText(this, "Thank you for your submission", Toast.LENGTH_LONG).show()
                        disableOptions()
                        submitButton.isEnabled = false

                        // Get answers as strings
                        val answer1 = findViewById<RadioButton>(selectedAnswer1)?.text?.toString()
                        val answer3 = findViewById<RadioButton>(selectedAnswer3)?.text?.toString()
                        val answer4 = findViewById<RadioButton>(selectedAnswer4)?.text?.toString()
                        val answer5 = findViewById<RadioButton>(selectedAnswer5)?.text?.toString()
                        Log.d("QuestionActivity", "Network available: ${isNetworkAvailable(this)}")

                        // Check if answers are not null
                        if (answer1 != null && answer3 != null && answer4 != null && answer5 != null) {
                            if (isNetworkAvailable(this)) {
                                // Send responses to Google Web App if network is available
                                sendResponsesToGoogleWebApp(
                                    username,
                                    answer1,
                                    answer2aProgress,
                                    answer2bProgress,
                                    answer2cProgress,
                                    answer3,
                                    answer4,
                                    answer5,
                                    currentDateTime,
                                    displayDate
                                )
                            } else {
                                // Save responses locally if network is not available
                                saveResponsesLocally(
                                    username,
                                    answer1,
                                    answer2aProgress,
                                    answer2bProgress,
                                    answer2cProgress,
                                    answer3,
                                    answer4,
                                    answer5,
                                    currentDateTime,
                                    displayDate
                                )
                                Toast.makeText(
                                    this,
                                    "Responses saved locally. Please connect to the internet.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
                            isSubmitting = false // Reset the flag on error
                        }
                    } else {
                        Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
                        isSubmitting = false // Reset the flag on error
                    }
                } catch (e: Exception) {
                    Log.e("QuestionActivity", "Error on submit: ${e.message}")
                    Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_LONG).show()
                    isSubmitting = false // Reset the flag on error
                }
            }
            // Schedule periodic sync task
            scheduleSyncTask()
            updateDayCount()
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "An error occurred. Please restart the app.", Toast.LENGTH_LONG).show()
        }
    }
    // Update the day count based on the first submission date
    private fun updateDayCount() {
        try {
            val firstSubmissionDateStr = sharedPreferences.getString("firstSubmissionDate", null)
            if (firstSubmissionDateStr != null) {
                val firstSubmissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(firstSubmissionDateStr)
                val currentDate = Date()

                val diffInMillies = currentDate.time - firstSubmissionDate!!.time
                val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1

                dayCountTextView.text = "Day $diffInDays of 30"
            } else {
                dayCountTextView.text = "Day 1 of 30"
            }
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in updateDayCount: ${e.message}")
        }
    }
    // Schedule a periodic sync task using WorkManager
    private fun scheduleSyncTask() {
        try {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "sync_task",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in scheduleSyncTask: ${e.message}")
        }
    }
    // Send responses to Google Web App using Volley
    private fun sendResponsesToGoogleWebApp(
        username: String?,
        answer1: String,
        answer2a: Int,
        answer2b: Int,
        answer2c: Int,
        answer3: String,
        answer4: String,
        answer5: String,
        currentDateTime: String,
        displayDate: String
    ) {
        try {
            val queue: RequestQueue = Volley.newRequestQueue(this)
            val url = "https://script.google.com/macros/s/AKfycbysPc5mIxpX3UVGV7RUPwa6L0EiTVbEasDkqkUDKSYUZh9oA7B-CP-rICeJf2KBF_SR/exec"

            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    try {
                        Log.d("QuestionActivity", "Response from Google Web App: $response")
                        saveResponsesLocally(
                            username,
                            answer1,
                            answer2a,
                            answer2b,
                            answer2c,
                            answer3,
                            answer4,
                            answer5,
                            currentDateTime,
                            displayDate,
                            synced = true
                        )
                    } catch (e: Exception) {
                        Log.e("QuestionActivity", "Error in response listener: ${e.message}")
                    }
                },
                Response.ErrorListener { error ->
                    Log.e("QuestionActivity", "Error in StringRequest: ${error.message}")
                    Toast.makeText(this, "Failed to sync. Please try again later.", Toast.LENGTH_LONG).show()
                    saveResponsesLocally(
                        username,
                        answer1,
                        answer2a,
                        answer2b,
                        answer2c,
                        answer3,
                        answer4,
                        answer5,
                        currentDateTime,
                        displayDate
                    )
                }) {
                override fun getParams(): Map<String, String> {
                    val params= hashMapOf(
                        "action" to "addItem",
                        "username" to username.orEmpty(),
                        "answer1" to answer1,
                        "answer2a" to answer2a.toString(),
                        "answer2b" to answer2b.toString(),
                        "answer2c" to answer2c.toString(),
                        "answer3" to answer3,
                        "answer4" to answer4,
                        "answer5" to answer5,
                        "timestamp" to currentDateTime,
                        "displayDate" to displayDate
                    )
                    Log.d("QuestionActivity", "Params: $params")
                    return params
                }

                override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                    return try {
                        val responseStr = String(response?.data ?: ByteArray(0))
                        Log.d("QuestionActivity", "Raw response: $responseStr")
                        super.parseNetworkResponse(response)
                    } catch (e: Exception) {
                        Log.e("QuestionActivity", "Error in parseNetworkResponse: ${e.message}")
                        super.parseNetworkResponse(response)
                    }
                }
            }

            queue.add(stringRequest)
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in sendResponsesToGoogleWebApp: ${e.message}")
        }
    }

    // Save responses locally using the database helper
    private fun saveResponsesLocally(
        username: String?,
        answer1: String,
        answer2a: Int,
        answer2b: Int,
        answer2c: Int,
        answer3: String,
        answer4: String,
        answer5: String,
        currentDateTime: String,
        displayDate: String,
        synced: Boolean = false
    ) {
        try {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_USERNAME, username)
                put(COLUMN_ANSWER1, answer1)
                put(COLUMN_ANSWER2A, answer2a)
                put(COLUMN_ANSWER2B, answer2b)
                put(COLUMN_ANSWER2C, answer2c)
                put(COLUMN_ANSWER3, answer3)
                put(COLUMN_ANSWER4, answer4)
                put(COLUMN_ANSWER5, answer5)
                put(COLUMN_TIMESTAMP, currentDateTime)
                put(COLUMN_DISPLAY_DATE, displayDate)
                put(COLUMN_SYNCED, if (synced) 1 else 0)
            }

            db.insert(TABLE_NAME, null, values)
            db.close()
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in saveResponsesLocally: ${e.message}")
        }
    }

    private fun setupSeekBar(seekBar: SeekBar, textView: TextView) {
        try {
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    textView.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in setupSeekBar: ${e.message}")
        }
    }
    // Disable the options after submission
    private fun disableOptions() {
        try {
            val answers1 = findViewById<RadioGroup>(R.id.answers1)
            val answer2a = findViewById<SeekBar>(R.id.answer2a)
            val answer2b = findViewById<SeekBar>(R.id.answer2b)
            val answer2c = findViewById<SeekBar>(R.id.answer2c)
            val answers3 = findViewById<RadioGroup>(R.id.answers3)
            val answers4 = findViewById<RadioGroup>(R.id.answers4)
            val answers5 = findViewById<RadioGroup>(R.id.answers5)

            for (i in 0 until answers1.childCount) {
                answers1.getChildAt(i).isEnabled = false
            }
            answer2a.isEnabled = false
            answer2b.isEnabled = false
            answer2c.isEnabled = false
            for (i in 0 until answers3.childCount) {
                answers3.getChildAt(i).isEnabled = false
            }
            for (i in 0 until answers4.childCount) {
                answers4.getChildAt(i).isEnabled = false
            }
            for (i in 0 until answers5.childCount) {
                answers5.getChildAt(i).isEnabled = false
            }
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in disableOptions: ${e.message}")
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.e("QuestionActivity", "Error in isNetworkAvailable: ${e.message}")
            return false
        }
    }
}

