package com.example.tinkdrao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullScreenMediaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_media);

        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);
        VideoView fullScreenVideoView = findViewById(R.id.fullScreenVideoView);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String videoUrl = intent.getStringExtra("videoUrl");

        if (imageUrl != null) {
            fullScreenImageView.setVisibility(View.VISIBLE);
            fullScreenVideoView.setVisibility(View.GONE);
            Glide.with(this).load(imageUrl).into(fullScreenImageView);
        } else if (videoUrl != null) {
            fullScreenImageView.setVisibility(View.GONE);
            fullScreenVideoView.setVisibility(View.VISIBLE);

            Uri videoUri = Uri.parse(videoUrl);
            fullScreenVideoView.setVideoURI(videoUri);

            MediaController mediaController = new MediaController(this);
            fullScreenVideoView.setMediaController(mediaController);
            mediaController.setAnchorView(fullScreenVideoView);

            fullScreenVideoView.start();
        }
    }
}
