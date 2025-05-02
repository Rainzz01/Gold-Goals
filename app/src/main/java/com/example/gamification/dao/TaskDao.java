package com.example.gamification.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gamification.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    LiveData<List<Task>> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    LiveData<List<Task>> getIncompleteTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND date(deadline / 1000, 'unixepoch') = date('now')")
    LiveData<List<Task>> getTasksCompletedToday();
}
