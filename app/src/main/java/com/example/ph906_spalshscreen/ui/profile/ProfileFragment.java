package com.example.ph906_spalshscreen.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {
    private TextView tvFirstName, tvLastName, tvBirthdate, tvNickname, tvMobile,
            tvAddress, tvGuardian, tvGuardianMobile, tvBaptized, tvTeacher;
    private EditText etFirstName, etLastName, etBirthdate, etNickname, etMobile,
            etAddress, etGuardian, etGuardianMobile, etBaptized, etTeacher;
    private Button btnEdit, btnSave, btnCancel;

    private boolean isEditing = false;
    private ApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        apiClient = new ApiClient(requireContext());

        // Bind TextViews
        tvFirstName = root.findViewById(R.id.tv_first_name);
        tvLastName = root.findViewById(R.id.tv_last_name);
        tvBirthdate = root.findViewById(R.id.tv_birthdate);
        tvNickname = root.findViewById(R.id.tv_nickname);
        tvMobile = root.findViewById(R.id.tv_mobile);
        tvAddress = root.findViewById(R.id.tv_address);
        tvGuardian = root.findViewById(R.id.tv_guardian);
        tvGuardianMobile = root.findViewById(R.id.tv_guardian_mobile);
        tvBaptized = root.findViewById(R.id.tv_baptized);
        tvTeacher = root.findViewById(R.id.tv_teacher);

        // Bind EditTexts
        etFirstName = root.findViewById(R.id.et_first_name);
        etLastName = root.findViewById(R.id.et_last_name);
        etBirthdate = root.findViewById(R.id.et_birthdate);
        etNickname = root.findViewById(R.id.et_nickname);
        etMobile = root.findViewById(R.id.et_mobile);
        etAddress = root.findViewById(R.id.et_address);
        etGuardian = root.findViewById(R.id.et_guardian);
        etGuardianMobile = root.findViewById(R.id.et_guardian_mobile);
        etBaptized = root.findViewById(R.id.et_baptized);
        etTeacher = root.findViewById(R.id.et_teacher);

        // Bind Buttons
        btnEdit = root.findViewById(R.id.btnEdit);
        btnSave = root.findViewById(R.id.btnSave);
        btnCancel = root.findViewById(R.id.btnCancel);

        btnEdit.setOnClickListener(v -> switchToEditMode());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> cancelEditing());

        // Fetch profile when fragment loads
        loadProfile();

        return root;
    }

    private void loadProfile() {
        apiClient.getMyProfile(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (response.optBoolean("success")) {
                        JSONObject data = response.optJSONObject("data");
                        if (data != null) {
                            tvFirstName.setText(data.optString("first_name", ""));
                            tvLastName.setText(data.optString("last_name", ""));
                            tvBirthdate.setText(data.optString("birthday", ""));
                            tvNickname.setText(data.optString("nickname", ""));
                            tvMobile.setText(data.optString("mobile", ""));
                            tvAddress.setText(data.optString("address", ""));
                            tvGuardian.setText(data.optString("guardian_name", ""));
                            tvGuardianMobile.setText(data.optString("guardian_mobile", ""));
                            tvBaptized.setText("Water Baptized: " + data.optString("baptized", "No"));
                            tvTeacher.setText(data.optString("teacher", ""));
                        }
                    } else {
                        Toast.makeText(getActivity(), response.optString("message", "Failed to load profile"), Toast.LENGTH_SHORT).show();
                    }

                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Profile load failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void switchToEditMode() {
        isEditing = true;
        btnEdit.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        toggleFields(true);
    }

    private void cancelEditing() {
        isEditing = false;
        btnEdit.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        toggleFields(false);
    }

    private void saveChanges() {
        JSONObject payload = new JSONObject();
        try {
            payload.put("first_name", etFirstName.getText().toString());
            payload.put("last_name", etLastName.getText().toString());
            payload.put("birthday", etBirthdate.getText().toString());
            payload.put("nickname", etNickname.getText().toString());
            payload.put("mobile", etMobile.getText().toString());
            payload.put("address", etAddress.getText().toString());
            payload.put("guardian_name", etGuardian.getText().toString());
            payload.put("guardian_mobile", etGuardianMobile.getText().toString());
            payload.put("baptized", etBaptized.getText().toString());
            payload.put("teacher", etTeacher.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiClient.updateMyProfile(payload, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    saveToTextViews();
                    cancelEditing();
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Update failed: " + errorMessage, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void saveToTextViews() {
        tvFirstName.setText(etFirstName.getText().toString());
        tvLastName.setText(etLastName.getText().toString());
        tvBirthdate.setText(etBirthdate.getText().toString());
        tvNickname.setText(etNickname.getText().toString());
        tvMobile.setText(etMobile.getText().toString());
        tvAddress.setText(etAddress.getText().toString());
        tvGuardian.setText(etGuardian.getText().toString());
        tvGuardianMobile.setText(etGuardianMobile.getText().toString());
        tvBaptized.setText("Water Baptized: " + etBaptized.getText().toString());
        tvTeacher.setText(etTeacher.getText().toString());
    }

    private void toggleFields(boolean editable) {
        toggle(tvFirstName, etFirstName, editable);
        toggle(tvLastName, etLastName, editable);
        toggle(tvBirthdate, etBirthdate, editable);
        toggle(tvNickname, etNickname, editable);
        toggle(tvMobile, etMobile, editable);
        toggle(tvAddress, etAddress, editable);
        toggle(tvGuardian, etGuardian, editable);
        toggle(tvGuardianMobile, etGuardianMobile, editable);
        toggle(tvBaptized, etBaptized, editable);
        toggle(tvTeacher, etTeacher, editable);
    }

    private void toggle(TextView tv, EditText et, boolean editable) {
        if (editable) {
            et.setText(tv.getText().toString().replace("Water Baptized: ", ""));
            tv.setVisibility(View.GONE);
            et.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.VISIBLE);
            et.setVisibility(View.GONE);
        }
    }
}
