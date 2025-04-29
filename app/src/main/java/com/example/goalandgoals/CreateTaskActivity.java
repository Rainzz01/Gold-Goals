package com.example.goalandgoals;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Utils.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText taskNameEditText, descriptionEditText;
    private Spinner difficultySpinner;
    private TextView expRewardTextView, coinRewardTextView, penaltyTextView;
    private Button setCoinRewardButton, startTimeButton, deadlineButton, reminderTimeButton, saveButton;
    private AppDatabase db;
    private int coinReward = 0;
    private String startTime, deadline, reminderTime;
    private int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        db = AppDatabase.getInstance(this);

        // Card 1: Task Name, Description
        taskNameEditText = findViewById(R.id.taskNameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        // Card 2: Difficulty, EXP Reward, Coin Reward, Penalty
        difficultySpinner = findViewById(R.id.difficultySpinner);
        expRewardTextView = findViewById(R.id.expRewardTextView);
        coinRewardTextView = findViewById(R.id.coinRewardTextView);
        setCoinRewardButton = findViewById(R.id.setCoinRewardButton);
        penaltyTextView = findViewById(R.id.penaltyTextView);

        // Card 3: Start Time, Deadline, Reminder Time
        startTimeButton = findViewById(R.id.startTimeButton);
        deadlineButton = findViewById(R.id.deadlineButton);
        reminderTimeButton = findViewById(R.id.reminderTimeButton);
        saveButton = findViewById(R.id.saveButton);

        // Setup Difficulty Spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
        difficultySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateExpRewardAndPenalty(position + 1);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Setup Coin Reward Button
        setCoinRewardButton.setOnClickListener(v -> showCoinRewardDialog());

        // Setup Time Pickers
        startTimeButton.setOnClickListener(v -> showDateTimePicker(startTimeButton, time -> startTime = time));
        deadlineButton.setOnClickListener(v -> showDateTimePicker(deadlineButton, time -> deadline = time));
        reminderTimeButton.setOnClickListener(v -> showDateTimePicker(reminderTimeButton, time -> reminderTime = time));

        // Load existing task data if editing
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        if (id != -1) {
            setTitle("Edit Task");
            taskNameEditText.setText(intent.getStringExtra("task"));
            descriptionEditText.setText(intent.getStringExtra("description"));
            String difficulty = intent.getStringExtra("difficulty");
            int difficultyIndex = getDifficultyIndex(difficulty);
            difficultySpinner.setSelection(difficultyIndex);
            coinReward = intent.getIntExtra("coin_reward", 0);
            coinRewardTextView.setText(String.format(Locale.getDefault(), "Coin Reward: %d", coinReward));
            startTime = intent.getStringExtra("start_time");
            deadline = intent.getStringExtra("deadline");
            reminderTime = intent.getStringExtra("reminder_time");
            startTimeButton.setText(startTime != null ? startTime : "Set Start Time");
            deadlineButton.setText(deadline != null ? deadline : "Set Deadline");
            reminderTimeButton.setText(reminderTime != null ? reminderTime : "Set Reminder Time (Optional)");
            updateExpRewardAndPenalty(difficultyIndex + 1); // Update penalties for existing task
        } else {
            setTitle("Create Task");
            updateExpRewardAndPenalty(1); // Default to Easy
        }

        // Save Task
        saveButton.setOnClickListener(v -> saveTask());
    }

    private int getDifficultyIndex(String difficulty) {
        String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i].equals(difficulty)) {
                return i;
            }
        }
        return 0; // Default to Easy
    }

    private void updateExpRewardAndPenalty(int level) {
        int expReward;
        double penaltyMultiplier;
        switch (level) {
            case 1: // Easy
                expReward = 10;
                penaltyMultiplier = 0.01;
                break;
            case 2: // Medium
                expReward = 20;
                penaltyMultiplier = 0.02;
                break;
            case 3: // Hard
                expReward = 30;
                penaltyMultiplier = 0.03;
                break;
            case 4: // Extreme
                expReward = 40;
                penaltyMultiplier = 0.04;
                break;
            default:
                expReward = 10;
                penaltyMultiplier = 0.01;
        }
        expRewardTextView.setText(String.format(Locale.getDefault(), "EXP Reward: %d", expReward));

        // Calculate penalties
        double expPenalty = expReward * penaltyMultiplier;
        double coinPenalty = coinReward * penaltyMultiplier;
        penaltyTextView.setText(String.format(Locale.getDefault(), "Penalty: %s %.2f (*%.2f) | %s %.2f (*%.2f)",
                getString(R.string.exp_icon), expPenalty, penaltyMultiplier,
                getString(R.string.coin_icon), coinPenalty, penaltyMultiplier));
    }

    private void showCoinRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_coin_reward, null);
        builder.setView(dialogView);

        EditText coinInput = dialogView.findViewById(R.id.coinInput);
        Button randomButton = dialogView.findViewById(R.id.randomButton);
        Button okButton = dialogView.findViewById(R.id.okButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        int maxCoins;
        int level = difficultySpinner.getSelectedItemPosition() + 1;
        switch (level) {
            case 1: maxCoins = 50; break; // Easy
            case 2: maxCoins = 100; break; // Medium
            case 3: maxCoins = 150; break; // Hard
            case 4: maxCoins = 200; break; // Extreme
            default: maxCoins = 50;
        }

        randomButton.setOnClickListener(v -> {
            Random random = new Random();
            int randomCoins = random.nextInt(maxCoins + 1);
            coinInput.setText(String.valueOf(randomCoins));
        });

        AlertDialog dialog = builder.create();
        okButton.setOnClickListener(v -> {
            String input = coinInput.getText().toString().trim();
            if (input.isEmpty()) {
                coinReward = 0;
            } else {
                try {
                    int coins = Integer.parseInt(input);
                    if (coins <= maxCoins) {
                        coinReward = coins;
                        coinRewardTextView.setText(String.format(Locale.getDefault(), "Coin Reward: %d", coinReward));
                        updateExpRewardAndPenalty(difficultySpinner.getSelectedItemPosition() + 1); // Update penalties
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Coin reward exceeds maximum (" + maxCoins + ")", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid coin value", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDateTimePicker(Button button, TimeCallback callback) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String time = sdf.format(calendar.getTime());
                button.setText(time);
                callback.onTimeSelected(time);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String taskName = taskNameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String difficulty = difficultySpinner.getSelectedItem().toString();

        if (taskName.isEmpty()) {
            Toast.makeText(this, "Task name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startTime == null || deadline == null) {
            Toast.makeText(this, "Start time and deadline are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int expReward;
        double penaltyMultiplier;
        int level = difficultySpinner.getSelectedItemPosition() + 1;
        switch (level) {
            case 1: expReward = 10; penaltyMultiplier = 0.01; break;
            case 2: expReward = 20; penaltyMultiplier = 0.02; break;
            case 3: expReward = 30; penaltyMultiplier = 0.03; break;
            case 4: expReward = 40; penaltyMultiplier = 0.04; break;
            default: expReward = 10; penaltyMultiplier = 0.01;
        }

        int expPenalty = (int) (expReward * penaltyMultiplier);
        int coinPenalty = (int) (coinReward * penaltyMultiplier);

        if (id == -1) {
            // Create new task
            ToDoModel task = new ToDoModel();
            task.setTask(taskName);
            task.setDescription(description);
            task.setStatus(0);
            task.setDifficulty(difficulty);
            task.setStartTime(startTime);
            task.setDeadline(deadline);
            task.setExpReward(expReward);
            task.setCoinReward(coinReward);
            task.setExpPenalty(expPenalty);
            task.setCoinPenalty(coinPenalty);
            task.setReminderTime(reminderTime);
            task.setTaskType("Normal");
            task.setRepeatCount(1);

            AsyncTask.execute(() -> {
                db.toDoDao().insertTask(task);
                syncTaskToFirebase(task);
                runOnUiThread(() -> {
                    Toast.makeText(CreateTaskActivity.this, "Task created", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            });
        } else {
            // Update existing task
            AsyncTask.execute(() -> {
                db.toDoDao().updateTask(id, taskName, description, difficulty, startTime, deadline,
                        expReward, coinReward, expPenalty, coinPenalty, reminderTime, "Normal", 1);
                ToDoModel updatedTask = new ToDoModel();
                updatedTask.setId(id);
                updatedTask.setTask(taskName);
                updatedTask.setDescription(description);
                updatedTask.setStatus(0); // 这里要根据实际设置
                updatedTask.setDifficulty(difficulty);
                updatedTask.setStartTime(startTime);
                updatedTask.setDeadline(deadline);
                updatedTask.setExpReward(expReward);
                updatedTask.setCoinReward(coinReward);
                updatedTask.setExpPenalty(expPenalty);
                updatedTask.setCoinPenalty(coinPenalty);
                updatedTask.setReminderTime(reminderTime);
                updatedTask.setTaskType("Normal");
                updatedTask.setRepeatCount(1);

                syncTaskToFirebase(updatedTask);

                runOnUiThread(() -> {
                    Toast.makeText(CreateTaskActivity.this, "Task updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            });
        }
    }

    interface TimeCallback {
        void onTimeSelected(String time);
    }

    private void syncTaskToFirebase(ToDoModel task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // ⚡ 用你的 Realtime Database 完整 URL！
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");

            DatabaseReference tasksRef = database.getReference("users")
                    .child(uid)
                    .child("tasks");


            tasksRef.push().setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseSync", "Task successfully uploaded: " + task.getTask());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseSync", "Failed to upload task: " + e.getMessage());
                    });
        } else {
            Log.e("FirebaseSync", "User is null, cannot sync to Firebase.");
        }
    }


}