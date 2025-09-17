package com.example.ph906_spalshscreen.network;

import com.example.ph906_spalshscreen.models.LoginRequest;
import com.example.ph906_spalshscreen.models.LoginResponse;
import com.example.ph906_spalshscreen.models.Master;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    // 🔹 Login API
    @POST("api/api.php/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    // 🔹 Fetch Masterlist (example)
    @GET("api/masterlist.php")
    Call<List<Master>> getMasterlist(@Header("Authorization") String authHeader);
}