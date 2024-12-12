package com.dicoding.intro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dicoding.heartalert2.AppDataStore
import com.dicoding.heartalert2.MainActivity
import com.dicoding.heartalert2.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChestTightnessFragment : Fragment(R.layout.fragment_chest_tightness) {
    private lateinit var finishButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appDataStore = AppDataStore(requireContext())

        finishButton = view.findViewById(R.id.btn_finish)
        radioGroup = view.findViewById(R.id.chestTightnessRadioGroup)

        finishButton.isEnabled = false

        radioGroup.setOnCheckedChangeListener { _, _ ->
            finishButton.isEnabled = radioGroup.checkedRadioButtonId != -1
        }

        finishButton.setOnClickListener {
            val chestTightness = when (radioGroup.checkedRadioButtonId) {
                R.id.radio_yes -> 1
                R.id.radio_no -> 0
                else -> 0
            }

            val currentDate = SimpleDateFormat("dd-MMM-yy HH:mm:ss", Locale.getDefault()).format(Date())

            lifecycleScope.launch {
                val userInput = appDataStore.userInputFlow.first()
                appDataStore.saveUserInput(
                    gender = userInput.gender,
                    age = userInput.age,
                    chestPainLevel = userInput.chestPainLevel,
                    restingBpm = userInput.restingBpm,
                    activityBpm = userInput.activityBpm,
                    chestTightness = chestTightness,
                    date = currentDate
                )
                Log.d("ChestTightnessFragment", "Data disimpan: chestTightness=$chestTightness, date=$currentDate")

                val input = listOf(
                    userInput.age,
                    userInput.gender,
                    userInput.chestPainLevel,
                    userInput.restingBpm,
                    userInput.activityBpm,
                    chestTightness
                )

                val prediction = sendPredictionRequest(input)
                if (prediction != null) {
                    appDataStore.savePredictionResult(prediction)
                    findNavController().navigate(R.id.action_chestTightnessFragment_to_resultFragment)
                } else {
                    Toast.makeText(requireContext(), "Gagal memproses prediksi", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private suspend fun sendPredictionRequest(input: List<Int>): Double? {
        val url = "https://model-service-218244789825.asia-southeast2.run.app/predict"
        val jsonBody = mapOf("input" to input)

        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e("Prediction Error", "HTTP Error: ${res.code}, ${res.message}")
                        return@withContext null
                    }

                    val predictionResult = JSONObject(res.body?.string() ?: "").getJSONArray("prediction")
                    val predictionValue = predictionResult.getJSONArray(0).getDouble(0)

                    Log.d("ChestTightnessFragment", "Prediction: $predictionValue")
                    return@withContext predictionValue
                }
            } catch (e: Exception) {
                Log.e("PredictionError", "Error sending request", e)
                return@withContext null
            }
        }
    }
}