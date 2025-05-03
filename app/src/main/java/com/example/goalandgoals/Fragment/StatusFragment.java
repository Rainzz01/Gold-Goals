package com.example.goalandgoals.Fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.goalandgoals.Adapter.StatsAdapter;
import com.example.goalandgoals.Model.StatItem;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Utils.AppDatabase;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatusFragment extends Fragment {

    private RecyclerView statsRecyclerView;
    private PieChart taskCompletionChart;
    private PieChart punctualityChart;
    private PieChart personalityChart;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        db = AppDatabase.getInstance(requireContext());
        statsRecyclerView = view.findViewById(R.id.stats_recycler_view);
        taskCompletionChart = view.findViewById(R.id.task_completion_chart);
        punctualityChart = view.findViewById(R.id.punctuality_chart);
        personalityChart = view.findViewById(R.id.personality_chart);

        setupRecyclerView();
        loadDashboardData();

        return view;
    }

    private void setupRecyclerView() {
        statsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadDashboardData() {
        AsyncTask.execute(() -> {
            List<ToDoModel> tasks = db.toDoDao().getAllTasks();
            List<StatItem> statsList = new ArrayList<>();
            float completedPercentage = calculateCompletedPercentage(tasks);
            String punctualityRate = calculatePunctualityRate(tasks);
            String personalityTrait = inferPersonalityTrait(tasks);

            statsList.add(new StatItem("Total Tasks", String.valueOf(tasks.size())));
            statsList.add(new StatItem("Completed Tasks", String.valueOf(getCompletedTasksCount(tasks))));
            statsList.add(new StatItem("Completion Rate", String.format(Locale.getDefault(), "%.1f%%", completedPercentage)));
            statsList.add(new StatItem("Punctuality Rate", punctualityRate));
            statsList.add(new StatItem("Dominant Trait", personalityTrait));

            requireActivity().runOnUiThread(() -> {
                statsRecyclerView.setAdapter(new StatsAdapter(statsList));
                setupTaskCompletionChart(tasks);
                setupPunctualityChart(tasks);
                setupPersonalityChart(tasks);
            });
        });
    }

    private int getCompletedTasksCount(List<ToDoModel> tasks) {
        int completed = 0;
        for (ToDoModel task : tasks) {
            if (task.getStatus() == 1) {
                completed++;
            }
        }
        return completed;
    }

    private float calculateCompletedPercentage(List<ToDoModel> tasks) {
        if (tasks.isEmpty()) return 0f;
        int completed = getCompletedTasksCount(tasks);
        return (completed * 100f) / tasks.size();
    }

    private String calculatePunctualityRate(List<ToDoModel> tasks) {
        if (tasks.isEmpty()) return "N/A";
        int onTime = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (ToDoModel task : tasks) {
            if (task.getStatus() == 1 && task.getDeadline() != null) {
                try {
                    Date deadline = sdf.parse(task.getDeadline());
                    if (deadline != null && deadline.after(new Date())) {
                        onTime++;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        int completed = getCompletedTasksCount(tasks);
        if (completed == 0) return "N/A";
        float punctuality = (onTime * 100f) / completed;
        return String.format(Locale.getDefault(), "%.1f%%", punctuality);
    }

    private String inferPersonalityTrait(List<ToDoModel> tasks) {
        if (tasks.isEmpty()) return "Unknown";
        int easy = 0, medium = 0, hard = 0, extreme = 0;
        for (ToDoModel task : tasks) {
            switch (task.getDifficulty() != null ? task.getDifficulty() : "") {
                case "Easy":
                    easy++;
                    break;
                case "Medium":
                    medium++;
                    break;
                case "Hard":
                    hard++;
                    break;
                case "Extreme":
                    extreme++;
                    break;
            }
        }
        int max = Math.max(Math.max(easy, medium), Math.max(hard, extreme));
        if (max == 0) return "Balanced";
        if (max == easy) return "Cautious";
        if (max == medium) return "Balanced";
        if (max == hard) return "Challenger";
        if (max == extreme) return "Fearless";
        return "Unknown";
    }

    private void setupTaskCompletionChart(List<ToDoModel> tasks) {
        List<PieEntry> entries = new ArrayList<>();
        int completed = getCompletedTasksCount(tasks);
        int incomplete = tasks.size() - completed;

        if (completed > 0) {
            entries.add(new PieEntry(completed, "Completed"));
        }
        if (incomplete > 0) {
            entries.add(new PieEntry(incomplete, "Incomplete"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Task Completion");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(taskCompletionChart));

        PieData data = new PieData(dataSet);
        taskCompletionChart.setData(data);
        taskCompletionChart.setUsePercentValues(true);
        taskCompletionChart.getDescription().setEnabled(false);
        taskCompletionChart.setCenterText("Tasks");
        taskCompletionChart.animateY(1000);
        taskCompletionChart.invalidate();
    }

    private void setupPunctualityChart(List<ToDoModel> tasks) {
        List<PieEntry> entries = new ArrayList<>();
        int onTime = 0, late = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (ToDoModel task : tasks) {
            if (task.getStatus() == 1 && task.getDeadline() != null) {
                try {
                    Date deadline = sdf.parse(task.getDeadline());
                    if (deadline != null) {
                        if (deadline.after(new Date())) {
                            onTime++;
                        } else {
                            late++;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (onTime > 0) {
            entries.add(new PieEntry(onTime, "On Time"));
        }
        if (late > 0) {
            entries.add(new PieEntry(late, "Late"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Punctuality");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(punctualityChart));

        PieData data = new PieData(dataSet);
        punctualityChart.setData(data);
        punctualityChart.setUsePercentValues(true);
        punctualityChart.getDescription().setEnabled(false);
        punctualityChart.setCenterText("Punctuality");
        punctualityChart.animateY(1000);
        punctualityChart.invalidate();
    }

    private void setupPersonalityChart(List<ToDoModel> tasks) {
        List<PieEntry> entries = new ArrayList<>();
        int easy = 0, medium = 0, hard = 0, extreme = 0;
        for (ToDoModel task : tasks) {
            switch (task.getDifficulty() != null ? task.getDifficulty() : "") {
                case "Easy":
                    easy++;
                    break;
                case "Medium":
                    medium++;
                    break;
                case "Hard":
                    hard++;
                    break;
                case "Extreme":
                    extreme++;
                    break;
            }
        }

        if (easy > 0) entries.add(new PieEntry(easy, "Cautious"));
        if (medium > 0) entries.add(new PieEntry(medium, "Balanced"));
        if (hard > 0) entries.add(new PieEntry(hard, "Challenger"));
        if (extreme > 0) entries.add(new PieEntry(extreme, "Fearless"));

        PieDataSet dataSet = new PieDataSet(entries, "Personality Traits");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(personalityChart));

        PieData data = new PieData(dataSet);
        personalityChart.setData(data);
        personalityChart.setUsePercentValues(true);
        personalityChart.getDescription().setEnabled(false);
        personalityChart.setCenterText("Traits");
        personalityChart.animateY(1000);
        personalityChart.invalidate();
    }
}