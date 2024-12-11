package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.MainActivity
import kotlinx.coroutines.launch

class GenderFragment : Fragment(R.layout.fragment_gender) {
    private lateinit var appDataStore: AppDataStore
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var nextButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)
        nextButton = view.findViewById(R.id.btn_next)

        nextButton.setOnClickListener {
            val selectedGender = when (genderRadioGroup.checkedRadioButtonId) {
                R.id.radio_male -> 1
                R.id.radio_female -> 0
                else -> -1
            }

            lifecycleScope.launch {
                appDataStore.userInputFlow.collect { userInput ->
                    appDataStore.saveUserInput(
                        gender = selectedGender,
                        age = userInput.age,
                        chestPainLevel = userInput.chestPainLevel,
                        restingBpm = userInput.restingBpm,
                        activityBpm = userInput.activityBpm,
                        chestTightness = userInput.chestTightness,
                        date = userInput.date
                    )
                }

                (activity as MainActivity).moveToNextPage()
            }
        }
    }
}