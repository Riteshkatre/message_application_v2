package com.example.massageapplication.massage;

import static com.example.massageapplication.R.string;
import static com.example.massageapplication.R.string.message_sent_successfully;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
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
import com.example.massageapplication.ScheduleDialog;
import com.example.massageapplication.ScheduledSMSReceiver;
import com.example.massageapplication.contact.ContactDetailsActivity;
import com.example.massageapplication.databinding.ActivityMessageBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private final ArrayList<SmsModel> messagesList = new ArrayList<>();
    private final ArrayList<String> blockedNumbers = new ArrayList<>();
    ActivityMessageBinding b;
    private MessageAdapter messageAdapter;
    private String senderAddress;
    private String senderName;
    private String phoneNumber;
    int color;
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



        // Get sender info from Intent
        senderAddress = getIntent().getStringExtra("address");
        senderName = getIntent().getStringExtra("date");
        color = getIntent().getIntExtra("color", Color.BLUE);


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
            Toast.makeText(MessageActivity.this, R.string.sender_address_is_missing, Toast.LENGTH_SHORT).show();
        }

        b.imgCalender.setOnClickListener(v -> {
            ScheduleDialog scheduleDialog = new ScheduleDialog(phoneNumber);
            scheduleDialog.show(getSupportFragmentManager(), "ScheduleDialog");
            scheduleDialog.setOnItemClickListener(new ScheduleDialog.OnItemClickListener() {
                @Override
                public void onSubmitClick(String dateTime, String message) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        Date date = format.parse(dateTime);
                        if (date != null) {
                            long triggerTime = date.getTime();
                            saveScheduledSMS(phoneNumber, message, triggerTime); // Save data
                            setAlarmForScheduledSMS(phoneNumber, message, triggerTime);
                            Toast.makeText(MessageActivity.this, R.string.sms_scheduled_successfully, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MessageActivity.this, "Invalid date/time format", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


        b.laySend.setOnClickListener(v -> {
            String messageText = b.etEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                } else {
                    sendMessage(messageText);
                }
            } else {
                Toast.makeText(MessageActivity.this, R.string.please_enter_a_message, Toast.LENGTH_SHORT).show();
            }
        });

        b.imgThreeDots.setOnClickListener(v -> {
            // Create a PopupMenu anchored to imgThreeDots
            PopupMenu popupMenu = new PopupMenu(this, b.imgThreeDots);

            // Inflate menu items or add them programmatically
            popupMenu.getMenu().add(R.string.schedule);
            popupMenu.getMenu().add(R.string.more_details);

            // Set a click listener for menu items
            popupMenu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                String moreDetailsText = getString(R.string.more_details);

                if (title.equals(getString(R.string.schedule))) {
                    // Handle "Schedule" action
                    Toast.makeText(this, "Schedule clicked", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (title.equals(moreDetailsText)) {
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

        b.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.llSearch.setVisibility(View.VISIBLE);
            }
        });
        b.imgMainClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.llSearch.setVisibility(View.GONE);
                b.searchText.getText().clear();
                b.llNoData.setVisibility(View.GONE);
            }
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
                phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, "IN"); // देश कोड सेट करें
            }

            SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
            String blockedContactsJson = preferences.getString("blockedContacts", "[]");
            ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>() {
            }.getType());

            boolean isBlocked = false;
            for (SmsModel blockedContact : blockedContacts) {
                if (blockedContact.getSender().equals(phoneNumber)) {
                    isBlocked = true;
                    break;
                }
            }

            if (isBlocked) {
                runOnUiThread(() -> Toast.makeText(MessageActivity.this, R.string.this_number_is_blocked_the_message_will_not_be_shown, Toast.LENGTH_SHORT).show());
                return;
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

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String time = timeFormat.format(date);

                    newMessagesList.add(new SmsModel(type.equals("received") ? senderName : "You", body, date.toString(), time, dateMillis, type,color));
                }
                cursor.close();

                new Handler(Looper.getMainLooper()).post(() -> {
                    messagesList.clear();
                    messagesList.addAll(newMessagesList);
                    messageAdapter.notifyDataSetChanged();
                    if (!messagesList.isEmpty()) {
                        b.rcvMassage.scrollToPosition(messagesList.size() - 1);
                    }
                });
            }
        }).start();
    }

    @SuppressLint("Range")
    private String getPhoneNumberFromContacts(String contactName) {
        String phoneNumber = null;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?", new String[]{"%" + contactName + "%"}, null);

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
            Toast.makeText(MessageActivity.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // ब्लॉक किए गए नंबर को चेक करें
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>() {
        }.getType());

        boolean isBlocked = false;
        for (SmsModel blockedContact : blockedContacts) {
            if (blockedContact.getSender().equals(phoneNumber)) {
                isBlocked = true;
                break;
            }
        }

        if (isBlocked) {
            Toast.makeText(this, string.you_have_already_blocked_this_number_messages_cannot_be_sent, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);

            Date currentDate = new Date();
            String date = currentDate.toString();
            String time = date.substring(11, 19);

            SmsModel newMessage = new SmsModel("You", messageText, date, time, System.currentTimeMillis(), "sent",color);

            messagesList.add(newMessage);
            messageAdapter.notifyItemInserted(messagesList.size() - 1);
            b.rcvMassage.smoothScrollToPosition(messagesList.size() - 1);
            b.etEditText.getText().clear();
            Toast.makeText(this, message_sent_successfully, Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("refresh", true);
            setResult(RESULT_OK, resultIntent);

        } catch (Exception e) {
            Toast.makeText(this, getString(string.failed_to_send_message) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(smsObserver);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("refresh", true);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    private void resetScheduledSMSAlarms() {
        SharedPreferences preferences = getSharedPreferences("ScheduledSMS", MODE_PRIVATE);
        Map<String, ?> allEntries = preferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();

            String[] parts = value.split("\\|");
            String phoneNumber = parts[0];
            String message = parts[1];
            long triggerTime = Long.parseLong(key.replace("scheduled_sms_", ""));

            if (triggerTime > System.currentTimeMillis()) {
                setAlarmForScheduledSMS(phoneNumber, message, triggerTime);
            } else {
                // Remove expired alarms
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(key);
                editor.apply();
            }
        }
    }

    private void setAlarmForScheduledSMS(String phoneNumber, String message, long triggerTime) {
        Intent intent = new Intent(this, ScheduledSMSReceiver.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void saveScheduledSMS(String phoneNumber, String message, long triggerTime) {
        SharedPreferences preferences = getSharedPreferences("ScheduledSMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String key = "scheduled_sms_" + triggerTime;
        String value = phoneNumber + "|" + message;

        editor.putString(key, value);
        editor.apply();
    }

}
