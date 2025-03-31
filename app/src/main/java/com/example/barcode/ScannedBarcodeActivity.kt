package com.example.barcode

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.barcode.databinding.ActivityScannedBarcodeBinding
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannedBarcodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannedBarcodeBinding
    private lateinit var cameraExecutor: ExecutorService
    private var scanningLineAnimator: ObjectAnimator? = null
    private var intentData: String? = null
    private var isEmail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannedBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        checkCameraPermission()

        binding.btnAction.setOnClickListener {
            intentData?.let {
                val intent = if (isEmail) {
                    Intent(this, EmailActivity::class.java).putExtra("email_address", it)
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse(it))
                }
                startActivity(intent)
            }
        }
    }

    private fun checkCameraPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(640, 480))
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { processBarcodeImage(it) }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                startScanningAnimation()
            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Camera use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processBarcodeImage(image: ImageProxy) {
        try {
            image.image?.let {
                val inputImage = InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees)
                scanBarcodes(inputImage)
            }
        } catch (e: Exception) {
            Log.e("BarcodeScanner", "Image processing failed", e)
        } finally {
            image.close()
        }
    }

    private fun scanBarcodes(image: InputImage) {
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    intentData = barcode.rawValue
                    binding.txtBarcodeValue.text = intentData

                    isEmail = barcode.valueType == Barcode.TYPE_EMAIL
                    binding.btnAction.text = if (isEmail) "ADD CONTENT TO THE MAIL" else "LAUNCH"

                    scanningLineAnimator?.takeIf { it.isRunning }?.cancel()
                    binding.scanningLine.visibility = View.INVISIBLE
                }
            }
            .addOnFailureListener { e -> Log.e("BarcodeScanner", "Barcode scan failed", e) }
    }

    private fun startScanningAnimation() {
        binding.scanningLine.apply {
            visibility = View.VISIBLE
            scanningLineAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f, (binding.previewView.height - height).toFloat()).apply {
                duration = 1500
                interpolator = LinearInterpolator()
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        scanningLineAnimator?.cancel()
    }
}
