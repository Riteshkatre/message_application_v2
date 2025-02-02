package com.example.massageapplication.massage;

public class SmsModel  {
    private String sender;
    private String body;
    private String date;
    private String time;
    private long dateMillis;
    private String status;

    private boolean isBlocked;
    private boolean isPinned;
    private boolean isArchive;
    private int originalPosition;
    private Integer  color;

    public SmsModel(String sender, String body, String date, String time, long dateMillis, String status,Integer  color) {
        this.sender = sender;
        this.body = body;
        this.date = date;
        this.time = time;
        this.dateMillis = dateMillis;
        this.status = status;
        this.color = color;

    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public void setArchive(boolean archive) {
        isArchive = archive;
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

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDateMillis(long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public void setStatus(String status) {
        this.status = status;
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
