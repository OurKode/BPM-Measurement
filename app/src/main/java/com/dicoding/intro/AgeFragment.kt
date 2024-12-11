package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import kotlinx.coroutines.launch

class AgeFragment : Fragment(R.layout.fragment_age) {
    private lateinit var appDataStore: AppDataStore
    private lateinit var numberPicker: NumberPicker
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        numberPicker = view.findViewById(R.id.numberPicker)
        nextButton = view.findViewById(R.id.btn_next)
        backButton = view.findViewById(R.id.btn_back)

        numberPicker.minValue = 16
        numberPicker.maxValue = 111
        numberPicker.wrapSelectorWheel = false

        // Load the saved age from DataStore and set it to the NumberPicker
        lifecycleScope.launch {
            appDataStore.userInputFlow.collect { userInput ->
                numberPicker.value = userInput.age
            }
        }

        nextButton.setOnClickListener {
            val age = numberPicker.value

            // Save the age to DataStore
            lifecycleScope.launch {
                appDataStore.userInputFlow.collect { userInput ->
                    appDataStore.saveUserInput(
                        gender = userInput.gender,
                        age = age,
                        chestPainLevel = userInput.chestPainLevel,
                        restingBpm = userInput.restingBpm,
                        activityBpm = userInput.activityBpm,
                        chestTightness = userInput.chestTightness,
                        date = userInput.date
                    )
                }

                Toast.makeText(requireContext(), "Data umur berhasil disimpan: $age", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).moveToNextPage()
            }
        }

        backButton.setOnClickListener {
            (activity as MainActivity).moveToPreviousPage()
        }
    }
}