package com.example.ph906_spalshscreen.ui.letters;

public class Letter {
    private String ph906;
    private String lastName;
    private String firstName;
    private String address;
    private String type;
    private String status;

    public Letter(String ph906, String lastName, String firstName, String address, String type, String status) {
        this.ph906 = ph906;
        this.lastName = lastName;
        this.firstName = firstName;
        this.address = address;
        this.type = type;
        this.status = status;
    }

    // Getters
    public String getPh906() { return ph906; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public String getStatus() { return status; }
}