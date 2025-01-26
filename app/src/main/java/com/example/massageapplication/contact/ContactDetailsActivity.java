package com.example.massageapplication.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.databinding.ActivityContactDetalisBinding;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ContactDetailsActivity extends AppCompatActivity {
    ActivityContactDetalisBinding b;
    ArrayList<SmsModel> messagesList=new ArrayList<>();

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

        b.ivArchiveBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b.layBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and show the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetailsActivity.this);
                builder.setTitle("Block Contact").setMessage("Are you sure you want to block this contact?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blockSender(messagesList);
                        Toast.makeText(ContactDetailsActivity.this, name + " has been blocked.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ContactDetailsActivity.this, BlockActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog on "No"
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
    }


    private void blockSender(List<SmsModel> block) {
        SharedPreferences preferences = getSharedPreferences("BlockedMessages", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Fetch existing blocked contacts
        String blockedJson = preferences.getString("blockedMessages", "[]");
        ArrayList<SmsModel> blockedContacts = new Gson().fromJson(blockedJson, new TypeToken<ArrayList<SmsModel>>() {}.getType());

        if (blockedContacts == null) {
            blockedContacts = new ArrayList<>();
        }

        // Loop through the messages to check if the sender is already blocked
        boolean isSenderBlocked = false;
        for (SmsModel message : block) {
            // Check if the sender is already in the blocked list
            for (SmsModel blockedContact : blockedContacts) {
                if (blockedContact.getSender().equals(message.getSender())) {
                    isSenderBlocked = true;
                    break;
                }
            }

            if (isSenderBlocked) {
                // If already blocked, show a message and skip blocking
                Toast.makeText(ContactDetailsActivity.this, "The sender " + message.getSender() + " is already blocked.", Toast.LENGTH_SHORT).show();
                return; // Exit the method as the sender is already blocked
            }
        }

        // Add the new sender to the blocked list
        for (SmsModel message : block) {
            SmsModel blockedContact = new SmsModel(message.getSender(), message.getBody(), message.getDate(), message.getTime(), message.getDateMillis(), "blocked");
            blockedContacts.add(blockedContact); // Add blocked sender
        }

        // Save the updated list to SharedPreferences
        String updatedJson = new Gson().toJson(blockedContacts);
        editor.putString("blockedMessages", updatedJson); // Save blocked messages list
        editor.apply();
    }



}