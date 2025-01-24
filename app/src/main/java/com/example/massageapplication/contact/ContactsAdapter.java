package com.example.massageapplication.contact;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private final Context context;
    private final List<Contact> contacts; // Original list of all contacts
    private final List<Contact> displayedContacts; // List used for filtering
    private OnItemClickListener listener;

    // Listener interface
    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor
    public ContactsAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
        this.displayedContacts = new ArrayList<>(contacts); // Initially, show all contacts
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = displayedContacts.get(position);

        holder.nameTextView.setText(contact.getName());
        holder.number.setText(contact.getPhone());

        // Set the first letter as a circle background
        if (!contact.getName().isEmpty()) {
            String initial = String.valueOf(contact.getName().charAt(0)).toUpperCase();
            holder.firstName.setText(initial);

            int color = getColorForInitial(initial); // Get dynamic color based on the initial
            holder.nameLay.setBackgroundResource(R.drawable.circle_background);
            GradientDrawable background = (GradientDrawable) holder.nameLay.getBackground();
            background.setColor(color); // Apply the color
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(contact);
            }
        });
    }

    // Generate a unique color for the initial
    private int getColorForInitial(String initial) {
        int hash = Math.abs(initial.hashCode());
        int[] colors = {
                Color.parseColor("#FFCDD4"), // Light red
                Color.parseColor("#F8BBD4"), // Light pink
                Color.parseColor("#E1BEE9"), // Light purple
                Color.parseColor("#D1C4E3"), // Light indigo
                Color.parseColor("#BBDEF5"), // Light blue
                Color.parseColor("#B2EBF5"), // Light cyan
                Color.parseColor("#C8E6C7"), // Light green
                Color.parseColor("#DCEDC9"), // Light lime
                Color.parseColor("#FFF9C3"), // Light yellow
                Color.parseColor("#FFE0B8")  // Light orange
        };
        return colors[hash % colors.length];
    }

    @Override
    public int getItemCount() {
        return displayedContacts.size();
    }

    // ViewHolder class for RecyclerView
    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, firstName, number;
        LinearLayout nameLay;

        public ContactViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            nameLay = itemView.findViewById(R.id.nameLay);
            firstName = itemView.findViewById(R.id.firstName);
            number = itemView.findViewById(R.id.number);
        }
    }

    // Method to filter contacts based on a query
    public void filterContacts(String query) {
        displayedContacts.clear();

        if (query.isEmpty()) {
            displayedContacts.addAll(contacts); // Show all contacts if query is empty
        } else {
            ArrayList<String> addedNumbers = new ArrayList<>();
            for (Contact contact : contacts) {
                String normalizedPhone = contact.getPhone().replaceAll("\\s", "");
                if ((contact.getName().toLowerCase().contains(query.toLowerCase()) ||
                        normalizedPhone.contains(query)) &&
                        !addedNumbers.contains(normalizedPhone)) {
                    displayedContacts.add(contact);
                    addedNumbers.add(normalizedPhone); // Track added numbers to avoid duplicates
                }
            }
        }

        notifyDataSetChanged();
    }
}
