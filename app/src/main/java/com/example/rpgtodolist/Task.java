package com.example.rpgtodolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.database.IgnoreExtraProperties;

@Entity(tableName = "tasks")  // 表名 "tasks"
@IgnoreExtraProperties  // Firebase 序列化时，忽略不必要的字段
public class Task {
    @PrimaryKey(autoGenerate = true)  // 自动生成唯一 ID
    private int id;
    private String title;
    private String description;
    private int difficulty;  // 任务难度
    private boolean isCompleted;  // 是否完成

    // 构造方法
    public Task(String title, String description, int difficulty, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.isCompleted = isCompleted;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
