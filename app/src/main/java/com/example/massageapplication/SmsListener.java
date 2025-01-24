package com.example.massageapplication;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.massageapplication.massage.MainActivity;
import com.example.massageapplication.massage.SmsModel;

import java.util.Date;

public class SmsListener extends BroadcastReceiver {

    private static final String CHANNEL_ID = "SMS_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus == null) return;

                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    StringBuilder messageBody = new StringBuilder();
                    String sender = "";

                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        if (i == 0) {
                            sender = messages[i].getOriginatingAddress();
                        }
                        messageBody.append(messages[i].getMessageBody());
                    }

                    Log.d("SmsListener", "Sender: " + sender + ", Message: " + messageBody);

                    String contactName = getContactName(context, sender);

                    showNotification(context, contactName, messageBody.toString());
                    addReceivedMessageToList(contactName, messageBody.toString(), context);

                } catch (Exception e) {
                    Log.e("SmsListener", "Exception: " + e.getMessage());
                }
            }
        }
    }

    private void showNotification(Context context, String sender, String messageBody) {
        // Create NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for SMS notifications");
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent for launching the app when the notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_email) // Replace with your app icon
                .setContentTitle("New SMS from " + sender)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Display the notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void addReceivedMessageToList(String sender, String messageBody, Context context) {
        long timestamp = System.currentTimeMillis();
        SmsModel receivedSms = new SmsModel(sender, messageBody,
                android.text.format.DateFormat.format("yyyy-MM-dd", new Date(timestamp)).toString(),
                android.text.format.DateFormat.format("HH:mm", new Date(timestamp)).toString(),
                timestamp, "received");

        if (context instanceof MainActivity) {
            MainActivity smsActivity = (MainActivity) context;
            smsActivity.addReceivedMessageToList(receivedSms);
        }
    }

    @SuppressLint("Range")
    private String getContactName(Context context, String phoneNumber) {
        String contactName = phoneNumber; // Default to phone number if no contact found

        try {
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber)
            );

            Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    );
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("SmsListener", "Error getting contact name: " + e.getMessage());
        }

        return contactName;
    }
}
