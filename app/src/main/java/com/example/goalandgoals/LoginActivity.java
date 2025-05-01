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
                                    // Clear the local room
                                    new Thread(() -> {
                                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                        db.userProgressDao().deleteAll();
                                        db.toDoDao().deleteAllTasks(); // make sure ToDoDao have deleteAllTasks()
                                    }).start();
                                    // login success, redirect to MainActivity
                                    Toast.makeText(LoginActivity.this, "Login Successful！", Toast.LENGTH_SHORT).show();
                                    syncUserProgressFromFirebase(); //  sync progress
                                    syncTasksFromFirebase();         //  sync tasks
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish(); // end current LoginActivity
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Error：" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });


        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // redirect to register page
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
                    Log.d("FirebaseSync", "No progress found for this user.");
                }
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
                        ToDoModel task = taskSnap.getValue(ToDoModel.class);
                        if (task != null) {
                            taskList.add(task);
                        }
                    }
                    AsyncTask.execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        for (ToDoModel task : taskList) {
                            db.toDoDao().insertTask(task);
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
