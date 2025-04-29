package com.example.goalandgoals.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.goalandgoals.Model.ToDoModel;

import java.util.List;

@Dao
public interface ToDoDao {

    @Insert
    void insertTask(ToDoModel task);

    @Delete
    void deleteTask(ToDoModel task);

    @Query("SELECT * FROM todo")
    List<ToDoModel> getAllTasks();

    @Query("SELECT * FROM todo")
    LiveData<List<ToDoModel>> getAllTasksLiveData();

    @Query("UPDATE todo SET status = :status WHERE id = :id")
    void updateStatus(int id, int status);

    @Query("UPDATE todo SET task = :task, description = :description, difficulty = :difficulty, start_time = :startTime, deadline = :deadline, exp_reward = :expReward, coin_reward = :coinReward, exp_penalty = :expPenalty, coin_penalty = :coinPenalty, reminder_time = :reminderTime, task_type = :taskType, repeat_count = :repeatCount WHERE id = :id")
    void updateTask(int id, String task, String description, String difficulty, String startTime, String deadline, int expReward, int coinReward, int expPenalty, int coinPenalty, String reminderTime, String taskType, int repeatCount);

    @Query("DELETE FROM todo")
    void deleteAllTasks();

}