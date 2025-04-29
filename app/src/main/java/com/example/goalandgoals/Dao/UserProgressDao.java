package com.example.goalandgoals.Dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.goalandgoals.Model.UserProgress;

@Dao
public interface UserProgressDao {

    @Insert
    void insert(UserProgress userProgress);

    @Update
    void update(UserProgress userProgress);

    @Query("SELECT * FROM user_progress WHERE id = :id LIMIT 1")
    UserProgress getUserProgressById(int id);

    @Query("DELETE FROM user_progress")
    void deleteAll();  // 可选：比如重置玩家数据
}
