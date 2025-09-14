package com.example.ph906_spalshscreen.ui.profile;

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
    private Button btnEdit, btnSave, btnCancel;

    private boolean isEditing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

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

        // Set listeners
        btnEdit.setOnClickListener(v -> switchToEditMode());
        btnSave.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> cancelEditing());

        return root;
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
        // Example: copy values from EditText to TextView
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

        cancelEditing(); // switch back to view mode
    }

    private void toggleFields(boolean editable) {
        // Show EditTexts in edit mode, show TextViews in view mode
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
            et.setText(tv.getText().toString());
            tv.setVisibility(View.GONE);
            et.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.VISIBLE);
            et.setVisibility(View.GONE);
        }
    }
}
