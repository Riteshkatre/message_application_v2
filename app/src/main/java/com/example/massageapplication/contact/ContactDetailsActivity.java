package com.example.massageapplication.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.databinding.ActivityContactDetalisBinding;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ContactDetailsActivity extends AppCompatActivity {
    ActivityContactDetalisBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityContactDetalisBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        String name = getIntent().getStringExtra("name");
        String number = getIntent().getStringExtra("number");

        b.name.setText(name);
        b.number.setText(number);

        if (name != null && name.length() > 0) {
            String initial = String.valueOf(name.charAt(0)).toUpperCase();
            b.shortName.setText(initial);
        }

        // ब्लॉक और अनब्लॉक का चेक करने के लिए
        checkBlockStatus(name);

        b.ivArchiveBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b.layBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // अगर संपर्क पहले से ब्लॉक है तो अनब्लॉक करें, वरना ब्लॉक करें
                if (b.tvBlock.getText().toString().equals("Block")) {
                    // ब्लॉक करने के लिए कन्फर्मेशन डायलॉग
                    AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailsActivity.this);
                    builder.setTitle("Block Contact").setMessage("Are you sure you want to block this contact?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // संपर्क को ब्लॉक करें
                                    blockContact(name);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // "No" दबाने पर डायलॉग को बंद करें
                                    dialog.dismiss();
                                }
                            }).create().show();
                } else {
                    // अनब्लॉक करने के लिए कन्फर्मेशन डायलॉग
                    unblockContact(number);
                }
            }
        });
    }

    private void checkBlockStatus(String phoneNumber) {
        // ब्लॉक किए गए संपर्कों की स्थिति चेक करें
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>(){}.getType());

        // अगर संपर्क ब्लॉक है, तो UI में अनब्लॉक दिखाएं
        for (SmsModel contact : blockedContacts) {
            if (contact.getSender().equals(phoneNumber)) {
                b.tvBlock.setText("Unblock");
                return; // Exit as soon as the contact is found
            }
        }

        // अगर संपर्क ब्लॉक नहीं है, तो UI में ब्लॉक दिखाएं
        b.tvBlock.setText("Block");
    }

    private void blockContact(String phoneNumber) {
        // ब्लॉक किए गए संपर्क के लिए SmsModel बनाएँ
        SmsModel blockedMessage = new SmsModel(
                phoneNumber,   // sender (फोन नंबर)
                "Blocked",     // body (मैसेज का कंटेंट)
                "",            // date (डेटा, आप इसे खाली छोड़ सकते हैं या सेट कर सकते हैं)
                "",            // time (समय, आप इसे खाली छोड़ सकते हैं या सेट कर सकते हैं)
                System.currentTimeMillis(),  // dateMillis (वर्तमान टाइमस्टैम्प)
                "Blocked"      // status (ब्लॉक स्थिति)
        );

        blockedMessage.setBlocked(true);  // संपर्क को ब्लॉक के रूप में चिह्नित करें

        // ब्लॉक किए गए संपर्क को SharedPreferences में सेव करें
        saveBlockedContact(blockedMessage);

        Toast.makeText(ContactDetailsActivity.this, "Contact has been blocked.", Toast.LENGTH_SHORT).show();

        // ब्लॉक किए गए संपर्कों की स्क्रीन पर रीडायरेक्ट करें
        Intent intent = new Intent(ContactDetailsActivity.this, BlockActivity.class);
        startActivity(intent);

        // UI को ब्लॉक से अनब्लॉक में बदलें
        b.tvBlock.setText("Unblock");
    }

    private void saveBlockedContact(SmsModel blockedMessage) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // वर्तमान में ब्लॉक किए गए संपर्क प्राप्त करें
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>(){}.getType());

        // नया ब्लॉक किया गया संपर्क जोड़ें
        blockedContacts.add(blockedMessage);

        // अपडेट की गई लिस्ट को SharedPreferences में सेव करें
        String updatedJson = new Gson().toJson(blockedContacts);
        editor.putString("blockedContacts", updatedJson);
        editor.apply();
    }

    private void unblockContact(String phoneNumber) {
        // कन्फर्मेशन डायलॉग दिखाएं
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailsActivity.this);
        builder.setTitle("Unblock Contact")
                .setMessage("Are you sure you want to unblock this contact?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // अगर हां, तो अनब्लॉक करें
                        performUnblock(phoneNumber);
                    }
                })
                .setNegativeButton("No", null)  // "No" दबाने पर कुछ नहीं होगा, डायलॉग बंद हो जाएगा
                .create().show();
    }

    private void performUnblock(String phoneNumber) {
        SharedPreferences preferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        String blockedContactsJson = preferences.getString("blockedContacts", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedContactsJson, new TypeToken<ArrayList<SmsModel>>(){}.getType());

        // ब्लॉक लिस्ट से संपर्क को हटाएं
        for (int i = 0; i < blockedContacts.size(); i++) {
            if (blockedContacts.get(i).getSender().equals(phoneNumber)) {
                blockedContacts.remove(i);
                break;
            }
        }

        // SharedPreferences को अपडेट करें
        SharedPreferences.Editor editor = preferences.edit();
        String updatedJson = new Gson().toJson(blockedContacts);
        editor.putString("blockedContacts", updatedJson);
        editor.apply();

        // UI को Unblock से Block में बदलें
        b.tvBlock.setText("Block");

        Toast.makeText(ContactDetailsActivity.this, "Contact has been unblocked.", Toast.LENGTH_SHORT).show();
    }
}
