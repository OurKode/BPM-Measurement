package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.dicoding.heartalert2.R
import androidx.navigation.fragment.findNavController
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavOptions
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.SharedPreferencesHelper

class IntroFragment : Fragment(R.layout.fragment_intro) {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        val startButton: Button = view.findViewById(R.id.btn_start)
        val lastResultTextView: TextView = view.findViewById(R.id.last_result_text)

        val lastResult = sharedPreferencesHelper.getString("lastResult")
        if (!lastResult.isNullOrEmpty()) {
            lastResultTextView.text = lastResult
            lastResultTextView.visibility = View.VISIBLE
            lastResultTextView.setOnClickListener {
//                findNavController().navigate(
//                    R.id.action_introFragment_to_allHistoryFragment,
//                    null,
//                    NavOptions.Builder().setPopUpTo(R.id.introFragment, true).build()
//                )
                Toast.makeText(requireContext(), "Last Result: $lastResult", Toast.LENGTH_SHORT).show()
            }
        }

        startButton.setOnClickListener {
            (activity as MainActivity).moveToNextPage()
        }
    }
}