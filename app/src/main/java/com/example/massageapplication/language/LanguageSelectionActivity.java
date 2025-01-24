package com.example.massageapplication.language;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.massage.MainActivity;
import com.example.massageapplication.R;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageSelectionActivity extends AppCompatActivity implements LanguageAdapter.SetUpInterFace {
    LanguageAdapter adapter;
    LinearLayout done, nativeAdContainer;
    boolean languageSelect = false;
    ImageView ivLanguageBack;
    private AdLoader adLoader;
    String selectedLanguage;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        ivLanguageBack = findViewById(R.id.ivLanguageBack);
        recyclerView = findViewById(R.id.recyclerView);
        nativeAdContainer = findViewById(R.id.nativeAdContainer);
        done = findViewById(R.id.done);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ivLanguageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            }
        });

        // Initialize the Mobile Ads SDK on the main thread
        MobileAds.initialize(this, initializationStatus -> {
            // Initialize AdLoader after MobileAds SDK is initialized
            loadNativeAd();
        });

        // Populate languages
        List<LanguageModel> languages = new ArrayList<>();
        languages.add(new LanguageModel("en", "English", "(English)", R.drawable.english_flag));
        languages.add(new LanguageModel("hi", "Hindi", "(हिंदी)", R.drawable.hindi_flag));
        languages.add(new LanguageModel("gu", "Gujarati", "(ગુજરાતી)", R.drawable.hindi_flag));
        languages.add(new LanguageModel("", "Chinese", "(中文)", R.drawable.chinese_flag));
        languages.add(new LanguageModel("", "Spanish", "(Española)", R.drawable.spanish_flag));
        languages.add(new LanguageModel("", "Arabic", "(العربية)", R.drawable.arabic_flag));
        languages.add(new LanguageModel("", "German", "(Deutsch)", R.drawable.german_flag));
        languages.add(new LanguageModel("", "French", "(Français)", R.drawable.french_flag));
        languages.add(new LanguageModel("", "Portuguese", "(Português)", R.drawable.portegues_flag));

        adapter = new LanguageAdapter(this, languages, this);
        recyclerView.setAdapter(adapter);

        done.setOnClickListener(v -> {
            if (languageSelect) {
                // Retrieve the selected language from SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                selectedLanguage = sharedPreferences.getString("language", "en"); // Default to English

                // Apply the selected language
                setLocale(selectedLanguage);

                // Redirect to MainActivity
                Intent intent = new Intent(LanguageSelectionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LanguageSelectionActivity.this, "Please Select Language First", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadNativeAd() {
        adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110") // Test Native Ad ID
                .forNativeAd(nativeAd -> {
                    // Inflate the custom layout for native ads
                    NativeAdView adView = (NativeAdView) getLayoutInflater().inflate(R.layout.native_ad_layout, null);

                    // Populate the native ad with data
                    populateNativeAdView(nativeAd, adView);

                    // Add the ad view to your container
                    nativeAdContainer.removeAllViews();
                    nativeAdContainer.addView(adView);
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .build())
                .build();

        // Load the native ad
        AdRequest adRequest = new AdRequest.Builder().build();
        adLoader.loadAd(adRequest);
    }

    @Override
    public void onLanguageItemClick(int position, LanguageModel languageModel) {
        // Set the selected language
        selectedLanguage = languageModel.getXmlLanguageName();
        languageSelect = true;

        // Store the selected language in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", selectedLanguage);  // Save the selected language
        editor.apply();

        // Update the background to indicate selection
        done.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_color_primary));
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        android.content.res.Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);

        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        // Restart the activity to apply the language change
        Intent intent = new Intent(this, LanguageSelectionActivity.class);
        finish();
        startActivity(intent);
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the headline
        adView.setHeadlineView(adView.findViewById(R.id.adTitle));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // Set the body text
        adView.setBodyView(adView.findViewById(R.id.adDescription));
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());

        // Set the image
        adView.setImageView(adView.findViewById(R.id.adImage));
        ((ImageView) adView.getImageView()).setImageDrawable(nativeAd.getImages().get(0).getDrawable());

        // Set the call to action button
        adView.setCallToActionView(adView.findViewById(R.id.adCtaButton));
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // Register the ad
        adView.setNativeAd(nativeAd);
    }
}
