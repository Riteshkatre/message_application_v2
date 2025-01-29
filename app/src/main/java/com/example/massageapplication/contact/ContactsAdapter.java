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
import com.example.massageapplication.massage.SmsModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private final Context context;
    private final List<Contact> contacts;
    private  List<Contact> searchContacts;
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
        this.searchContacts = contacts; // Initially, show all contacts
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = searchContacts.get(position);

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
        return searchContacts.size();
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

    public void search(CharSequence charSequence, RecyclerView rcv, LinearLayout textView) {
        try {
            String charString = charSequence.toString().toLowerCase().trim();
            if (charString.isEmpty()) {
                searchContacts = contacts;
                rcv.setVisibility(View.VISIBLE);
            } else {
                int flag = 0;
                ArrayList<Contact> filterList = new ArrayList<>();
                Set<String> uniqueSenders = new HashSet<>(); // Track unique senders

                for (Contact row : contacts) {
                    if (row.getName().toLowerCase().contains(charString)) {
                        if (!uniqueSenders.contains(row.getName())) { // Check if sender already exists
                            uniqueSenders.add(row.getName()); // Add sender to Set
                            filterList.add(row);
                            flag = 1;
                        }
                    }
                }

                if (flag == 1) {
                    searchContacts = filterList;
                    rcv.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);
                } else {
                    rcv.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
