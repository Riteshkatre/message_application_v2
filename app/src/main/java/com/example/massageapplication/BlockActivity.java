package com.example.massageapplication;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.massage.MessageActivity;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BlockActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageView ivUnBlock, imgBack;
    LinearLayout linNoData;
    private BlockedAdapter adapter;
    private ArrayList<SmsModel> blockedMessagesList;
    private final Map<String, String> contactCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        recyclerView = findViewById(R.id.blocked_recycler_view);
        ivUnBlock = findViewById(R.id.ivUnBlock);
        imgBack = findViewById(R.id.imgBack);
        linNoData = findViewById(R.id.linNoData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ब्लॉक किए गए उपयोगकर्ताओं को लोड करें और प्रदर्शित करें
        blockedMessagesList = loadBlockedMessages();
        adapter = new BlockedAdapter(this, blockedMessagesList);
        recyclerView.setAdapter(adapter);
        if (blockedMessagesList.isEmpty()) {
            recyclerView.setVisibility(View.GONE); // RecyclerView को छिपाएं
            linNoData.setVisibility(View.VISIBLE); // "No Data" TextView को दिखाएं
        } else {
            recyclerView.setVisibility(View.VISIBLE); // RecyclerView को दिखाएं
            linNoData.setVisibility(View.GONE); // "No Data" TextView को छिपाएं
        }

        adapter.setOnItemClickListener(new BlockedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SmsModel smsModel) {
                Intent intent = new Intent(BlockActivity.this, MessageActivity.class);
                intent.putExtra("address", smsModel.getSender());
                intent.putExtra("date", smsModel.getSender());
                startActivity(intent);
            }

            @Override
            public void longClickListener(int position, SmsModel smsModel) {
                ivUnBlock.setVisibility(View.VISIBLE);
                ivUnBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showConfirmationDialog(smsModel);
                    }
                });
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra("refresh", true);
                setResult(RESULT_OK,resultIntent);
                finish();
            }
        });
    }

    private ArrayList<SmsModel> loadBlockedMessages() {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedMessagesJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> allMessages = new Gson().fromJson(blockedMessagesJson, new TypeToken<ArrayList<SmsModel>>() {}.getType());

        ArrayList<SmsModel> blockedMessages = new ArrayList<>();
        for (SmsModel message : allMessages) {
            if (message.isBlocked()) {
                String contactName = getContactName(message.getSender()); // Get contact name
                message.setSender(contactName); // Update sender with contact name
                blockedMessages.add(message);
            }
        }
        return blockedMessages;
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


    private void unblockUser(SmsModel smsModel) {
        blockedMessagesList.remove(smsModel);
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String updatedJson = new Gson().toJson(blockedMessagesList);
        editor.putString("blockedContacts", updatedJson);
        editor.apply();
        adapter.updateList(blockedMessagesList);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("refresh", true);
        setResult(RESULT_OK,resultIntent);
        Toast.makeText(BlockActivity.this, smsModel.getSender() + " has been unblocked.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showConfirmationDialog(SmsModel smsModel) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to unblock this user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unblockUser(smsModel);
                        ivUnBlock.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}
