package com.example.massageapplication.drawerActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.R;
import com.example.massageapplication.massage.MessageActivity;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Archive extends AppCompatActivity {
    private final Map<String, String> contactCache = new HashMap<>();
    private RecyclerView recyclerView;
    private ArchiveAdapter pinnedMessagesAdapter;
    private ActivityResultLauncher<Intent> sendSmsLauncher;
    private ImageView ivArchiveBack, ivUnArchive;
    private LinearLayout llNoData;
    private ArrayList<SmsModel> pinnedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupActivityLauncher();
    }

    private void initViews() {
        ivArchiveBack = findViewById(R.id.imgBack);
        ivUnArchive = findViewById(R.id.ivUnArchieve);
        recyclerView = findViewById(R.id.rcvArcheiv);
        llNoData = findViewById(R.id.llNoData); // Initialize "No Data Found" layout

        // Apply window insets for a smooth UI experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pinnedMessages = loadArchivedMessages();
        pinnedMessagesAdapter = new ArchiveAdapter(pinnedMessages);
        recyclerView.setAdapter(pinnedMessagesAdapter);

        checkEmptyState(); // Check if list is empty
    }

    private void setupListeners() {
        ivArchiveBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        pinnedMessagesAdapter.setOnItemClickListener(new ArchiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SmsModel smsModel) {
                openMessageActivity(smsModel);
            }

            @Override
            public void longClickListener(int position, SmsModel smsModel) {
                ivUnArchive.setVisibility(View.VISIBLE);
                ivUnArchive.setOnClickListener(v -> showConfirmationDialog(smsModel));
            }
        });
    }

    private void setupActivityLauncher() {
        sendSmsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                loadArchivedMessages(); // Refresh data
                checkEmptyState(); // Check if empty
            }
        });
    }

    private void openMessageActivity(SmsModel smsModel) {
        Intent intent = new Intent(Archive.this, MessageActivity.class);
        intent.putExtra("address", smsModel.getSender());
        intent.putExtra("date", smsModel.getSender());
        sendSmsLauncher.launch(intent);
    }


    private ArrayList<SmsModel> loadArchivedMessages() {
        SharedPreferences preferences = getSharedPreferences("ArchivedContacts", MODE_PRIVATE);
        String archiveMessagesJson = preferences.getString("archiveContacts", "[]");
        ArrayList<SmsModel> allMessages = new Gson().fromJson(archiveMessagesJson, new TypeToken<ArrayList<SmsModel>>() {
        }.getType());

        ArrayList<SmsModel> archiveMessages = new ArrayList<>();
        for (SmsModel message : allMessages) {
            if (message.isArchive()) { // Sirf archived messages ko load karen
                String contactName = getContactName(message.getSender()); // Get contact name
                message.setSender(contactName); // Update sender with contact name
                archiveMessages.add(message);
            }
        }
        return archiveMessages;
    }

    private String getContactName(String phoneNumber) {
        if (contactCache.containsKey(phoneNumber)) {
            return contactCache.get(phoneNumber);
        }
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.withAppendedPath(android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        String contactName = phoneNumber;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        contactCache.put(phoneNumber, contactName);
        return contactName;
    }

    private void showConfirmationDialog(SmsModel smsModel) {
        new AlertDialog.Builder(this).setMessage(R.string.are_you_sure_you_want_to_unarchive_this_user).setPositiveButton(R.string.yes, (dialog, which) -> {
            unarchiveUser(smsModel);
            ivUnArchive.setVisibility(View.GONE);
        }).setNegativeButton(R.string.no, null).create().show();
    }

    private void unarchiveUser(SmsModel smsModel) {
        smsModel.setArchive(false);
        pinnedMessages.remove(smsModel);
        SharedPreferences preferences = getSharedPreferences("ArchivedContacts", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String updatedJson = new Gson().toJson(pinnedMessages);
        editor.putString("archiveContacts", updatedJson);
        editor.apply();
        pinnedMessagesAdapter.updateList(pinnedMessages);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("refresh", true);
        setResult(RESULT_OK,resultIntent);
        Toast.makeText(Archive.this, smsModel.getSender() + R.string.has_been_unarchived, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkEmptyState() {
        if (pinnedMessages.isEmpty()) {
            llNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            llNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

}
