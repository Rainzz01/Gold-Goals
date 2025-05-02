package com.example.gamification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gamification.TaskViewModel;
import com.example.gamification.model.Task;

public class ChallengeFragment extends Fragment {

    TextView challengeTitle, challengeProgress, challengeStatus;
    Button resetChallengeBtn;
    int requiredTasks = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_challenge, container, false);

        challengeTitle = view.findViewById(R.id.challengeTitle);
        challengeProgress = view.findViewById(R.id.challengeProgress);
        challengeStatus = view.findViewById(R.id.challengeStatus);
        resetChallengeBtn = view.findViewById(R.id.resetChallengeBtn);

        TaskViewModel taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getTasksCompletedToday().observe(getViewLifecycleOwner(), tasks -> {
            int progress = tasks.size();
            updateChallengeUI(progress);
        });

        resetChallengeBtn.setOnClickListener(v ->
                Toast.makeText(getContext(), "Simulated Reset (DB not cleared)", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void updateChallengeUI(int progress) {
        challengeProgress.setText("Progress: " + progress + "/" + requiredTasks);

        if (progress == requiredTasks) {
            challengeStatus.setText("Status: Done! 🎉");
            challengeStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (progress == requiredTasks - 1) {
            challengeStatus.setText("Status: One to go!");
            challengeStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            challengeStatus.setText("Status: In Progress");
            challengeStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }
}