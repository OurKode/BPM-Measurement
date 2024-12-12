package com.dicoding.heartalert2

import android.content.Context
import android.gesture.Prediction
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_inputs")

class AppDataStore(private val context: Context) {

    companion object {
        val GENDER_KEY = intPreferencesKey("gender")
        val AGE_KEY = intPreferencesKey("age")
        val CHEST_PAIN_LEVEL_KEY = intPreferencesKey("chestPainLevel")
        val RESTING_BPM_KEY = intPreferencesKey("restingBpm")
        val ACTIVITY_BPM_KEY = intPreferencesKey("activityBpm")
        val CHEST_TIGHTNESS_KEY = intPreferencesKey("chestTightness")
        val DATE_KEY = stringPreferencesKey("date")
        val PREDICTION_RESULT_KEY = stringPreferencesKey("predictionResult") // Tambahkan key untuk prediksi
    }

    private var lastSavedUserInput: UserInput? = null // cache data terakhir

    suspend fun saveUserInput(
        gender: Int, age: Int, chestPainLevel: Int,
        restingBpm: Int, activityBpm: Int, chestTightness: Int, date: String
    ) {
        val newUserInput = UserInput(
            gender = gender,
            age = age,
            chestPainLevel = chestPainLevel,
            restingBpm = restingBpm,
            activityBpm = activityBpm,
            chestTightness = chestTightness,
            date = date
        )

        // Cek apakah data baru sama dengan data terakhir yang disimpan
        if (newUserInput == lastSavedUserInput) {
            Log.d("AppDataStore", "No changes in user input, skipping save.")
            return
        }

        // Simpan data baru
        context.dataStore.edit { preferences ->
            Log.d("AppDataStore", "Saving User Input: $newUserInput")

            preferences[GENDER_KEY] = gender
            preferences[AGE_KEY] = age
            preferences[CHEST_PAIN_LEVEL_KEY] = chestPainLevel
            preferences[RESTING_BPM_KEY] = restingBpm
            preferences[ACTIVITY_BPM_KEY] = activityBpm
            preferences[CHEST_TIGHTNESS_KEY] = chestTightness
            preferences[DATE_KEY] = date
        }
        // Perbarui cache
        lastSavedUserInput = newUserInput
    }

    suspend fun savePredictionResult(prediction: Double) {
        context.dataStore.edit { preferences ->
            preferences[PREDICTION_RESULT_KEY] = prediction.toString()
        }
    }

    val userInputFlow: Flow<UserInput> = context.dataStore.data
        .map { preferences ->
            UserInput(
                gender = preferences[GENDER_KEY] ?: -1,
                age = preferences[AGE_KEY] ?: 16,
                chestPainLevel = preferences[CHEST_PAIN_LEVEL_KEY] ?: -1,
                restingBpm = preferences[RESTING_BPM_KEY] ?: -1,
                activityBpm = preferences[ACTIVITY_BPM_KEY] ?: -1,
                chestTightness = preferences[CHEST_TIGHTNESS_KEY] ?: -1,
                date = preferences[DATE_KEY] ?: ""
            )
        }
    val predictionResultFlow: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[PREDICTION_RESULT_KEY]?.toDoubleOrNull() ?: 0.0
        }
}

data class UserInput(
    val gender: Int,
    val age: Int,
    val chestPainLevel: Int,
    val restingBpm: Int,
    val activityBpm: Int,
    val chestTightness: Int,
    val date: String
)