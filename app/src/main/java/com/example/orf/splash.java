package com.example.orf;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

public class splash extends AppCompatActivity {

    private TextView bigText;
    private FrameLayout textContainer;
    private NestedScrollView scrollView;
    private int initialLeftMargin;
    private ImageView logo;
    private FullScreenVideoView backdrop;
    private Uri videoUri;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set SplashActivity layout

        backdrop = findViewById(R.id.backdrop);
        videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bgvideo); // Replace with your video resource

        initializeVideoView();

        bigText = findViewById(R.id.big_text);
        textContainer = findViewById(R.id.text_container);
        scrollView = findViewById(R.id.nested_scroll_view);
        logo = findViewById(R.id.logo);

        // Initialize Vibrator
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // Store the initial left margin of the text
        initialLeftMargin = ((FrameLayout.LayoutParams) bigText.getLayoutParams()).leftMargin;

        // Set up scroll listener
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // Calculate the scroll progress (0 to 1)
                float scrollProgress = Math.min(scrollY / (float) bigText.getHeight(), 1f);

                // Move the text left
                float translationX = -scrollProgress * bigText.getWidth();
                bigText.setTranslationX(translationX);

                // Fade out the text
                bigText.setAlpha(1 - scrollProgress);

                // Adjust the left margin of bigText
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bigText.getLayoutParams();
                params.leftMargin = (int) (initialLeftMargin - (scrollProgress * initialLeftMargin));
                bigText.setLayoutParams(params);

                // Move the logo left
                float logoTranslationX = -scrollProgress * logo.getWidth();
                logo.setTranslationX(logoTranslationX);

                // Fade out the logo
                logo.setAlpha(1 - scrollProgress);

                // Adjust the left margin of logo if needed
                FrameLayout.LayoutParams logoParams = (FrameLayout.LayoutParams) logo.getLayoutParams();
                logoParams.leftMargin = (int) (initialLeftMargin - (scrollProgress * initialLeftMargin));
                logo.setLayoutParams(logoParams);
            }
        });

        // Find all FrameLayouts (buttons) by their IDs
        FrameLayout button1 = findViewById(R.id.button1);
        FrameLayout button2 = findViewById(R.id.button2);
        FrameLayout button3 = findViewById(R.id.button3);
        FrameLayout button4 = findViewById(R.id.button4);
        FrameLayout button5 = findViewById(R.id.button5);
        FrameLayout button6 = findViewById(R.id.button6);
        FrameLayout button7 = findViewById(R.id.button7);
        FrameLayout button8 = findViewById(R.id.button8);
        FrameLayout button9 = findViewById(R.id.button9);

        // Set OnClickListener for each button
        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vibrate when button is clicked
                if (vibrator != null) {
                    vibrator.vibrate(1); // Vibrate for 100 milliseconds
                }

                Class<?> targetActivity;
                int id = v.getId();
                if (id == R.id.button1) {
                    targetActivity = Button1.class;
                } else if (id == R.id.button2) {
                    targetActivity = button2.class;
                } else if (id == R.id.button3) {
                    targetActivity = button3.class;
                } else if (id == R.id.button4) {
                    targetActivity = button4.class;
                } else if (id == R.id.button5) {
                    targetActivity = button5.class;
                } else if (id == R.id.button6) {
                    targetActivity = button6.class;
                } else if (id == R.id.button7) {
                    targetActivity = button7.class;
                } else if (id == R.id.button8) {
                    // Open the Google Form link directly in a browser
                    openWebPage("https://forms.gle/zRLooQKuyiEndJ749");
                    return; // Return early as we don't want to call openActivity for button8

                }else if (id == R.id.button9) {
                    // Open the Google Form link directly in a browser
                    openWebPage("https://maps.app.goo.gl/rBPXL6cvAcAQ6VFe6");
                    return; }
                else {
                    return;
                }
                openActivity(targetActivity);
            }
        };

        // Set the click listener for all buttons
        button1.setOnClickListener(buttonClickListener);
        button2.setOnClickListener(buttonClickListener);
        button3.setOnClickListener(buttonClickListener);
        button4.setOnClickListener(buttonClickListener);
        button5.setOnClickListener(buttonClickListener);
        button6.setOnClickListener(buttonClickListener);
        button7.setOnClickListener(buttonClickListener);
        button8.setOnClickListener(buttonClickListener);
        button9.setOnClickListener(buttonClickListener);
    }

    private void openActivity(Class<?> targetActivity) {
        Intent intent = new Intent(splash.this, targetActivity);
        startActivity(intent);
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void initializeVideoView() {
        backdrop.setVideoURI(videoUri);

        // Set media player listeners to loop the video
        backdrop.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        // Start playing the video
        backdrop.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!backdrop.isPlaying()) {
            backdrop.setVideoURI(videoUri);
            backdrop.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backdrop.isPlaying()) {
            backdrop.pause();
        }
    }
}