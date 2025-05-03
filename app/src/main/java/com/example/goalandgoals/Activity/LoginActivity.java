package com.example.goalandgoals.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.goalandgoals.R;
import com.example.goalandgoals.Utils.FirebaseSyncUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton, goToRegisterButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);
        progressBar = findViewById(R.id.progressBar);

        // Load saved credentials
        loadCredentials();

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Save credentials if "Remember Me" is checked
                                if (rememberMeCheckBox.isChecked()) {
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("email", email);
                                    editor.putString("password", password);
                                    editor.putBoolean("remember", true);
                                    editor.apply();
                                } else {
                                    // Clear saved credentials
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove("email");
                                    editor.remove("password");
                                    editor.putBoolean("remember", false);
                                    editor.apply();
                                }

                                // Sync tasks from Firebase
                                FirebaseSyncUtils.syncTasksFromFirebase(LoginActivity.this, new FirebaseSyncUtils.SyncCallback() {
                                    // In LoginActivity.java, onSyncComplete can be simplified to:
                                    @Override
                                    public void onSyncComplete(boolean success) {
                                        progressBar.setVisibility(View.GONE);
                                        loginButton.setEnabled(true);
                                        if (success) {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to sync tasks. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loadCredentials() {
        boolean remember = prefs.getBoolean("remember", false);
        if (remember) {
            String email = prefs.getString("email", "");
            String password = prefs.getString("password", "");
            emailEditText.setText(email);
            passwordEditText.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressBar.setVisibility(View.VISIBLE);
            // Sync tasks from Firebase for auto-login
            FirebaseSyncUtils.syncTasksFromFirebase(LoginActivity.this, new FirebaseSyncUtils.SyncCallback() {
                @Override
                public void onSyncComplete(boolean success) {
                    // Run UI updates on the main thread
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (success) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to sync tasks. Please log in again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}