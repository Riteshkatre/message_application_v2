package com.example.massageapplication.massage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.massageapplication.BlockActivity;
import com.example.massageapplication.R;
import com.example.massageapplication.Search;
import com.example.massageapplication.SmsDiffCallback;
import com.example.massageapplication.SmsListener;
import com.example.massageapplication.contact.ContactsActivity;
import com.example.massageapplication.databinding.ActivityMainBinding;
import com.example.massageapplication.drawerActivity.Archive;
import com.example.massageapplication.language.LanguageSelectionActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private final ArrayList<SmsModel> smsList = new ArrayList<>();
    private final Map<String, String> contactCache = new HashMap<>();
    private final Object smsListLock = new Object(); // Add lock object
    private ActivityMainBinding b;
    private SmsAdapter smsAdapter;
    private ActivityResultLauncher<Intent> sendSmsLauncher;
    private ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private SmsListener smsListener;
    private BroadcastReceiver newSmsReceiver;

    @SuppressLint({"WrongViewCast",})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);
        setTheme(isDarkMode ? R.style.DarkTheme : R.style.LightTheme);

        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        smsListener = new SmsListener();
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsListener, intentFilter);  // Register the receiver here
        Log.d("HGP", "SmsListener registered.");

        navDrawerClickS();
        b.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsAdapter = new SmsAdapter(smsList);
        b.messagesRecyclerView.setAdapter(smsAdapter);
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);
        prepareIntentLauncher();
        if (isFirstTime) {
//            requestDefaultSmsRole();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime", false); // Update the flag to false after the first run
            editor.apply();
        }
        if (!isDefaultSmsApp()) {
//            requestDefaultSmsApp();
        } else {
            loadMessages();
        }
        smsAdapter.setOnItemClickListener(new SmsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SmsModel smsModel) {
                if (smsAdapter.getSelectedItemCount() == 0) {
                    Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                    intent.putExtra("address", smsModel.getSender());
                    intent.putExtra("date", smsModel.getSender());
                    sendSmsLauncher.launch(intent);
                }
            }
            @Override
            public void longClickListener(int position, SmsModel smsModel) {
                b.mainArchiveLayout.setVisibility(View.VISIBLE);
                b.llOne.setVisibility(View.GONE);
                b.layArchive.icPin.setOnClickListener(view -> {
                    List<SmsModel> selectedMessagesList = smsAdapter.getSelectedItems();
                    if (selectedMessagesList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No messages selected for archive.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    archiveMessages(selectedMessagesList);
                    smsAdapter.clearSelection();
                    smsAdapter.notifyDataSetChanged();
                    b.mainArchiveLayout.setVisibility(View.GONE);
                    b.llOne.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(MainActivity.this, Archive.class);
                    startActivity(intent);
                });
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onSelectionCountChanged(int count, SmsModel smsModel) {
                b.layArchive.tvSelected.setText(count + " " + "Selected");
            }
        });

        b.layArchive.icClose.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                smsAdapter.clearSelection();
                smsAdapter.notifyDataSetChanged();
                b.mainArchiveLayout.setVisibility(View.GONE);
                b.llOne.setVisibility(View.VISIBLE);
            }
        });

        b.ivSearchMsg.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Search.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        });

        b.ivTheme.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean isCurrentlyDarkMode = sharedPreferences.getBoolean("isDarkMode", false);
            if (isCurrentlyDarkMode) {
                editor.putBoolean("isDarkMode", false);
                editor.apply();
                setTheme(R.style.LightTheme);
                b.ivTheme.setImageResource(R.drawable.moon); // Replace with moon icon
            } else {
                editor.putBoolean("isDarkMode", true);
                editor.apply();
                setTheme(R.style.DarkTheme);
                b.ivTheme.setImageResource(R.drawable.sun); // Replace with sun icon
            }
            recreate(); // Recreate activity to apply the new theme
        });

        b.cvDrawer.setOnClickListener(v -> {
            if (!b.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                b.drawerLayout.openDrawer(GravityCompat.START);
            } else {
                b.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        b.fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            sendSmsLauncher.launch(intent);
        });

        sendSmsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    loadMessages();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            loadMessages(); // Load messages immediately if permissions are granted
        }

        b.ivSearchMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolBar(true);
                b.searchText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(b.searchText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        b.imgMainClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateToolBar(false);
                b.searchText.getText().clear();
                b.llNoData.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(b.searchText.getWindowToken(), 0);
                }
            }
        });
        b.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                smsAdapter.search(s.toString(), b.messagesRecyclerView, b.llNoData);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void updateToolBar(Boolean isSearchActive) {
        if (isSearchActive) {
            b.llSearch.setVisibility(View.VISIBLE);
            b.imgMainClose.setVisibility(View.VISIBLE);
        } else {
            b.llSearch.setVisibility(View.GONE);
            b.imgMainClose.setVisibility(View.GONE);
        }
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean smsPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean contactsPermissionGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (smsPermissionGranted && contactsPermissionGranted) {
                    loadMessages(); // Load messages immediately after permissions are granted
                } else {
                    Toast.makeText(this, "Permissions are required to access messages and contacts.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void loadMessages() {
        new Handler(Looper.getMainLooper()).post(() -> {
            b.messagesRecyclerView.setVisibility(View.GONE);
            b.idPBLoading.setVisibility(View.VISIBLE);
        });

        new Thread(() -> {
            ArrayList<SmsModel> tempList = new ArrayList<>();
            Map<String, SmsModel> uniqueMessages = new HashMap<>();
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(Uri.parse("content://sms/"), null, null, null, "date DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    Date date = new Date(dateMillis);

                    String dateStr = android.text.format.DateFormat.format("yyyy-MM-dd", date).toString();
                    String timeStr = android.text.format.DateFormat.format("HH:mm", date).toString();
                    String name = getContactName(address);
                    if (!uniqueMessages.containsKey(address)) {
                        uniqueMessages.put(address, new SmsModel(name, body, dateStr, timeStr, dateMillis, "received"));
                    }
                }
                cursor.close();
            }
            tempList.addAll(uniqueMessages.values());
            Collections.sort(tempList, (m1, m2) -> Long.compare(m2.getDateMillis(), m1.getDateMillis()));
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SmsDiffCallback(smsList, tempList));
            synchronized (smsListLock) {
                smsList.clear();
                smsList.addAll(tempList);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                b.messagesRecyclerView.setVisibility(View.VISIBLE);
                b.idPBLoading.setVisibility(View.GONE);
                diffResult.dispatchUpdatesTo(smsAdapter);
            });
        }).start();
    }
    private boolean isBlocked(String phoneNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("BlockedContacts", MODE_PRIVATE);
        Set<String> blockedContacts = sharedPreferences.getStringSet("BlockedList", new HashSet<>());
        return blockedContacts.contains(phoneNumber);
    }
    private String getContactName(String phoneNumber) {
        if (contactCache.containsKey(phoneNumber)) {
            return contactCache.get(phoneNumber);
        }
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.withAppendedPath(android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        String contactName = phoneNumber;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        contactCache.put(phoneNumber, contactName);
        return contactName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDefaultSmsApp()) {
            loadMessages();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (newSmsReceiver != null) {
            unregisterReceiver(newSmsReceiver);
        }
    }

    public void navDrawerClickS() {
        b.navDrawer.layoutLanguage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LanguageSelectionActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        b.navDrawer.layoutArchive.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Archive.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        b.navDrawer.layoutBlockMsg.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BlockActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }
    private void requestDefaultSmsApp() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        startActivity(intent);
    }
    private boolean isDefaultSmsApp() {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
        return defaultSmsPackage != null && defaultSmsPackage.equals(getPackageName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsListener);
        Log.d("HGP", "SmsListener Unregistered.");
    }

    private void requestDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            boolean isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS);
            if (isRoleAvailable) {
                boolean isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS);
                if (!isRoleHeld) {
                    intentActivityResultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS));
                } else {
                    Toast.makeText(this, "role is not available for this app", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(this, "permission not executed", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, 1001);
        }
    }

    private void prepareIntentLauncher() {
        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    loadMessages();
                } else {
                    Toast.makeText(MainActivity.this, "Faied!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addReceivedMessageToList(SmsModel newSms) {
        synchronized (smsListLock) {
            smsList.add(0, newSms);
        }
        runOnUiThread(() -> smsAdapter.notifyItemInserted(0));
    }

    private void archiveMessages(List<SmsModel> messagesToArchive) {
        SharedPreferences preferences = getSharedPreferences("ArchivedMessages", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        List<SmsModel> existingArchivedMessages = getArchivedMessages();
        boolean isNewArchive = false;
        for (SmsModel newMessage : messagesToArchive) {
            boolean isSenderAlreadyArchived = false;
            for (SmsModel archivedMessage : existingArchivedMessages) {
                if (archivedMessage.getSender().equals(newMessage.getSender())) {
                    isSenderAlreadyArchived = true;
                    break;
                }
            }

            if (isSenderAlreadyArchived) {
                Toast.makeText(MainActivity.this, "Message from " + newMessage.getSender() + " is already archived.", Toast.LENGTH_SHORT).show();
            } else {
                existingArchivedMessages.add(newMessage);
                isNewArchive = true;
            }
        }
        if (isNewArchive) {
            Gson gson = new Gson();
            String json = gson.toJson(existingArchivedMessages);
            editor.putString("ArchivedMessagesList", json);
            editor.apply();
            Toast.makeText(MainActivity.this, "Messages archived successfully.", Toast.LENGTH_SHORT).show();
        }
    }
    private List<SmsModel> getArchivedMessages() {
        SharedPreferences preferences = getSharedPreferences("ArchivedMessages", MODE_PRIVATE);
        String json = preferences.getString("ArchivedMessagesList", null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<List<SmsModel>>() {
            }.getType());
        }
        return new ArrayList<>();
    }

}