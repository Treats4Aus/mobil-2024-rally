package com.example.rally;

public class AppGroup {
    private String name;
    private String description;
    private String eventDate;

    public AppGroup() { }

    public AppGroup(String name, String description, String eventDate) {
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getEventDate() {
        return eventDate;
    }
}
