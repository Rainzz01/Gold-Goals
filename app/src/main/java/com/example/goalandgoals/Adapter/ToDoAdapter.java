package com.example.goalandgoals.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goalandgoals.CreateTaskActivity;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;
import com.example.goalandgoals.Utils.AppDatabase;
import com.example.goalandgoals.Dao.ToDoDao;
import com.example.goalandgoals.Dao.UserProgressDao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ToDoAdapter";
    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;
    private List<ToDoModel> incompleteTasks;
    private List<ToDoModel> completedTasks;
    private ToDoDao toDoDao;
    private UserProgressDao userProgressDao;
    private FragmentActivity activity;
    private boolean isUserInteraction = false; // Flag to track user-initiated changes

    public ToDoAdapter(FragmentActivity activity) {
        this.activity = activity;
        this.toDoDao = AppDatabase.getInstance(activity).toDoDao();
        this.userProgressDao = AppDatabase.getInstance(activity).userProgressDao();
        this.incompleteTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        Log.d(TAG, "ToDoAdapter: Initialized with activity=" + activity);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == incompleteTasks.size()) {
            return TYPE_HEADER;
        }
        return TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_completed_tasks, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_layout, parent, false);
            Log.d(TAG, "onCreateViewHolder: Inflated task_layout");
            return new TaskViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            // Header for completed tasks
            ((HeaderViewHolder) holder).headerText.setText("Completed Tasks");
            return;
        }

        ToDoModel item = getTaskAtPosition(position);
        if (item == null) {
            Log.e(TAG, "onBindViewHolder: ToDoModel is null at position=" + position);
            return;
        }

        TaskViewHolder taskHolder = (TaskViewHolder) holder;

        // Set task name
        taskHolder.taskNameTextView.setText(item.getTask() != null ? item.getTask() : "No Task Name");
        Log.d(TAG, "onBindViewHolder: Task name set to " + item.getTask());

        // Set rewards and difficulty
        String difficultyEmoji;
        switch (item.getDifficulty() != null ? item.getDifficulty() : "") {
            case "Easy":
                difficultyEmoji = activity.getString(R.string.easy);
                break;
            case "Medium":
                difficultyEmoji = activity.getString(R.string.medium);
                break;
            case "Hard":
                difficultyEmoji = activity.getString(R.string.hard);
                break;
            case "Extreme":
                difficultyEmoji = activity.getString(R.string.extreme);
                break;
            default:
                difficultyEmoji = "";
                Log.w(TAG, "onBindViewHolder: Unknown difficulty=" + item.getDifficulty());
        }
        String rewardDifficultyText = String.format(
                "%s %d | %s %d | %s %s %s",
                activity.getString(R.string.exp_icon), item.getExpReward(),
                activity.getString(R.string.coin_icon), item.getCoinReward(),
                activity.getString(R.string.difficulties), item.getDifficulty() != null ? item.getDifficulty() : "Unknown", difficultyEmoji
        );
        taskHolder.rewardDifficultyTextView.setText(rewardDifficultyText);
        Log.d(TAG, "onBindViewHolder: Reward/Difficulty set to " + rewardDifficultyText);

        // Set description
        taskHolder.descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "");
        Log.d(TAG, "onBindViewHolder: Description set to " + (item.getDescription() != null ? item.getDescription() : "null"));

        // Show checkbox and set state without triggering listener
        taskHolder.task.setVisibility(View.VISIBLE);
        taskHolder.task.setOnCheckedChangeListener(null); // Remove listener to prevent triggering
        taskHolder.task.setChecked(toBoolean(item.getStatus()));
        Log.d(TAG, "onBindViewHolder: Checkbox state set to " + toBoolean(item.getStatus()));

        // Set listener for user interactions
        taskHolder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isUserInteraction) return; // Ignore non-user interactions
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return; // Prevent invalid position
                int newStatus = isChecked ? 1 : 0;
                if (!isChecked && item.getStatus() == 1) {
                    // Show confirmation dialog for uncompleting a task
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Uncomplete Task");
                    builder.setMessage("Are you sure you want to mark this task as incomplete? This will deduct " + item.getExpReward() + " EXP and " + item.getCoinReward() + " Coins.");
                    builder.setPositiveButton("Confirm", (dialog, which) -> {
                        AsyncTask.execute(() -> {
                            toDoDao.updateStatus(item.getId(), newStatus);
                            // Deduct rewards from user progress
                            UserProgress progress = userProgressDao.getUserProgressById(1);
                            if (progress != null) {
                                progress.setXp(Math.max(0, progress.getXp() - item.getExpReward()));
                                progress.setCoins(Math.max(0, progress.getCoins() - item.getCoinReward()));
                                userProgressDao.update(progress);
                            }
                            syncTaskToFirebase(item, newStatus);
                            activity.runOnUiThread(() -> {
                                // Move task to incomplete list with precise notifications
                                int completedIndex = adapterPosition - incompleteTasks.size() - 1;
                                if (completedIndex >= 0 && completedIndex < completedTasks.size()) {
                                    completedTasks.remove(completedIndex);
                                    item.setStatus(newStatus);
                                    incompleteTasks.add(item);
                                    notifyItemRemoved(adapterPosition);
                                    notifyItemInserted(incompleteTasks.size() - 1);
                                    if (completedTasks.isEmpty()) {
                                        notifyItemRemoved(incompleteTasks.size());
                                    }
                                }
                            });
                        });
                        Log.d(TAG, "onCheckedChanged: Task ID=" + item.getId() + " marked incomplete");
                    });
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        // Revert checkbox state
                        taskHolder.task.setChecked(true);
                    });
                    builder.show();
                } else if (isChecked && item.getStatus() == 0) {
                    AsyncTask.execute(() -> {
                        toDoDao.updateStatus(item.getId(), newStatus);
                        // Add rewards to user progress
                        UserProgress progress = userProgressDao.getUserProgressById(1);
                        if (progress == null) {
                            progress = new UserProgress(1, 0, 0);
                            progress.setXp(item.getExpReward());
                            progress.setCoins(item.getCoinReward());
                            userProgressDao.insert(progress);
                        } else {
                            progress.setXp(progress.getXp() + item.getExpReward());
                            progress.setCoins(progress.getCoins() + item.getCoinReward());
                            userProgressDao.update(progress);
                        }
                        syncTaskToFirebase(item, newStatus);
                        activity.runOnUiThread(() -> {
                            // Move task to completed list with precise notifications
                            int incompleteIndex = adapterPosition;
                            if (incompleteIndex >= 0 && incompleteIndex < incompleteTasks.size()) {
                                incompleteTasks.remove(incompleteIndex);
                                item.setStatus(newStatus);
                                boolean wasEmpty = completedTasks.isEmpty();
                                completedTasks.add(item);
                                notifyItemRemoved(adapterPosition);
                                int newPosition = incompleteTasks.size() + (wasEmpty ? 0 : 1);
                                notifyItemInserted(newPosition);
                                if (wasEmpty) {
                                    notifyItemInserted(incompleteTasks.size());
                                }
                            }
                        });
                    });
                    Log.d(TAG, "onCheckedChanged: Task ID=" + item.getId() + " marked complete");
                }
            }
        });

        // Enable user interaction tracking
        taskHolder.task.setOnTouchListener((v, event) -> {
            isUserInteraction = true;
            return false;
        });
    }

    private ToDoModel getTaskAtPosition(int position) {
        if (position < incompleteTasks.size()) {
            return incompleteTasks.get(position);
        } else if (position > incompleteTasks.size()) {
            return completedTasks.get(position - incompleteTasks.size() - 1);
        }
        return null;
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        int count = incompleteTasks.size() + completedTasks.size() + (completedTasks.isEmpty() ? 0 : 1);
        Log.d(TAG, "getItemCount: Returning " + count);
        return count;
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        // Create new lists to avoid modifying during iteration
        List<ToDoModel> newIncompleteTasks = new ArrayList<>();
        List<ToDoModel> newCompletedTasks = new ArrayList<>();
        if (todoList != null) {
            for (ToDoModel task : todoList) {
                // Avoid duplicates by checking IDs
                boolean existsInIncomplete = newIncompleteTasks.stream().anyMatch(t -> t.getId() == task.getId());
                boolean existsInCompleted = newCompletedTasks.stream().anyMatch(t -> t.getId() == task.getId());
                if (!existsInIncomplete && !existsInCompleted) {
                    if (task.getStatus() == 0) {
                        newIncompleteTasks.add(task);
                    } else {
                        newCompletedTasks.add(task);
                    }
                }
            }
        }
        // Update lists and notify adapter
        incompleteTasks.clear();
        incompleteTasks.addAll(newIncompleteTasks);
        completedTasks.clear();
        completedTasks.addAll(newCompletedTasks);
        notifyDataSetChanged();
        Log.d(TAG, "setTasks: Tasks updated, incomplete=" + incompleteTasks.size() + ", completed=" + completedTasks.size());
    }

    public void deleteItem(int position) {
        ToDoModel item = getTaskAtPosition(position);
        if (item == null) {
            Log.e(TAG, "deleteItem: Invalid position=" + position);
            return;
        }
        if (item.getStatus() == 1) {
            Toast.makeText(activity, "Cannot delete completed tasks", Toast.LENGTH_SHORT).show();
            notifyDataSetChanged(); // Reset swipe
            return;
        }
        AsyncTask.execute(() -> {
            toDoDao.deleteTask(item);
            activity.runOnUiThread(() -> {
                if (position < incompleteTasks.size()) {
                    incompleteTasks.remove(position);
                    notifyItemRemoved(position);
                }
            });
        });
        Log.d(TAG, "deleteItem: Deleted task at position=" + position);
    }

    public void editItem(int position) {
        ToDoModel item = getTaskAtPosition(position);
        if (item == null) {
            Log.e(TAG, "editItem: Invalid position=" + position);
            return;
        }
        if (item.getStatus() == 1) {
            Toast.makeText(activity, "Cannot edit completed tasks", Toast.LENGTH_SHORT).show();
            notifyDataSetChanged(); // Reset swipe
            return;
        }
        Intent intent = new Intent(activity, CreateTaskActivity.class);
        intent.putExtra("id", item.getId());
        intent.putExtra("task", item.getTask());
        intent.putExtra("description", item.getDescription());
        intent.putExtra("difficulty", item.getDifficulty());
        intent.putExtra("start_time", item.getStartTime());
        intent.putExtra("deadline", item.getDeadline());
        intent.putExtra("exp_reward", item.getExpReward());
        intent.putExtra("coin_reward", item.getCoinReward());
        intent.putExtra("exp_penalty", item.getExpPenalty());
        intent.putExtra("coin_penalty", item.getCoinPenalty());
        intent.putExtra("reminder_time", item.getReminderTime());
        intent.putExtra("task_type", item.getTaskType());
        intent.putExtra("repeat_count", item.getRepeatCount());
        activity.startActivity(intent);
        Log.d(TAG, "editItem: Started CreateTaskActivity for task ID=" + item.getId());
    }

    private void syncTaskToFirebase(ToDoModel task, int newStatus) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference tasksRef = database.getReference("users").child(uid).child("tasks");
            tasksRef.orderByChild("id").equalTo(task.getId()).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnap : snapshot.getChildren()) {
                        task.setStatus(newStatus);
                        taskSnap.getRef().setValue(task)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task status synced to Firebase: ID=" + task.getId()))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync task status: " + e.getMessage()));
                    }
                } else {
                    // If task doesn't exist in Firebase, create it
                    tasksRef.push().setValue(task)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "New task synced to Firebase: ID=" + task.getId()))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to sync new task: " + e.getMessage()));
                }
            });
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView taskNameTextView;
        TextView rewardDifficultyTextView;
        TextView descriptionTextView;

        TaskViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            taskNameTextView = view.findViewById(R.id.taskNameTextView);
            rewardDifficultyTextView = view.findViewById(R.id.rewardDifficultyTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
            Log.d(TAG, "TaskViewHolder: Initialized with taskNameTextView=" + (taskNameTextView != null) +
                    ", rewardDifficultyTextView=" + (rewardDifficultyTextView != null) +
                    ", descriptionTextView=" + (descriptionTextView != null));
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        HeaderViewHolder(View view) {
            super(view);
            headerText = view.findViewById(R.id.headerText);
        }
    }
}