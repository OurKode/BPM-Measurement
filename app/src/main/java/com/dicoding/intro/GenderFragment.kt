package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.AppDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GenderFragment : Fragment(R.layout.fragment_gender) {
    private lateinit var appDataStore: AppDataStore
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)
        nextButton = view.findViewById(R.id.btn_next)
        backButton = view.findViewById(R.id.btn_back)

        nextButton.isEnabled = false

        genderRadioGroup.setOnCheckedChangeListener { _, _ ->
            nextButton.isEnabled = genderRadioGroup.checkedRadioButtonId != -1
        }

        nextButton.setOnClickListener {
            val selectedGender = when (genderRadioGroup.checkedRadioButtonId) {
                R.id.radio_male -> 1
                R.id.radio_female -> 0
                else -> -1
            }

            lifecycleScope.launch {
                val userInput = appDataStore.userInputFlow.first() // Ambil data sekali
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
            // Navigasi ke AgeFragment
            findNavController().navigate(R.id.action_genderFragment_to_ageFragment)
        }
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}