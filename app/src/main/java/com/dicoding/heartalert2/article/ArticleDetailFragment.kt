package com.dicoding.heartalert2.article

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.ArticlesItem

class ArticleDetailFragment : Fragment(R.layout.fragment_article_detail) {

    private lateinit var titleTextView: TextView
    private lateinit var articleImageView: ImageView
    private lateinit var contentTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind Views
        titleTextView = view.findViewById(R.id.titleTextView)
        articleImageView = view.findViewById(R.id.articleImageView)
        contentTextView = view.findViewById(R.id.contentTextView)
        val backButton: ImageView = view.findViewById(R.id.backButton)

        // Get article data from arguments using getParcelableCompat method
        val article: ArticlesItem? = arguments?.getParcelableCompat("article")
        Log.d("ArticleDetailFragment", "Received article: $article")

        article?.let {
            titleTextView.text = it.title
            contentTextView.text = it.content

            // Load image using Glide
            Glide.with(this)
                .load(it.imageUrl)
                .into(articleImageView)

            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
    }

    // Helper method to handle deprecated getParcelable
    private inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
        return if (Build.VERSION.SDK_INT >= 33) {
            getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelable(key) as? T
        }
    }
}