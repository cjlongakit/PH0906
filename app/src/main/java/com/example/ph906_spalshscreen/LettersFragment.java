package com.example.ph906_spalshscreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private RecyclerView recyclerView;
    private LettersAdapter adapter;
    private List<Letter> letterList;
    private ApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_letters, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewLetters);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        letterList = new ArrayList<>();
        adapter = new LettersAdapter(letterList);
        recyclerView.setAdapter(adapter);

        apiClient = new ApiClient(requireContext());

        loadLettersFromDatabase();

        return root;
    }

    private void loadLettersFromDatabase() {
        apiClient.getMasterlist(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                letterList.clear();

                try {
                    if (result.has("data")) {
                        JSONArray array = result.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            String ph906 = obj.optString("ph906", "");
                            String name = obj.optString("name", "");
                            String address = obj.optString("address", "");
                            String type = obj.optString("type", "N/A");
                            String deadline = obj.optString("deadline", "N/A");
                            String status = obj.optString("status", "N/A");

                            Letter letter = new Letter(ph906, "", name, address, type, deadline, status);
                            letterList.add(letter);
                        }
                    }

                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (JSONException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}