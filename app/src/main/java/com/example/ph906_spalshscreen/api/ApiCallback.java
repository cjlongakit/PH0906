package com.example.ph906_spalshscreen.api;

import org.json.JSONObject;

public interface ApiCallback {
    void onSuccess(JSONObject response);
    void onError(String error);
}