package com.example.goalandgoals.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.firebase.database.PropertyName;

@Entity(tableName = "todo")
public class ToDoModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userId;
    private String task;
    private String description;
    private int status;
    private String difficulty;
    @ColumnInfo(name = "start_time")
    private String startTime;
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
    private String taskType;
    @ColumnInfo(name = "repeat_count")
    private int repeatCount;
    private String firebaseKey;

    // Constructor and getters/setters (as provided earlier)
    public ToDoModel() {
    }

    @Ignore
    public ToDoModel(String userId, String task, String description, int status, String difficulty,
                     String startTime, String deadline, int expReward, int coinReward,
                     int expPenalty, int coinPenalty, String reminderTime, String taskType,
                     int repeatCount, String firebaseKey) {
        this.userId = userId;
        this.task = task;
        this.description = description;
        this.status = status;
        this.difficulty = difficulty;
        this.startTime = startTime;
        this.deadline = deadline;
        this.expReward = expReward;
        this.coinReward = coinReward;
        this.expPenalty = expPenalty;
        this.coinPenalty = coinPenalty;
        this.reminderTime = reminderTime;
        this.taskType = taskType;
        this.repeatCount = repeatCount;
        this.firebaseKey = firebaseKey;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @PropertyName("userId")
    public String getUserId() { return userId; }
    @PropertyName("userId")
    public void setUserId(String userId) { this.userId = userId; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public int getExpReward() { return expReward; }
    public void setExpReward(int expReward) { this.expReward = expReward; }

    public int getCoinReward() { return coinReward; }
    public void setCoinReward(int coinReward) { this.coinReward = coinReward; }

    public int getExpPenalty() { return expPenalty; }
    public void setExpPenalty(int expPenalty) { this.expPenalty = expPenalty; }

    public int getCoinPenalty() { return coinPenalty; }
    public void setCoinPenalty(int coinPenalty) { this.coinPenalty = coinPenalty; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}