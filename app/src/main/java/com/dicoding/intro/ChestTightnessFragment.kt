package com.dicoding.intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper

class ChestTightnessFragment : Fragment(R.layout.fragment_chest_tightness) {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var finishButton: Button
    private lateinit var radioGroup: RadioGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            (activity as IntroActivity).moveToPreviousPage()
        }

        finishButton = view.findViewById(R.id.btn_finish)
        Log.d("ChestTightnessFragment", "Finish button initialized: ${finishButton}")

        radioGroup = view.findViewById(R.id.chestTightnessRadioGroup)
        Log.d("ChestTightnessFragment", "Radio group initialized: ${radioGroup}")

        // Disable finish button initially
        finishButton.isEnabled = false

        // Enable finish button only if a selection is made
        radioGroup.setOnCheckedChangeListener { _, _ ->
            finishButton.isEnabled = radioGroup.checkedRadioButtonId != -1
        }

        finishButton.setOnClickListener {
            val chestTightness = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_yes -> 1
                R.id.radio_no -> 0
                else -> 0
            }
            sharedPreferencesHelper.saveInt("chestTightness", chestTightness)
            Toast.makeText(requireContext(), "Data sesak dada berhasil disimpan: ${if (chestTightness == 1) "Ya" else "Tidak"} ($chestTightness)", Toast.LENGTH_SHORT).show()

            // Selesaikan onboarding dan mulai MainActivity
            val sharedPreferences = requireActivity().getSharedPreferences("HeartAlertPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false)
            editor.apply()

            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}