package com.example.ph906_spalshscreen.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {
    private CalendarView calendarView;
    private LinearLayout upcomingEventsLayout;
    private TextView tvHeader;

    private final OkHttpClient http = new OkHttpClient();

    private final SimpleDateFormat fmtYMD  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat fmtHuman = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    // Cache: date -> events (only for the session)
    private final HashMap<String, List<Event>> cacheByDate = new HashMap<>();
    private List<Event> cacheUpcoming = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        calendarView = v.findViewById(R.id.calendarView);
        upcomingEventsLayout = v.findViewById(R.id.upcomingEvents);
        tvHeader = v.findViewById(R.id.tv_upcoming);

        // Initial load: show current and upcoming (no past)
        showUpcoming();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showEventsForDate(date);
        });

        return v;
    }

    private void showUpcoming() {
        tvHeader.setText("UPCOMING EVENTS");
        // Use cache if available
        if (cacheUpcoming != null) {
            renderList(cacheUpcoming, /*prefixDate*/true);
            return;
        }

        String url = "https://hjcdc.swuitapp.com/api/events.php?upcoming=1&_ts=" + System.currentTimeMillis();
        Request req = new Request.Builder().url(url).build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to load upcoming events", Toast.LENGTH_SHORT).show());
                }
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String body = response.body() != null ? response.body().string() : "[]";
                    JSONArray arr = new JSONArray(body);

                    List<Event> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String date = obj.optString("date", obj.optString("event_date", ""));
                        String title = obj.optString("title", obj.optString("event_title", ""));
                        if (date == null || date.isEmpty()) continue;
                        list.add(new Event(title, date));
                    }
                    cacheUpcoming = list;

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> renderList(list, /*prefixDate*/true));
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    private void showEventsForDate(String ymd) {
        // Change the header to reflect the selected date (can be past)
        tvHeader.setText("EVENTS ON " + toHuman(ymd).toUpperCase(Locale.US));

        // Use cache if present
        if (cacheByDate.containsKey(ymd)) {
            renderList(cacheByDate.get(ymd), /*prefixDate*/false);
            return;
        }

        String url = "https://hjcdc.swuitapp.com/api/events.php?date=" + ymd + "&_ts=" + System.currentTimeMillis();
        Request req = new Request.Builder().url(url).build();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to load events for " + toHuman(ymd), Toast.LENGTH_SHORT).show());
                }
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String body = response.body() != null ? response.body().string() : "[]";
                    JSONArray arr = new JSONArray(body);

                    List<Event> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String date = obj.optString("date", obj.optString("event_date", ymd));
                        String title = obj.optString("title", obj.optString("event_title", ""));
                        list.add(new Event(title, date));
                    }
                    cacheByDate.put(ymd, list);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> renderList(list, /*prefixDate*/false));
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    private void renderList(@Nullable List<Event> list, boolean prefixDate) {
        upcomingEventsLayout.removeAllViews();
        if (list == null || list.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("No events to display.");
            tv.setPadding(0, 12, 0, 12);
            upcomingEventsLayout.addView(tv);
            return;
        }
        for (Event e : list) {
            TextView tv = new TextView(getContext());
            String line = prefixDate ? (toHuman(e.date) + " — " + e.title) : e.title;
            tv.setText(line);
            tv.setPadding(0, 8, 0, 16);
            upcomingEventsLayout.addView(tv);
        }
    }

    private String toHuman(String ymd) {
        try {
            return fmtHuman.format(fmtYMD.parse(ymd));
        } catch (ParseException e) {
            return ymd;
        }
    }

    static class Event {
        final String title;
        final String date;
        Event(String title, String date) {
            this.title = title == null ? "" : title;
            this.date = date == null ? "" : date;
        }
    }
}