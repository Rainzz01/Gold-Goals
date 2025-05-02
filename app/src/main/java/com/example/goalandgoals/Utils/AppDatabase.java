package com.example.goalandgoals.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.goalandgoals.Dao.ToDoDao;
import com.example.goalandgoals.Dao.UserProgressDao;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;

@Database(entities = {ToDoModel.class, UserProgress.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ToDoDao toDoDao();
    public abstract UserProgressDao userProgressDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "goalandgoals.db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add the firebaseKey column to the todo table, allowing null values
            database.execSQL("ALTER TABLE todo ADD COLUMN firebaseKey TEXT");
        }
    };
}