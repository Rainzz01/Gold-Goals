package com.example.goalandgoals.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.goalandgoals.Activity.LoginActivity;
import com.example.goalandgoals.Model.TaskViewModel;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Utils.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;

public class SettingFragment extends Fragment {

    private FirebaseAuth mAuth;
    private AppDatabase db;
    private TaskViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = AppDatabase.getInstance(requireContext());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // Logout Button
        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            // Clear all local tasks and user progress
            AsyncTask.execute(() -> {
                db.toDoDao().deleteAllTasksForUser(""); // Clear all tasks
                db.userProgressDao().deleteAll(); // Clear all user progress
            });

            mAuth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Dark Mode Switch
        Switch darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        return view;
    }
}