package com.example.ph906_spalshscreen.models;

public class LoginResponse {
    public String status;             // "success" or "error"
    public String message;            // error message (if any)
    public String role;               // "admin" or "student"
    public String ph906;              // student/admin ID
    public String username;           // same as ph906
    public String name;               // full name
    public String token;              // auth token
    public boolean is_default_password; // true if using birthday
}