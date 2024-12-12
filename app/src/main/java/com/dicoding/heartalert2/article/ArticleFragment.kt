package com.dicoding.heartalert2.article

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.adapter.ArticleAdapter
import com.dicoding.heartalert2.api.ArticlesItem
import com.dicoding.heartalert2.api.RetrofitInstance
import kotlinx.coroutines.launch

class ArticleFragment : Fragment(R.layout.fragment_article) {
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        fetchArticles()

    }

    private fun fetchArticles() {
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
        articleAdapter = ArticleAdapter(articleList) { article ->
            // Menggunakan NavController untuk navigasi ke ArticleDetailFragment
            val bundle = bundleOf("article" to article)
            findNavController().navigate(R.id.action_articleFragment_to_articleDetailFragment, bundle)
        }
        recyclerView.adapter = articleAdapter
    }
}