package com.example.massageapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.massageapplication.language.LanguageSelectionActivity;
import com.example.massageapplication.massage.MainActivity;

public class SplashScreen extends AppCompatActivity {

    ImageView ivMessageLogo;
    boolean isFirstLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ivMessageLogo = findViewById(R.id.ivMessageLogo);

        // Check if language is already selected
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isFirstLaunch = sharedPreferences.getBoolean("first_launch", true);

        // Start splash animation
        startMainActivityWithAnimation();
    }

    private void startMainActivityWithAnimation() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(2000);

        Animation scaleUp = new ScaleAnimation(
                0.5f, 1.0f,
                0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(1500);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(scaleUp);

        ivMessageLogo.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                navigateToLanguageSelectionOrMainActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void navigateToLanguageSelectionOrMainActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean languageSelected = sharedPreferences.contains("language");

        // If it's the first launch and language not selected, go to LanguageSelectionActivity
        if (isFirstLaunch && !languageSelected) {
            // Mark that the app has been launched once
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first_launch", false);
            editor.apply();

            Intent intent = new Intent(SplashScreen.this, LanguageSelectionActivity.class);
            startActivity(intent);
        } else {
            // If language is selected, go directly to MainActivity
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
