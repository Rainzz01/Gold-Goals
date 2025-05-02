package com.example.goalandgoals.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo")
public class ToDoModel {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "task")
    private String task;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "status")
    private int status;

    @ColumnInfo(name = "difficulty")
    private String difficulty;

    @ColumnInfo(name = "start_time")
    private String startTime;

    @ColumnInfo(name = "deadline")
    private String deadline;

    @ColumnInfo(name = "exp_reward")
    private int expReward;

    @ColumnInfo(name = "coin_reward")
    private int coinReward;

    @ColumnInfo(name = "exp_penalty")
    private int expPenalty;

    @ColumnInfo(name = "coin_penalty")
    private int coinPenalty;

    @ColumnInfo(name = "reminder_time")
    private String reminderTime;

    @ColumnInfo(name = "task_type")
    private String taskType = "Normal";

    @ColumnInfo(name = "repeat_count")
    private int repeatCount = 1;

    private String firebaseKey; // Added to store Firebase reference key

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getExpReward() {
        return expReward;
    }

    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public void setCoinReward(int coinReward) {
        this.coinReward = coinReward;
    }

    public int getExpPenalty() {
        return expPenalty;
    }

    public void setExpPenalty(int expPenalty) {
        this.expPenalty = expPenalty;
    }

    public int getCoinPenalty() {
        return coinPenalty;
    }

    public void setCoinPenalty(int coinPenalty) {
        this.coinPenalty = coinPenalty;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}