package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class EventsActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LinearLayout upcomingEvents;
    private ApiClient apiClient;
    private JSONArray eventsData = new JSONArray(); // cache for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dashboard); // uses dashboard.xml

        calendarView = findViewById(R.id.calendarView);
        upcomingEvents = findViewById(R.id.upcomingEvents);
        apiClient = new ApiClient(this);

        // 📌 Hint Toast for students
        Toast.makeText(this, "💡 Long press an event to save it to your Google Calendar", Toast.LENGTH_LONG).show();

        // Load events from API
        loadEvents();

        // Filter when date is changed
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showEventsForDate(selectedDate);
        });
    }

    private void loadEvents() {
        apiClient.getUpcomingEvents(new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        JSONArray arr = response.optJSONArray("data");
                        eventsData = (arr != null) ? arr : new JSONArray();
                        showAllEvents();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EventsActivity.this, "Error parsing events", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(EventsActivity.this, "Failed to load events: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void showAllEvents() {
        upcomingEvents.removeAllViews();
        if (eventsData.length() == 0) {
            showNoEvents("No upcoming events");
            return;
        }
        for (int i = 0; i < eventsData.length(); i++) {
            try {
                JSONObject event = eventsData.getJSONObject(i);
                addEventCard(event);
            } catch (Exception ignored) { }
        }
    }

    private void showEventsForDate(String date) {
        upcomingEvents.removeAllViews();
        boolean found = false;
        for (int i = 0; i < eventsData.length(); i++) {
            try {
                JSONObject event = eventsData.getJSONObject(i);
                if (date.equals(event.optString("event_date", ""))) {
                    addEventCard(event);
                    found = true;
                }
            } catch (Exception ignored) { }
        }
        if (!found) showNoEvents("No events for " + date);
    }

    private void showNoEvents(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        tv.setGravity(Gravity.CENTER);
        upcomingEvents.addView(tv);
    }

    private void addEventCard(JSONObject event) {
        try {
            String title = event.optString("event_title", "(no title)");
            String date = event.optString("event_date", "");

            TextView tv = new TextView(this);
            tv.setText("📅 " + date + "  —  " + title);
            tv.setTextSize(16f);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextColor(getResources().getColor(android.R.color.white));
            tv.setBackgroundResource(R.drawable.btn_gradient);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            tv.setLayoutParams(params);

            // Long press → add to Google Calendar
            tv.setOnLongClickListener(v -> {
                saveToGoogleCalendar(event);
                return true;
            });

            upcomingEvents.addView(tv);

        } catch (Exception ignored) { }
    }

    private void saveToGoogleCalendar(JSONObject event) {
        try {
            String title = event.optString("event_title", "PH906 Event");
            String date = event.optString("event_date", ""); // YYYY-MM-DD

            if (date.isEmpty()) {
                Toast.makeText(this, "Invalid event date", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(parts[2]);

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(year, month, day, 9, 0);

            Calendar endTime = Calendar.getInstance();
            endTime.set(year, month, day, 10, 0);

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, title)
                    .putExtra(CalendarContract.Events.DESCRIPTION, "PH906 School Event")
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "Campus")
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

            startActivity(intent);

        } catch (Exception ex) {
            Toast.makeText(this, "Failed to add event to calendar", Toast.LENGTH_SHORT).show();
        }
    }
}
