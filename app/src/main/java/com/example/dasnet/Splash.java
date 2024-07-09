package com.example.dasnet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_DELAY = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);

        // Load GIF into ImageView
        ImageView imageViewGif = findViewById(R.id.imageViewGif);
        Glide.with(this)
                .asGif()
                .load(R.raw.dasnetlogo)
                .into(imageViewGif);

        // Delay for splash screen
        new Handler().postDelayed(this::startMainActivity, SPLASH_DELAY);
    }

    private void startMainActivity() {
        Intent intent = new Intent(Splash.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
