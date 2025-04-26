package com.example.rpgtodolist;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class, UserProgress.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract UserProgressDao userProgressDao();

}

