package com.example.barcode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmailActivity extends AppCompatActivity {

    private EditText inSubject, inBody;
    private TextView txtEmailAddress;
    private Button btnSendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        initViews();
    }

    private void initViews() {
        inSubject = findViewById(R.id.inSubject);
        inBody = findViewById(R.id.inBody);
        txtEmailAddress = findViewById(R.id.txtEmailAddress);
        btnSendEmail = findViewById(R.id.btnSendEmail);

        String emailAddress = getIntent().getStringExtra("email_address");
        if (emailAddress != null) {
            txtEmailAddress.setText("Recipient: " + emailAddress);
        }

        btnSendEmail.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String email = getIntent().getStringExtra("email_address");
        if (email == null || email.isEmpty()) {
            txtEmailAddress.setText("No recipient found.");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822"); // Ensures email-specific apps handle the intent
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, inSubject.getText().toString().trim());
        intent.putExtra(Intent.EXTRA_TEXT, inBody.getText().toString().trim());

        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
