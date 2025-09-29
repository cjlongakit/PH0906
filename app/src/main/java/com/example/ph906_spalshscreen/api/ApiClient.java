package com.example.ph906_spalshscreen.api;

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.example.ph906_spalshscreen.PrefsHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
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
    private final Context appContext;

    public ApiClient(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        prefsHelper = new PrefsHelper(context);
        appContext = context.getApplicationContext();
    }

    // Expose OkHttp so activities can reuse it for custom requests/fallbacks
    public OkHttpClient getOkHttp() {
        return client;
    }

    // ==============================
    // LOGIN (with tolerant retries)
    // ==============================
    public void studentLogin(String ph906Input, String birthdayInput, ApiCallback callback) {
        // Prepare candidate combos to tolerate backend format quirks
        String digits = ph906Input == null ? "" : ph906Input.replaceAll("[^0-9]", "");
        String ymd = birthdayInput == null ? "" : birthdayInput.trim();
        String us = toUsDate(ymd);
        String pref = "PH906-";
        String padded4 = leftPad(digits, 4);

        String[][] combos = new String[][]{
                {digits, ymd},
                {pref + digits, ymd},
                {pref + padded4, ymd},
                {digits, us},
                {pref + digits, us},
                {pref + padded4, us}
        };
        String[] endpoints = new String[]{
                "/login.php",
                "/api.php?resource=login",
                "/api.php/login"
        };
        attemptLoginCombos(combos, 0, endpoints, 0, callback);
    }

    private void attemptLoginCombos(String[][] combos, int comboIdx, String[] endpoints, int epIdx, ApiCallback callback) {
        if (comboIdx >= combos.length) { callback.onError("Invalid credentials"); return; }
        if (epIdx >= endpoints.length) { attemptLoginCombos(combos, comboIdx + 1, endpoints, 0, callback); return; }
        String ph = combos[comboIdx][0];
        String bd = combos[comboIdx][1];
        String endpoint = endpoints[epIdx];
        attemptLoginOnEndpoint(endpoint, ph, bd, new ApiCallback() {
            @Override public void onSuccess(JSONObject response) { callback.onSuccess(response); }
            @Override public void onError(String message) {
                // If looks like invalid/401, try next combo; otherwise try next endpoint first
                String lower = message == null ? "" : message.toLowerCase(java.util.Locale.US);
                if (lower.contains("invalid") || lower.contains("401")) {
                    attemptLoginCombos(combos, comboIdx + 1, endpoints, 0, callback);
                } else {
                    attemptLoginCombos(combos, comboIdx, endpoints, epIdx + 1, callback);
                }
            }
        });
    }

    private void attemptLoginOnEndpoint(String endpoint, String ph, String bd, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("ph906", ph);
            json.put("birthday", bd);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + endpoint)
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
                        String status = jsonResponse.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            // Save details into prefs
                            prefsHelper.saveLoginInfo(
                                    jsonResponse.optString("ph906"),
                                    jsonResponse.optString("token"),
                                    jsonResponse.optString("first_name", "") + " " + jsonResponse.optString("last_name", ""),
                                    jsonResponse.optBoolean("is_default_password", true)
                            );
                            prefsHelper.saveBirthday(jsonResponse.optString("birthday", bd));
                            boolean adult = isAdultFromBirthday(jsonResponse.optString("birthday", bd));
                            prefsHelper.saveVersion(adult ? "adult" : "minor");
                            callback.onSuccess(jsonResponse);
                        } else {
                            String msg = jsonResponse.optString("message", "Login failed (HTTP " + response.code() + ")");
                            callback.onError(msg);
                        }
                    } catch (JSONException e) {
                        callback.onError("JSON parse error (HTTP " + response.code() + ") on " + endpoint + ": " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    private String toUsDate(String ymd) {
        try {
            if (ymd == null) return "";
            // If already YYYY-MM-DD, convert to MM/DD/YYYY for tolerant retry
            if (ymd.matches("\\d{4}-\\d{2}-\\d{2}")) {
                String[] p = ymd.split("-");
                return p[1] + "/" + p[2] + "/" + p[0];
            }
            return ymd;
        } catch (Exception ignored) { return ymd; }
    }

    private String leftPad(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < width; i++) sb.append('0');
        sb.append(s);
        return sb.toString();
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

            RequestBody body = RequestBody.create(json.toString(), JSON);
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
                            callback.onError(jsonResponse.optString("message", "Failed to change password (HTTP " + response.code() + ")"));
                        }
                    } catch (JSONException e) {
                        callback.onError("JSON parse error (HTTP " + response.code() + "): " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // ==============================
    // PROFILE (robust with ph906 param + fallback)
    // ==============================
    public void getMyProfile(ApiCallback callback) {
        String digits = safeDigits(prefsHelper.getPh906());
        String[] endpoints = new String[] {
                "/api.php?resource=my_profile" + (digits.isEmpty() ? "" : "&ph906=" + digits),
                "/api.php?route=my_profile"    + (digits.isEmpty() ? "" : "&ph906=" + digits),
                "/api.php/my_profile"          + (digits.isEmpty() ? "" : "?ph906=" + digits),
                "/my_profile.php"              + (digits.isEmpty() ? "" : "?ph906=" + digits)
        };
        requestWithTokenFallback(endpoints, "GET", null, callback, 0, "GET my_profile");
    }

    public void updateMyProfile(JSONObject payload, ApiCallback callback) {
        String digits = safeDigits(prefsHelper.getPh906());
        String[] endpoints = new String[] {
                "/api.php?resource=masterlist" + (digits.isEmpty() ? "" : "&ph906=" + digits),
                "/api.php?route=masterlist"    + (digits.isEmpty() ? "" : "&ph906=" + digits),
                "/api.php/masterlist"          + (digits.isEmpty() ? "" : "?ph906=" + digits),
                "/masterlist.php"              + (digits.isEmpty() ? "" : "?ph906=" + digits)
        };
        requestWithTokenFallback(endpoints, "PUT", payload, callback, 0, "PUT masterlist");
    }

    private void requestWithTokenFallback(String[] endpoints, String method, JSONObject jsonBody,
                                          ApiCallback callback, int idx, String opTag) {
        if (idx >= endpoints.length) {
            callback.onError(opTag + " failed on all endpoints");
            return;
        }
        String token = prefsHelper.getToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Not logged in");
            return;
        }

        String endpoint = endpoints[idx];
        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", token)
                .addHeader("Accept", "application/json");

        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            RequestBody body = jsonBody != null
                    ? RequestBody.create(jsonBody.toString(), JSON)
                    : RequestBody.create("{}", JSON);
            builder.method(method, body);
        } else {
            builder.get();
        }

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Try next endpoint
                requestWithTokenFallback(endpoints, method, jsonBody, callback, idx + 1, opTag);
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject jsonResponse = new JSONObject(res);
                    // Accept only explicit success
                    if (response.isSuccessful() && "success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                        callback.onSuccess(jsonResponse);
                    } else {
                        // Try next endpoint; if this was the last, surface detailed message
                        if (idx + 1 < endpoints.length) {
                            requestWithTokenFallback(endpoints, method, jsonBody, callback, idx + 1, opTag);
                        } else {
                            String msg = jsonResponse.optString("message", res);
                            callback.onError(opTag + " failed (HTTP " + response.code() + " @ " + endpoint + "): " + msg);
                        }
                    }
                } catch (JSONException e) {
                    // If body wasn't valid JSON (e.g., empty or HTML), try next; else report with context
                    if (idx + 1 < endpoints.length) {
                        requestWithTokenFallback(endpoints, method, jsonBody, callback, idx + 1, opTag);
                    } else {
                        String snippet = res == null ? "" : (res.length() > 180 ? res.substring(0, 180) + "..." : res);
                        callback.onError(opTag + " JSON parse error (HTTP " + response.code() + " @ " + endpoint + "): " + e.getMessage() +
                                (snippet.isEmpty() ? "" : " body=" + snippet));
                    }
                }
            }
        });
    }

    // ==============================
    // GET MASTERLIST (LETTERS) — unchanged
    // ==============================
    public void getMasterlist(final ApiCallback callback) {
        String endpointUrl = BASE_URL + "/get_students.php";

        Request request = new Request.Builder()
                .url(endpointUrl)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }
            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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

    // ==============================
    // UPDATE MASTERLIST ENTRY (id-based) — unchanged
    // ==============================
    public void updateMasterlist(String studentId, JSONObject payload, ApiCallback callback) {
        String token = prefsHelper.getToken(); // if you require auth header
        String url = BASE_URL + "/masterlist/" + studentId;

        RequestBody body = RequestBody.create(payload.toString(), JSON);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .put(body); // HTTP PUT

        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject jsonResponse = new JSONObject(res);
                    if ("success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                        callback.onSuccess(jsonResponse);
                    } else {
                        callback.onError("Update failed (HTTP " + response.code() + "): " +
                                jsonResponse.optString("message", res));
                    }
                } catch (JSONException e) {
                    callback.onError("JSON parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Uploads a profile photo for the currently logged-in student.
     * Endpoint: POST {BASE_URL}/upload_profile_photo.php
     * Form fields: ph906 (digits only), photo (file)
     * Returns JSON with {status, url}
     */
    public void uploadProfilePhoto(Uri imageUri, ApiCallback callback) {
        if (imageUri == null) {
            callback.onError("No image selected");
            return;
        }
        String rawId = prefsHelper.getPh906();
        if (rawId == null || rawId.trim().isEmpty()) {
            callback.onError("No student id in session");
            return;
        }
        String digits = rawId.replaceAll("[^0-9]", "");

        String fileName = queryDisplayName(imageUri);
        if (fileName == null || fileName.isEmpty()) fileName = "profile.jpg";

        RequestBody fileBody = new RequestBody() {
            @Override public MediaType contentType() { return MediaType.parse("image/*"); }
            @Override public void writeTo(BufferedSink sink) throws IOException {
                try (java.io.InputStream is = appContext.getContentResolver().openInputStream(imageUri)) {
                    if (is == null) throw new IOException("Cannot open imageUri");
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        sink.write(buffer, 0, read);
                    }
                }
            }
        };

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ph906", digits)
                .addFormDataPart("photo", fileName, fileBody)
                .build();

        Request.Builder reqBuilder = new Request.Builder()
                .url(BASE_URL + "/upload_profile_photo.php")
                .post(requestBody)
                .addHeader("Accept", "application/json");
        String token = prefsHelper.getToken();
        if (token != null && !token.isEmpty()) reqBuilder.addHeader("Authorization", token);

        client.newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError("Upload failed: " + e.getMessage());
            }
            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject json = new JSONObject(body);
                    if ("success".equalsIgnoreCase(json.optString("status"))) {
                        callback.onSuccess(json);
                    } else {
                        callback.onError("Upload failed (HTTP " + response.code() + "): " +
                                json.optString("message", body));
                    }
                } catch (JSONException e) {
                    callback.onError("Upload JSON parse error (HTTP " + response.code() + "): " + e.getMessage() +
                            (body == null || body.isEmpty() ? "" : " body=" + (body.length() > 180 ? body.substring(0,180)+"..." : body)));
                }
            }
        });
    }

    private String queryDisplayName(Uri uri) {
        try (android.database.Cursor c = appContext.getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return null;
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

    private String safeDigits(String raw) {
        if (raw == null) return "";
        String d = raw.replaceAll("[^0-9]", "");
        return d == null ? "" : d;
    }
}