package com.example.rpgtodolist;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import androidx.room.Room;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate executed!");

        // 初始化 Room 数据库
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task_database")
                .allowMainThreadQueries() //
                .build();

        // 初始化 Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = database.getReference("tasks");

        // 示例：添加任务和用户进度
        addSampleData();

        // 示例：显示所有任务
        displayTasks();

        // 示例：获取用户进度
        getUserProgress();

        // 测试写一笔简单数据到 Firebase
        databaseReference.child("testUser").setValue("Hello Firebase!")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MainActivity", "Test data uploaded successfully!");
                    } else {
                        Log.d("MainActivity", "Test data upload failed.", task.getException());
                    }
                });

        // 同步任务到 Firebase
        syncTasksToFirebase();
    }

    private void addSampleData() {
        Task task = new Task("Sample Task", "This is a description.", 3, false);
        db.taskDao().insert(task);

        UserProgress userProgress = new UserProgress(100, 50);
        db.userProgressDao().insert(userProgress);
    }

    private void displayTasks() {
        List<Task> tasks = db.taskDao().getAllTasks();
        for (Task task : tasks) {
            Log.d("MainActivity", "Task: " + task.getTitle() + ", Completed: " + task.isCompleted());
        }
    }

    private void getUserProgress() {
        UserProgress progress = db.userProgressDao().getUserProgressById(1);
        if (progress != null) {
            Log.d("MainActivity", "User Progress: XP = " + progress.getXp() + ", Coins = " + progress.getCoins());
        } else {
            Log.d("MainActivity", "No user progress found.");
        }
    }

    private void syncTasksToFirebase() {
        List<Task> allTasks = db.taskDao().getAllTasks();

        for (Task task : allTasks) {
            String taskId = String.valueOf(task.getId());

            // 检查任务是否已存在于 Firebase 中
            databaseReference.child(taskId).get().addOnCompleteListener(taskSnapshot -> {
                if (taskSnapshot.isSuccessful()) {
                    if (!taskSnapshot.getResult().exists()) {
                        // 如果任务不存在，则上传任务
                        databaseReference.child(taskId).setValue(task)
                                .addOnCompleteListener(uploadTask -> {
                                    if (uploadTask.isSuccessful()) {
                                        Log.d("MainActivity", "Task " + taskId + " uploaded successfully!");
                                    } else {
                                        Log.d("MainActivity", "Task " + taskId + " upload failed.", uploadTask.getException());
                                    }
                                });
                    } else {
                        Log.d("MainActivity", "Task " + taskId + " already exists in Firebase.");
                    }
                } else {
                    Log.d("MainActivity", "Error checking task " + taskId + ": " + taskSnapshot.getException().getMessage());
                }
            });
        }
    }



}
