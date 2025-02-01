package com.example.massageapplication.massage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsViewHolder> {
    private final ArrayList<SmsModel> smsList;
    private final Set<Integer> selectedItems;
    private final List<String> mColors = new ArrayList<>(Arrays.asList("D32F2F", // Dark Red
            "C2185B", // Dark Pink
            "7B1FA2", // Dark Purple
            "512DA8", // Deep Purple
            "303F9F", // Dark Blue
            "1976D2", // Royal Blue
            "0288D1", // Deep Sky Blue
            "00796B", // Teal
            "388E3C", // Dark Green
            "5D4037", // Dark Brown
            "455A64", // Dark Grayish Blue
            "37474F", // Charcoal Gray
            "263238"  // Almost Black
    ));
    private ArrayList<SmsModel> searchList;
    private OnItemClickListener listener;

    Context context;

    public SmsAdapter(ArrayList<SmsModel> smsList, Context context) {
        this.smsList = smsList;
        this.searchList = smsList;
        this.selectedItems = new HashSet<>();
        this.context=context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsViewHolder holder, int position) {
        SmsModel sms = searchList.get(position);
        String sender = sms.getStatus().equals("sent") ? "Me" : sms.getSender();
        holder.senderTextView.setText(sender);
        holder.messageTextView.setText(sms.getBody());
        String formattedTime = sms.getTime();
        holder.dateTextView.setText(formattedTime); // For now, show just the time part
        if (sender.length() > 0) {

            String initial = String.valueOf(Character.toUpperCase(sender.charAt(0)));
            holder.senderInitialTextView.setText(initial);


            int senderColor = getColorForSender(sender);
            holder.nameLay.setBackgroundTintList(ColorStateList.valueOf(senderColor));

            holder.itemView.setOnClickListener(v -> {
                if (selectedItems.size() > 0) {
                    toggleSelection(position);
                } else {
                    if (listener != null) {
                        listener.onItemClick(position, sms);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                toggleSelection(position);
                if (listener != null) {
                    listener.longClickListener(position, sms);
                }
                return true;
            });

            if (sms.isPinned()){
                holder.isPinIcon.setVisibility(View.VISIBLE);
            }else {
                holder.isPinIcon.setVisibility(View.GONE);
            }

        }

        // Set formatted date
        String formattedDate = getFormattedDate(sms.getDateMillis());
        holder.dateTextView.setText(formattedDate);

        if (selectedItems.contains(position)) {
            holder.checkmarkIcon.setVisibility(View.VISIBLE);  // Show checkmark icon
        } else {
            holder.checkmarkIcon.setVisibility(View.GONE);  // Hide checkmark icon
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public void search(CharSequence charSequence, RecyclerView rcv, LinearLayout textView) {
        try {
            String charString = charSequence.toString().toLowerCase().trim();
            if (charString.isEmpty()) {
                searchList = smsList;
                rcv.setVisibility(View.VISIBLE);
            } else {
                int flag = 0;
                ArrayList<SmsModel> filterList = new ArrayList<>();
                Set<String> uniqueSenders = new HashSet<>(); // Track unique senders

                for (SmsModel row : smsList) {
                    if (row.getSender().toLowerCase().contains(charString)) {
                        if (!uniqueSenders.contains(row.getSender())) { // Check if sender already exists
                            uniqueSenders.add(row.getSender()); // Add sender to Set
                            filterList.add(row);
                            flag = 1;
                        }
                    }
                }

                if (flag == 1) {
                    searchList = filterList;
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

    private String getFormattedDate(long dateMillis) {
        Date messageDate = new Date(dateMillis);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = timeFormat.format(messageDate);
        Calendar currentCalendar = Calendar.getInstance();
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);

        if (isSameDay(currentCalendar, messageCalendar)) {
            return "Today, " + time;
        } else if (isYesterday(currentCalendar, messageCalendar)) {
            return "Yesterday, " + time;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Updated date format
            String date = dateFormat.format(messageDate); // Format the date
            return date; // Combine date and time
        }
    }

    private boolean isSameDay(Calendar current, Calendar message) {
        return current.get(Calendar.YEAR) == message.get(Calendar.YEAR) && current.get(Calendar.DAY_OF_YEAR) == message.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(Calendar current, Calendar message) {
        current.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(current, message);
    }

    public void toggleSelection(int position) {
        if (selectedItems.size() >= 3 && !selectedItems.contains(position)) {
            Toast.makeText(context, "You cannot select more than 3 items", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);

        if (listener != null) {
            listener.onSelectionCountChanged(getSelectedItemCount(), searchList.get(position));
        }
    }

    public void clearSelection() {
        selectedItems.clear(); // Clear all selected items
    }



    private int getColorForSender(String sender) {
        if (sender == null || sender.isEmpty()) {
            return Color.parseColor("#808080"); // Default gray color
        }

        int hash = sender.hashCode(); // Generate hash from sender name
        int index = Math.abs(hash % mColors.size()); // Get index within color list
        String colorHex = "#" + mColors.get(index); // Get color from list

        return Color.parseColor(colorHex);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, SmsModel smsModel);

        void longClickListener(int position, SmsModel smsModel);

        void onSelectionCountChanged(int count, SmsModel smsModel);

    }

    static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView, dateTextView, senderInitialTextView;
        LinearLayout nameLay;
        ImageView checkmarkIcon, isPinIcon;


        public SmsViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            senderInitialTextView = itemView.findViewById(R.id.sender_initial_text_view);
            nameLay = itemView.findViewById(R.id.nameLay);
            checkmarkIcon = itemView.findViewById(R.id.checkmarkIcon);
            isPinIcon = itemView.findViewById(R.id.isPinIcon);
        }
    }

    public void pinSelectedItems() {
        List<SmsModel> selectedSms = getSelectedItems();

        // Check if adding these senders will exceed the limit
        int newPinnedSenders = 0;
        for (SmsModel sms : selectedSms) {
            if (!sms.isPinned()) {
                newPinnedSenders++;
            }
        }

        if (getPinnedSendersCount() + newPinnedSenders > 3) {
            Toast.makeText(context, "You cannot pin more than 3 senders", Toast.LENGTH_SHORT).show();
            return;
        }

        for (SmsModel sms : selectedSms) {
            sms.setPinned(!sms.isPinned()); // Toggle pin status
            savePinnedStatus(sms); // Save pinned status
        }
        notifyDataSetChanged();
    }

    private void savePinnedStatus(SmsModel sms) {
        SharedPreferences preferences = context.getSharedPreferences("PinnedMessages", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(sms.getSender(), sms.isPinned()); // Save pinned status using sender as key
        editor.apply();
    }

    public List<SmsModel> getSelectedItems() {
        List<SmsModel> selectedSms = new ArrayList<>();
        for (int i = 0; i < smsList.size(); i++) {
            if (selectedItems.contains(i)) {
                selectedSms.add(smsList.get(i));
            }
        }
        return selectedSms;
    }

    public void sortList() {
        Collections.sort(smsList, (m1, m2) -> {
            if (m1.isPinned() && !m2.isPinned()) {
                return -1; // m1 comes first
            } else if (!m1.isPinned() && m2.isPinned()) {
                return 1; // m2 comes first
            } else {
                return Long.compare(m2.getDateMillis(), m1.getDateMillis()); // Sort by date
            }
        });
        new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
    }
    public void loadPinnedStatus(List<SmsModel> smsList) {
        SharedPreferences preferences = context.getSharedPreferences("PinnedMessages", Context.MODE_PRIVATE);
        for (SmsModel sms : smsList) {
            boolean isPinned = preferences.getBoolean(sms.getSender(), false); // Load pinned status
            sms.setPinned(isPinned);
        }
    }


    public int getPinnedSendersCount() {
        int count = 0;
        for (SmsModel sms : smsList) {
            if (sms.isPinned()) {
                count++;
            }
        }
        return count;
    }
}