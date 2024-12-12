package com.dicoding.intro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChestTightnessFragment : Fragment(R.layout.fragment_chest_tightness) {
    private lateinit var finishButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            (activity as MainActivity).moveToPreviousPage()
        }

        finishButton = view.findViewById(R.id.btn_finish)
        radioGroup = view.findViewById(R.id.chestTightnessRadioGroup)

        finishButton.isEnabled = false

        radioGroup.setOnCheckedChangeListener { _, _ ->
            finishButton.isEnabled = radioGroup.checkedRadioButtonId != -1
        }

        finishButton.setOnClickListener {
            val chestTightness = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_yes -> 1
                R.id.radio_no -> 0
                else -> 0
            }

            // Get current date
            val currentDate = SimpleDateFormat("dd-MMM-yy HH:mm:ss", Locale.getDefault()).format(Date())

            lifecycleScope.launch {
                // Retrieve other inputs from DataStore
                appDataStore.userInputFlow.collect { userInput ->
                    appDataStore.saveUserInput(
                        gender = userInput.gender,
                        age = userInput.age,
                        chestPainLevel = userInput.chestPainLevel,
                        restingBpm = userInput.restingBpm,
                        activityBpm = userInput.activityBpm,
                        chestTightness = chestTightness,
                        date = currentDate
                    )
                }
            }
            Toast.makeText(requireContext(), "Semua data berhasil disimpan", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                (activity as MainActivity).moveToNextPage()
            }, 1000) // 1 second delay
        }
    }
}