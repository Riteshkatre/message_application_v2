package com.example.massageapplication.drawerActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    ImageView ivArchiveBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archive);

        ivArchiveBack = findViewById(R.id.ivArchiveBack);
        recyclerView = findViewById(R.id.rcvArcheiv);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<SmsModel> pinnedMessages = getArchivedMessages();
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

}