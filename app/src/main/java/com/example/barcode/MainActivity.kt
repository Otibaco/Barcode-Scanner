package com.example.barcode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.barcode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener using binding
        binding.btnScanBarcode.setOnClickListener {
            val intent = Intent(this, ScannedBarcodeActivity::class.java)
            startActivity(intent)
        }
    }
}
