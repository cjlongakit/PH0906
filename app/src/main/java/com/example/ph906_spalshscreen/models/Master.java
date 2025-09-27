package com.example.ph906_spalshscreen.models;

import com.google.gson.annotations.SerializedName;

// This class now perfectly matches your masterlist table and API response
public class Master {

    // The @SerializedName annotation links the JSON key from the API
    // to the Java variable. This is the magic key.

    @SerializedName("ph906")
    private String ph906;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("sex")
    private String sex;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("age")
    private int age;

    @SerializedName("caseworker_assigned")
    private String caseworkerAssigned;

    @SerializedName("teacher")
    private String teacher;

    @SerializedName("mobile")
    private String mobile;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("mobile_number")
    private String mobileNumber;

    @SerializedName("address")
    private String address;

    @SerializedName("guardian_name")
    private String guardianName;

    @SerializedName("guardian_mobile")
    private String guardianMobile;

    @SerializedName("water_baptized")
    private String waterBaptized;

    // --- Generate Getters for ALL fields ---
    // In Android Studio: Right-click in the editor -> Generate -> Getter... -> Hold Shift and select all fields -> OK

    public String getPh906() { return ph906; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getSex() { return sex; }
    public String getBirthday() { return birthday; }
    public int getAge() { return age; }
    public String getCaseworkerAssigned() { return caseworkerAssigned; }
    public String getTeacher() { return teacher; }
    public String getMobile() { return mobile; }
    public String getNickname() { return nickname; }
    public String getMobileNumber() { return mobileNumber; }
    public String getAddress() { return address; }
    public String getGuardianName() { return guardianName; }
    public String getGuardianMobile() { return guardianMobile; }
    public String getWaterBaptized() { return waterBaptized; }
}