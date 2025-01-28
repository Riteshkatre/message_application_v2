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

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.R;
import com.example.massageapplication.massage.MainActivity;
import com.example.massageapplication.massage.MessageActivity;
import com.example.massageapplication.massage.SmsAdapter;
import com.example.massageapplication.massage.SmsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Archive extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArchiveAdapter pinnedMessagesAdapter;
    private ActivityResultLauncher<Intent> sendSmsLauncher;
    ImageView ivArchiveBack,ivUnArchieve;
    ArrayList<SmsModel> pinnedMessages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);

        ivArchiveBack = findViewById(R.id.imgBack);
        ivUnArchieve = findViewById(R.id.ivUnArchieve);
        recyclerView = findViewById(R.id.rcvArcheiv);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

       pinnedMessages = getArchivedMessages();
        pinnedMessagesAdapter = new ArchiveAdapter(pinnedMessages);
        recyclerView.setAdapter(pinnedMessagesAdapter);

        pinnedMessagesAdapter.setOnItemClickListener(new ArchiveAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SmsModel smsModel) {
                Intent intent = new Intent(Archive.this, MessageActivity.class);
                intent.putExtra("address", smsModel.getSender());
                intent.putExtra("date", smsModel.getSender());
                sendSmsLauncher.launch(intent);


            }

            @Override
            public void longClickListener(int position, SmsModel smsModel) {
                ivUnArchieve.setVisibility(View.VISIBLE);
                ivUnArchieve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // अनब्लॉक करने से पहले पुष्टि डायलॉग दिखाएं
                        showConfirmationDialog(smsModel);
                    }
                });
            }
        });

        ivArchiveBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sendSmsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    getArchivedMessages();
                }
            }
        });
    }

    private ArrayList<SmsModel> getArchivedMessages() {
        SharedPreferences preferences = getSharedPreferences("ArchivedMessages", MODE_PRIVATE);
        String json = preferences.getString("ArchivedMessagesList", null);
        Log.e("selectedMessagesList3",json);

        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<SmsModel>>() {}.getType());
        }

        return new ArrayList<>();
    }

    private void showConfirmationDialog(SmsModel smsModel) {
        // अनब्लॉक करने के लिए पुष्टि डायलॉग
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to UnArchive this user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unarchivedUser(smsModel);
                        ivUnArchieve.setVisibility(View.GONE);
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    private void unarchivedUser(SmsModel smsModel) {
        // ब्लॉक किए गए उपयोगकर्ता को लिस्ट से हटा दें
        pinnedMessages.remove(smsModel);

        // SharedPreferences को अपडेट करें
        SharedPreferences preferences = getSharedPreferences("ArchivedMessages", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();



        // अपडेट की गई लिस्ट को JSON में बदलें
        String updatedJson = new Gson().toJson(pinnedMessages);
        editor.putString("ArchivedMessagesList", updatedJson);
        editor.apply();

        // RecyclerView को अपडेट करें
        pinnedMessagesAdapter.updateList(pinnedMessages);

        // यूजर को सूचित करें
        Toast.makeText(Archive.this, smsModel.getSender() + " has been unArchive.", Toast.LENGTH_SHORT).show();
    }


}