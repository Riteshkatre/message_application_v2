package com.example.massageapplication.massage;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsViewHolder> {
    private ArrayList<SmsModel> smsList;
    private ArrayList<SmsModel> searchList;
    private OnItemClickListener listener;
    private Set<Integer> selectedItems;
    private boolean isSelectionMode = false;


    public void enableSelectionMode(boolean enable) {
        isSelectionMode = enable;
    }


    public interface OnItemClickListener {
        void onItemClick(int position,SmsModel smsModel);
        void longClickListener(int position,SmsModel smsModel);

        void onSelectionCountChanged(int count);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }




    public SmsAdapter(ArrayList<SmsModel> smsList) {
        this.smsList = smsList;
        this.searchList = smsList;
        this.selectedItems = new HashSet<>();
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

        // Set the sender's name or "Me"
        String sender = sms.getStatus().equals("sent") ? "Me" : sms.getSender();
        holder.senderTextView.setText(sender);

        // Set the message body
        holder.messageTextView.setText(sms.getBody());

        // Set the time format
        String formattedTime = sms.getTime();
        holder.dateTextView.setText(formattedTime); // For now, show just the time part

        // Set the sender's initial (first letter) in uppercase
        if (sender.length() > 0) {
            String initial = String.valueOf(sender.charAt(0)).toUpperCase();
            holder.senderInitialTextView.setText(initial);

            // Get a unique color for the initial
            int color = getColorForInitial(initial);

            // Change the background color of the `circle_background` drawable
            holder.nameLay.setBackgroundResource(R.drawable.circle_background); // Ensure the shape is applied
            GradientDrawable background = (GradientDrawable) holder.nameLay.getBackground();
            background.setColor(color); // Set the dynamic color
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

    static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView, dateTextView, senderInitialTextView;
        LinearLayout nameLay;
        ImageView checkmarkIcon,isPinIcon;


        public SmsViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            senderInitialTextView = itemView.findViewById(R.id.sender_initial_text_view);
            nameLay = itemView.findViewById(R.id.nameLay);
            checkmarkIcon = itemView.findViewById(R.id.checkmarkIcon);
            isPinIcon = itemView.findViewById(R.id.checkmarkIcon);
        }
    }

    // Helper method to generate a unique color for each initial
    private int getColorForInitial(String initial) {
        // Generate color based on the hashcode of the initial
        int hash = Math.abs(initial.hashCode());
        int[] colors = {
                Color.parseColor("#FFCDD2"), // Light red
                Color.parseColor("#F8BBD0"), // Light pink
                Color.parseColor("#E1BEE7"), // Light purple
                Color.parseColor("#D1C4E9"), // Light indigo
                Color.parseColor("#BBDEFB"), // Light blue
                Color.parseColor("#B2EBF2"), // Light cyan
                Color.parseColor("#C8E6C9"), // Light green
                Color.parseColor("#DCEDC8"), // Light lime
                Color.parseColor("#FFF9C4"), // Light yellow
                Color.parseColor("#FFE0B2"), // Light orange
        };
        return colors[hash % colors.length];
    }

    // Search functionality
    public void search(CharSequence charSequence, RecyclerView rcv, LinearLayout textView) {
        try {
            String charString = charSequence.toString().toLowerCase().trim();
            if (charString.isEmpty()) {
                searchList = smsList;
                rcv.setVisibility(View.VISIBLE);
            } else {
                int flag = 0;
                ArrayList<SmsModel> filterList = new ArrayList<>();
                for (SmsModel row : smsList) {
                    if (row.getSender().toLowerCase().contains(charString)) {
                        filterList.add(row);
                        flag = 1;
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
        Date currentDate = new Date();
        Date messageDate = new Date(dateMillis);

        // Format for time (HH:mm)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = timeFormat.format(messageDate);

        // Check if the message is today, yesterday, or an older date
        Calendar currentCalendar = Calendar.getInstance();
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);

        if (isSameDay(currentCalendar, messageCalendar)) {
            return "Today, " + time;
        } else if (isYesterday(currentCalendar, messageCalendar)) {
            return "Yesterday, " + time;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
            return dateFormat.format(messageDate);
        }
    }

    // Helper method to check if the message is from the same day
    private boolean isSameDay(Calendar current, Calendar message) {
        return current.get(Calendar.YEAR) == message.get(Calendar.YEAR) &&
                current.get(Calendar.DAY_OF_YEAR) == message.get(Calendar.DAY_OF_YEAR);
    }

    // Helper method to check if the message is from yesterday
    private boolean isYesterday(Calendar current, Calendar message) {
        current.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(current, message);
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);

        if (listener != null) {
            listener.onSelectionCountChanged(getSelectedItemCount());
        }
    }

    public void clearSelection() {
        selectedItems.clear(); // Clear all selected items
    }
    public ArrayList<SmsModel> getSelectedItems() {
        ArrayList<SmsModel> selectedItems = new ArrayList<>();
        for (int i = 0; i < smsList.size(); i++) {
            if (smsList.get(i).isPinned()) { // Assuming you have an isSelected() method
                selectedItems.add(smsList.get(i));
            }
        }
        return selectedItems;
    }

}