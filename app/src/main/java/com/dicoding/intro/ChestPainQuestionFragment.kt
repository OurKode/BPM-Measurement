package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper

class ChestPainQuestionFragment : Fragment(R.layout.fragment_chest_pain_question) {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var nextButton: Button
    private lateinit var radioGroup: RadioGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            (activity as IntroActivity).moveToPreviousPage()
        }

        nextButton = view.findViewById(R.id.btn_next)
        radioGroup = view.findViewById(R.id.chestPainRadioGroup)

        // Disable next button initially
        nextButton.isEnabled = false

        // Enable next button only if a chest pain option is selected
        radioGroup.setOnCheckedChangeListener { _, _ ->
            nextButton.isEnabled = radioGroup.checkedRadioButtonId != -1
        }

        nextButton.setOnClickListener {
            val chestPainLevel = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_yes -> -1 // Placeholder value, will be set in ChestPainSliderFragment
                R.id.radio_no -> 0
                else -> -1
            }
            sharedPreferencesHelper.saveInt("chestPainLevel", chestPainLevel)
            if (chestPainLevel == 0) {
                sharedPreferencesHelper.saveBoolean("skipChestPainSlider", true)
                Toast.makeText(requireContext(), "Data nyeri dada berhasil disimpan: 'Tidak' ($chestPainLevel)", Toast.LENGTH_SHORT).show()
                (activity as IntroActivity).moveToPage(5) // Skip to resting BPM fragment
            } else {
                sharedPreferencesHelper.saveBoolean("skipChestPainSlider", false)
                Toast.makeText(requireContext(), "Data nyeri dada berhasil disimpan: 'Ya' ($chestPainLevel)", Toast.LENGTH_SHORT).show()
                (activity as IntroActivity).moveToPage(4) // Navigate to chest pain slider fragment
            }
        }
    }
}