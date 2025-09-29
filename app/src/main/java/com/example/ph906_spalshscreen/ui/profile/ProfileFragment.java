package com.example.ph906_spalshscreen.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ph906_spalshscreen.PrefsHelper;
import com.example.ph906_spalshscreen.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private TextView tvUserId, tvFirstName, tvLastName, tvBirthdate, tvNickname,
            tvMobile, tvAddress, tvGuardian, tvGuardianMobile, tvBaptized, tvTeacher,
            tvSex, tvCaseworker, tvAge;
    private Button btnEdit;
    private ImageView imgProfile;

    private String ph906Raw; // what we get from prefs (likely a 3-digit numeric)
    private String apiBase = "https://hjcdc.swuitapp.com/api";
    private RequestQueue queue;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && getContext() != null) {
                    PrefsHelper prefs = new PrefsHelper(requireContext());
                    String photoUri = prefs.getProfilePhotoUri();
                    if (photoUri != null) {
                        try { imgProfile.setImageURI(Uri.parse(photoUri)); } catch (Exception ignored) {}
                    }
                    loadProfile();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        imgProfile = v.findViewById(R.id.img_profile);
        tvUserId = v.findViewById(R.id.tv_user_id);
        tvFirstName = v.findViewById(R.id.tv_first_name);
        tvLastName = v.findViewById(R.id.tv_last_name);
        tvBirthdate = v.findViewById(R.id.tv_birthdate);
        tvNickname = v.findViewById(R.id.tv_nickname);
        tvMobile = v.findViewById(R.id.tv_mobile);
        tvAddress = v.findViewById(R.id.tv_address);
        tvGuardian = v.findViewById(R.id.tv_guardian);
        tvGuardianMobile = v.findViewById(R.id.tv_guardian_mobile);
        tvBaptized = v.findViewById(R.id.tv_baptized);
        tvTeacher = v.findViewById(R.id.tv_teacher);
        tvSex = v.findViewById(R.id.tv_sex);
        tvCaseworker = v.findViewById(R.id.tv_caseworker);
        tvAge = v.findViewById(R.id.tv_age);
        btnEdit = v.findViewById(R.id.btn_edit);

        queue = Volley.newRequestQueue(requireContext());

        // Pull ph906 and photo from prefs
        PrefsHelper prefs = new PrefsHelper(requireContext());
        ph906Raw = prefs.getPh906();
        tvUserId.setText(formatPh906(ph906Raw));

        String photoUri = prefs.getProfilePhotoUri();
        if (photoUri != null) {
            try {
                imgProfile.setImageURI(Uri.parse(photoUri));
            } catch (Exception ignored) {}
        }

        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            // Pass only the ph906Id; Edit screen will load rest and allow photo change
            intent.putExtra("ph906Id", ph906Raw);
            editLauncher.launch(intent);
        });

        loadProfile();
        return v;
    }

    private String formatPh906(String raw) {
        if (raw == null || raw.isEmpty()) return "PH906-"; // fallback
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() >= 3) {
            return "PH906-" + digits; // show as-is (no forced leading zeros)
        }
        return raw.startsWith("PH906-") ? raw : ("PH906-" + raw);
    }

    private void loadProfile() {
        PrefsHelper prefs = new PrefsHelper(requireContext());
        String token = prefs.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(getContext(), "Not logged in (missing token)", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] urls = new String[] {
                apiBase + "/api.php/my_profile",
                apiBase + "/api.php?resource=my_profile",
                apiBase + "/api.php?route=my_profile",
                apiBase + "/my_profile.php"
        };
        requestProfileWithAuth(urls, 0, token);
    }

    private void requestProfileWithAuth(String[] urls, int index, String token) {
        if (index >= urls.length) {
            Toast.makeText(getContext(), "Load failed (all endpoints)", Toast.LENGTH_LONG).show();
            return;
        }
        String url = urls[index];
        Log.d(TAG, "GET " + url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                this::handleProfileResponse,
                error -> {
                    logVolleyError(url, error);
                    requestProfileWithAuth(urls, index + 1, token);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Accept", "application/json");
                h.put("Authorization", token);
                return h;
            }
        };
        queue.add(req);
    }

    private void logVolleyError(String url, VolleyError error) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request failed: ").append(url).append('\n');
        if (error.networkResponse != null) {
            sb.append("code=").append(error.networkResponse.statusCode);
            try {
                String body = new String(error.networkResponse.data);
                sb.append(" body=").append(body);
            } catch (Exception ignored) {}
        } else if (error.getMessage() != null) {
            sb.append("msg=").append(error.getMessage());
        } else {
            sb.append(String.valueOf(error));
        }
        Log.e(TAG, sb.toString());
        Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
    }

    private void handleProfileResponse(JSONObject response) {
        try {
            // Accept either { ...fields } or { data: { ...fields } } or { status: success, data: {...} }
            JSONObject obj = response;
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                obj = response.getJSONObject("data");
            }
            // Fill views with graceful fallbacks
            tvFirstName.setText(optString(obj, "first_name"));
            tvLastName.setText(optString(obj, "last_name"));
            tvBirthdate.setText(optString(obj, "birthday"));
            tvNickname.setText(optString(obj, "nickname"));
            tvMobile.setText(firstNonEmpty(obj, "mobile_number", "mobile"));
            tvAddress.setText(optString(obj, "address"));
            tvGuardian.setText(optString(obj, "guardian_name"));
            tvGuardianMobile.setText(optString(obj, "guardian_mobile"));
            String baptized = firstNonEmpty(obj, "water_baptized", "baptized");
            tvBaptized.setText(baptized);
            tvTeacher.setText(optString(obj, "teacher"));
            tvSex.setText(optString(obj, "sex"));
            tvCaseworker.setText(firstNonEmpty(obj, "caseworker_assigned", "caseworker"));
            tvAge.setText(optString(obj, "age"));
        } catch (Exception ignored) {
            Toast.makeText(getContext(), "Parse error", Toast.LENGTH_SHORT).show();
        }
    }

    private String optString(JSONObject obj, String key) {
        String v = obj.optString(key, "");
        return v == null ? "" : v;
    }
    private String firstNonEmpty(JSONObject obj, String... keys) {
        for (String k : keys) {
            String v = obj.optString(k, "");
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return "";
    }
}
