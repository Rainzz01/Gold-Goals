package com.example.rpgtodolist; // 改成你的包名

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface TaskDao {

    // 插入新任务
    @Insert
    void insert(Task task);

    // 更新任务
    @Update
    void update(Task task);

    // 删除任务
    @Delete
    void delete(Task task);

    // 查询所有任务
    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    // 根据完成状态查询
    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted")
    List<Task> getTasksByCompletion(boolean isCompleted);
}
