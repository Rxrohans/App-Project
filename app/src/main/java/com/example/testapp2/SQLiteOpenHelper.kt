package com.example.testapp2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "Questionnaire.db"
private const val DATABASE_VERSION = 1

const val TABLE_NAME = "questionnaire"
const val COLUMN_ID = "_id"
const val COLUMN_USERNAME = "username"
const val COLUMN_ANSWER1 = "answer1"
const val COLUMN_ANSWER2A = "answer2a"
const val COLUMN_ANSWER2B = "answer2b"
const val COLUMN_ANSWER2C = "answer2c"
const val COLUMN_ANSWER3 = "answer3"
const val COLUMN_ANSWER4 = "answer4"
const val COLUMN_ANSWER5 = "answer5"
const val COLUMN_TIMESTAMP = "timestamp"
const val COLUMN_DISPLAY_DATE = "displayDate"
const val COLUMN_SYNCED = "synced"

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE $TABLE_NAME (" +
            "$COLUMN_ID INTEGER PRIMARY KEY," +
            "$COLUMN_USERNAME TEXT," +
            "$COLUMN_ANSWER1 TEXT," +
            "$COLUMN_ANSWER2A INTEGER," +
            "$COLUMN_ANSWER2B INTEGER," +
            "$COLUMN_ANSWER2C INTEGER," +
            "$COLUMN_ANSWER3 TEXT," +
            "$COLUMN_ANSWER4 TEXT," +
            "$COLUMN_ANSWER5 TEXT," +
            "$COLUMN_TIMESTAMP TEXT," +
            "$COLUMN_DISPLAY_DATE TEXT," +
            "$COLUMN_SYNCED INTEGER DEFAULT 0)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}
