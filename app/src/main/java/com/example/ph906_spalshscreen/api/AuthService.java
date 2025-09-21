package com.example.ph906_spalshscreen.api;

import com.example.ph906_spalshscreen.data.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthService {

    @FormUrlEncoded
    @POST("login.php") // or "api.php" depending on your server
    Call<User> login(
            @Field("username") String username,
            @Field("birthday") String birthday
    );
}
