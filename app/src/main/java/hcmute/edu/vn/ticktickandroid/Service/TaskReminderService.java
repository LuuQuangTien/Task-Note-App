package hcmute.edu.vn.ticktickandroid.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.Database.AppDatabase;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationDao;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationEntity;
import hcmute.edu.vn.ticktickandroid.R;
import hcmute.edu.vn.ticktickandroid.Task.TaskEntity;

public class TaskReminderService extends Service {
    private static final String CHANNEL_ID = "TaskReminderChannel";
    private final IBinder binder = new LocalBinder();
    private Handler handler;
    private Runnable checkDeadlineRunnable;
    private static final long CHECK_INTERVAL = 30 * 1000;

    private final Set<String> sentNotifications = new HashSet<>();

    private OnNotificationListener notificationListener;

    public interface OnNotificationListener {
        void onNewNotification();
    }

    public void setNotificationListener(OnNotificationListener listener) {
        this.notificationListener = listener;
    }

    private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000L;
    private static final long ONE_HOUR_MS = 60 * 60 * 1000L;
    private static final long THIRTY_MIN_MS = 30 * 60 * 1000L;
    private static final long ONE_MIN_MS = 60 * 1000L;

    private static final long TOLERANCE_LARGE = 5 * 60 * 1000L;
    private static final long TOLERANCE_SMALL = 60 * 1000L;
    private static final long TOLERANCE_TINY = 30 * 1000L;

    public class LocalBinder extends Binder {
        public TaskReminderService getService() {
            return TaskReminderService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startCheckingDeadlines();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void startCheckingDeadlines() {
        handler = new Handler(Looper.getMainLooper());
        checkDeadlineRunnable = new Runnable() {
            @Override
            public void run() {
                checkTasksForDeadline();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(checkDeadlineRunnable);
    }

    private void checkTasksForDeadline() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<TaskEntity> tasks = db.taskDao().getAll();
            List<Category> categories = db.categoryDao().getAll();
            java.util.Map<Integer, String> catMap = new java.util.HashMap<>();
            for (Category c : categories) {
                catMap.put(c.getId(), c.getName());
            }
            long now = System.currentTimeMillis();
            boolean hasNew = false;

            for (TaskEntity task : tasks) {
                if (task.isCompleted() || task.getDueDate() <= 0 || task.getDueDate() <= now) {
                    continue;
                }

                long timeLeft = task.getDueDate() - now;
                String catName = catMap.getOrDefault(task.getCategoryId(), "Unknown");

                if (checkInterval(db, task, catName, timeLeft, ONE_DAY_MS, TOLERANCE_LARGE, "1_day",
                        "Còn 1 ngày")) {
                    hasNew = true;
                }
                if (checkInterval(db, task, catName, timeLeft, ONE_HOUR_MS, TOLERANCE_SMALL, "1_hour",
                        "Còn 1 giờ")) {
                    hasNew = true;
                }
                if (checkInterval(db, task, catName, timeLeft, THIRTY_MIN_MS, TOLERANCE_SMALL, "30_min",
                        "Còn 30 phút")) {
                    hasNew = true;
                }
                if (checkInterval(db, task, catName, timeLeft, ONE_MIN_MS, TOLERANCE_TINY, "1_min",
                        "Còn 1 phút")) {
                    hasNew = true;
                }
            }

            if (hasNew && notificationListener != null) {
                new Handler(Looper.getMainLooper()).post(() -> notificationListener.onNewNotification());
            }
        }).start();
    }

    private boolean checkInterval(AppDatabase db, TaskEntity task, String categoryName, long timeLeft,
                                   long intervalMs, long toleranceMs, String intervalKey, String message) {
        String key = task.getId() + "_" + intervalKey;
        if (sentNotifications.contains(key)) return false;

        if (Math.abs(timeLeft - intervalMs) <= toleranceMs) {
            sentNotifications.add(key);
            showNotification(task, message);
            saveNotificationToDb(db, task, categoryName, message);
            return true;
        }
        return false;
    }

    private void showNotification(TaskEntity task, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_on)
                .setContentTitle(task.getTitle())
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        int notifId = (task.getId() + "_" + message).hashCode();
        notificationManager.notify(notifId, builder.build());
    }

    private void saveNotificationToDb(AppDatabase db, TaskEntity task, String categoryName, String message) {
        NotificationDao dao = db.notificationDao();
        NotificationEntity entity = new NotificationEntity(task.getId(), task.getTitle(), categoryName, message);
        dao.insert(entity);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkDeadlineRunnable);
        }
    }
}
