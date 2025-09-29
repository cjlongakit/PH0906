package com.example.ph906_spalshscreen.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ph906_spalshscreen.PrefsHelper;
import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private TextView tvUserId, tvFirstName, tvLastName, tvBirthdate, tvNickname,
            tvMobile, tvAddress, tvGuardian, tvGuardianMobile, tvBaptized, tvTeacher,
            tvSex, tvCaseworker, tvAge;
    private Button btnEdit;
    private ImageView imgProfile;

    private String ph906Raw;
    private ApiClient apiClient;
    private PrefsHelper prefs;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isAdded()) return;
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String savedUrl = prefs.getProfilePhotoUri();
                    if (savedUrl != null && !savedUrl.isEmpty()) {
                        loadProfileImage(savedUrl, true); // cache-bust to show new image immediately
                    }
                    loadProfile(); // refresh profile fields too
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

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

        apiClient = new ApiClient(requireContext());
        prefs = new PrefsHelper(requireContext());

        ph906Raw = prefs.getPh906();
        set(tvUserId, formatPh906(ph906Raw));

        String savedUrl = prefs.getProfilePhotoUri();
        if (savedUrl != null && !savedUrl.isEmpty()) {
            loadProfileImage(savedUrl, false);
        } else {
            Glide.with(this).load(R.drawable.account_circle).into(imgProfile);
        }

        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            intent.putExtra("ph906Id", ph906Raw);
            editLauncher.launch(intent);
        });

        loadProfile();
        return v;
    }

    private String formatPh906(String raw) {
        if (raw == null || raw.isEmpty()) return "PH906-";
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() >= 1) return "PH906-" + digits;
        return raw.startsWith("PH906-") ? raw : ("PH906-" + raw);
    }

    private void loadProfile() {
        apiClient.getMyProfile(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> handleProfileResponse(response));
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Load failed: " + message, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void handleProfileResponse(JSONObject response) {
        try {
            JSONObject obj = response;
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                obj = response.getJSONObject("data");
            }
            // ALWAYS coerce to String; never pass numbers directly to setText(int)
            set(tvFirstName, obj.optString("first_name", ""));
            set(tvLastName,  obj.optString("last_name", ""));
            set(tvBirthdate, obj.optString("birthday", ""));
            set(tvNickname,  obj.optString("nickname", ""));

            String mobile = firstNonEmpty(obj.optString("mobile_number", ""), obj.optString("mobile", ""));
            set(tvMobile, mobile);

            set(tvAddress,  obj.optString("address", ""));
            set(tvGuardian, obj.optString("guardian_name", ""));
            set(tvGuardianMobile, obj.optString("guardian_mobile", ""));
            set(tvBaptized, firstNonEmpty(obj.optString("water_baptized", ""), obj.optString("baptized", "")));
            set(tvTeacher,  obj.optString("teacher", ""));
            set(tvSex,      obj.optString("sex", ""));
            // age can be int or string; convert safely
            String ageStr = obj.has("age") ? String.valueOf(obj.opt("age")) : "";
            set(tvAge, "null".equalsIgnoreCase(ageStr) ? "" : ageStr);
            set(tvCaseworker, obj.optString("caseworker_assigned", ""));

            // If backend includes photo_url, prefer it and save
            String apiPhotoUrl = obj.optString("photo_url", "").trim();
            if (!apiPhotoUrl.isEmpty()) {
                prefs.saveProfilePhotoUri(apiPhotoUrl);
                loadProfileImage(apiPhotoUrl, true); // cache-bust once
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) return a;
        return (b == null) ? "" : b;
    }

    private void loadProfileImage(String url, boolean bustCache) {
        if (!isAdded()) return;
        String toLoad = url;
        if (bustCache) {
            String sep = url.contains("?") ? "&" : "?";
            toLoad = url + sep + "t=" + System.currentTimeMillis();
        }
        // Clear any pending request to avoid flicker/race
        try { Glide.with(this).clear(imgProfile); } catch (Exception ignored) {}

        try {
            Glide.with(this)
                    .load(toLoad)
                    .placeholder(R.drawable.account_circle)
                    .error(R.drawable.account_circle)
                    .centerCrop()
                    .diskCacheStrategy(bustCache ? DiskCacheStrategy.NONE : DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(bustCache)
                    .into(imgProfile);
        } catch (IllegalStateException ignored) {
            // Fragment might be in transition; skip
        }
    }

    // Safe setter: coerce any value to string and coalesce nulls to ""
    private void set(TextView v, Object val) {
        if (v == null) return;
        String s;
        if (val == null) {
            s = "";
        } else {
            s = String.valueOf(val);
            if ("null".equalsIgnoreCase(s)) s = "";
        }
        v.setText(s);
    }
}