package com.example.massageapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.role.RoleManager;
import android.provider.Telephony;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.massageapplication.language.LanguageSelectionActivity;
import com.example.massageapplication.massage.MainActivity;

public class SplashScreen extends AppCompatActivity {

    private static final String APP_PREFERENCES = "AppPreferences";
    private static final String PREF_DEFAULT_SMS_REQUESTED = "defaultSmsRequested";

    ImageView ivMessageLogo;
    boolean isFirstLaunch;
    private ActivityResultLauncher<Intent> defaultSmsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ivMessageLogo = findViewById(R.id.ivMessageLogo);
        prepareDefaultSmsLauncher();

        // Check if language is already selected
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
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
                requestDefaultSmsIfNeeded();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void requestDefaultSmsIfNeeded() {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        boolean alreadyRequested = sharedPreferences.getBoolean(PREF_DEFAULT_SMS_REQUESTED, false);
        boolean isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this) != null
                && Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName());

        if (!alreadyRequested && !isDefaultSmsApp) {
            sharedPreferences.edit().putBoolean(PREF_DEFAULT_SMS_REQUESTED, true).apply();
            requestDefaultSmsRole();
            return;
        }

        navigateToLanguageSelectionOrMainActivity();
    }

    private void navigateToLanguageSelectionOrMainActivity() {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
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

    private void requestDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                defaultSmsLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS));
            } else {
                Toast.makeText(this, "Default SMS role is not available on this device.", Toast.LENGTH_LONG).show();
                navigateToLanguageSelectionOrMainActivity();
            }
        } else {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            defaultSmsLauncher.launch(intent);
        }
    }

    private void prepareDefaultSmsLauncher() {
        defaultSmsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (!isFinishing()) {
                            navigateToLanguageSelectionOrMainActivity();
                        }
                    }
                }
        );
    }
}
