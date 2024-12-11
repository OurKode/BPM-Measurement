package com.dicoding.heartalert2.article

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.API.RetrofitInstance
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.adapter.ArticleAdapter
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
                    articleAdapter = ArticleAdapter(articleList) { article ->
                        val detailFragment = ArticleDetailFragment.newInstance(article)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, detailFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    recyclerView.adapter = articleAdapter
                } else {
                    Toast.makeText(context, "Failed to load articles.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}