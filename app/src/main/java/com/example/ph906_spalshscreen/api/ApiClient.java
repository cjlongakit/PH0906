package com.example.ph906_spalshscreen.api;

import android.content.Context;
import com.example.ph906_spalshscreen.PrefsHelper;
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
import org.jetbrains.annotations.NotNull;

public class ApiClient {

    private static final String BASE_URL = "https://hjcdc.swuitapp.com/api"; // adjust if needed
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final PrefsHelper prefsHelper;

    public ApiClient(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        prefsHelper = new PrefsHelper(context);
    }

    // ==============================
    // LOGIN
    // ==============================
    public void studentLogin(String ph906, String birthday, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("ph906", ph906);
            json.put("birthday", birthday);

            RequestBody body = RequestBody.create(json.toString(), JSON); // updated signature
            Request request = new Request.Builder()
                    .url(BASE_URL + "/login.php")
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                            // Save details into prefs
                            prefsHelper.saveLoginInfo(
                                    jsonResponse.optString("ph906"),
                                    jsonResponse.optString("token"),
                                    jsonResponse.optString("first_name", "") + " " + jsonResponse.optString("last_name", ""),
                                    jsonResponse.optBoolean("is_default_password", true)
                            );
                            prefsHelper.saveBirthday(jsonResponse.optString("birthday", birthday));
                            boolean adult = isAdultFromBirthday(jsonResponse.optString("birthday", birthday));
                            prefsHelper.saveVersion(adult ? "adult" : "minor");

                            callback.onSuccess(jsonResponse);
                        } else {
                            callback.onError(jsonResponse.optString("message", "Login failed"));
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
    // CHANGE PASSWORD
    // ==============================
    public void changePassword(String currentPassword, String newPassword, ApiCallback callback) {
        String token = prefsHelper.getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("current_password", currentPassword);
            json.put("new_password", newPassword);

            RequestBody body = RequestBody.create(json.toString(), JSON); // updated signature
            Request request = new Request.Builder()
                    .url(BASE_URL + "/change_password.php")
                    .addHeader("Authorization", token)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                            // Update prefs: no longer default password
                            prefsHelper.saveLoginInfo(
                                    prefsHelper.getPh906(),
                                    prefsHelper.getToken(),
                                    prefsHelper.getFullName(),
                                    false
                            );
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
    // PROFILE
    // ==============================
    public void getMyProfile(ApiCallback callback) {
        requestWithToken("/my_profile.php", "GET", null, callback);
    }

    public void updateMyProfile(JSONObject payload, ApiCallback callback) {
        requestWithToken("/my_profile.php", "PUT", payload, callback);
    }

    private void requestWithToken(String endpoint, String method, JSONObject jsonBody, ApiCallback callback) {
        String token = prefsHelper.getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", token)
                .addHeader("Accept", "application/json");

        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            RequestBody body = jsonBody != null ?
                    RequestBody.create(jsonBody.toString(), JSON) : // updated signature
                    RequestBody.create("{}", JSON); // updated signature
            builder.method(method, body);
        } else {
            builder.get();
        }

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject jsonResponse = new JSONObject(res);
                    if ("success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                        callback.onSuccess(jsonResponse);
                    }
                    else {
                        callback.onError(jsonResponse.optString("message", "Request failed"));
                    }
                } catch (JSONException e) {
                    callback.onError("JSON parse error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // SESSION HELPERS
    // ==============================
    public boolean isLoggedIn() { return prefsHelper.isLoggedIn(); }
    public String getFullName() { return prefsHelper.getFullName(); }
    public String getLoggedInStudentId() { return prefsHelper.getPh906(); }
    public String getSavedVersion() { return prefsHelper.getVersion(); }
    public void logout() { prefsHelper.clearAll(); }

    // ==============================
    // AGE CALCULATOR
    // ==============================
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

    // ==============================
    // GET MASTERLIST (LETTERS)
    // ==============================
    /**
     * Fetches the student masterlist from the PHP endpoint.
     * The endpoint should be get_students.php on your server.
     * Returns the JSON response to the provided ApiCallback.
     */
    public void getMasterlist(final ApiCallback callback) {
        String endpointUrl = BASE_URL + "/get_students.php";

        Request request = new Request.Builder()
                .url(endpointUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    callback.onSuccess(jsonResponse);
                } catch (JSONException e) {
                    callback.onError("JSON parse error: " + e.getMessage());
                }
            }
        });
    }
}
