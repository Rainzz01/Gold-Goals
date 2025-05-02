package com.example.goalandgoals;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;
import com.example.goalandgoals.Utils.AppDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private boolean tasksSynced = false;
    private boolean progressSynced = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        Button goToRegisterButton = findViewById(R.id.goToRegisterButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please input email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Clear the local Room database
                                    new Thread(() -> {
                                        try {
                                            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                            db.userProgressDao().deleteAll();
                                            db.toDoDao().deleteAllTasks();
                                        } catch (Exception e) {
                                            Log.e("LoginActivity", "Failed to clear local database: " + e.getMessage());
                                        }
                                    }).start();
                                    // Login success, sync data
                                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    syncUserProgressFromFirebase();
                                    syncTasksFromFirebase();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to register page
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void syncUserProgressFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference progressRef = database.getReference("users").child(uid).child("progress");

            progressRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    Integer xp = snapshot.child("xp").getValue(Integer.class);
                    Integer coins = snapshot.child("coins").getValue(Integer.class);

                    if (xp == null) xp = 0;
                    if (coins == null) coins = 0;

                    UserProgress userProgress = new UserProgress(1, xp, coins);

                    int finalXp = xp;
                    int finalCoins = coins;

                    AsyncTask.execute(() -> {
                        AppDatabase.getInstance(getApplicationContext())
                                .userProgressDao().insert(userProgress);
                        Log.d("FirebaseSync", "Progress: XP=" + finalXp + ", Coins=" + finalCoins);
                    });
                } else {
                    // Initialize default progress if none exists
                    UserProgress userProgress = new UserProgress(1, 0, 0);
                    AsyncTask.execute(() -> {
                        AppDatabase.getInstance(getApplicationContext())
                                .userProgressDao().insert(userProgress);
                        Log.d("FirebaseSync", "Initialized default progress: XP=0, Coins=0");
                    });
                    progressRef.setValue(userProgress);
                }
                progressSynced = true;
                tryStartMainActivity();
            }).addOnFailureListener(e -> {
                Log.e("FirebaseSync", "Failed to fetch progress: " + e.getMessage());
                progressSynced = true;
                tryStartMainActivity();
            });
        }
    }

    private void syncTasksFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference tasksRef = database.getReference("users").child(uid).child("tasks");

            tasksRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    List<ToDoModel> taskList = new ArrayList<>();
                    for (DataSnapshot taskSnap : snapshot.getChildren()) {
                        ToDoModel task = new ToDoModel();
                        // Explicitly map all fields to ensure consistency
                        task.setId(taskSnap.child("id").getValue(Integer.class) != null ?
                                taskSnap.child("id").getValue(Integer.class) : 0);
                        task.setTask(taskSnap.child("task").getValue(String.class));
                        task.setDescription(taskSnap.child("description").getValue(String.class));
                        task.setStatus(taskSnap.child("status").getValue(Integer.class) != null ?
                                taskSnap.child("status").getValue(Integer.class) : 0);
                        task.setDifficulty(taskSnap.child("difficulty").getValue(String.class));
                        task.setStartTime(taskSnap.child("startTime").getValue(String.class));
                        task.setDeadline(taskSnap.child("deadline").getValue(String.class));
                        task.setExpReward(taskSnap.child("expReward").getValue(Integer.class) != null ?
                                taskSnap.child("expReward").getValue(Integer.class) : 0);
                        task.setCoinReward(taskSnap.child("coinReward").getValue(Integer.class) != null ?
                                taskSnap.child("coinReward").getValue(Integer.class) : 0);
                        task.setExpPenalty(taskSnap.child("expPenalty").getValue(Integer.class) != null ?
                                taskSnap.child("expPenalty").getValue(Integer.class) : 0);
                        task.setCoinPenalty(taskSnap.child("coinPenalty").getValue(Integer.class) != null ?
                                taskSnap.child("coinPenalty").getValue(Integer.class) : 0);
                        task.setReminderTime(taskSnap.child("reminderTime").getValue(String.class));
                        task.setTaskType(taskSnap.child("taskType").getValue(String.class) != null ?
                                taskSnap.child("taskType").getValue(String.class) : "Normal");
                        task.setRepeatCount(taskSnap.child("repeatCount").getValue(Integer.class) != null ?
                                taskSnap.child("repeatCount").getValue(Integer.class) : 1);
                        // Store Firebase key for reference
                        task.setFirebaseKey(taskSnap.getKey());
                        taskList.add(task);
                    }
                    AsyncTask.execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        for (ToDoModel task : taskList) {
                            // Check for duplicates by ID
                            List<ToDoModel> existingTasks = db.toDoDao().getAllTasks();
                            boolean exists = false;
                            for (ToDoModel existingTask : existingTasks) {
                                if (existingTask.getId() == task.getId()) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                db.toDoDao().insertTask(task);
                            }
                        }
                        Log.d("FirebaseSync", "Downloaded " + taskList.size() + " tasks.");
                    });
                } else {
                    Log.d("FirebaseSync", "No tasks found for this user.");
                }
                tasksSynced = true;
                tryStartMainActivity();
            }).addOnFailureListener(e -> {
                Log.e("FirebaseSync", "Failed to fetch tasks: " + e.getMessage());
                tasksSynced = true;
                tryStartMainActivity();
            });
        }
    }

    private void tryStartMainActivity() {
        // Wait for progress and tasks to be synchronized before entering MainActivity
        if (tasksSynced && progressSynced) {
            runOnUiThread(() -> {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            });
        }
    }
}