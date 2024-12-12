package com.dicoding.heartalert2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.api.RetrofitInstance
import com.dicoding.heartalert2.adapter.ArticleAdapter
import com.dicoding.heartalert2.article.ArticleDetailFragment
import kotlinx.coroutines.launch

class ResultFragment : Fragment(R.layout.fragment_result) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        val resultTextView: TextView = view.findViewById(R.id.resultTextView)
        val predictionTextView: TextView = view.findViewById(R.id.predictionTextView)
        val remeasureButton: Button = view.findViewById(R.id.btn_remeasure)

        recyclerView = view.findViewById(R.id.recyclerViewHealthArticles)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        articleAdapter = ArticleAdapter(emptyList()) { }
        recyclerView.adapter = articleAdapter

        loadResults(resultTextView, predictionTextView)

        remeasureButton.setOnClickListener {
            resetDataStore()
            navigateToIntro()
        }

        fetchArticles()
    }

    private fun navigateToIntro() {
        findNavController().navigate(R.id.action_resultFragment_to_introFragment)
    }

    private fun loadResults(resultTextView: TextView, predictionTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    appDataStore.userInputFlow.collect { userInput ->
                        val result = """
                        Gender: ${if (userInput.gender == 1) "Laki-laki" else if (userInput.gender == 0) "Perempuan" else "Tidak Diketahui"}
                        Age: ${userInput.age}
                        Chest Pain Level: ${userInput.chestPainLevel}
                        Resting BPM: ${userInput.restingBpm}
                        Activity BPM: ${userInput.activityBpm}
                        Chest Tightness: ${userInput.chestTightness}
                        Date: ${userInput.date}
                    """.trimIndent()

                        resultTextView.text = result
                    }
                }

                launch {
                    appDataStore.predictionResultFlow.collect { prediction ->
                        predictionTextView.text = "Prediction: ${String.format("%.2f", prediction)}"
                    }
                }
            }
        }
    }


    private fun fetchArticles() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getArticles()
                if (response.isSuccessful) {
                    val articleResponse = response.body()
                    val articles = articleResponse?.articles ?: emptyList()
                    articleAdapter = ArticleAdapter(articles) { article ->
                        val bundle = Bundle().apply {
                            putParcelable("article", article)
                        }
                        findNavController().navigate(R.id.action_articleFragment_to_articleDetailFragment, bundle)
                    }
                    recyclerView.adapter = articleAdapter
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(context, "Failed to load articles: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ResultFragment", "fetchArticles: Error loading articles", e)
            }
        }
    }

    private fun resetDataStore() {
        lifecycleScope.launch {
            try {
                appDataStore.saveUserInput(
                    gender = -1,
                    age = 0,
                    chestPainLevel = -1,
                    restingBpm = -1,
                    activityBpm = -1,
                    chestTightness = -1,
                    date = ""
                )
                Toast.makeText(context, "Data has been reset", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ResultFragment", "resetDataStore: Failed to reset data", e)
                Toast.makeText(context, "Failed to reset data.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}