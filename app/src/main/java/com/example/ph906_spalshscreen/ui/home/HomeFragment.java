package com.example.ph906_spalshscreen.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.DashboardFragment;
import com.example.ph906_spalshscreen.LettersFragment;
import com.example.ph906_spalshscreen.R;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private Button btnDashboard;
    private MaterialButton btnLetters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout you sent
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Bind buttons
        btnDashboard = root.findViewById(R.id.btn_dashboard);
        btnLetters = root.findViewById(R.id.btn_letters);

        // 👉 Dashboard navigation
        btnDashboard.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 👉 Letters navigation
        btnLetters.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LettersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return root;
    }
}
