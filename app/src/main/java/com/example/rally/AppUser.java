package com.example.rally;

public class AppUser {
    private String displayName;
    private String email;
    private String phone;

    public AppUser() { }

    public AppUser(String displayName, String email, String phone) {
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
