package com.dicoding.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.SharedPreferencesHelper
import com.dicoding.heartalert2.adapter.HistoryAdapter
import kotlinx.coroutines.launch

class AllHistoryFragment : Fragment(R.layout.fragment_all_history) {

//    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appDataStore = AppDataStore(requireContext())
//        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.allHistoryRv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(emptyList()) { date, result ->
            Toast.makeText(context, "Clicked: $date - $result", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = historyAdapter

        // Load history from AppDataStore
        loadHistory()
    }

    private var isHistoryLoaded = false

    private fun loadHistory() {
        lifecycleScope.launch {
            appDataStore.historyFlow.collect { historyList ->
                if (!isHistoryLoaded) {
                    // Update RecyclerView hanya sekali setelah data tersedia
                    historyAdapter.updateData(historyList)
                    isHistoryLoaded = true // Menandai bahwa data sudah dimuat
                }
            }
        }
    }
}