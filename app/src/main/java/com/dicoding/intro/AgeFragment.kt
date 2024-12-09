package com.dicoding.intro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper

class AgeFragment : Fragment(R.layout.fragment_age) {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var nextButton: Button
    private lateinit var ageEditText: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            (activity as IntroActivity).moveToPreviousPage()
        }

        nextButton = view.findViewById(R.id.btn_next)
        ageEditText = view.findViewById(R.id.et_age)

        // Disable next button initially
        nextButton.isEnabled = false

        // Enable next button only if age is entered
        ageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                nextButton.isEnabled = s?.isNotEmpty() ?: false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        nextButton.setOnClickListener {
            val age = ageEditText.text.toString().toIntOrNull() ?: 0
            sharedPreferencesHelper.saveInt("age", age)
            Toast.makeText(requireContext(), "Usia berhasil disimpan: $age", Toast.LENGTH_SHORT).show()
            (activity as IntroActivity).moveToNextPage()
        }
    }
}