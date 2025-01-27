package com.example.massageapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.massage.SmsModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.ViewHolder> {
    private ArrayList<SmsModel> blockedMessages;
    private Context context;
    private OnItemClickListener listener;
    private Set<Integer> selectedItems;

    public interface OnItemClickListener {
        void onItemClick(int position, SmsModel smsModel);
        void longClickListener(int position, SmsModel smsModel);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BlockedAdapter(Context context, ArrayList<SmsModel> blockedMessages) {
        this.blockedMessages = blockedMessages;
        this.context = context;
        this.selectedItems = new HashSet<>();
    }

    public void updateList(ArrayList<SmsModel> newList) {
        this.blockedMessages = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsModel message = blockedMessages.get(position);
        holder.senderTextView.setText(message.getSender());
        holder.messageTextView.setText(message.getBody());
        holder.dateTextView.setText(message.getDate());

        holder.itemView.setOnClickListener(v -> {
            if (selectedItems.size() > 0) {
                toggleSelection(position);
            } else {
                if (listener != null) {
                    listener.onItemClick(position, message);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            toggleSelection(position);
            if (listener != null) {
                listener.longClickListener(position, message);
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
        return blockedMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView, dateTextView, senderInitialTextView;
        LinearLayout nameLay;
        ImageView checkmarkIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            senderInitialTextView = itemView.findViewById(R.id.sender_initial_text_view);
            nameLay = itemView.findViewById(R.id.nameLay);
            checkmarkIcon = itemView.findViewById(R.id.checkmarkIcon);
        }
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);
    }

}
