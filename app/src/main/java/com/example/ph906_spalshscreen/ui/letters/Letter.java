package com.example.ph906_spalshscreen.ui.letters;

public class Letter {
    private String ph906;
    private String firstName;
    private String lastName;
    private String address;
    private String type;
    private String deadline;
    private String status;

    public Letter(String ph906, String firstName, String lastName, String address,
                  String type, String deadline, String status) {
        this.ph906 = ph906;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.type = type;
        this.deadline = deadline;
        this.status = status;
    }

    public String getPh906() {
        return ph906;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }
}
