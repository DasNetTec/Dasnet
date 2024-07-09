package com.example.dasnet;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class Contact extends AppCompatActivity {

    // UI elements


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_us);


        // Initialize the UI elements
        initializeUI();


    }

    /**
     * Initialize UI elements.
     */
    private void initializeUI() {
        // Initialize cand start Lottie animation
        LottieAnimationView animationView = findViewById(R.id.AnimationView);
        animationView.setAnimation(R.raw.correo);
        animationView.playAnimation();

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }



}