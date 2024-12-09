package com.dicoding.heartalert2

import android.content.Context

class SharedPreferencesHelper(private val context: Context) {

    private val preferences = context.getSharedPreferences("HeartAlertPrefs", Context.MODE_PRIVATE)

    fun saveString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun getString(key: String): String? {
        return preferences.getString(key, null)
    }

    fun getInt(key: String): Int {
        return preferences.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }
}