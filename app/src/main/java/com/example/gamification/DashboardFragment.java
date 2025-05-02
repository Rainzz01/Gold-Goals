package com.example.gamification;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private LineChart lineChart;
    private TextView streakText;
    private CalendarView calendarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        lineChart = view.findViewById(R.id.lineChart);
        streakText = view.findViewById(R.id.streakText);
        calendarView = view.findViewById(R.id.calendarView);

        setupChart();
        setupStreak();
        setupCalendar();

        return view;
    }

    private void setupChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1f, 20f));
        entries.add(new Entry(2f, 40f));
        entries.add(new Entry(3f, 65f));
        entries.add(new Entry(4f, 100f));

        LineDataSet dataSet = new LineDataSet(entries, "XP Progress");
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh chart
    }

    private void setupStreak() {
        int calculatedStreak = 3; // Replace with dynamic logic later
        streakText.setText("Current Streak: " + calculatedStreak + " days");
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            Toast.makeText(requireContext(), "Tapped: " + date, Toast.LENGTH_SHORT).show();
        });
    }
}
