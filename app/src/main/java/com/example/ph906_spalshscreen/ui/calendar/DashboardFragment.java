package com.example.ph906_spalshscreen.ui.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;
import com.example.ph906_spalshscreen.notifications.SyncWorker;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {
    private MaterialCalendarView calendarView;
    private LinearLayout upcomingEventsLayout;
    private TextView tvHeader;

    private final OkHttpClient http = new OkHttpClient();

    private final SimpleDateFormat fmtYMD  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat fmtHuman = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    // Cache: date -> events (only for the session)
    private final HashMap<String, List<Event>> cacheByDate = new HashMap<>();
    private List<Event> cacheUpcoming = null;

    // Decorator data for calendar dots
    private final Set<CalendarDay> daysWithEvents = new HashSet<>();
    private EventDayDecorator eventDecorator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        calendarView = v.findViewById(R.id.calendarView);
        // Enable dynamic height in code (replaces XML attribute)
        calendarView.setDynamicHeightEnabled(true);
        upcomingEventsLayout = v.findViewById(R.id.upcomingEvents);
        tvHeader = v.findViewById(R.id.tv_upcoming);

        // Trigger instant notification check
        SyncWorker.checkNow(requireContext());

        // Reload button
        Button btnReloadEvents = v.findViewById(R.id.btnReloadEvents);
        btnReloadEvents.setOnClickListener(view -> {
            // Trigger instant notification check on reload
            SyncWorker.checkNow(requireContext());
            reloadAllEvents();
        });

        // Decorator for event dots
        eventDecorator = new EventDayDecorator(daysWithEvents);
        calendarView.addDecorator(eventDecorator);

        // Initial load: show current and upcoming (no past)
        showUpcoming();

        // Load events for the visible month to decorate dates
        CalendarDay current = calendarView.getCurrentDate();
        loadMonthEvents(current.getYear(), current.getMonth());

        // When a day is tapped -> show events for that date
        calendarView.setOnDateChangedListener((@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) -> {
            String ymd = toYMD(date);
            showEventsForDate(ymd);
        });

        // When month is changed -> refresh decoration for that month
        calendarView.setOnMonthChangedListener((@NonNull MaterialCalendarView widget, @NonNull CalendarDay date) -> {
            loadMonthEvents(date.getYear(), date.getMonth());
        });

        return v;
    }

    // ===================== Month events + decoration =====================
    private void loadMonthEvents(int year, int month) {
        // month is 1..12 in CalendarDay
        String ym = String.format(Locale.US, "%04d-%02d", year, month);

        // Try multiple endpoint styles for robustness
        String base = "https://hjcdc.swuitapp.com/api/events.php";
        String[] urls = new String[] {
                base + "?month=" + ym + "&_ts=" + System.currentTimeMillis(),
                base + "?from=" + ym + "-01&to=" + ym + "-31&_ts=" + System.currentTimeMillis(),
                base + "?upcoming=1&_ts=" + System.currentTimeMillis()
        };
        fetchMonthEventsWithFallback(urls, 0, ym);
    }

    private void fetchMonthEventsWithFallback(String[] urls, int idx, String ym) {
        if (idx >= urls.length) return;
        Request req = new Request.Builder().url(urls[idx]).build();
        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> fetchMonthEventsWithFallback(urls, idx + 1, ym));
                }
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> fetchMonthEventsWithFallback(urls, idx + 1, ym));
                    }
                    return;
                }
                try {
                    String body = response.body() != null ? response.body().string() : "[]";
                    JSONArray arr = parseToArray(body);

                    // Build a new set of CalendarDays for this month
                    Set<CalendarDay> monthDays = new HashSet<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        String date = obj.optString("date", obj.optString("event_date", ""));
                        if (date == null || date.isEmpty()) continue;
                        if (!date.startsWith(ym)) continue; // keep only this month
                        CalendarDay cd = toCalendarDay(date);
                        if (cd != null) monthDays.add(cd);
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Replace content of the set and refresh decorators
                            daysWithEvents.clear();
                            daysWithEvents.addAll(monthDays);
                            calendarView.invalidateDecorators();
                        });
                    }
                } catch (Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> fetchMonthEventsWithFallback(urls, idx + 1, ym));
                    }
                }
            }
        });
    }

    // ===================== Upcoming and per-date list =====================
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
                    JSONArray arr = parseToArray(body);

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
                    JSONArray arr = parseToArray(body);

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

    private void reloadAllEvents() {
        // Clear all caches
        cacheByDate.clear();
        cacheUpcoming = null;
        daysWithEvents.clear();

        // Show loading message
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), "Reloading events...", Toast.LENGTH_SHORT).show()
            );
        }

        // Reload upcoming events
        showUpcoming();

        // Reload calendar decorations for current month
        CalendarDay current = calendarView.getCurrentDate();
        loadMonthEvents(current.getYear(), current.getMonth());
    }

    private void testEventNotification() {
        // Manually trigger an event notification for testing
        if (getActivity() != null) {
            android.app.PendingIntent pi = android.app.PendingIntent.getActivity(
                getContext(),
                1002,
                getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName()),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );
            com.example.ph906_spalshscreen.notifications.NotificationUtils.notify(
                getContext(),
                1002,
                "Test Event Notification",
                "This is a test notification. If you see this, notifications are working!",
                pi
            );
            Toast.makeText(getContext(), "Test notification sent!", Toast.LENGTH_SHORT).show();
        }
    }

    private String toHuman(String ymd) {
        try {
            return fmtHuman.format(fmtYMD.parse(ymd));
        } catch (ParseException e) {
            return ymd;
        }
    }

    private String toYMD(CalendarDay day) {
        return String.format(Locale.US, "%04d-%02d-%02d", day.getYear(), day.getMonth(), day.getDay());
    }

    @Nullable
    private CalendarDay toCalendarDay(String ymd) {
        try {
            String[] p = ymd.split("-");
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2]);
            return CalendarDay.from(y, m, d);
        } catch (Exception e) {
            return null;
        }
    }

    private JSONArray parseToArray(String body) throws JSONException {
        String trimmed = body.trim();
        if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }
        if (trimmed.startsWith("{")) {
            JSONObject obj = new JSONObject(trimmed);
            if (obj.has("data") && obj.get("data") instanceof JSONArray) {
                return obj.getJSONArray("data");
            }
        }
        // Fallback empty
        return new JSONArray();
    }

    static class Event {
        final String title;
        final String date;
        Event(String title, String date) {
            this.title = title == null ? "" : title;
            this.date = date == null ? "" : date;
        }
    }

    // Decorator draws a small dot under days that have events
    static class EventDayDecorator implements DayViewDecorator {
        private final Set<CalendarDay> dates;
        private final int color;
        EventDayDecorator(Set<CalendarDay> dates) {
            this.dates = dates;
            // Light blue dot; avoid resource references to keep it robust
            this.color = Color.parseColor("#38BDF8");
        }
        @Override public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }
        @Override public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, color)); // 8px radius dot
        }
    }
}
