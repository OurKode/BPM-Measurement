package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChestPainQuestionFragment : Fragment(R.layout.fragment_chest_pain_question) {
    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        val radioGroup: RadioGroup = view.findViewById(R.id.chestPainRadioGroup)
        val nextButton: Button = view.findViewById(R.id.btn_next)
        val backButton: Button = view.findViewById(R.id.btn_back)

        nextButton.isEnabled = false
        radioGroup.setOnCheckedChangeListener { _, _ ->
            nextButton.isEnabled = radioGroup.checkedRadioButtonId != -1
        }

        nextButton.setOnClickListener {
            val chestPainLevel = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_yes -> -1
                R.id.radio_no -> 0
                else -> -1
            }

            // Save chest pain level to DataStore
            lifecycleScope.launch {
                val userInput = appDataStore.userInputFlow.first()
                appDataStore.saveUserInput(
                    gender = userInput.gender,
                    age = userInput.age,
                    chestPainLevel = chestPainLevel,
                    restingBpm = userInput.restingBpm,
                    activityBpm = userInput.activityBpm,
                    chestTightness = userInput.chestTightness,
                    date = userInput.date
                )
            }
            if (radioGroup.checkedRadioButtonId == R.id.radio_no) {
                // Langsung ke RestingBpmFragment
                findNavController().navigate(R.id.action_chestPainQuestionFragment_to_restingBpmFragment)
            } else {
                findNavController().navigate(R.id.action_chestPainQuestionFragment_to_chestPainSliderFragment)
            }
        }
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
