package com.example.massageapplication.massage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.massageapplication.R;
import com.example.massageapplication.contact.ContactDetailsActivity;
import com.example.massageapplication.databinding.ActivityMessageBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {
    private final ArrayList<SmsModel> messagesList = new ArrayList<>();
    ActivityMessageBinding b;
    private MessageAdapter messageAdapter;
    private String senderAddress;
    private String senderName;
    private String phoneNumber;

    private final ContentObserver smsObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Reload messages when there is a change in SMS content
            loadMessagesFromSender();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.imgCalender.setOnClickListener(v -> {
            // Implement calendar click action
        });

        // Get sender info from Intent
        senderAddress = getIntent().getStringExtra("address");
        senderName = getIntent().getStringExtra("date");

        b.contactName.setText(senderAddress);
        phoneNumber = getPhoneNumberFromContacts(senderAddress);
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            b.phoneNumber.setText(phoneNumber);
        } else {
            b.phoneNumber.setText("");
        }
        if (senderAddress != null && senderAddress.length() > 0) {
            String initial = String.valueOf(senderAddress.charAt(0)).toUpperCase();
            b.firstName.setText(initial);

            // Set color for initial
            int color = getColorForInitial(initial);
            b.nameLay.setBackgroundResource(R.drawable.circle_background);
            GradientDrawable background = (GradientDrawable) b.nameLay.getBackground();
            background.setColor(color);
        }

        // Initialize RecyclerView
        b.rcvMassage.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messagesList);
        b.rcvMassage.setAdapter(messageAdapter);

        b.imgBack.setOnClickListener(v -> onBackPressed());

        // Load messages
        if (senderAddress != null && !senderAddress.isEmpty()) {
            loadMessagesFromSender();
        } else {
            Toast.makeText(this, "Sender address is missing", Toast.LENGTH_SHORT).show();
        }

        b.laySend.setOnClickListener(v -> {
            String messageText = b.etEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                } else {
                    if (isBlocked(senderAddress)) {
                        Toast.makeText(this, "Cannot send message to blocked contact", Toast.LENGTH_SHORT).show();
                    } else {
                        sendMessage(messageText);
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        b.imgThreeDots.setOnClickListener(v -> {
            // Create a PopupMenu anchored to imgThreeDots
            PopupMenu popupMenu = new PopupMenu(this, b.imgThreeDots);

            // Inflate menu items or add them programmatically
            popupMenu.getMenu().add("Schedule");
            popupMenu.getMenu().add("More Details");

            // Set a click listener for menu items
            popupMenu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                if (title.equals("Schedule")) {
                    // Handle "Schedule" action
                    Toast.makeText(this, "Schedule clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (title.equals("More Details")) {
                    // Handle "More Details" action - redirect to another activity
                    Intent intent = new Intent(this, ContactDetailsActivity.class);
                    intent.putExtra("name", senderAddress);
                    intent.putExtra("number", phoneNumber);

                    startActivity(intent);
                    return true;
                }
                return false;
            });

            // Show the PopupMenu
            popupMenu.show();
        });

        b.imgSearch.setOnClickListener(v -> {
            b.llSearch.setVisibility(View.VISIBLE);
        });

        b.imgMainClose.setOnClickListener(v -> {
            b.llSearch.setVisibility(View.GONE);
            b.searchText.getText().clear();
            b.llNoData.setVisibility(View.GONE);
        });

        b.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageAdapter.search(s.toString(), b.rcvMassage, b.llNoData);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadMessagesFromSender() {
        new Thread(() -> {
            ContentResolver cr = getContentResolver();
            phoneNumber = getPhoneNumberFromContacts(senderAddress);

            if (phoneNumber != null) {
                phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, "IN"); // Adjust country code if needed
            }

            Cursor cursor = cr.query(Uri.parse("content://sms/"), null, phoneNumber != null ? "address = ?" : "address LIKE ?", phoneNumber != null ? new String[]{phoneNumber} : new String[]{"%" + senderAddress + "%"}, "date ASC");

            if (cursor != null) {
                ArrayList<SmsModel> newMessagesList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    Date date = new Date(dateMillis);
                    int messageType = cursor.getInt(cursor.getColumnIndexOrThrow("type"));

                    String type = (messageType == 1) ? "received" : "sent";

                    if (isBlocked(senderAddress)) {
                        continue;  // Skip blocked sender's messages
                    }

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String time = timeFormat.format(date);

                    newMessagesList.add(new SmsModel(type.equals("received") ? senderName : "You", body, date.toString(), time, dateMillis, type));
                }
                cursor.close();

                // Update messagesList in the main thread and notify the adapter
                new Handler(Looper.getMainLooper()).post(() -> {
                    messagesList.clear(); // Clear the old data
                    messagesList.addAll(newMessagesList); // Add the new data

                    if (messagesList.isEmpty()) {
                        Toast.makeText(this, "No messages found", Toast.LENGTH_SHORT).show();
                    }
                    messageAdapter.notifyDataSetChanged(); // Notify the adapter
                    if (!messagesList.isEmpty()) {
                        b.rcvMassage.scrollToPosition(messagesList.size() - 1); // Scroll to the bottom
                    }
                });
            }
        }).start();
    }

    private boolean isBlocked(String phoneNumber) {
        // Check if the phone number is in the blocked contacts list
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>(){}.getType());

        for (SmsModel contact : blockedContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                return true;  // Contact is blocked
            }
        }
        return false;  // Contact is not blocked
    }

    private void sendMessage(String messageText) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = senderAddress;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isBlocked(phoneNumber)) {
            Toast.makeText(this, "Cannot send message to blocked contact", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);

            Date currentDate = new Date();
            String date = currentDate.toString();
            String time = date.substring(11, 19);

            SmsModel newMessage = new SmsModel("You", messageText, date, time, System.currentTimeMillis(), "sent");

            messagesList.add(newMessage);
            messageAdapter.notifyItemInserted(messagesList.size() - 1);
            b.rcvMassage.smoothScrollToPosition(messagesList.size() - 1);
            b.etEditText.getText().clear();
            Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("refresh", true);
            setResult(RESULT_OK, resultIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private String getPhoneNumberFromContacts(String senderAddress) {
        // Query the contacts database to retrieve phone number for the sender
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?", new String[]{"%" + senderAddress + "%"}, null);

        if (cursor != null && cursor.moveToFirst()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            cursor.close();
            return number;
        }
        return null;
    }

    private int getColorForInitial(String initial) {
        // Generate a random color or use any logic for color selection
        return Color.parseColor("#FF6347");  // Example color (Tomato)
    }
}
