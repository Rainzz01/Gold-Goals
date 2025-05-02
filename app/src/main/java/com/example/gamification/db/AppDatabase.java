package com.example.gamification.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.gamification.dao.TaskDao;
import com.example.gamification.model.Badge;
import com.example.gamification.model.Challenge;
import com.example.gamification.model.DateConverter;
import com.example.gamification.model.Reward;
import com.example.gamification.model.Task;
import com.example.gamification.model.User;

@Database(entities = {Task.class, Reward.class, User.class, Badge.class, Challenge.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract TaskDao taskDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
