package com.dicoding.heartalert2.article

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.dicoding.heartalert2.API.Article
import com.dicoding.heartalert2.R

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
        val article: Article? = arguments?.getParcelableCompat("article")
        article?.let {
            titleTextView.text = it.title
            contentTextView.text = it.content

            // Load image using Glide
            Glide.with(this)
                .load(it.image_url)
                .into(articleImageView)

            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        fun newInstance(article: Article): ArticleDetailFragment {
            val fragment = ArticleDetailFragment()
            val args = Bundle().apply {
                putParcelable("article", article)
            }
            fragment.arguments = args
            return fragment
        }
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