package com.example.testapp2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request.Method.POST
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val dbHelper = DatabaseHelper(applicationContext)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM questionnaire WHERE synced = 0", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                val answer1 = cursor.getString(cursor.getColumnIndexOrThrow("answer1"))
                val answer2a = cursor.getInt(cursor.getColumnIndexOrThrow("answer2a"))
                val answer2b = cursor.getInt(cursor.getColumnIndexOrThrow("answer2b"))
                val answer2c = cursor.getInt(cursor.getColumnIndexOrThrow("answer2c"))
                val answer3 = cursor.getString(cursor.getColumnIndexOrThrow("answer3"))
                val answer4 = cursor.getString(cursor.getColumnIndexOrThrow("answer4"))
                val answer5 = cursor.getString(cursor.getColumnIndexOrThrow("answer5"))
                val timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"))
                val displayDate = cursor.getString(cursor.getColumnIndexOrThrow("displayDate"))

                sendResponsesToGoogleWebApp(
                    id,
                    username,
                    answer1,
                    answer2a,
                    answer2b,
                    answer2c,
                    answer3,
                    answer4,
                    answer5,
                    timestamp,
                    displayDate
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return Result.success()
    }

    private fun sendResponsesToGoogleWebApp(
        id: Int,
        username: String?,
        answer1: String,
        answer2a: Int,
        answer2b: Int,
        answer2c: Int,
        answer3: String,
        answer4: String,
        answer5: String,
        timestamp: String,
        displayDate: String
    ) {
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val url = "https://script.google.com/macros/s/AKfycbysPc5mIxpX3UVGV7RUPwa6L0EiTVbEasDkqkUDKSYUZh9oA7B-CP-rICeJf2KBF_SR/exec"
        val stringRequest = object : StringRequest(
            POST, url,
            Response.Listener { response ->
                try {
                    Log.d("SyncWorker", "Response from Google Web App: $response")
                    updateSyncedStatus(id)
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error in response listener: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("SyncWorker", "Error in StringRequest: ${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                return hashMapOf(
                    "action" to "addItem",
                    "username" to username.orEmpty(),
                    "answer1" to answer1,
                    "answer2a" to answer2a.toString(),
                    "answer2b" to answer2b.toString(),
                    "answer2c" to answer2c.toString(),
                    "answer3" to answer3,
                    "answer4" to answer4,
                    "answer5" to answer5,
                    "timestamp" to timestamp,
                    "displayDate" to displayDate
                )
            }
        }

        queue.add(stringRequest)
    }

    private fun updateSyncedStatus(id: Int) {
        try {
            val dbHelper = DatabaseHelper(applicationContext)
            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put("synced", 1)
            }

            db.update("questionnaire", values, "_id=?", arrayOf(id.toString()))
            db.close()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error in updateSyncedStatus: ${e.message}")
        }
    }
}