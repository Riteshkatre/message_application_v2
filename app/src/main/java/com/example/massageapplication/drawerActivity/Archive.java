package com.example.massageapplication.drawerActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.R;
import com.example.massageapplication.massage.MessageActivity;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Archive extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArchiveAdapter pinnedMessagesAdapter;
    private ActivityResultLauncher<Intent> sendSmsLauncher;
    private ImageView ivArchiveBack, ivUnArchive;
    private ArrayList<SmsModel> pinnedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupActivityLauncher();
    }

    private void initViews() {
        ivArchiveBack = findViewById(R.id.imgBack);
        ivUnArchive = findViewById(R.id.ivUnArchieve);
        recyclerView = findViewById(R.id.rcvArcheiv);

        // Apply window insets for a smooth UI experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pinnedMessages = getArchivedMessages();
        pinnedMessagesAdapter = new ArchiveAdapter(pinnedMessages);
        recyclerView.setAdapter(pinnedMessagesAdapter);
    }

    private void setupListeners() {
        ivArchiveBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        pinnedMessagesAdapter.setOnItemClickListener(new ArchiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SmsModel smsModel) {
                openMessageActivity(smsModel);
            }

            @Override
            public void longClickListener(int position, SmsModel smsModel) {
                ivUnArchive.setVisibility(View.VISIBLE);
                ivUnArchive.setOnClickListener(v -> showConfirmationDialog(smsModel));
            }
        });
    }

    private void setupActivityLauncher() {
        sendSmsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                getArchivedMessages(); // Refresh data
            }
        });
    }

    private void openMessageActivity(SmsModel smsModel) {
        Intent intent = new Intent(Archive.this, MessageActivity.class);
        intent.putExtra("address", smsModel.getSender());
        intent.putExtra("date", smsModel.getSender());
        sendSmsLauncher.launch(intent);
    }

    private ArrayList<SmsModel> getArchivedMessages() {
        SharedPreferences preferences = getSharedPreferences("ArchivedMessages", MODE_PRIVATE);
        String json = preferences.getString("ArchivedMessagesList", null);
        Log.e("selectedMessagesList", json != null ? json : "No archived messages");

        if (json != null) {
            return new Gson().fromJson(json, new TypeToken<List<SmsModel>>() {}.getType());
        }
        return new ArrayList<>();
    }

    private void showConfirmationDialog(SmsModel smsModel) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to unarchive this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    unarchiveUser(smsModel);
                    ivUnArchive.setVisibility(View.GONE);
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void unarchiveUser(SmsModel smsModel) {
        // Remove from archive list
        pinnedMessages.remove(smsModel);

        SharedPreferences preferences = getSharedPreferences("ArchivedMessages", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String updatedJson = new Gson().toJson(pinnedMessages);
        editor.putString("ArchivedMessagesList", updatedJson);
        editor.apply();

        SharedPreferences mainPreferences = getSharedPreferences("MainMessages", MODE_PRIVATE);
        String mainJson = mainPreferences.getString("MainMessagesList", null);
        ArrayList<SmsModel> mainMessages = new ArrayList<>();

        if (mainJson != null) {
            mainMessages = new Gson().fromJson(mainJson, new TypeToken<List<SmsModel>>() {}.getType());
        }
        mainMessages.add(smsModel);

        // Save the updated main message list
        SharedPreferences.Editor mainEditor = mainPreferences.edit();
        mainEditor.putString("MainMessagesList", new Gson().toJson(mainMessages));
        mainEditor.apply();

        // ✅ Refresh UI
        pinnedMessagesAdapter.updateList(pinnedMessages);

        // Notify main message list that an item has been added (if you have an adapter for the main list)
        Toast.makeText(Archive.this, smsModel.getSender() + " has been unarchived.", Toast.LENGTH_SHORT).show();

        // ✅ Close archive activity and refresh main message list
        finish();
    }
}
