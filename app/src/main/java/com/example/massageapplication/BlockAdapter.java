package com.example.massageapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.massageapplication.drawerActivity.ArchiveAdapter;
import com.example.massageapplication.massage.SmsModel;

import java.util.ArrayList;

class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.ViewHolder> {
    private ArrayList<SmsModel> blockedMessages;
    Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position,SmsModel smsModel);


    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public BlockedAdapter(Context context,ArrayList<SmsModel> blockedMessages) {
        this.blockedMessages = blockedMessages;
        this.context=context;
    }

    public void updateList(ArrayList<SmsModel> updatedList) {
        this.blockedMessages = updatedList;
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
/*
        holder.threeDot.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Unblock Sender")
                    .setMessage("Are you sure you want to unblock this sender?")
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        // Remove sender from blocked list
                        unblockSender(message.getSender());
                        blockedMessages.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, blockedMessages.size());
                    })
                    .setNegativeButton("No", (dialogInterface, which) -> {
                        // Dismiss the dialog explicitly
                        dialogInterface.dismiss();
                    })
                    .create();

            dialog.show();
        });
*/
        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                listener.onItemClick(position, blockedMessages.get(position));
            }

        });

    }

    @Override
    public int getItemCount() {
        return blockedMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView, dateTextView, senderInitialTextView;
        LinearLayout nameLay;
        ImageView checkmarkIcon,isPinIcon;
        public ViewHolder(@NonNull View itemView) {
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

    private void unblockSender(String sender) {
        SharedPreferences preferences = context.getSharedPreferences("BlockedSenders", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(sender); // Remove sender from blocked list
        editor.apply();

    }
}
