package com.example.dasnet;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.appcompat.app.AppCompatActivity;

public class Description extends AppCompatActivity {
    private String stepDescription;
    private TextView stepDescriptionText;
    private VideoView videoView;
    private Button viewImageButton;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_description);

        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);

        // Get the step description
        stepDescription = getIntent().getStringExtra("StepDescription");
        stepDescriptionText = findViewById(R.id.StepDescription);
        stepDescriptionText.setText(stepDescription);

        // Initialize VideoView
        videoView = findViewById(R.id.videoView);

        // Set video path
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video;

        // Set video URI
        videoView.setVideoURI(Uri.parse(videoPath));

        // Add media controller for video controls
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Start playing the video
        videoView.start();

        // Initialize ImageView and Button
        imageView = findViewById(R.id.imageView);
        viewImageButton = findViewById(R.id.viewImageButton);

        // Set Button click listener to toggle ImageView visibility
        viewImageButton.setOnClickListener(v -> {
            if (imageView.getVisibility() == View.GONE) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.technologycenter);
            } else {
                imageView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // Release resources related to the VideoView
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
