package com.example.massageapplication.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.databinding.ActivityContactDetalisBinding;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ContactDetailsActivity extends AppCompatActivity {
    private ActivityContactDetalisBinding b;
    private String name, number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityContactDetalisBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");

        if (name != null) {
            b.name.setText(name);
            b.shortName.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
        b.number.setText(number);

        checkBlockStatus(number);
        setupNotificationSwitch();

        b.ivArchiveBack.setOnClickListener(v -> finish());
        b.callImage.setOnClickListener(v -> makePhoneCall());
        b.llDeleteConversation.setOnClickListener(v ->
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
        );
        b.layBlock.setOnClickListener(v -> handleBlockAction());
    }

    // Setup Notification Switch
    private void setupNotificationSwitch() {
        boolean isEnabled = isNotificationEnabled(number);
        b.notificationSwitch.setChecked(isEnabled);

        b.notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setNotificationPreference(number, isChecked);
            String message = isChecked ? "Notifications Enabled" : "Notifications Disabled";
            Toast.makeText(this, message + " for " + name, Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isNotificationEnabled(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("NotificationPreferences", MODE_PRIVATE);
        return preferences.getBoolean(phoneNumber, true);
    }

    private void setNotificationPreference(String phoneNumber, boolean isEnabled) {
        SharedPreferences preferences = getSharedPreferences("NotificationPreferences", MODE_PRIVATE);
        preferences.edit().putBoolean(phoneNumber, isEnabled).apply();
    }

    private void makePhoneCall() {
        String phoneNumber = getValidPhoneNumber();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 1);
        }
    }

    private String getValidPhoneNumber() {
        return number != null && !number.trim().isEmpty() ? number : null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted. Try again to make the call.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied. Cannot make the call.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBlockStatus(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>(){}.getType());

        for (SmsModel contact : blockedContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                b.tvBlock.setText("Unblock");
                return;
            }
        }
        b.tvBlock.setText("Block");
    }

    private void handleBlockAction() {
        if (b.tvBlock.getText().toString().equals("Block")) {
            showBlockDialog();
        } else {
            unblockContact(number);
        }
    }

    private void showBlockDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Block Contact")
                .setMessage("Are you sure you want to block this contact?")
                .setPositiveButton("Yes", (dialog, which) -> blockContact(number))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void blockContact(String phoneNumber) {
        SmsModel blockedMessage = new SmsModel(phoneNumber, "Blocked", "", "", System.currentTimeMillis(), "Blocked");
        blockedMessage.setBlocked(true);
        saveBlockedContact(blockedMessage);
        Toast.makeText(this, "Contact has been blocked.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, BlockActivity.class));
        b.tvBlock.setText("Unblock");
    }

    private void saveBlockedContact(SmsModel blockedMessage) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(preferences.getString("blockedContacts", "[]"), new TypeToken<ArrayList<SmsModel>>(){}.getType());
        blockedContacts.add(blockedMessage);
        preferences.edit().putString("blockedContacts", new Gson().toJson(blockedContacts)).apply();
    }

    private void unblockContact(String phoneNumber) {
        new AlertDialog.Builder(this)
                .setTitle("Unblock Contact")
                .setMessage("Are you sure you want to unblock this contact?")
                .setPositiveButton("Yes", (dialog, which) -> performUnblock(phoneNumber))
                .setNegativeButton("No", null)
                .create().show();
    }

    private void performUnblock(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(preferences.getString("blockedContacts", "[]"), new TypeToken<ArrayList<SmsModel>>(){}.getType());
        blockedContacts.removeIf(contact -> contact.getSender().equals(phoneNumber));
        preferences.edit().putString("blockedContacts", new Gson().toJson(blockedContacts)).apply();
        b.tvBlock.setText("Block");
        Toast.makeText(this, "Contact has been unblocked.", Toast.LENGTH_SHORT).show();
    }
}
