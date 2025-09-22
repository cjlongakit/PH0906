package com.example.ph906_spalshscreen.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String BASE_URL = "https://hjcdc.swuitapp.com/api/api.php";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;
    private SharedPreferences prefs;

    public ApiClient(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        prefs = context.getSharedPreferences("student_prefs", Context.MODE_PRIVATE);
    }
    // ==============================
    // STUDENT LOGIN (use this one)
    // ==============================
    public void studentLogin(String ph906, String birthday, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("ph906", ph906);
            json.put("birthday", birthday);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url("https://hjcdc.swuitapp.com/api/login.php") // ✅ confirmed endpoint
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equals(jsonResponse.optString("status"))) {
                            boolean adult = isAdultFromBirthday(jsonResponse.optString("birthday", birthday));
                            String version = adult ? "adult" : "minor";

                            saveLoginInfo(
                                    jsonResponse.optString("ph906"),
                                    jsonResponse.optString("token"),
                                    jsonResponse.optString("first_name", "") + " " + jsonResponse.optString("last_name", ""),
                                    jsonResponse.optBoolean("is_default_password", true)
                            );

                            saveBirthday(jsonResponse.optString("birthday", birthday));
                            saveVersion(version);

                            callback.onSuccess(jsonResponse);
                        } else {
                            callback.onError("Login failed: " + jsonResponse.optString("message", "Login failed"));
                        }
                    } catch (JSONException e) {
                        callback.onError("JSON parsing error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // ==============================
    // MASTERLIST DATA
    // ==============================
    public void getMasterlist(ApiCallback callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/masterlist")
                .addHeader("Authorization", token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_DEBUG", "Masterlist response: " + responseBody);

                try {
                    if (responseBody.trim().startsWith("[")) {
                        JSONObject wrapper = new JSONObject();
                        wrapper.put("data", new JSONArray(responseBody));
                        callback.onSuccess(wrapper);
                    } else {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        callback.onSuccess(jsonResponse);
                    }
                } catch (JSONException e) {
                    callback.onError("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // CHANGE PASSWORD
    // ==============================
    public void changePassword(String currentPassword, String newPassword, ApiCallback callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("current_password", currentPassword);
            json.put("new_password", newPassword);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "/change_password")
                    .addHeader("Authorization", token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equals(jsonResponse.optString("status"))) {
                            prefs.edit().putBoolean("is_default_password", false).apply();
                            callback.onSuccess(jsonResponse);
                        } else {
                            callback.onError(jsonResponse.optString("message", "Failed to change password"));
                        }
                    } catch (JSONException e) {
                        callback.onError("JSON parse error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // ==============================
    // PROFILE & EVENTS
    // ==============================
    public void getMyProfile(ApiCallback callback) {
        requestWithToken("/my_profile", "GET", null, callback);
    }

    public void updateMyProfile(JSONObject payload, ApiCallback callback) {
        requestWithToken("/my_profile", "PUT", payload, callback);
    }

    public void getUpcomingEvents(ApiCallback callback) {
        requestWithToken("/my_events", "GET", null, callback);
    }

    // ==============================
    // INTERNAL HELPER: REQUEST WITH TOKEN
    // ==============================
    private void requestWithToken(String endpoint, String method, JSONObject jsonBody, ApiCallback callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", token);

        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            RequestBody body = jsonBody != null ?
                    RequestBody.create(JSON, jsonBody.toString()) :
                    RequestBody.create(JSON, "{}");
            builder.method(method, body);
        } else {
            builder.get();
        }

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String res = response.body().string();
                    JSONObject jsonResponse = new JSONObject(res);
                    if ("success".equals(jsonResponse.optString("status"))) {
                        callback.onSuccess(jsonResponse);
                    } else {
                        callback.onError(jsonResponse.optString("message", "Request failed"));
                    }
                } catch (Exception e) {
                    callback.onError("JSON parse error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // LOCAL STORAGE METHODS
    // ==============================
    private void saveLoginInfo(String ph906, String token, String fullName, boolean isDefaultPassword) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ph906", ph906);
        editor.putString("token", token);
        editor.putString("full_name", fullName);
        editor.putBoolean("is_default_password", isDefaultPassword);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    public String getToken() { return prefs.getString("token", null); }
    public String getLoggedInStudentId() { return prefs.getString("ph906", null); }
    public String getFullName() { return prefs.getString("full_name", ""); }
    public boolean isDefaultPassword() { return prefs.getBoolean("is_default_password", true); }
    public boolean isLoggedIn() { return prefs.getBoolean("is_logged_in", false); }
    public void logout() { prefs.edit().clear().apply(); }

    public void saveBirthday(String birthday) { prefs.edit().putString("birthday", birthday).apply(); }
    public String getBirthday() { return prefs.getString("birthday", null); }

    public void saveVersion(String version) { prefs.edit().putString("version", version).apply(); }
    public String getSavedVersion() { return prefs.getString("version", null); }

    // ==============================
    // AGE HELPERS
    // ==============================
    public boolean isAdult() {
        String birthday = getBirthday();
        if (birthday == null) return false;
        return isAdultFromBirthday(birthday);
    }

    private boolean isAdultFromBirthday(String birthday) {
        try {
            String[] parts = birthday.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            Calendar dob = Calendar.getInstance();
            dob.set(year, month - 1, day);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--;

            return age >= 18;
        } catch (Exception e) {
            return false;
        }
    }
}
