package com.example.goalandgoals;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Utils.AppDatabase;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private final LiveData<List<ToDoModel>> tasks;
    private final AppDatabase db;

    public TaskViewModel(Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        tasks = db.toDoDao().getAllTasksLiveData();
    }

    public LiveData<List<ToDoModel>> getTasks() {
        return tasks;
    }
}