package com.dicoding.heartalert2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.adapter.BPMHistoryAdapter
import com.dicoding.heartalert2.adapter.BPMRecord

class InsightFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var bpmList: MutableList<BPMRecord>
    private var sortByLatest = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_insight, container, false)

        viewManager = LinearLayoutManager(requireContext())
        bpmList = loadBpmHistory().toMutableList()
        viewAdapter = BPMHistoryAdapter(bpmList)

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val filterButton: Button = view.findViewById(R.id.button_filter)
        filterButton.setOnClickListener {
            sortByLatest = !sortByLatest
            filterButton.text = if (sortByLatest) "Terlama" else "Terbaru"
            sortBpmList()
        }

        return view
    }

    private fun loadBpmHistory(): List<BPMRecord> {
        val sharedPref = requireActivity().getSharedPreferences("HeartRateMonitorPrefs", Context.MODE_PRIVATE)
        val bpmList = mutableListOf<BPMRecord>()
        val allEntries = sharedPref.all

        for ((key, value) in allEntries) {
            if (key.startsWith("bpm_") && value is Int) {
                val timestamp = sharedPref.getString("timestamp_${key.removePrefix("bpm_")}", "")
                bpmList.add(BPMRecord(value, timestamp ?: ""))
            }
        }

        return bpmList
    }

    private fun sortBpmList() {
        bpmList.sortBy { it.timestamp }
        if (sortByLatest) {
            bpmList.reverse()
        }
        viewAdapter.notifyDataSetChanged()
    }
}