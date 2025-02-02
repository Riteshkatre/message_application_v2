package com.example.massageapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.example.massageapplication.massage.SmsModel;

public class MmsReciever extends BroadcastReceiver {
    public static MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(SmsModel message);
    }

    public static void setListener(MessageListener listener) {
        messageListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = sms.getOriginatingAddress();
                        String messageBody = sms.getMessageBody();
                        String timestamp = String.valueOf(sms.getTimestampMillis());

                        // Correct constructor call
                        SmsModel newMessage = new SmsModel(sender, messageBody, timestamp, "", System.currentTimeMillis(), "",null);

                        // Notify the listener (Archive Activity)
                        if (messageListener != null) {
                            messageListener.onMessageReceived(newMessage);
                        }
                    }
                }
            }
        }
    }

}
