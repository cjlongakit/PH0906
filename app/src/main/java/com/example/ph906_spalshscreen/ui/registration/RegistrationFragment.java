package com.example.ph906_spalshscreen.ui.registration;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;

public class RegistrationFragment extends Fragment {

    private EditText etNickname, etPlace, etTeacher, etMobile, etAltMobile;
    private CheckBox cbAgree;
    private Button btnContinue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout (connect XML to this Fragment)
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        // Initialize UI components
        etNickname = view.findViewById(R.id.etNickname);
        etPlace = view.findViewById(R.id.etPlace);
        etTeacher = view.findViewById(R.id.etTeacher);
        etMobile = view.findViewById(R.id.etMobile);
        etAltMobile = view.findViewById(R.id.etAltMobile);
        cbAgree = view.findViewById(R.id.cbAgree);
        btnContinue = view.findViewById(R.id.btnContinue);

        // Button click
        btnContinue.setOnClickListener(v -> validateInputs());

        return view;
    }

    private void validateInputs() {
        String nickname = etNickname.getText().toString().trim();
        String place = etPlace.getText().toString().trim();
        String teacher = etTeacher.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String altMobile = etAltMobile.getText().toString().trim();

        if (TextUtils.isEmpty(nickname) ||
                TextUtils.isEmpty(place) ||
                TextUtils.isEmpty(teacher) ||
                TextUtils.isEmpty(mobile) ||
                TextUtils.isEmpty(altMobile)) {

            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAgree.isChecked()) {
            Toast.makeText(getContext(), "You must agree to the Terms and Privacy", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all are filled and checkbox is checked
        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
    }
}
