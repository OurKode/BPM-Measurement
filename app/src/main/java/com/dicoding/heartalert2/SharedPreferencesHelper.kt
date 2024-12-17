package com.dicoding.heartalert2

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SharedPreferencesHelper(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("HeartAlertPrefs", Context.MODE_PRIVATE)

    // Save measurement result with date as key
    fun saveMeasurementResult(date: String, result: String) {
        if (result.contains(",")) { // Validate format before saving
            preferences.edit().putString("result_$date", result).apply()
        } else {
            Log.e("SharedPreferencesHelper", "Invalid result format: $result")
        }
    }
}