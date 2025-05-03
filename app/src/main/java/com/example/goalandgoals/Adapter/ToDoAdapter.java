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

import com.example.goalandgoals.Activity.CreateTaskActivity;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.MutableData;

import java.util.ArrayList;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ToDoAdapter";
    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;
    private final List<ToDoModel> incompleteTasks;
    private final List<ToDoModel> completedTasks;
    private final ToDoDao toDoDao;
    private final UserProgressDao userProgressDao;
    private final FragmentActivity activity;
    private boolean isUserInteraction = true; // Flag to track user-initiated changes
    private boolean isUpdating = false; // Flag to prevent concurrent updates

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
        int viewType = position == incompleteTasks.size() && !completedTasks.isEmpty() ? TYPE_HEADER : TYPE_TASK;
        Log.d(TAG, "getItemViewType: position=" + position + ", viewType=" + viewType);
        return viewType;
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
        Log.d(TAG, "onBindViewHolder: position=" + position);
        if (getItemViewType(position) == TYPE_HEADER) {
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

        // Show checkbox and set state without triggering listener
        taskHolder.task.setVisibility(View.VISIBLE);
        taskHolder.task.setOnCheckedChangeListener(null); // Remove listener to prevent triggering
        taskHolder.task.setChecked(item.getStatus() == 1);
        Log.d(TAG, "onBindViewHolder: Checkbox state set to " + (item.getStatus() == 1));

        // Set listener for user interactions
        taskHolder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged called: taskId=" + item.getId() + ", isChecked=" + isChecked + ", userInteraction=" + isUserInteraction);
                if (!isUserInteraction) {
                    Log.d(TAG, "Ignoring non-user interaction for taskId=" + item.getId());
                    return;
                }
                if (isUpdating) {
                    Log.d(TAG, "Skipping onCheckedChanged due to ongoing update: taskId=" + item.getId());
                    return;
                }
                isUpdating = true;

                int newStatus = isChecked ? 1 : 0;
                StringBuilder logBuilder = new StringBuilder();
                logBuilder.append("Task Check Change Log:\n")
                        .append("Task ID: ").append(item.getId()).append("\n")
                        .append("Task Name: ").append(item.getTask()).append("\n")
                        .append("New Status: ").append(newStatus == 1 ? "Completed" : "Incomplete").append("\n");

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");

                if (!isChecked && item.getStatus() == 1) {
                    // Uncompleting a task
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Uncomplete Task");
                    builder.setMessage("Are you sure you want to mark this task as incomplete? This will deduct " + item.getExpReward() + " EXP and " + item.getCoinReward() + " Coins.");
                    builder.setPositiveButton("Confirm", (dialog, which) -> {
                        logBuilder.append("Action: Confirmed uncomplete\n");
                        AsyncTask.execute(() -> {
                            toDoDao.updateStatus(item.getId(), newStatus);
                            logBuilder.append("Local DB: Updated status to ").append(newStatus).append("\n");

                            // Update Firebase progress using transaction
                            if (user != null) {
                                String uid = user.getUid();
                                DatabaseReference progressRef = database.getReference("users").child(uid).child("progress");
                                progressRef.runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                        UserProgress progress = mutableData.getValue(UserProgress.class);
                                        if (progress == null) {
                                            progress = new UserProgress(1, 0, 0);
                                        }
                                        progress.setXp(Math.max(0, progress.getXp() - item.getExpReward()));
                                        progress.setCoins(Math.max(0, progress.getCoins() - item.getCoinReward()));
                                        mutableData.setValue(progress);
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@NonNull DatabaseError error, boolean committed, @NonNull DataSnapshot snapshot) {
                                        if (error != null) {
                                            logBuilder.append("Firebase: Failed to update progress - ").append(error.getMessage()).append("\n");
                                            Log.e(TAG, "Failed to update progress: " + error.getMessage());
                                        } else {
                                            logBuilder.append("Firebase: Progress updated, XP deducted=").append(item.getExpReward())
                                                    .append(", Coins deducted=").append(item.getCoinReward()).append("\n");
                                            Log.d(TAG, "Progress updated successfully");
                                            // Sync local Room database
                                            AsyncTask.execute(() -> {
                                                UserProgress progress = userProgressDao.getUserProgressById(1);
                                                if (progress != null) {
                                                    progress.setXp(Math.max(0, progress.getXp() - item.getExpReward()));
                                                    progress.setCoins(Math.max(0, progress.getCoins() - item.getCoinReward()));
                                                    userProgressDao.update(progress);
                                                    logBuilder.append("Local DB: Synced progress, XP=").append(progress.getXp())
                                                            .append(", Coins=").append(progress.getCoins()).append("\n");
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                            syncTaskToFirebase(item, newStatus, logBuilder);
                            activity.runOnUiThread(() -> {
                                // Log lists before modification
                                logBuilder.append("Before Move: incompleteTasks=").append(incompleteTasks.size())
                                        .append(", completedTasks=").append(completedTasks.size()).append("\n");

                                // Remove from completedTasks
                                int completedPosition = completedTasks.indexOf(item);
                                if (completedPosition != -1) {
                                    completedTasks.remove(completedPosition);
                                    int headerPosition = incompleteTasks.size();
                                    if (completedTasks.isEmpty()) {
                                        Log.d(TAG, "Removing header at position=" + headerPosition);
                                        notifyItemRemoved(headerPosition);
                                    }
                                    Log.d(TAG, "Removing task at position=" + (headerPosition + 1 + completedPosition));
                                    notifyItemRemoved(headerPosition + 1 + completedPosition);
                                } else {
                                    logBuilder.append("Warning: Task not found in completedTasks\n");
                                    Log.w(TAG, "Task ID=" + item.getId() + " not found in completedTasks");
                                }

                                // Update status and add to incompleteTasks
                                item.setStatus(newStatus);
                                if (!incompleteTasks.contains(item)) {
                                    Log.d(TAG, "Adding task to incompleteTasks: taskId=" + item.getId() + ", taskName=" + item.getTask());
                                    incompleteTasks.add(0, item);
                                    Log.d(TAG, "Inserting task at position=0");
                                    notifyItemInserted(0);
                                } else {
                                    logBuilder.append("Warning: Task already in incompleteTasks\n");
                                    Log.w(TAG, "Task ID=" + item.getId() + " already in incompleteTasks");
                                }

                                // Log lists after modification
                                logBuilder.append("After Move: incompleteTasks=").append(incompleteTasks.size())
                                        .append(", completedTasks=").append(completedTasks.size()).append("\n");

                                showDebugDialog("Task Uncompleted", logBuilder.toString());
                                isUpdating = false;
                            });
                        });
                        Log.d(TAG, "onCheckedChanged: Task ID=" + item.getId() + " marked incomplete");
                    });
                    builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        taskHolder.task.setChecked(true);
                        logBuilder.append("Action: Cancelled uncomplete\n");
                        showDebugDialog("Task Uncomplete Cancelled", logBuilder.toString());
                        isUpdating = false;
                    });
                    builder.show();
                } else if (isChecked && item.getStatus() == 0) {
                    // Completing a task
                    AsyncTask.execute(() -> {
                        toDoDao.updateStatus(item.getId(), newStatus);
                        logBuilder.append("Local DB: Updated status to ").append(newStatus).append("\n");

                        // Update Firebase progress using transaction
                        if (user != null) {
                            String uid = user.getUid();
                            DatabaseReference progressRef = database.getReference("users").child(uid).child("progress");
                            progressRef.runTransaction(new Transaction.Handler() {
                                @NonNull
                                @Override
                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                    UserProgress progress = mutableData.getValue(UserProgress.class);
                                    if (progress == null) {
                                        progress = new UserProgress(1, 0, 0);
                                    }
                                    progress.setXp(progress.getXp() + item.getExpReward());
                                    progress.setCoins(progress.getCoins() + item.getCoinReward());
                                    mutableData.setValue(progress);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(@NonNull DatabaseError error, boolean committed, @NonNull DataSnapshot snapshot) {
                                    if (error != null) {
                                        logBuilder.append("Firebase: Failed to update progress - ").append(error.getMessage()).append("\n");
                                        Log.e(TAG, "Failed to update progress: " + error.getMessage());
                                    } else {
                                        logBuilder.append("Firebase: Progress updated, XP added=").append(item.getExpReward())
                                                .append(", Coins added=").append(item.getCoinReward()).append("\n");
                                        Log.d(TAG, "Progress updated successfully");
                                        // Sync local Room database
                                        AsyncTask.execute(() -> {
                                            UserProgress progress = userProgressDao.getUserProgressById(1);
                                            if (progress == null) {
                                                progress = new UserProgress(1, item.getExpReward(), item.getCoinReward());
                                                userProgressDao.insert(progress);
                                            } else {
                                                progress.setXp(progress.getXp() + item.getExpReward());
                                                progress.setCoins(progress.getCoins() + item.getCoinReward());
                                                userProgressDao.update(progress);
                                            }
                                            logBuilder.append("Local DB: Synced progress, XP=").append(progress.getXp())
                                                    .append(", Coins=").append(progress.getCoins()).append("\n");
                                        });
                                    }
                                }
                            });
                        }

                        syncTaskToFirebase(item, newStatus, logBuilder);
                        activity.runOnUiThread(() -> {
                            // Log lists before modification
                            logBuilder.append("Before Move: incompleteTasks=").append(incompleteTasks.size())
                                    .append(", completedTasks=").append(completedTasks.size()).append("\n");

                            // Remove from incompleteTasks
                            int incompletePosition = incompleteTasks.indexOf(item);
                            if (incompletePosition != -1) {
                                incompleteTasks.remove(incompletePosition);
                                Log.d(TAG, "Removing task at position=" + incompletePosition);
                                notifyItemRemoved(incompletePosition);
                            } else {
                                logBuilder.append("Warning: Task not found in incompleteTasks\n");
                                Log.w(TAG, "Task ID=" + item.getId() + " not found in incompleteTasks");
                            }

                            // Update status and add to completedTasks
                            item.setStatus(newStatus);
                            if (!completedTasks.contains(item)) {
                                Log.d(TAG, "Adding task to completedTasks: taskId=" + item.getId() + ", taskName=" + item.getTask());
                                completedTasks.add(0, item);
                                int headerPosition = incompleteTasks.size();
                                if (completedTasks.size() == 1) {
                                    Log.d(TAG, "Inserting header at position=" + headerPosition);
                                    notifyItemInserted(headerPosition);
                                }
                                Log.d(TAG, "Inserting task at position=" + (headerPosition + 1));
                                notifyItemInserted(headerPosition + 1);
                            } else {
                                logBuilder.append("Warning: Task already in completedTasks\n");
                                Log.w(TAG, "Task ID=" + item.getId() + " already in completedTasks");
                            }

                            // Log lists after modification
                            logBuilder.append("After Move: incompleteTasks=").append(incompleteTasks.size())
                                    .append(", completedTasks=").append(completedTasks.size()).append("\n");

                            showDebugDialog("Task Completed", logBuilder.toString());
                            isUpdating = false;
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
            ToDoModel task = incompleteTasks.get(position);
            Log.d(TAG, "getTaskAtPosition: position=" + position + ", taskId=" + task.getId() + ", incomplete");
            return task;
        } else if (position == incompleteTasks.size() && !completedTasks.isEmpty()) {
            Log.d(TAG, "getTaskAtPosition: position=" + position + ", header");
            return null; // Header position
        } else if (position > incompleteTasks.size() && position <= incompleteTasks.size() + completedTasks.size()) {
            ToDoModel task = completedTasks.get(position - incompleteTasks.size() - 1);
            Log.d(TAG, "getTaskAtPosition: position=" + position + ", taskId=" + task.getId() + ", completed");
            return task;
        }
        Log.e(TAG, "getTaskAtPosition: Invalid position=" + position);
        return null;
    }

    @Override
    public int getItemCount() {
        int count = incompleteTasks.size() + completedTasks.size() + (completedTasks.isEmpty() ? 0 : 1);
        Log.d(TAG, "getItemCount: incomplete=" + incompleteTasks.size() + ", completed=" + completedTasks.size() + ", total=" + count);
        return count;
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        Log.d(TAG, "setTasks called: todoListSize=" + (todoList != null ? todoList.size() : 0));
        if (isUpdating) {
            Log.d(TAG, "Skipping setTasks due to ongoing update");
            return;
        }
        incompleteTasks.clear();
        completedTasks.clear();
        if (todoList != null) {
            for (ToDoModel task : todoList) {
                if (task.getStatus() == 0) {
                    if (!incompleteTasks.contains(task)) {
                        incompleteTasks.add(task);
                    } else {
                        Log.w(TAG, "setTasks: Duplicate incomplete task ID=" + task.getId());
                    }
                } else {
                    if (!completedTasks.contains(task)) {
                        completedTasks.add(task);
                    } else {
                        Log.w(TAG, "setTasks: Duplicate completed task ID=" + task.getId());
                    }
                }
            }
        }
        Log.d(TAG, "setTasks: incomplete=" + incompleteTasks.size() + ", completed=" + completedTasks.size());
        notifyDataSetChanged();
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
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Task Deletion Log:\n")
                .append("Task ID: ").append(item.getId()).append("\n")
                .append("Task Name: ").append(item.getTask()).append("\n");
        AsyncTask.execute(() -> {
            toDoDao.deleteTask(item);
            logBuilder.append("Local DB: Deleted task\n");
            deleteTaskFromFirebase(item, logBuilder);
            activity.runOnUiThread(() -> {
                if (position < incompleteTasks.size()) {
                    incompleteTasks.remove(position);
                } else {
                    completedTasks.remove(position - incompleteTasks.size() - 1);
                }
                notifyDataSetChanged();
                logBuilder.append("UI: Removed task from list\n");
                showDebugDialog("Task Deleted", logBuilder.toString());
                Log.d(TAG, "deleteItem: Deleted task at position=" + position);
            });
        });
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

    private void syncTaskToFirebase(ToDoModel task, int newStatus, StringBuilder logBuilder) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference tasksRef = database.getReference("users").child(uid).child("tasks");

            if (task.getFirebaseKey() != null && !task.getFirebaseKey().isEmpty()) {
                // Update existing task
                task.setStatus(newStatus);
                tasksRef.child(task.getFirebaseKey()).setValue(task)
                        .addOnSuccessListener(aVoid -> {
                            logBuilder.append("Firebase: Updated task status to ").append(newStatus).append("\n");
                            Log.d(TAG, "Task status synced to Firebase: ID=" + task.getId());
                        })
                        .addOnFailureListener(e -> {
                            logBuilder.append("Firebase: Failed to update task status - ").append(e.getMessage()).append("\n");
                            Log.e(TAG, "Failed to sync task status: " + e.getMessage());
                        });
            } else {
                // Create new task
                String newTaskKey = tasksRef.push().getKey();
                task.setFirebaseKey(newTaskKey);
                task.setStatus(newStatus);
                tasksRef.child(newTaskKey).setValue(task)
                        .addOnSuccessListener(aVoid -> {
                            logBuilder.append("Firebase: Created new task with status ").append(newStatus).append("\n");
                            Log.d(TAG, "New task synced to Firebase: ID=" + task.getId());
                            AsyncTask.execute(() -> {
                                toDoDao.updateFirebaseKey(task.getId(), newTaskKey);
                            });
                        })
                        .addOnFailureListener(e -> {
                            logBuilder.append("Firebase: Failed to create new task - ").append(e.getMessage()).append("\n");
                            Log.e(TAG, "Failed to sync new task: " + e.getMessage());
                        });
            }
        } else {
            logBuilder.append("Firebase: No user logged in\n");
            Log.e(TAG, "No user logged in, cannot sync to Firebase");
        }
    }

    private void deleteTaskFromFirebase(ToDoModel task, StringBuilder logBuilder) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference tasksRef = database.getReference("users").child(uid).child("tasks");

            if (task.getFirebaseKey() != null && !task.getFirebaseKey().isEmpty()) {
                tasksRef.child(task.getFirebaseKey()).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            logBuilder.append("Firebase: Deleted task\n");
                            Log.d(TAG, "Task deleted from Firebase: ID=" + task.getId());
                        })
                        .addOnFailureListener(e -> {
                            logBuilder.append("Firebase: Failed to delete task - ").append(e.getMessage()).append("\n");
                            Log.e(TAG, "Failed to delete task from Firebase: " + e.getMessage());
                        });
            } else {
                logBuilder.append("Firebase: No firebaseKey found for task\n");
                Log.w(TAG, "Cannot delete task from Firebase: No firebaseKey for ID=" + task.getId());
                tasksRef.orderByChild("id").equalTo(task.getId()).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        for (DataSnapshot taskSnap : snapshot.getChildren()) {
                            taskSnap.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        logBuilder.append("Firebase: Deleted task via id query\n");
                                        Log.d(TAG, "Task deleted from Firebase via id query: ID=" + task.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        logBuilder.append("Firebase: Failed to delete task via id query - ").append(e.getMessage()).append("\n");
                                        Log.e(TAG, "Failed to delete task via id query: " + e.getMessage());
                                    });
                        }
                    } else {
                        logBuilder.append("Firebase: Task not found\n");
                    }
                }).addOnFailureListener(e -> {
                    logBuilder.append("Firebase: Failed to query tasks for deletion - ").append(e.getMessage()).append("\n");
                    Log.e(TAG, "Failed to query tasks for deletion: " + e.getMessage());
                });
            }
        } else {
            logBuilder.append("Firebase: No user logged in\n");
            Log.e(TAG, "No user logged in, cannot delete from Firebase");
        }
    }

    private void showDebugDialog(String title, String logMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(logMessage);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
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