package com.example.ph906_spalshscreen.ui.privacy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.PrivacyActivity;
import com.example.ph906_spalshscreen.R;

public class DataPrivacyMinorFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_privacy, container, false);

        Button btnAccept = root.findViewById(R.id.btnAcceptMinor);
        btnAccept.setOnClickListener(v -> {
            if (getActivity() instanceof PrivacyActivity) {
                ((PrivacyActivity) getActivity()).onAgreed();
            }
        });

        return root;
    }
}
