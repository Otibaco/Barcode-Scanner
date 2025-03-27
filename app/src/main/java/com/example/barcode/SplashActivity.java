package com.example.barcode;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find the text view
        TextView txtBarcode = findViewById(R.id.txtBarcode);

        // Load the bounce animation
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        txtBarcode.startAnimation(bounceAnim);

        // Delay before opening MainActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 3000); // 3 seconds
    }
}
