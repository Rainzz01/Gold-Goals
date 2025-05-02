package com.example.gamification.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int taskId;

    @NonNull
    public String taskName;

    public String description;

    @NonNull
    public String tier; // Easy, Medium, Hard

    public int xp;
    public int coins;

    public Date deadline;

    public boolean isCompleted;

    public Task(@NonNull String taskName, String description, @NonNull String tier, int xp, int coins, Date deadline, boolean isCompleted) {
        this.taskName = taskName;
        this.description = description;
        this.tier = tier;
        this.xp = xp;
        this.coins = coins;
        this.deadline = deadline;
        this.isCompleted = isCompleted;
    }
}
