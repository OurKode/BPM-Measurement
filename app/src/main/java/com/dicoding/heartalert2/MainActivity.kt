// File: MainActivity.kt
package com.dicoding.heartalert2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var startStopButton: Button
    private lateinit var bpmTextView: TextView
    private lateinit var previewView: PreviewView

    private var isMonitoring = false
    private var sampleBuffer = mutableListOf<Pair<Long, Double>>()
    private val maxSamples = 60 * 5
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startStopButton = findViewById(R.id.start_stop_button)
        bpmTextView = findViewById(R.id.bpm_text)
        previewView = findViewById(R.id.preview_view)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startStopButton.setOnClickListener { toggleMonitoring() }

        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
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
            Toast.makeText(this, "Izin kamera diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
            return
        }

        isMonitoring = true
        sampleBuffer.clear()
        bpmTextView.text = "Mengukur..."
        startStopButton.text = "Berhenti"

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                    val brightness = averageBrightness(image)
                    val timestamp = System.currentTimeMillis()

                    runOnUiThread {
                        if (isMonitoring) {
                            sampleBuffer.add(timestamp to brightness)
                            if (sampleBuffer.size > maxSamples) {
                                sampleBuffer.removeAt(0)
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
        }, ContextCompat.getMainExecutor(this))

        // Stabilizing camera image before starting measurements
        cameraProviderFuture.addListener({
            runOnUiThread {
                Toast.makeText(this, "Stabilizing camera...", Toast.LENGTH_SHORT).show()
                previewView.postDelayed({
                    startSampling()
                }, 1500) // Wait for 1.5 seconds
            }
        }, ContextCompat.getMainExecutor(this))

        // Update BPM every 1.5 seconds
        startStopButton.postDelayed(updateBpmRunnable, 1500)
    }

    private fun stopMonitoring() {
        isMonitoring = false
        camera?.cameraControl?.enableTorch(false)
        camera?.let {
            val cameraProvider = ProcessCameraProvider.getInstance(this).get()
            cameraProvider.unbindAll()
            camera = null
        }
        startStopButton.text = "Mulai"
        Toast.makeText(this, "Pengukuran dihentikan", Toast.LENGTH_SHORT).show()

        // Show final BPM value
        val dataStats = analyzeData(sampleBuffer)
        val finalBpm = calculateBpm(dataStats.crossings)
        finalBpm?.let {
            bpmTextView.text = "Detak Jantung Akhir: ${it.roundToInt()} BPM"
        }

        startStopButton.removeCallbacks(updateBpmRunnable)
    }

    private fun startSampling() {
        Toast.makeText(this, "Memulai Pengukuran...", Toast.LENGTH_SHORT).show()
    }

    private val updateBpmRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                val dataStats = analyzeData(sampleBuffer)
                val bpm = calculateBpm(dataStats.crossings)

                bpm?.let {
                    bpmTextView.text = "Detak Jantung: ${it.roundToInt()} BPM"
                }
                startStopButton.postDelayed(this, 3000)
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
            sum += data[i].toInt() and 0xFF // Red channel
            sum += data[i + 1].toInt() and 0xFF // Green channel
        }

        val avg = sum / (data.size * 0.5)

        return avg / 255
    }

    private fun permissionsGranted() = arrayOf(
        Manifest.permission.CAMERA
    ).all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!permissionsGranted()) {
                Toast.makeText(this, "Izin diperlukan untuk melanjutkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "HeartRateMonitor"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    data class DataStats(val average: Double, val min: Double, val max: Double, val range: Double, val crossings: List<Long>)
}