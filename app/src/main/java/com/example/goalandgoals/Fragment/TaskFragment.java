package com.example.goalandgoals.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
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
import com.example.goalandgoals.Activity.CreateTaskActivity;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Helper.RecyclerItemTouchHelper;
import com.example.goalandgoals.Model.TaskViewModel;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;
import com.example.goalandgoals.Utils.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

    private static final String TAG = "TaskFragment";
    private static final int REQUEST_CODE_EDIT_TASK = 100;
    private ToDoAdapter adapter;
    private TaskViewModel viewModel;
    private List<ToDoModel> lastTaskList = new ArrayList<>();
    private RecyclerItemTouchHelper itemTouchHelperCallback;

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
        itemTouchHelperCallback = new RecyclerItemTouchHelper(adapter);
        new androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        // Initialize ViewModel with current userId
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "";
        viewModel = new ViewModelProvider(requireActivity(), new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new TaskViewModel(requireActivity().getApplication(), userId);
            }
        }).get(TaskViewModel.class);
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                Log.d(TAG, "onCreateView: Updating adapter with task list: size=" + tasks.size());
                adapter.setTasks(tasks);
                lastTaskList = new ArrayList<>(tasks);
            } else {
                Log.w(TAG, "onCreateView: Task list is null");
                adapter.setTasks(new ArrayList<>());
            }
            // Update user progress display after tasks change
            loadUserDetails(view);
        });

        // Initialize ImageButton for new task
        ImageButton newQuestButton = view.findViewById(R.id.button_new_quest);
        newQuestButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_TASK);
            Log.d(TAG, "onCreateView: New quest button clicked");
        });

        // Load user details
        loadUserDetails(view);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_TASK && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Edit task completed, resetting swipe state");
            itemTouchHelperCallback.resetSwipeState(adapter.getRecyclerView());
        }
    }

    private void loadUserDetails(View view) {
        TextView userIdTextView = view.findViewById(R.id.userIdTextView);
        TextView userExpTextView = view.findViewById(R.id.userExpTextView);
        TextView userCoinsTextView = view.findViewById(R.id.userCoinsTextView);

        // Get Firebase UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userIdTextView.setText("UID: " + uid);
        } else {
            userIdTextView.setText("UID: Unknown");
        }

        // Get XP/Coins from local Room database
        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            UserProgress userProgress = db.userProgressDao().getUserProgressById(1);

            requireActivity().runOnUiThread(() -> {
                if (userProgress != null) {
                    userExpTextView.setText(String.format("📊 EXP: %d", userProgress.getXp()));
                    userCoinsTextView.setText(String.format("💰 Coins: %d", userProgress.getCoins()));
                    Log.d(TAG, "loadUserDetails: Loaded XP=" + userProgress.getXp() +
                            ", Coins=" + userProgress.getCoins());
                    // Sync progress to Firebase
                    syncUserProgressToFirebase(userProgress);
                } else {
                    userExpTextView.setText("📊 EXP: 0");
                    userCoinsTextView.setText("💰 Coins: 0");
                    Log.w(TAG, "loadUserDetails: No user progress found for ID=1");
                }
            });
        });
    }

    private void syncUserProgressToFirebase(UserProgress progress) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference progressRef = database.getReference("users").child(uid).child("progress");
            progressRef.setValue(progress)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User progress synced to Firebase: XP=" + progress.getXp() + ", Coins=" + progress.getCoins()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to sync user progress: " + e.getMessage()));
        }
    }
}