package com.example.massageapplication.massage;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<SmsModel> messageList;
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    public MessageAdapter(ArrayList<SmsModel> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        SmsModel sms = messageList.get(position);
        if ("You".equalsIgnoreCase(sms.getSender())) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_message, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_other_message, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SmsModel sms = messageList.get(position);

        if (holder instanceof MyMessageViewHolder) {
            ((MyMessageViewHolder) holder).messageTextView.setText(sms.getBody());
            ((MyMessageViewHolder) holder).timeTextView.setText(getFormattedDate(sms.getDateMillis()));
        } else if (holder instanceof OtherMessageViewHolder) {
            ((OtherMessageViewHolder) holder).messageTextView.setText(sms.getBody());
            String senderText = sms.getSender() + " " + getFormattedDate(sms.getDateMillis());
            ((OtherMessageViewHolder) holder).timeTextView.setText(senderText);

            if (sms.getSender().length() > 0) {
                String initial = String.valueOf(sms.getSender().charAt(0)).toUpperCase();
                ((OtherMessageViewHolder) holder).firstName.setText(initial);

                int color = getColorForInitial(initial);

                ((OtherMessageViewHolder) holder).nameLay.setBackgroundResource(R.drawable.circle_background);
                GradientDrawable background = (GradientDrawable) ((OtherMessageViewHolder) holder).nameLay.getBackground();
                background.setColor(color);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeTextView;

        public MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tvMyMessage);
            timeTextView = itemView.findViewById(R.id.tvTime);
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeTextView, firstName;
        LinearLayout nameLay;

        public OtherMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tvMyMessage);
            timeTextView = itemView.findViewById(R.id.tvTime);
            firstName = itemView.findViewById(R.id.firstName);
            nameLay = itemView.findViewById(R.id.nameLay);
        }
    }

    private int getColorForInitial(String initial) {
        int hash = Math.abs(initial.hashCode());
        int[] colors = {
                Color.parseColor("#2b73ec"), // Example color
        };
        return colors[hash % colors.length];
    }

    // Helper method to format date
    private String getFormattedDate(long dateMillis) {
        Date currentDate = new Date();
        Date messageDate = new Date(dateMillis);

        // Format for time (HH:mm)
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
            return dateFormat.format(messageDate);
        }
    }

    // Check if the message is from the same day
    private boolean isSameDay(Calendar current, Calendar message) {
        return current.get(Calendar.YEAR) == message.get(Calendar.YEAR) &&
                current.get(Calendar.DAY_OF_YEAR) == message.get(Calendar.DAY_OF_YEAR);
    }

    // Check if the message is from yesterday
    private boolean isYesterday(Calendar current, Calendar message) {
        current.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(current, message);
    }
}
