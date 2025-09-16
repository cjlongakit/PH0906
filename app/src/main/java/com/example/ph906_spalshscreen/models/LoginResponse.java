package com.example.ph906_spalshscreen.models;

public class LoginResponse {
    public String status;     // "success" or "error"
    public String message;    // server message
    public String token;      // JWT or session token
    public String username;   // logged-in username
    public int age;           // user’s calculated age
}
