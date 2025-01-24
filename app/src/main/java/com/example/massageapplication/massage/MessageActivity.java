package com.example.massageapplication.massage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.massageapplication.R;
import com.example.massageapplication.databinding.ActivityMessageBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding b;
    private MessageAdapter messageAdapter;
    private final ArrayList<SmsModel> messagesList = new ArrayList<>();
    private String senderAddress;
    private String senderName;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.imgCalender.setOnClickListener(v -> {

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
                    sendMessage(messageText);
                }
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessagesFromSender() {
        new Thread(() -> {
            ContentResolver cr = getContentResolver();

            // Fetch phone number from contacts
            phoneNumber = getPhoneNumberFromContacts(senderAddress);

            if (phoneNumber != null) {
                // Normalize the phone number
                phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, "IN"); // Adjust country code if needed
            }

            // Query messages
            Cursor cursor = cr.query(
                    Uri.parse("content://sms/"),
                    null,
                    phoneNumber != null ? "address = ?" : "address LIKE ?", // Use phoneNumber if available, else fallback to senderAddress
                    phoneNumber != null ? new String[]{phoneNumber} : new String[]{"%" + senderAddress + "%"},
                    "date ASC"
            );

            if (cursor != null) {
                messagesList.clear();
                while (cursor.moveToNext()) {
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    Date date = new Date(dateMillis);
                    int messageType = cursor.getInt(cursor.getColumnIndexOrThrow("type")); // Fetch the type

                    String type = (messageType == 1) ? "received" : "sent"; // Determine the type

                    // Extract time from the Date object
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String time = timeFormat.format(date); // Get the time as HH:mm

                    messagesList.add(new SmsModel(
                            type.equals("received") ? senderName : "You", // Use senderName for received, "You" for sent
                            body,
                            date.toString(),
                            time, // Pass the time
                            dateMillis,
                            type
                    ));
                }

                cursor.close();
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (messagesList.isEmpty()) {
                }
                messageAdapter.notifyDataSetChanged();

                // Scroll to the last message
                if (!messagesList.isEmpty()) {
                    b.rcvMassage.smoothScrollToPosition(messagesList.size() - 1);
                }
            });
        }).start();
    }

    @SuppressLint("Range")
    private String getPhoneNumberFromContacts(String contactName) {
        String phoneNumber = null;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                new String[]{"%" + contactName + "%"},
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            cursor.close();
        }
        return phoneNumber;
    }

    private void sendMessage(String messageText) {

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = senderAddress;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);

            // Create a Date object to get the current time and date
            Date currentDate = new Date();
            String date = currentDate.toString(); // Full date and time
            String time = date.substring(11, 19); // Extract the time part (HH:MM:SS)

// Create the new SmsModel with both date and time
            SmsModel newMessage = new SmsModel(
                    "You",               // Sender
                    messageText,         // Message body
                    date,                // Full date (Date.toString())
                    time,                // Extracted time (HH:MM:SS)
                    System.currentTimeMillis(), // Current time in milliseconds
                    "sent"               // Status (sent)
            );


            messagesList.add(newMessage);
            messageAdapter.notifyItemInserted(messagesList.size() - 1);
            b.rcvMassage.smoothScrollToPosition(messagesList.size() - 1);
            b.etEditText.getText().clear();
            Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("refresh", true); // Indicate that a message was sent
            setResult(RESULT_OK, resultIntent);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveSentMessage(String recipient, String messageBody) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put("address", recipient);   // Recipient's phone number
        values.put("body", messageBody);   // Message body
        values.put("date", System.currentTimeMillis());
        values.put("type", 2);             // Type 2 = Sent message
        contentResolver.insert(Uri.parse("content://sms/sent"), values);
    }


    private int getColorForInitial(String initial) {
        int hash = Math.abs(initial.hashCode());
        int[] colors = {
                Color.parseColor("#2b73ec"), // Example color
        };
        return colors[hash % colors.length];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted to send SMS", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Prepare data to send back (if needed)
        Intent resultIntent = new Intent();
        resultIntent.putExtra("refresh", true); // Example data to indicate refresh
        setResult(RESULT_OK, resultIntent);

        // Finish the activity and return to the caller
        super.onBackPressed();
    }
}
