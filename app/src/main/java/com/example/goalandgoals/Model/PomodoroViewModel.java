package com.example.goalandgoals.Model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import com.example.goalandgoals.Utils.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PomodoroViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final MutableLiveData<Long> timeLeftInMillis = new MutableLiveData<>();
    private final MutableLiveData<Boolean> timerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<TimerPhase> currentPhase = new MutableLiveData<>(TimerPhase.WORK);
    private final MutableLiveData<ToDoModel> selectedTask = new MutableLiveData<>();
    private final MutableLiveData<Long> customWorkDuration = new MutableLiveData<>();
    private final MutableLiveData<Long> customBreakDuration = new MutableLiveData<>(5 * 60 * 1000L);
    private final MutableLiveData<Integer> pomodoroCycleCount = new MutableLiveData<>(0);

    public enum TimerPhase { WORK, BREAK, LONG_BREAK }

    public PomodoroViewModel(Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<Long> getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public LiveData<Boolean> getTimerRunning() {
        return timerRunning;
    }

    public LiveData<TimerPhase> getCurrentPhase() {
        return currentPhase;
    }

    public LiveData<ToDoModel> getSelectedTask() {
        return selectedTask;
    }

    public LiveData<Long> getCustomWorkDuration() {
        return customWorkDuration;
    }

    public LiveData<Long> getCustomBreakDuration() {
        return customBreakDuration;
    }

    public LiveData<Integer> getPomodoroCycleCount() {
        return pomodoroCycleCount;
    }

    public void setTimeLeftInMillis(long time) {
        timeLeftInMillis.setValue(time);
    }

    public void setTimerRunning(boolean running) {
        timerRunning.setValue(running);
    }

    public void setCurrentPhase(TimerPhase phase) {
        currentPhase.setValue(phase);
    }

    public void setSelectedTask(ToDoModel task) {
        selectedTask.setValue(task);
    }

    public void setCustomWorkDuration(long duration) {
        customWorkDuration.setValue(duration);
    }

    public void setCustomBreakDuration(long duration) {
        customBreakDuration.setValue(duration);
    }

    public void incrementPomodoroCycle() {
        Integer count = pomodoroCycleCount.getValue();
        if (count != null) {
            pomodoroCycleCount.setValue(count + 1);
        }
    }

    public void resetPomodoroCycle() {
        pomodoroCycleCount.setValue(0);
    }

    public void completeTask() {
        ToDoModel task = selectedTask.getValue();
        if (task != null) {
            task.setStatus(1);
            new Thread(() -> {
                db.toDoDao().updateStatus(task.getId(), 1);
                Log.d("PomodoroViewModel", "Task completed: ID=" + task.getId());
                syncTaskToFirebase(task);
            }).start();
        }
    }
    private void syncTaskToFirebase(ToDoModel task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://rpgtodoapp-8e638-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference taskRef = database.getReference("users").child(uid).child("tasks").child(String.valueOf(task.getId()));
            taskRef.setValue(task)
                    .addOnSuccessListener(aVoid -> Log.d("PomodoroViewModel", "Task synced to Firebase: ID=" + task.getId()))
                    .addOnFailureListener(e -> Log.e("PomodoroViewModel", "Failed to sync task: " + e.getMessage()));
        }
    }
}