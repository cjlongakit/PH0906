package com.example.ph906_spalshscreen;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.R;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private CalendarView calendarView;
    private LinearLayout upcomingEvents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // IMPORTANT: this must match the filename you created: res/layout/fragment_dashboard.xml
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // explicit findViewById with cast
        calendarView = (CalendarView) root.findViewById(R.id.calendarView);
        upcomingEvents = (LinearLayout) root.findViewById(R.id.upcomingEvents);

        // Sanity checks
        if (calendarView == null) {
            Log.e(TAG, "calendarView is null — check res/layout/fragment_dashboard.xml has a CalendarView with @+id/calendarView");
            throw new IllegalStateException("calendarView not found in fragment_dashboard.xml");
        }
        if (upcomingEvents == null) {
            Log.e(TAG, "upcomingEvents is null — check res/layout/fragment_dashboard.xml has a LinearLayout with @+id/upcomingEvents");
            throw new IllegalStateException("upcomingEvents not found in fragment_dashboard.xml");
        }

        // sample events (replace with real data later)
        addEvent("20 Aug 2025", "Events Opening - 10:00 AM");
        addEvent("20 Aug 2025", "Games - 1:30 PM");
        addEvent("30 Aug 2025", "Presentation - 11:00 AM");
        addEvent("30 Aug 2025", "Awarding - 1:30 PM");

        // example date selection handler
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            Log.d(TAG, "Selected date: " + date);
            // TODO: filter upcomingEvents by date
        });

        return root;
    }

    private void addEvent(String date, String details) {
        TextView eventView = new TextView(getContext());
        eventView.setText(date + " — " + details);
        eventView.setTextColor(Color.WHITE);
        eventView.setPadding(12, 12, 12, 12);
        upcomingEvents.addView(eventView);
    }
}
