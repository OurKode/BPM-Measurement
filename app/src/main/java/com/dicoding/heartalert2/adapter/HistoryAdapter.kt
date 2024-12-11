package com.dicoding.heartalert2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dicoding.heartalert2.R
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val historyList: Map<String, String>,
    private val onItemClick: (String, String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val date = historyList.keys.toList()[position]
        val result = historyList[date] ?: ""
        holder.bind(date, result)
        holder.itemView.setOnClickListener {
            onItemClick(date, result)
        }
    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val resultTextView: TextView = itemView.findViewById(R.id.resultTextView)

        fun bind(date: String, result: String) {
            dateTextView.text = date
            resultTextView.text = result
        }
    }
}