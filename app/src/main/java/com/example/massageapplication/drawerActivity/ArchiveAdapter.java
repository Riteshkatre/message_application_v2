package com.example.massageapplication.drawerActivity;

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
import com.example.massageapplication.massage.SmsAdapter;
import com.example.massageapplication.massage.SmsModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.SmsViewHolder> {
    private ArrayList<SmsModel> smsList;
    private OnItemClickListener listener;
    private Set<Integer> selectedItems;
    public interface OnItemClickListener {
        void onItemClick(int position,SmsModel smsModel);
        void longClickListener(int position, SmsModel smsModel);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void updateList(ArrayList<SmsModel> newList) {
        this.smsList = newList;
        notifyDataSetChanged();
    }
    public ArchiveAdapter(ArrayList<SmsModel> smsList) {
        this.smsList = smsList;
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
        SmsModel sms = smsList.get(position);

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
            background.setColor(color);
        }
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

        if (selectedItems.contains(position)) {
            holder.checkmarkIcon.setVisibility(View.VISIBLE);
        } else {
            holder.checkmarkIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return smsList.size();
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

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);
    }
    public void removeItem(SmsModel smsModel) {
        int position = smsList.indexOf(smsModel); // Assuming archivedMessages is the list in your adapter
        if (position != -1) {
            smsList.remove(position);
            notifyItemRemoved(position);
        }
    }

}
