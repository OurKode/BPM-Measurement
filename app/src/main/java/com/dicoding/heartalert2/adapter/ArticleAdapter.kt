package com.dicoding.heartalert2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.R
import com.dicoding.heartalert2.api.ArticlesItem
import com.dicoding.heartalert2.databinding.ItemArticleBinding


class ArticleAdapter(
    private var articles: List<ArticlesItem>,
    private val onItemClick: (ArticlesItem) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: ArticlesItem) {
            binding.tvTitle.text = article.title
            binding.tvDate.text = article.createdAt.substring(0, 10)
            binding.tvDescription.text = article.content.take(100) + "..."

            itemView.setOnClickListener {
                onItemClick(article)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        if (article != null) {
            holder.bind(article)
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateArticles(newArticles: List<ArticlesItem>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(articles, newArticles))
        articles = newArticles
        diffResult.dispatchUpdatesTo(this)
    }

    private class DiffUtilCallback(private val oldList: List<ArticlesItem>, private val newList: List<ArticlesItem>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}