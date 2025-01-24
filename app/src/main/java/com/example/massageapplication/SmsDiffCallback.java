package com.example.massageapplication;

import androidx.recyclerview.widget.DiffUtil;

import com.example.massageapplication.massage.SmsModel;

import java.util.ArrayList;

public class SmsDiffCallback extends DiffUtil.Callback {

    private final ArrayList<SmsModel> oldList;
    private final ArrayList<SmsModel> newList;

    public SmsDiffCallback(ArrayList<SmsModel> oldList, ArrayList<SmsModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Check whether items are the same. Here, we're assuming that SmsModel has a unique identifier (like a message ID or sender address).
        return oldList.get(oldItemPosition).getSender().equals(newList.get(newItemPosition).getSender());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Check whether the contents of the items are the same.
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}