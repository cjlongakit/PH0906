package com.example.ph906_spalshscreen.models;

public class Master {
    private int id;
    private String name;
    private String details;

    // ✅ Required for Gson (Retrofit auto-mapping)
    public Master() {}

    public Master(int id, String name, String details) {
        this.id = id;
        this.name = name;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }
}
