package com.dicoding.heartalert2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InsightFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_insight, container, false)

        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = BPMHistoryAdapter(loadBpmHistory())

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return view
    }

    private fun loadBpmHistory(): List<BPMRecord> {
        val sharedPref = requireActivity().getSharedPreferences("HeartRateMonitorPrefs", Context.MODE_PRIVATE)
        val bpmList = mutableListOf<BPMRecord>()
        val allEntries = sharedPref.all

        for ((key, value) in allEntries) {
            if (value is Int) {
                val timestamp = sharedPref.getString("timestamp_$key", "")
                bpmList.add(BPMRecord(value, timestamp ?: ""))
            }
        }
        return bpmList
    }
}

data class BPMRecord(val bpm: Int, val timestamp: String)

class BPMHistoryAdapter(private val dataset: List<BPMRecord>) :
    RecyclerView.Adapter<BPMHistoryAdapter.BPMViewHolder>() {

    class BPMViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val bpmTextView: TextView = view.findViewById(R.id.bpm_text)
        val timestampTextView: TextView = view.findViewById(R.id.timestamp_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPMViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bpm_record_item, parent, false) as View
        return BPMViewHolder(view)
    }

    override fun onBindViewHolder(holder: BPMViewHolder, position: Int) {
        val item = dataset[position]
        holder.bpmTextView.text = "BPM: ${item.bpm}"
        holder.timestampTextView.text = "Timestamp: ${item.timestamp}"
    }

    override fun getItemCount() = dataset.size
}