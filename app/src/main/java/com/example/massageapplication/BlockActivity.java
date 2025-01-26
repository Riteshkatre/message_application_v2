package com.example.massageapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.drawerActivity.Archive;
import com.example.massageapplication.massage.MessageActivity;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class BlockActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BlockedAdapter adapter;
    private ArrayList<SmsModel> blockedMessagesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);

        recyclerView = findViewById(R.id.blocked_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load and display blocked users
        blockedMessagesList = loadBlockedMessages();
        adapter = new BlockedAdapter(this, blockedMessagesList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BlockedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SmsModel smsModel) {
                Intent intent = new Intent(BlockActivity.this, MessageActivity.class);
                intent.putExtra("address", smsModel.getSender());
                intent.putExtra("date", smsModel.getSender());
                startActivity(intent);
                finish();
            }
        });


    }

    private ArrayList<SmsModel> loadBlockedMessages() {
        SharedPreferences preferences = getSharedPreferences("BlockedMessages", MODE_PRIVATE);
        String blockedMessagesJson = preferences.getString("blockedMessages", "[]");

        ArrayList<SmsModel> allMessages = new Gson().fromJson(blockedMessagesJson, new TypeToken<ArrayList<SmsModel>>() {}.getType());

        if (allMessages != null) {
            return (ArrayList<SmsModel>) allMessages.stream()
                    .filter(SmsModel::isBlocked)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void unblockUser(SmsModel smsModel) {
        smsModel.setBlocked(false);

        // Update SharedPreferences
        SharedPreferences preferences = getSharedPreferences("BlockedMessages", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Load existing messages
        String blockedMessagesJson = preferences.getString("blockedMessages", "[]");
        ArrayList<SmsModel> allMessages = new Gson().fromJson(blockedMessagesJson, new TypeToken<ArrayList<SmsModel>>() {}.getType());

        // Update the specific blocked message
        for (SmsModel message : allMessages) {
            if (message.getSender().equals(smsModel.getSender())) {
                message.setBlocked(false);
                break;
            }
        }

        // Save the updated list
        String updatedJson = new Gson().toJson(allMessages);
        editor.putString("blockedMessages", updatedJson);
        editor.apply();

        // Refresh the adapter
        blockedMessagesList = loadBlockedMessages();
        adapter.updateList(blockedMessagesList);
    }
}
