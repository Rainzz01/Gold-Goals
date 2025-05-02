package com.example.gamification.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "challenges")
public class Challenge {
    @PrimaryKey(autoGenerate = true)
    public int challengeId;

    public String title;

    public String type; // "Daily" or "Weekly"

    public int requiredTasks;

    public int progress;

    public boolean isCompleted;

    public Challenge(String title, String type, int requiredTasks, int progress, boolean isCompleted) {
        this.title = title;
        this.type = type;
        this.requiredTasks = requiredTasks;
        this.progress = progress;
        this.isCompleted = isCompleted;
    }
}
