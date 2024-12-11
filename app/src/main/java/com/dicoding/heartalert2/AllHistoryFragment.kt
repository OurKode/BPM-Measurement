package com.dicoding.heartalert2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.adapter.HistoryAdapter

class AllHistoryFragment : Fragment(R.layout.fragment_all_history) {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = HistoryAdapter(sharedPreferencesHelper.getAllMeasurements()) { date, result ->
            val bundle = Bundle().apply {
                putString("date", date)
                putString("result", result)
            }
        }
    }
}