package com.example.goalandgoals.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goalandgoals.R;
import com.example.goalandgoals.Utils.AppDatabase;
import com.example.goalandgoals.Utils.FirebaseSyncUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmailEditText, registerPasswordEditText;
    private Button registerButton, backToLoginButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize components
        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = AppDatabase.getInstance(this);

        registerButton.setOnClickListener(view -> {
            String email = registerEmailEditText.getText().toString().trim();
            String password = registerPasswordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "The email and password shouldn't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);

            // Use Firebase register
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Registration successful
                                String uid = mAuth.getCurrentUser().getUid();
                                Log.d("Register", "New user UID: " + uid);

                                // Clear all local tasks and user progress
                                AsyncTask.execute(() -> {
                                    db.toDoDao().deleteAllTasksForUser(""); // Clear all tasks
                                    db.userProgressDao().deleteAll(); // Clear all user progress
                                });

                                // Sync tasks from Firebase (likely empty for new users)
                                FirebaseSyncUtils.syncTasksFromFirebase(RegisterActivity.this, new FirebaseSyncUtils.SyncCallback() {
                                    @Override
                                    public void onSyncComplete(boolean success) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            registerButton.setEnabled(true);
                                            if (success) {
                                                Toast.makeText(RegisterActivity.this, "Register successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Failed to sync tasks. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                registerButton.setEnabled(true);
                                // Registration failed
                                Toast.makeText(RegisterActivity.this, "Register fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        backToLoginButton.setOnClickListener(v -> finish());
    }
}