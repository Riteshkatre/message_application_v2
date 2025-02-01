package com.example.massageapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class ScheduledSMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String message = intent.getStringExtra("message");

        if (phoneNumber != null && message != null) {
            sendSMS(context, phoneNumber, message);
        }
    }

    private void sendSMS(Context context, String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "Scheduled SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send scheduled SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
