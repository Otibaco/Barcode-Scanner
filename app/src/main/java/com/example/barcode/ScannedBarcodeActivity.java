package com.example.barcode;

import androidx.camera.core.ExperimentalGetImage;
import androidx.annotation.OptIn;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.common.InputImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannedBarcodeActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private PreviewView previewView;
    private TextView txtBarcodeValue;
    private Button btnAction;
    private String intentData = "";
    private boolean isEmail = false;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_barcode);

        previewView = findViewById(R.id.previewView);
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        btnAction = findViewById(R.id.btnAction);

        btnAction.setOnClickListener(view -> {
            if (!intentData.isEmpty()) {
                if (isEmail) {
                    startActivity(new Intent(ScannedBarcodeActivity.this, EmailActivity.class)
                            .putExtra("email_address", intentData));
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                }
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::processBarcodeImage);

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("BarcodeScanner", "Camera use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processBarcodeImage(ImageProxy image) {
        try {
            InputImage inputImage = InputImage.fromMediaImage(image.getImage(),
                    image.getImageInfo().getRotationDegrees());
            scanBarcodes(inputImage);
        } catch (Exception e) {
            Log.e("BarcodeScanner", "Image conversion failed", e);
        } finally {
            image.close();
        }
    }

    private void scanBarcodes(InputImage image) {
        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        Barcode barcode = barcodes.get(0);
                        intentData = barcode.getRawValue();
                        txtBarcodeValue.setText(intentData);

                        if (barcode.getValueType() == Barcode.TYPE_EMAIL) {
                            isEmail = true;
                            btnAction.setText("ADD CONTENT TO THE MAIL");
                        } else {
                            isEmail = false;
                            btnAction.setText("LAUNCH URL");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("BarcodeScanner", "Barcode scan failed", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
