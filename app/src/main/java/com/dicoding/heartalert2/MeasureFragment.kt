package com.dicoding.heartalert2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MeasureFragment : Fragment(R.layout.fragment_measure) {

    private lateinit var startStopButton: Button
    private lateinit var bpmTextView: TextView
    private lateinit var previewView: PreviewView
    private lateinit var timerTextView: TextView

    private var isMonitoring = false
    private var sampleBuffer = mutableListOf<Pair<Long, Double>>()
    private val maxSamples = 60 * 5
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var timer: CountDownTimer? = null
    private var lastToastTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startStopButton = view.findViewById(R.id.start_stop_button)
        bpmTextView = view.findViewById(R.id.bpm_text)
        previewView = view.findViewById(R.id.preview_view)
        timerTextView = view.findViewById(R.id.timer_text)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startStopButton.setOnClickListener { toggleMonitoring() }

        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun toggleMonitoring() {
        if (isMonitoring) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }

    private fun startMonitoring() {
        if (!permissionsGranted()) {
            Toast.makeText(requireContext(), "Izin kamera diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
            return
        }

        isMonitoring = true
        sampleBuffer.clear()
        bpmTextView.text = "Mengukur..."
        startStopButton.text = "Berhenti"

        startTimer()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                    val brightness = averageBrightness(image)
                    val timestamp = System.currentTimeMillis()

                    requireActivity().runOnUiThread {
                        if (isMonitoring) {
                            sampleBuffer.add(timestamp to brightness)
                            if (sampleBuffer.size > maxSamples) {
                                sampleBuffer.removeAt(0)
                            }

                            val bpm = calculateBpm(analyzeData(sampleBuffer).crossings)
                            if (bpm != null && (bpm < 40 || bpm > 200)) {
                                resetData()
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastToastTime > 5000) { // 5 seconds delay between toasts
                                    Toast.makeText(requireContext(), "Pastikan jari anda menutupi kamera.", Toast.LENGTH_SHORT).show()
                                    lastToastTime = currentTime
                                }
                            }
                        }
                    }

                    image.close()
                })
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                camera?.cameraControl?.enableTorch(true)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        cameraProviderFuture.addListener({
            requireActivity().runOnUiThread {
                previewView.postDelayed({
                    startSampling()
                }, 1500)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        startStopButton.postDelayed(updateBpmRunnable, 1500)
    }

    private fun stopMonitoring() {
        isMonitoring = false
        camera?.cameraControl?.enableTorch(false)
        camera?.let {
            val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get()
            cameraProvider.unbindAll()
            camera = null
        }
        startStopButton.text = "Mulai"

        timer?.cancel()

        val dataStats = analyzeData(sampleBuffer)
        val finalBpm = calculateBpm(dataStats.crossings)
        finalBpm?.let {
            bpmTextView.text = "Detak Jantung Akhir: ${it.roundToInt()} BPM"
            saveBpm(finalBpm.roundToInt())
        }

        startStopButton.removeCallbacks(updateBpmRunnable)
    }

    private fun resetData() {
        sampleBuffer.clear()
        bpmTextView.text = "Mengukur..."
        restartTimer()
    }

    private fun startTimer() {
        timer?.cancel() // Ensure no existing timer is running
        timer = object : CountDownTimer(13000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = "Waktu Tersisa: $secondsRemaining detik"
            }

            override fun onFinish() {
                stopMonitoring()
            }
        }
        timer?.start()
    }

    private fun restartTimer() {
        timer?.cancel()
        startTimer()
    }

    private fun startSampling() {
        Toast.makeText(requireContext(), "Memulai Pengukuran...", Toast.LENGTH_SHORT).show()
    }

    private val updateBpmRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                val dataStats = analyzeData(sampleBuffer)
                val bpm = calculateBpm(dataStats.crossings)

                bpm?.let {
                    bpmTextView.text = "${it.roundToInt()} BPM"
                }
                startStopButton.postDelayed(this, 1000)
            }
        }
    }

    private fun calculateBpm(crossings: List<Long>): Double? {
        if (crossings.size < 2) return null

        val averageInterval = (crossings.last() - crossings.first()).toDouble() / (crossings.size - 1)
        return 60000 / averageInterval
    }

    private fun analyzeData(samples: List<Pair<Long, Double>>): DataStats {
        val average = samples.map { it.second }.average()
        val min = samples.minOf { it.second }
        val max = samples.maxOf { it.second }
        val range = max - min

        val crossings = getAverageCrossings(samples, average)
        return DataStats(average, min, max, range, crossings)
    }

    private fun getAverageCrossings(samples: List<Pair<Long, Double>>, average: Double): List<Long> {
        val crossingsSamples = mutableListOf<Long>()
        var previousSample = samples[0]

        for (currentSample in samples) {
            if (currentSample.second < average && previousSample.second > average) {
                crossingsSamples.add(currentSample.first)
            }
            previousSample = currentSample
        }

        return crossingsSamples
    }

    private fun averageBrightness(image: ImageProxy): Double {
        val buffer: ByteBuffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        var sum = 0
        for (i in data.indices step 4) {
            sum += data[i].toInt() and 0xFF
            sum += data[i + 1].toInt() and 0xFF
        }

        val avg = sum / (data.size * 0.5)

        return avg / 255
    }

    private fun permissionsGranted() = arrayOf(
        Manifest.permission.CAMERA
    ).all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!permissionsGranted()) {
                Toast.makeText(requireContext(), "Izin diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        timer?.cancel()
    }

    companion object {
        private const val TAG = "HeartRateMonitor"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    data class DataStats(val average: Double, val min: Double, val max: Double, val range: Double, val crossings: List<Long>)

    private fun saveBpm(bpm: Int) {
        val sharedPref = requireActivity().getSharedPreferences("HeartRateMonitorPrefs", Context.MODE_PRIVATE)
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val success = sharedPref.edit()
            .putInt("finalBpm", bpm)
            .putString("timestamp_$bpm", currentTime) // Ensure each BPM has a unique timestamp key
            .commit()
        if (success) {
            Toast.makeText(requireContext(), "BPM berhasil disimpan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan BPM", Toast.LENGTH_SHORT).show()
        }
    }
}