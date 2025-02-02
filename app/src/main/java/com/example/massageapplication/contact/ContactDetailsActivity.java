package com.example.massageapplication.contact;

import static com.example.massageapplication.R.string.*;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.R;
import com.example.massageapplication.databinding.ActivityContactDetalisBinding;
import com.example.massageapplication.drawerActivity.Archive;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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

        checkBlockStatus(number); // Check block status on activity creation
        setupNotificationSwitch();
        checkArchiveStatus(number);

        b.ivArchiveBack.setOnClickListener(v -> finish());
        b.callImage.setOnClickListener(v -> makePhoneCall());
        b.llDeleteConversation.setOnClickListener(v -> Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show());
        b.layBlock.setOnClickListener(v -> handleBlockAction());

        b.llArchiveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleArchiveAction();
            }
        });
    }

    private void handleBlockAction() {
        if (b.tvBlock.getText().toString().equals(getString(R.string.block))) {
            showBlockDialog();
        } else {
            Toast.makeText(ContactDetailsActivity.this, R.string.this_contact_is_already_blocked, Toast.LENGTH_SHORT).show();
        }
    }
    private void handleArchiveAction() {
        if (b.tvArchive.getText().toString().equals(R.string.archive)) {
            showArchiveDialog();
        } else {
            Toast.makeText(ContactDetailsActivity.this, R.string.this_contact_is_already_archive, Toast.LENGTH_SHORT).show();
        }
    }

    private void showBlockDialog() {
        new AlertDialog.Builder(this).setTitle(block_contact).setMessage(are_you_sure_you_want_to_block_this_contact).setPositiveButton(yes, (dialog, which) -> blockContact(number)).setNegativeButton(no, (dialog, which) -> dialog.dismiss()).create().show();
    }
    private void showArchiveDialog() {
        new AlertDialog.Builder(this).setTitle(archive_contact).setMessage(are_you_sure_you_want_to_archive_this_contact).setPositiveButton(yes, (dialog, which) -> archiveContact(number)).setNegativeButton(no, (dialog, which) -> dialog.dismiss()).create().show();
    }

    private void blockContact(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(preferences.getString("blockedContacts", "[]"), new TypeToken<ArrayList<SmsModel>>() {
        }.getType());

        // Check if the contact is already blocked
        for (SmsModel contact : blockedContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                Toast.makeText(this,
                        R.string.this_number_is_already_blocked,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Add the contact to the blocked list
        SmsModel blockedMessage = new SmsModel(phoneNumber, "Blocked", "", "", System.currentTimeMillis(), "Blocked",null);
        blockedMessage.setBlocked(true);
        blockedContacts.add(blockedMessage);
        preferences.edit().putString("blockedContacts", new Gson().toJson(blockedContacts)).apply();

        Toast.makeText(this, contact_has_been_blocked, Toast.LENGTH_SHORT).show();

        // Navigate to BlockActivity
        Intent intent = new Intent(ContactDetailsActivity.this, BlockActivity.class);
        startActivity(intent);

        // Update UI
        checkBlockStatus(phoneNumber);
    }
    private void archiveContact(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("ArchivedContacts", MODE_PRIVATE);
        ArrayList<SmsModel> archiveContacts = new Gson().fromJson(preferences.getString("archiveContacts", "[]"), new TypeToken<ArrayList<SmsModel>>() {
        }.getType());

        // Check if the contact is already blocked
        for (SmsModel contact : archiveContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                Toast.makeText(this, this_is_a_already_archive, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SmsModel archiveMessage = new SmsModel(phoneNumber, "Archive", "", "", System.currentTimeMillis(), "Archive",null);
        archiveMessage.setArchive(true);
        archiveContacts.add(archiveMessage);
        preferences.edit().putString("archiveContacts", new Gson().toJson(archiveContacts)).apply();

        Toast.makeText(this, contact_has_been_archive, Toast.LENGTH_SHORT).show();

        // Navigate to BlockActivity
        Intent intent = new Intent(ContactDetailsActivity.this, Archive.class);
        startActivity(intent);

        // Update UI
        checkArchiveStatus(phoneNumber);
    }

    private void checkBlockStatus(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>() {
        }.getType());

        for (SmsModel contact : blockedContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                b.tvBlock.setText(R.string.unblock);
                return;
            }
        }
        b.tvBlock.setText(R.string.block);
    }
    private void checkArchiveStatus(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("ArchivedContacts", MODE_PRIVATE);
        String archiveContactsJson = preferences.getString("archiveContacts", "[]");

        // Log the JSON data for debugging
        Log.d("ArchiveContactsJson", archiveContactsJson);

        ArrayList<SmsModel> archiveContacts = new ArrayList<>();
        try {
            archiveContacts = new Gson().fromJson(archiveContactsJson, new TypeToken<ArrayList<SmsModel>>() {}.getType());
        } catch (JsonSyntaxException e) {
            Log.e("JsonSyntaxException", "Invalid JSON format: " + e.getMessage());
            // Handle the error, e.g., by clearing the invalid data
            preferences.edit().remove("archiveContacts").apply();
        }

        for (SmsModel contact : archiveContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                b.tvArchive.setText(R.string.unarchive);
                return;
            }
        }
        b.tvArchive.setText(R.string.archive); // Fix: Set text to "Archive" if not found
    }

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
        // Check if 'number' is not null and not empty
        if (number != null && !number.trim().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 1);
            }
        } else {
            // If 'number' is null or empty, use 'name'
            if (name != null && !name.trim().isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + name));
                if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                }
            } else {
                // Handle the case where both number and name are null or empty
                Toast.makeText(this, "No valid phone number or name to call", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String getValidPhoneNumber() {
        return number != null && !number.trim().isEmpty() ? number : null;
    }

}