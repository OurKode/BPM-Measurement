package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.dicoding.heartalert2.R
import androidx.navigation.fragment.findNavController
import android.widget.TextView

class IntroFragment : Fragment(R.layout.fragment_intro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton: Button = view.findViewById(R.id.btn_start)
        val lastResultTextView: TextView = view.findViewById(R.id.last_result_text)

        startButton.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_genderFragment)
        }
    }
}