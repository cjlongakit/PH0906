package com.example.ph906_spalshscreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;

public class LettersFragment extends Fragment {

    private TableLayout tableLetters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_letters, container, false);

        tableLetters = root.findViewById(R.id.tableLetters);

        // 🔹 Later replace this with backend API call
        addRow("001", "Rodney Galanida", "General", "Outdated");
        addRow("002", "Rodney Galanida", "General", "Turned In");
        addRow("007", "Rodney Galanida", "General", "Pending");

        return root;
    }

    private void addRow(String code, String name, String type, String status) {
        TableRow row = new TableRow(getContext());

        TextView tvCode = new TextView(getContext());
        tvCode.setText(code);
        row.addView(tvCode);

        TextView tvName = new TextView(getContext());
        tvName.setText(name);
        row.addView(tvName);

        TextView tvType = new TextView(getContext());
        tvType.setText(type);
        row.addView(tvType);

        TextView tvStatus = new TextView(getContext());
        tvStatus.setText(status);

        // 🔹 Just plain text for now (since backend controls status)
        row.addView(tvStatus);

        tableLetters.addView(row);
    }
}
