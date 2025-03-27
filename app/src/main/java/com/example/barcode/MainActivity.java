package com.example.barcode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnScanBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScanBarcode = findViewById(R.id.btnScanBarcode);

        btnScanBarcode.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScannedBarcodeActivity.class);
            startActivity(intent);
        });
    }
}
