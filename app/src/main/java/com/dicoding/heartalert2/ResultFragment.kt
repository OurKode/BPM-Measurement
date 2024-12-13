package com.dicoding.heartalert2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.adapter.ArticleAdapter
import com.dicoding.heartalert2.api.ArticlesItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultFragment : Fragment(R.layout.fragment_result) {
    private lateinit var appDataStore: AppDataStore
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        val activityBpmTextView: TextView = view.findViewById(R.id.activityBpmTextView)
        val riskStatusTextView: TextView = view.findViewById(R.id.riskStatusTextView)
        val btnRemeasure: Button = view.findViewById(R.id.btn_remeasure)
        recyclerView = view.findViewById(R.id.recyclerViewHealthArticles)

        // Menampilkan hasil ketika fragment dibuat
        loadResults(activityBpmTextView, riskStatusTextView)

        // Mengatur RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Menambahkan listener untuk tombol "Mengukur Ulang"
        btnRemeasure.setOnClickListener {
            // Aksi ketika tombol diklik (Tanpa navigasi)
            findNavController().navigate(R.id.action_resultFragment_to_introFragment)
        }

        // Memuat artikel
        loadArticles()
    }

    private fun loadResults(activityBpmTextView: TextView, riskStatusTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Mengambil dan menampilkan activityBpm
                launch {
                    appDataStore.userInputFlow.collect { userInput ->
                        val activityBpm = "${userInput.activityBpm} BPM"
                        activityBpmTextView.text = activityBpm
                        saveToSharedPreferences(userInput.activityBpm.toString(), riskStatusTextView.text.toString())
                    }
                }

                // Mengambil dan menampilkan hasil prediksi dan status risiko
                launch {
                    appDataStore.predictionResultFlow.collect { prediction ->
                        val displayPrediction = prediction ?: 0.0
                        val riskStatus = if (displayPrediction >= 0.5) "Beresiko!" else "Normal"
                        riskStatusTextView.text = riskStatus
                        saveToSharedPreferences(activityBpmTextView.text.toString(), riskStatus)
                    }
                }
            }
        }
    }

    private fun saveToSharedPreferences(activityBpm: String, riskStatus: String) {
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val result = "$activityBpm, $riskStatus"
        sharedPreferencesHelper.saveMeasurementResult(date, result)
    }

    private fun loadArticles() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getArticles()
                if (response.isSuccessful) {
                    val articleList = response.body()?.articles ?: emptyList()
                    setupRecyclerView(articleList)
                } else {
                    Toast.makeText(context, "Failed to load articles.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView(articleList: List<ArticlesItem>) {
        articleAdapter = ArticleAdapter(articleList)
        recyclerView.adapter = articleAdapter
    }
}