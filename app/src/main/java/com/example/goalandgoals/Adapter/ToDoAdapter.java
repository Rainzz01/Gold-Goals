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

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goalandgoals.CreateTaskActivity;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Utils.AppDatabase;
import com.example.goalandgoals.Dao.ToDoDao;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {
    private static final String TAG = "ToDoAdapter";
    private List<ToDoModel> todoList;
    private ToDoDao toDoDao;
    private FragmentActivity activity;

    public ToDoAdapter(FragmentActivity activity) {
        this.activity = activity;
        this.toDoDao = AppDatabase.getInstance(activity).toDoDao();
        Log.d(TAG, "ToDoAdapter: Initialized with activity=" + activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        Log.d(TAG, "onCreateViewHolder: Inflated task_layout");
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (todoList == null || position >= todoList.size()) {
            Log.e(TAG, "onBindViewHolder: Invalid todoList or position=" + position);
            return;
        }

        final ToDoModel item = todoList.get(position);
        if (item == null) {
            Log.e(TAG, "onBindViewHolder: ToDoModel is null at position=" + position);
            return;
        }

        // Set task name
        holder.taskNameTextView.setText(item.getTask() != null ? item.getTask() : "No Task Name");
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
        holder.rewardDifficultyTextView.setText(rewardDifficultyText);
        Log.d(TAG, "onBindViewHolder: Reward/Difficulty set to " + rewardDifficultyText);

        // Set description
        holder.descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "");
        Log.d(TAG, "onBindViewHolder: Description set to " + (item.getDescription() != null ? item.getDescription() : "null"));

        // Handle checkbox state (still functional but hidden)
        holder.task.setChecked(toBoolean(item.getStatus()));
        Log.d(TAG, "onBindViewHolder: Checkbox state set to " + toBoolean(item.getStatus()));

        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AsyncTask.execute(() ->
                        toDoDao.updateStatus(item.getId(), isChecked ? 1 : 0)
                );
                Log.d(TAG, "onCheckedChanged: Updated status for task ID=" + item.getId() + " to " + isChecked);
            }
        });
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        int count = todoList == null ? 0 : todoList.size();
        Log.d(TAG, "getItemCount: Returning " + count);
        return count;
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
        Log.d(TAG, "setTasks: Tasks updated, count=" + (todoList == null ? 0 : todoList.size()));
    }

    public void deleteItem(int position) {
        if (todoList == null || position >= todoList.size()) {
            Log.e(TAG, "deleteItem: Invalid todoList or position=" + position);
            return;
        }
        ToDoModel item = todoList.get(position);
        AsyncTask.execute(() -> {
            toDoDao.deleteTask(item);
            activity.runOnUiThread(() -> {
                todoList.remove(position);
                notifyItemRemoved(position);
                Log.d(TAG, "deleteItem: Deleted task at position=" + position);
            });
        });
    }

    public void editItem(int position) {
        if (todoList == null || position >= todoList.size()) {
            Log.e(TAG, "editItem: Invalid todoList or position=" + position);
            return;
        }
        ToDoModel item = todoList.get(position);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView taskNameTextView;
        TextView rewardDifficultyTextView;
        TextView descriptionTextView;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            taskNameTextView = view.findViewById(R.id.taskNameTextView);
            rewardDifficultyTextView = view.findViewById(R.id.rewardDifficultyTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
            Log.d(TAG, "ViewHolder: Initialized with taskNameTextView=" + (taskNameTextView != null) +
                    ", rewardDifficultyTextView=" + (rewardDifficultyTextView != null) +
                    ", descriptionTextView=" + (descriptionTextView != null));
        }
    }
}