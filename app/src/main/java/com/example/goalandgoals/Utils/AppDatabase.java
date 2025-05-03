package com.example.goalandgoals.Utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.goalandgoals.Dao.ToDoDao;
import com.example.goalandgoals.Dao.UserProgressDao;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;

@Database(entities = {ToDoModel.class, UserProgress.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ToDoDao toDoDao();
    public abstract UserProgressDao userProgressDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "goal_and_goals_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Previous migration (if any)
            database.execSQL("ALTER TABLE todo ADD COLUMN start_time TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE todo ADD COLUMN deadline TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE todo ADD COLUMN exp_reward INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE todo ADD COLUMN coin_reward INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE todo ADD COLUMN exp_penalty INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE todo ADD COLUMN coin_penalty INTEGER DEFAULT 0");
            database.execSQL("ALTER TABLE todo ADD COLUMN reminder_time TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE todo ADD COLUMN task_type TEXT DEFAULT NULL");
            database.execSQL("ALTER TABLE todo ADD COLUMN repeat_count INTEGER DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Empty migration to force schema regeneration
        }
    };
}