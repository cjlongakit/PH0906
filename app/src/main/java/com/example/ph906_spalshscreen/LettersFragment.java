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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.ui.letters.Letter;
import com.example.ph906_spalshscreen.ui.letters.LettersAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LettersFragment extends Fragment {

    private EditText etSearch;
    private Button btnFilter;
    private RecyclerView recyclerView;
    private LettersAdapter adapter;
    private List<Letter> allLetters = new ArrayList<>();
    private List<Letter> filteredLetters = new ArrayList<>();
    private ApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_letters, container, false);

        initViews(root);
        setupRecyclerView();
        setupListeners();

        apiClient = new ApiClient(requireContext());
        loadRealDataFromWebsite();

        return root;
    }

    private void initViews(View root) {
        etSearch = root.findViewById(R.id.etSearch);
        btnFilter = root.findViewById(R.id.btnFilter);
        recyclerView = root.findViewById(R.id.recyclerViewLetters);
    }

    private void setupRecyclerView() {
        adapter = new LettersAdapter(filteredLetters);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLetters(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void loadRealDataFromWebsite() {
        apiClient.getMasterlist(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                allLetters.clear();
                try {
                    if (result.has("data")) {
                        parseStudentsArray(result.getJSONArray("data"));
                    } else {
                        parseSingleStudent(result);
                    }
                    updateUI();
                } catch (JSONException e) {
                    showError("Error parsing data: " + e.getMessage());
                }
            }

            @Override
            public void onError(String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    private void parseStudentsArray(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            allLetters.add(parseStudentObject(obj));
        }
    }

    private void parseSingleStudent(JSONObject obj) throws JSONException {
        allLetters.add(parseStudentObject(obj));
    }

    private Letter parseStudentObject(JSONObject obj) throws JSONException {
        return new Letter(
                obj.optString("ph906", ""),
                "",                                         // placeholder for the 2nd field your model expects
                obj.optString("full_name", ""),            // full_name (ApiClient now guarantees this)
                obj.optString("address", ""),
                obj.optString("type", ""),
                obj.optString("deadline", ""),             // keep deadline even if empty
                obj.optString("status", "")
        );
    }


    private void updateUI() {
        requireActivity().runOnUiThread(() -> {
            filteredLetters.clear();
            filteredLetters.addAll(allLetters);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(),
                    "Loaded " + allLetters.size() + " letters from database",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), "Failed to load letters: " + message,
                        Toast.LENGTH_LONG).show()
        );
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
                        letter.getStatus().toLowerCase().contains(lowerQuery) ||
                        letter.getType().toLowerCase().contains(lowerQuery)) {
                    filteredLetters.add(letter);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        Toast.makeText(getContext(), "Filter by status coming soon", Toast.LENGTH_SHORT).show();
    }
}
