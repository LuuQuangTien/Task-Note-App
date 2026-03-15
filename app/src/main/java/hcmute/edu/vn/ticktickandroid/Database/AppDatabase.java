package hcmute.edu.vn.ticktickandroid.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.Category.CategoryDao;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationDao;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationEntity;
import hcmute.edu.vn.ticktickandroid.Task.TaskEntity;
import hcmute.edu.vn.ticktickandroid.Task.TaskDao;

@Database(entities = {Category.class, TaskEntity.class, NotificationEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract CategoryDao categoryDao();
    public abstract TaskDao taskDao();
    public abstract NotificationDao notificationDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tasks ADD COLUMN dueDate INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "taskId INTEGER NOT NULL, " +
                    "taskTitle TEXT, " +
                    "categoryName TEXT, " +
                    "message TEXT, " +
                    "createdAt INTEGER NOT NULL, " +
                    "isRead INTEGER NOT NULL DEFAULT 0)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ticktick_db"
                    ).allowMainThreadQueries()
                     .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                     .build();

                    if (INSTANCE.categoryDao().getCount() == 0) {
                        String[] defaults = {"Personal", "Work", "Shopping", "Learning", "Fitness", "Wish List"};
                        for (String name : defaults) {
                            INSTANCE.categoryDao().insert(new Category(name));
                        }
                    }
                }
            }
        }
        return INSTANCE;
    }
}
