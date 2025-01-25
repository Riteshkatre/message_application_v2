package com.example.massageapplication.massage;

public class SmsModel {
    private String sender;
    private String body;
    private String date;
    private String time;
    private long dateMillis;
    private String status;

    private boolean isBlocked;
    private boolean isPinned;
    private int originalPosition;

    public SmsModel(String sender, String body, String date, String time, long dateMillis, String status) {
        this.sender = sender;
        this.body = body;
        this.date = date;
        this.time = time;
        this.dateMillis = dateMillis;
        this.status = status;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(int originalPosition) {
        this.originalPosition = originalPosition;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public long getDateMillis() {
        return dateMillis;
    }

    public String getStatus() {
        return status;
    }
}
