package com.example.ph906_spalshscreen.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;

public class ProfileFragment extends Fragment {

    private TextView tvFirstName, tvLastName, tvBirthdate, tvNickname, tvMobile,
            tvAddress, tvGuardian, tvGuardianMobile, tvBaptized, tvTeacher;

    private EditText etFirstName, etLastName, etBirthdate, etNickname, etMobile,
            etAddress, etGuardian, etGuardianMobile, etBaptized, etTeacher;

    private Button btnEdit, btnSave;

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // 🔹 Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("ProfileData", Context.MODE_PRIVATE);

        // 🔹 Find TextViews
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

        // 🔹 Find EditTexts
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

        // 🔹 Buttons
        btnEdit = root.findViewById(R.id.btnEdit);
        btnSave = root.findViewById(R.id.btnSave);

        // 🔹 Load saved data
        loadProfile();

        // 🔹 Edit button logic
        btnEdit.setOnClickListener(v -> {
            toggleEditMode(true);
        });

        // 🔹 Save button logic
        btnSave.setOnClickListener(v -> {
            saveProfile();
            toggleEditMode(false);
        });

        return root;
    }

    private void loadProfile() {
        tvFirstName.setText("First name: " + sharedPreferences.getString("first_name", "Rodney Divi"));
        tvLastName.setText("Last Name: " + sharedPreferences.getString("last_name", "Galanida"));
        tvBirthdate.setText("Birthdate: " + sharedPreferences.getString("birthdate", "June 30, 1997"));
        tvNickname.setText("Nickname: " + sharedPreferences.getString("nickname", "Divi"));
        tvMobile.setText("Mobile #: " + sharedPreferences.getString("mobile", "09123456789"));
        tvAddress.setText("Current Address: " + sharedPreferences.getString("address", "Cebu City"));
        tvGuardian.setText("Guardian: " + sharedPreferences.getString("guardian", "Maria Galanida"));
        tvGuardianMobile.setText("Guardian Mobile#: " + sharedPreferences.getString("guardian_mobile", "09987654321"));
        tvBaptized.setText("Water baptized: " + sharedPreferences.getString("baptized", "No"));
        tvTeacher.setText("Teacher: " + sharedPreferences.getString("teacher", "Mr. Santos"));
    }

    private void saveProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("first_name", etFirstName.getText().toString());
        editor.putString("last_name", etLastName.getText().toString());
        editor.putString("birthdate", etBirthdate.getText().toString());
        editor.putString("nickname", etNickname.getText().toString());
        editor.putString("mobile", etMobile.getText().toString());
        editor.putString("address", etAddress.getText().toString());
        editor.putString("guardian", etGuardian.getText().toString());
        editor.putString("guardian_mobile", etGuardianMobile.getText().toString());
        editor.putString("baptized", etBaptized.getText().toString());
        editor.putString("teacher", etTeacher.getText().toString());

        editor.apply();

        loadProfile();
    }

    private void toggleEditMode(boolean isEditing) {
        // Toggle visibility of TextViews and EditTexts
        tvFirstName.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvLastName.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvBirthdate.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvNickname.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvMobile.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvAddress.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvGuardian.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvGuardianMobile.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvBaptized.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        tvTeacher.setVisibility(isEditing ? View.GONE : View.VISIBLE);

        etFirstName.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etLastName.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etBirthdate.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etNickname.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etMobile.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etAddress.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etGuardian.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etGuardianMobile.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etBaptized.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        etTeacher.setVisibility(isEditing ? View.VISIBLE : View.GONE);

        // Fill EditTexts with current values when editing
        if (isEditing) {
            etFirstName.setText(sharedPreferences.getString("first_name", ""));
            etLastName.setText(sharedPreferences.getString("last_name", ""));
            etBirthdate.setText(sharedPreferences.getString("birthdate", ""));
            etNickname.setText(sharedPreferences.getString("nickname", ""));
            etMobile.setText(sharedPreferences.getString("mobile", ""));
            etAddress.setText(sharedPreferences.getString("address", ""));
            etGuardian.setText(sharedPreferences.getString("guardian", ""));
            etGuardianMobile.setText(sharedPreferences.getString("guardian_mobile", ""));
            etBaptized.setText(sharedPreferences.getString("baptized", ""));
            etTeacher.setText(sharedPreferences.getString("teacher", ""));
        }

        btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(isEditing ? View.VISIBLE : View.GONE);
    }
}
