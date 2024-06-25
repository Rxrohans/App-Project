package com.example.testapp2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import java.util.Locale

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentDate = SimpleDateFormat("EEEE, yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())
        val lastSubmissionDate = sharedPreferences.getString("lastSubmissionDate", "")

        if (currentDate != lastSubmissionDate) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.sendNotification()
        }
    }
}
