package com.example.goalandgoals.Model;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.goalandgoals.Utils.AppDatabase;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private LiveData<List<ToDoModel>> tasks;
    private final AppDatabase db;

    public TaskViewModel(Application application, String userId) {
        super(application);
        db = AppDatabase.getInstance(application);
        tasks = db.toDoDao().getAllTasksLiveData(userId);
    }

    public LiveData<List<ToDoModel>> getTasks() {
        return tasks;
    }

    // Method to update userId and refresh tasks
    public void setUserId(String userId) {
        tasks = db.toDoDao().getAllTasksLiveData(userId);
    }
}