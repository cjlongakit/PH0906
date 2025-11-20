package com.example.ph906_spalshscreen.ui.letters;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LettersFragment extends Fragment {

    private EditText etSearch;
    private Button btnAll, btnPending, btnOnHand, btnTurnedIn, btnTurnInLate, btnReload;
    private RecyclerView recyclerView;
    private LettersAdapter adapter;
    private List<Letter> allLetters = new ArrayList<>();
    private List<Letter> filteredLetters = new ArrayList<>();
    private ApiClient apiClient;
    private static final String TAG = "LETTERS_DEBUG";

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

        btnReload = root.findViewById(R.id.btnReload);
        btnReload.setOnClickListener(v -> loadRealDataFromWebsite());

        return root;
    }

    private void initViews(View root) {
        etSearch = root.findViewById(R.id.etSearch);
        recyclerView = root.findViewById(R.id.recyclerViewLetters);
        btnAll = root.findViewById(R.id.btnAll);
        btnPending = root.findViewById(R.id.btnPending);
        btnOnHand = root.findViewById(R.id.btnOnHand);
        btnTurnedIn = root.findViewById(R.id.btnTurnedIn);
        btnTurnInLate = root.findViewById(R.id.btnTurnInLate);
        btnReload = root.findViewById(R.id.btnReload);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LettersAdapter(filteredLetters);
        recyclerView.setAdapter(adapter);
    }

    private void loadRealDataFromWebsite() {
        apiClient.getMasterlist(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Log.d(TAG, "API call succeeded: " + result.toString());
                    allLetters.clear();
                    try {
                        if (result.has("data")) {
                            parseStudentsArray(result.getJSONArray("data"));
                        } else {
                            parseSingleStudent(result);
                        }
                        Log.d(TAG, "Loaded " + allLetters.size() + " letters");
                        Toast.makeText(getContext(), "Loaded " + allLetters.size() + " letters", Toast.LENGTH_LONG).show();
                        updateUI();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing data: " + e.getMessage());
                        showError("Error parsing data: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "API call failed: " + errorMessage);
                    Toast.makeText(getContext(), "API error: " + errorMessage, Toast.LENGTH_LONG).show();
                    showError(errorMessage);
                });
            }
        });
    }

    private void parseStudentsArray(JSONArray array) throws JSONException {
        Log.d(TAG, "Parsing students array of length: " + array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Log.d(TAG, "Student object: " + obj.toString());
            allLetters.add(parseStudentObject(obj));
        }
    }

    private void parseSingleStudent(JSONObject obj) throws JSONException {
        allLetters.add(parseStudentObject(obj));
    }

    private Letter parseStudentObject(JSONObject obj) throws JSONException {
        return new Letter(
                obj.optString("ph906", ""),
                obj.optString("full_name", ""),   // now matches the model
                obj.optString("address", ""),
                obj.optString("type", ""),
                obj.optString("deadline", ""),
                obj.optString("status", "")
        );
    }

    private void updateUI() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            filteredLetters.clear();
            filteredLetters.addAll(allLetters);
            adapter.notifyDataSetChanged();
        });
    }

    private void showError(String message) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), "Failed to load letters: " + message,
                        Toast.LENGTH_LONG).show()
        );
    }

    private void filterLetters(String query) {
        filteredLetters.clear();

        if (query == null || query.isEmpty()) {
            filteredLetters.addAll(allLetters);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Letter letter : allLetters) {
                if (letter.getPh906().toLowerCase().contains(lowerQuery) ||
                        letter.getFullName().toLowerCase().contains(lowerQuery) ||
                        normalizeStatus(letter.getStatus()).contains(lowerQuery) ||
                        letter.getType().toLowerCase().contains(lowerQuery)) {
                    filteredLetters.add(letter);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterByStatus(String status) {
        filteredLetters.clear();
        if (status.equals("ALL")) {
            filteredLetters.addAll(allLetters);
        } else {
            String want = normalizeStatus(status);
            for (Letter letter : allLetters) {
                if (normalizeStatus(letter.getStatus()).equals(want)) {
                    filteredLetters.add(letter);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String normalizeStatus(String s) {
        if (s == null) return "";
        String x = s.trim().toUpperCase();
        x = x.replace('_', ' ').replace('-', ' ');
        x = x.replaceAll("\\s+", " ");
        if ("TURN IN".equals(x)) x = "TURNED IN";
        return x;
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLetters(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        btnAll.setOnClickListener(v -> filterByStatus("ALL"));
        btnPending.setOnClickListener(v -> filterByStatus("PENDING"));
        btnOnHand.setOnClickListener(v -> filterByStatus("ON HAND"));
        btnTurnedIn.setOnClickListener(v -> filterByStatus("TURNED IN"));
        btnTurnInLate.setOnClickListener(v -> filterByStatus("TURN IN LATE"));
    }
}