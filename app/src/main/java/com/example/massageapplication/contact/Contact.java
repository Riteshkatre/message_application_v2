package com.example.massageapplication.contact;

public class Contact {
    private String name;
    private String phone;
    private String contactImageUri;

    // Constructor
    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;

    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getContactImageUri() {
        return contactImageUri;
    }
}
