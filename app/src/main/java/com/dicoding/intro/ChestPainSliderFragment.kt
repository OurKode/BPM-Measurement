package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper

import com.google.android.material.slider.Slider

class ChestPainSliderFragment : Fragment(R.layout.fragment_chest_pain_slider) {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var nextButton: Button
    private lateinit var slider: Slider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            (activity as MainActivity).moveToPreviousPage()
        }

        nextButton = view.findViewById(R.id.btn_next)
        slider = view.findViewById(R.id.slider_chest_pain)

        // Disable next button initially
        nextButton.isEnabled = false

        // Enable next button only if the user interacts with the slider
        slider.addOnChangeListener { slider, value, fromUser ->
            nextButton.isEnabled = true
        }

        nextButton.setOnClickListener {
            val chestPainLevel = slider.value.toInt() // 1 to 3
            sharedPreferencesHelper.saveInt("chestPainLevel", chestPainLevel)
            Toast.makeText(
                requireContext(),
                "Tingkat nyeri dada berhasil disimpan: ($chestPainLevel)",
                Toast.LENGTH_SHORT
            ).show()
            (activity as MainActivity).moveToNextPage()
        }
    }
}
