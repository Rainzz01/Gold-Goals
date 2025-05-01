package com.example.goalandgoals.Utils;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.goalandgoals.Dao.ToDoDao;
import com.example.goalandgoals.Dao.UserProgressDao;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;

@Database(entities = {ToDoModel.class, UserProgress.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ToDoDao toDoDao();
    public abstract UserProgressDao userProgressDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "toDoListDatabase"
                    ).allowMainThreadQueries() // for test use
                    .fallbackToDestructiveMigration() // clear when updated
                    .build();
        }
        return instance;
    }
}