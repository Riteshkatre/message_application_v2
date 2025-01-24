package com.example.massageapplication.contact;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.R;
import com.example.massageapplication.massage.MessageActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class ContactsActivity extends AppCompatActivity {
    private ImageView ivKeyboard, ivContactBack;
    private EditText etContactSearch;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private ArrayList<Contact> contacts;
    private boolean isKeyboardModeText = true;

    private final TextWatcher numberOnlyWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().matches("\\d*")) {
                etContactSearch.setText(s.toString().replaceAll("[^\\d]", ""));
                etContactSearch.setSelection(etContactSearch.getText().length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private final ActivityResultLauncher<Intent> messageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initializeViews();
        setupRecyclerView();
        handleBackButton();
        checkPermissionsAndLoadContacts();
        setupSearchFunctionality();
        setupKeyboardToggle();
    }

    private void initializeViews() {
        ivKeyboard = findViewById(R.id.ivKeyboard);
        ivContactBack = findViewById(R.id.ivContactBack);
        etContactSearch = findViewById(R.id.etContactSearch);
        recyclerView = findViewById(R.id.contacts_recycler_view);
    }

    private void setupRecyclerView() {
        contacts = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void handleBackButton() {
        ivContactBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void checkPermissionsAndLoadContacts() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        loadContacts();
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    private void setupSearchFunctionality() {
        etContactSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupKeyboardToggle() {
        ivKeyboard.setOnClickListener(v -> toggleKeyboardMode());
    }

    private void toggleKeyboardMode() {
        if (isKeyboardModeText) {
            switchToDialPadMode();
        } else {
            switchToTextMode();
        }
        isKeyboardModeText = !isKeyboardModeText;
    }

    private void switchToDialPadMode() {
        ivKeyboard.setImageResource(R.drawable.dialpad);
        etContactSearch.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        etContactSearch.setHint("Enter numbers");
        etContactSearch.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        etContactSearch.setText("");
        etContactSearch.addTextChangedListener(numberOnlyWatcher);
    }

    private void switchToTextMode() {
        ivKeyboard.setImageResource(R.drawable.keyboard);
        etContactSearch.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        etContactSearch.setHint("Search contacts");
        etContactSearch.setFilters(new InputFilter[]{});
        etContactSearch.setText("");
        etContactSearch.removeTextChangedListener(numberOnlyWatcher);
    }

    private void loadContacts() {
        ContentResolver cr = getContentResolver();
        @SuppressLint("Recycle")
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (!isDuplicate(phone)) {
                    contacts.add(new Contact(name, phone));
                }
            }
            cursor.close();
        }

        setupAdapter();
    }

    private boolean isDuplicate(String phone) {
        for (Contact contact : contacts) {
            if (contact.getPhone().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    private void setupAdapter() {
        adapter = new ContactsAdapter(this, new ArrayList<>(contacts));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(contact -> {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("address", contact.getName());
            intent.putExtra("date", new Date().toString());
            messageLauncher.launch(intent);
        });
    }

    private void filterContacts(String query) {
        ArrayList<Contact> filteredContacts = new ArrayList<>();
        HashSet<String> phoneSet = new HashSet<>();

        for (Contact contact : contacts) {
            String normalizedPhone = contact.getPhone().replaceAll("\\s", "");

            if ((contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                    normalizedPhone.contains(query)) &&
                    !phoneSet.contains(normalizedPhone)) {
                filteredContacts.add(contact);
                phoneSet.add(normalizedPhone);
            }
        }

        adapter.filterContacts(query);
    }
}
