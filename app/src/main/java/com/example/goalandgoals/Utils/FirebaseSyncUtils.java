package com.example.goalandgoals.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.goalandgoals.Model.ToDoModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseSyncUtils {

    public interface SyncCallback {
        void onSyncComplete(boolean success);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void syncTasksFromFirebase(Context context, SyncCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference tasksRef = database.getReference("users").child(uid).child("tasks");
            tasksRef.get().addOnSuccessListener(snapshot -> {
                AppDatabase db = AppDatabase.getInstance(context);
                executor.execute(() -> {
                    try {
                        db.toDoDao().deleteAllTasksForUser(uid);
                        for (DataSnapshot taskSnap : snapshot.getChildren()) {
                            ToDoModel firebaseTask = taskSnap.getValue(ToDoModel.class);
                            if (firebaseTask != null) {
                                ToDoModel task = new ToDoModel(
                                        uid,
                                        firebaseTask.getTask(),
                                        firebaseTask.getDescription(),
                                        firebaseTask.getStatus(),
                                        firebaseTask.getDifficulty(),
                                        firebaseTask.getStartTime(),
                                        firebaseTask.getDeadline(),
                                        firebaseTask.getExpReward(),
                                        firebaseTask.getCoinReward(),
                                        firebaseTask.getExpPenalty(),
                                        firebaseTask.getCoinPenalty(),
                                        firebaseTask.getReminderTime(),
                                        firebaseTask.getTaskType(),
                                        firebaseTask.getRepeatCount(),
                                        taskSnap.getKey()
                                );
                                db.toDoDao().insertTask(task);
                            }
                        }
                        Log.d("FirebaseSync", "Tasks synced successfully for user: " + uid);
                        mainHandler.post(() -> {
                            if (callback != null) {
                                callback.onSyncComplete(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FirebaseSync", "Error during sync: " + e.getMessage(), e);
                        mainHandler.post(() -> {
                            if (callback != null) {
                                callback.onSyncComplete(false);
                            }
                        });
                    }
                });
            }).addOnFailureListener(e -> {
                Log.e("FirebaseSync", "Failed to fetch tasks from Firebase: " + e.getMessage());
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSyncComplete(false);
                    }
                });
            });
        } else {
            Log.e("FirebaseSync", "No authenticated user found.");
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onSyncComplete(false);
                }
            });
        }
    }
}