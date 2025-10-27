package com.example.ph906_spalshscreen.ui.about;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Set version label if available
        TextView tvVersion = view.findViewById(R.id.tv_version);
        if (tvVersion != null && getContext() != null) {
            try {
                PackageManager pm = requireContext().getPackageManager();
                PackageInfo pInfo = pm.getPackageInfo(requireContext().getPackageName(), 0);
                String version = "v" + pInfo.versionName + " (" + pInfo.versionCode + ")";
                tvVersion.setText(version);
            } catch (Exception ignored) {
                tvVersion.setText("v1.0");
            }
        }
        return view;
    }
}
