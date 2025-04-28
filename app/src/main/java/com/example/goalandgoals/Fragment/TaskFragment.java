package com.example.goalandgoals.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goalandgoals.Adapter.ToDoAdapter;
import com.example.goalandgoals.CreateTaskActivity;
import com.example.goalandgoals.R;
import com.example.goalandgoals.RecyclerItemTouchHelper;
import com.example.goalandgoals.TaskViewModel;
import com.example.goalandgoals.Model.UserProgress;
import com.example.goalandgoals.Utils.AppDatabase;

public class TaskFragment extends Fragment {

    private static final String TAG = "TaskFragment";
    private ToDoAdapter adapter;
    private TaskViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating TaskFragment layout");
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.taskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ToDoAdapter(requireActivity());
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "onCreateView: RecyclerView adapter set");

        // Set up ItemTouchHelper for swipe actions
        RecyclerItemTouchHelper itemTouchHelperCallback = new RecyclerItemTouchHelper(adapter);
        new androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        // Initialize ViewModel and observe tasks
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks);
            Log.d(TAG, "onCreateView: Tasks observed, count=" + (tasks != null ? tasks.size() : 0));
        });

        // Initialize ImageButton for new task
        ImageButton newQuestButton = view.findViewById(R.id.button_new_quest);
        newQuestButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
            startActivity(intent);
            Log.d(TAG, "onCreateView: New quest button clicked");
        });

        // Load user details
        loadUserDetails(view);

        return view;
    }

    private void loadUserDetails(View view) {
        TextView userIdTextView = view.findViewById(R.id.userIdTextView);
        TextView userExpTextView = view.findViewById(R.id.userExpTextView);
        TextView userCoinsTextView = view.findViewById(R.id.userCoinsTextView);

        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            // Assuming user ID 1 for simplicity; adjust based on your authentication logic
            UserProgress userProgress = db.userProgressDao().getUserProgressById(1);
            requireActivity().runOnUiThread(() -> {
                if (userProgress != null) {
                    userIdTextView.setText("ID: " + userProgress.getId());
                    userExpTextView.setText(String.format("📊 EXP: %d", userProgress.getXp()));
                    userCoinsTextView.setText(String.format("💰 Coins: %d", userProgress.getCoins()));
                    Log.d(TAG, "loadUserDetails: Loaded user ID=" + userProgress.getId() +
                            ", XP=" + userProgress.getXp() + ", Coins=" + userProgress.getCoins());
                } else {
                    userIdTextView.setText("ID: Unknown");
                    userExpTextView.setText("📊 EXP: 0");
                    userCoinsTextView.setText("💰 Coins: 0");
                    Log.w(TAG, "loadUserDetails: No user progress found for ID=1");
                }
            });
        });
    }
}