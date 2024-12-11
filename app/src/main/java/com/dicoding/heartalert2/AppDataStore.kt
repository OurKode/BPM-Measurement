package com.dicoding.heartalert2

import android.content.Context
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
        val CHEST_PAIN_LEVEL_KEY = intPreferencesKey("chest_pain_level")
        val RESTING_BPM_KEY = intPreferencesKey("resting_bpm")
        val ACTIVITY_BPM_KEY = intPreferencesKey("activity_bpm")
        val CHEST_TIGHTNESS_KEY = intPreferencesKey("chest_tightness")
        val DATE_KEY = stringPreferencesKey("date")
    }

    suspend fun saveUserInput(gender: Int, age: Int, chestPainLevel: Int, restingBpm: Int, activityBpm: Int, chestTightness: Int, date: String) {
        context.dataStore.edit { preferences ->
            preferences[GENDER_KEY] = gender
            preferences[AGE_KEY] = age
            preferences[CHEST_PAIN_LEVEL_KEY] = chestPainLevel
            preferences[RESTING_BPM_KEY] = restingBpm
            preferences[ACTIVITY_BPM_KEY] = activityBpm
            preferences[CHEST_TIGHTNESS_KEY] = chestTightness
            preferences[DATE_KEY] = date
        }
    }

    val userInputFlow: Flow<UserInput> = context.dataStore.data
        .map { preferences ->
            UserInput(
                gender = preferences[GENDER_KEY] ?: -1,
                age = preferences[AGE_KEY] ?: 0,
                chestPainLevel = preferences[CHEST_PAIN_LEVEL_KEY] ?: -1,
                restingBpm = preferences[RESTING_BPM_KEY] ?: -1,
                activityBpm = preferences[ACTIVITY_BPM_KEY] ?: -1,
                chestTightness = preferences[CHEST_TIGHTNESS_KEY] ?: -1,
                date = preferences[DATE_KEY] ?: ""
            )
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