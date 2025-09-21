package com.example.ph906_spalshscreen.ui.privacy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.TermsActivity;

public class TermsAndAgreementsOlderFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.terms_and_agreements_older, container, false);

        Button btnAgree = root.findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(v -> {
            if (getActivity() instanceof TermsActivity) {
                ((TermsActivity) getActivity()).onAgreed();
            }
        });

        return root;
    }
}
