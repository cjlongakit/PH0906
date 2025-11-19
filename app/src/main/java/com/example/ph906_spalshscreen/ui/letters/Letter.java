package com.example.ph906_spalshscreen.ui.letters;

import org.json.JSONObject;
import org.json.JSONException;

public class Letter {
    private String ph906;
    private String fullName;
    private String address;
    private String type;
    private String deadline;
    private String status;

    public Letter(String ph906, String fullName, String address,
                  String type, String deadline, String status) {
        this.ph906 = ph906;
        this.fullName = fullName;
        this.address = address;
        this.type = type;
        this.deadline = deadline;
        this.status = status;
    }

    public String getPh906() { return ph906; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public String getDeadline() { return deadline; }
    public String getStatus() { return status; }

    public static Letter fromJson(JSONObject obj) {
        return new Letter(
            obj.optString("ph906", ""),
            obj.optString("full_name", ""),
            obj.optString("address", ""),
            obj.optString("type", ""),
            obj.optString("deadline", ""),
            obj.optString("status", "")
        );
    }
}
