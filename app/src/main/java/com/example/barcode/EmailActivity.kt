package com.example.barcode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.barcode.databinding.ActivityEmailBinding

class EmailActivity : AppCompatActivity() {

    // Declare the binding variable
    private lateinit var binding: ActivityEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set text for txtEmailAddress using the binding object
        val emailAddress = intent.getStringExtra("email_address")
        binding.txtEmailAddress.text = if (emailAddress != null) {
            "Recipient: $emailAddress"
        } else {
            "No recipient found."
        }

        // Set click listener using the binding
        binding.btnSendEmail.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        val email = intent.getStringExtra("email_address") ?: ""
        if (email.isEmpty()) {
            binding.txtEmailAddress.text = "No recipient found."
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, binding.inSubject.text.toString().trim())
            putExtra(Intent.EXTRA_TEXT, binding.inBody.text.toString().trim())
        }

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
}
