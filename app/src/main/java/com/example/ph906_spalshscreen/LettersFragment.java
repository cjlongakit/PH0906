package com.example.ph906_spalshscreen;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.ui.letters.Letter;
import com.example.ph906_spalshscreen.ui.letters.LettersAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LettersFragment extends Fragment {

    private EditText etSearch;
    private Button btnFilter;
    private RecyclerView recyclerViewLetters;
    private LettersAdapter adapter;
    private ApiClient apiClient;

    private List<Letter> allLetters = new ArrayList<>();
    private List<Letter> filteredLetters = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_letters, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        apiClient = new ApiClient(getContext());
        loadLettersFromDatabase();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        recyclerViewLetters = view.findViewById(R.id.recyclerViewLetters);
    }

    private void setupRecyclerView() {
        adapter = new LettersAdapter(filteredLetters);
        recyclerViewLetters.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewLetters.setAdapter(adapter);
    }

    private void setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLetters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void loadLettersFromDatabase() {
        // Call your API to get students data
        // This will fetch from your 'students' table which contains letters/documents for students

        // Create a custom API call for getting letters/students data
        apiClient.getStudentsData(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            parseLettersData(response);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading letters: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void parseLettersData(JSONObject response) throws Exception {
        allLetters.clear();

        if (response.has("data")) {
            JSONArray dataArray = response.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject studentObj = dataArray.getJSONObject(i);

                // Parse data from your 'students' table
                String ph906 = studentObj.optString("ph906");
                String name = studentObj.optString("name");
                String address = studentObj.optString("address");
                String type = studentObj.optString("type");
                String status = studentObj.optString("status");

                // Create Letter object from database data
                Letter letter = new Letter(ph906, "", name, address, type, status);
                allLetters.add(letter);
            }
        }

        filteredLetters.clear();
        filteredLetters.addAll(allLetters);
        adapter.notifyDataSetChanged();
    }

    private void filterLetters(String query) {
        filteredLetters.clear();

        if (query.isEmpty()) {
            filteredLetters.addAll(allLetters);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Letter letter : allLetters) {
                if (letter.getPh906().toLowerCase().contains(lowerQuery) ||
                        letter.getFullName().toLowerCase().contains(lowerQuery) ||
                        letter.getStatus().toLowerCase().contains(lowerQuery)) {
                    filteredLetters.add(letter);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        Toast.makeText(getContext(), "Filter options coming soon", Toast.LENGTH_SHORT).show();
    }
}