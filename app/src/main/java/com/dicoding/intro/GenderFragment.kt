package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper

class GenderFragment : Fragment(R.layout.fragment_gender) {
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
        radioGroup = view.findViewById(R.id.genderRadioGroup)

        // disable next button initially
        nextButton.isEnabled = false

        // enable next button only if a gender is selected
        radioGroup.setOnCheckedChangeListener { _, _ ->
            nextButton.isEnabled = radioGroup.checkedRadioButtonId != 1
        }

        nextButton.setOnClickListener{
            val gender = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_male -> "Laki-laki"
                R.id.radio_female -> "Perempuan"
                else -> ""
            }
            // Save gender to shared preferences or a temporary variable
            sharedPreferencesHelper.saveString("gender", gender)
            Toast.makeText(requireContext(), "Gender berhasil disimpan: $gender", Toast.LENGTH_SHORT).show()
            (activity as IntroActivity).moveToNextPage()
        }
    }
}