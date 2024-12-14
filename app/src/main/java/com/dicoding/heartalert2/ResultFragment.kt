package com.dicoding.heartalert2

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import com.google.android.gms.location.LocationRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.adapter.ArticleAdapter
import com.dicoding.heartalert2.adapter.HospitalAdapter
import com.dicoding.heartalert2.api.ArticlesItem
import com.dicoding.heartalert2.api.RetrofitInstance
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultFragment : Fragment(R.layout.fragment_result) {

    private lateinit var appDataStore: AppDataStore
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var recyclerViewArticles: RecyclerView
    private lateinit var recyclerViewHospitals: RecyclerView
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var shimmerLayoutArticles: ShimmerFrameLayout
    private lateinit var shimmerLayoutHospitals: ShimmerFrameLayout

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setMinUpdateIntervalMillis(2000) // Interval tercepat
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            location?.let {
                stopLocationUpdates()
                fetchHospitals(it.latitude, it.longitude)
            }
        }
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        val activityBpmTextView: TextView = view.findViewById(R.id.activityBpmTextView)
        val riskStatusTextView: TextView = view.findViewById(R.id.riskStatusTextView)
        val btnRemeasure: Button = view.findViewById(R.id.btn_remeasure)
        recyclerViewArticles = view.findViewById(R.id.recyclerViewHealthArticles)
        recyclerViewHospitals = view.findViewById(R.id.recyclerViewHospital)
        shimmerLayoutArticles = view.findViewById(R.id.shimmerLayoutArticle)
        shimmerLayoutHospitals = view.findViewById(R.id.shimmerLayoutHospitals)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        loadResults(activityBpmTextView, riskStatusTextView)
        setupRecyclerViews()
        setupButtonListeners(btnRemeasure)
        loadArticles()
        checkLocationPermission()
    }

    private fun setupRecyclerViews() {
        recyclerViewArticles.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHospitals.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupButtonListeners(btnRemeasure: Button) {
        btnRemeasure.setOnClickListener {
            findNavController().navigate(R.id.action_resultFragment_to_introFragment)
        }
    }

    private fun checkLocationPermission() {
        when {
            // Jika izin sudah diberikan, lanjutkan untuk mendapatkan lokasi
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            // Jika izin belum diberikan, minta izin
            else -> {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        startShimmerEffect(shimmerLayoutHospitals, recyclerViewHospitals)

        // Periksa izin sebelum melanjutkan permintaan lokasi
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    fetchHospitals(location.latitude, location.longitude)
                } else {
                    requestNewLocationUpdates()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
                stopShimmerEffect(shimmerLayoutHospitals, recyclerViewHospitals)
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
            stopShimmerEffect(shimmerLayoutHospitals, recyclerViewHospitals)
        }
    }

    private fun requestNewLocationUpdates() {
        // Meminta pembaruan lokasi baru hanya jika izin sudah diberikan
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                // Menangani kasus di mana izin tidak diberikan
                Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
                stopShimmerEffect(shimmerLayoutHospitals, recyclerViewHospitals)
            }
        } else {
            // Jika izin belum diberikan, tampilkan pesan kesalahan
            Toast.makeText(context, "Location permission is required to get location updates.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun fetchHospitals(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getHospitals(LocationRequest(latitude, longitude))
                if (response.hospitals.isNotEmpty()) {
                    setupHospitalRecyclerView(response.hospitals)
                } else {
                    Toast.makeText(context, "No hospitals found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                stopShimmerEffect(shimmerLayoutHospitals, recyclerViewHospitals)
            }
        }
    }

    private fun loadArticles() {
        startShimmerEffect(shimmerLayoutArticles, recyclerViewArticles)
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getArticles()
                if (response.isSuccessful) {
                    val articleList = response.body()?.articles ?: emptyList()
                    setupArticleRecyclerView(articleList)
                } else {
                    Toast.makeText(context, "Failed to load articles.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                stopShimmerEffect(shimmerLayoutArticles, recyclerViewArticles)
            }
        }
    }

    private fun setupArticleRecyclerView(articleList: List<ArticlesItem>) {
        articleAdapter = ArticleAdapter(articleList)
        recyclerViewArticles.adapter = articleAdapter
    }

    private fun setupHospitalRecyclerView(hospitals: List<Hospital>) {
        val hospitalAdapter = HospitalAdapter(hospitals)
        recyclerViewHospitals.adapter = hospitalAdapter
    }

    private fun startShimmerEffect(shimmerLayout: ShimmerFrameLayout, recyclerView: RecyclerView) {
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun stopShimmerEffect(shimmerLayout: ShimmerFrameLayout, recyclerView: RecyclerView) {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun loadResults(activityBpmTextView: TextView, riskStatusTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    appDataStore.userInputFlow.collect { userInput ->
                        val activityBpm = "${userInput.activityBpm} BPM"
                        activityBpmTextView.text = activityBpm
                        saveToSharedPreferences(userInput.activityBpm.toString(), riskStatusTextView.text.toString())
                    }
                }
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
}