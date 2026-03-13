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

import java.util.List;

import hcmute.edu.vn.ticktickandroid.Database.AppDatabase;
import hcmute.edu.vn.ticktickandroid.R;
import hcmute.edu.vn.ticktickandroid.Task.TaskEntity;

public class TaskReminderService extends Service {
    private static final String CHANNEL_ID = "TaskReminderChannel";
    private final IBinder binder = new LocalBinder();
    private Handler handler;
    private Runnable checkDeadlineRunnable;
    private static final long CHECK_INTERVAL = 60 * 1000; // Kiểm tra mỗi phút

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
            long currentTime = System.currentTimeMillis();
            long oneHourFromNow = currentTime + (60 * 60 * 1000);

            for (TaskEntity task : tasks) {
                // Điều kiện: Chưa hoàn thành, deadline lớn hơn hiện tại và trong vòng 1 giờ tới
                if (!task.isCompleted() && task.getDueDate() > currentTime && task.getDueDate() <= oneHourFromNow) {
                    showNotification(task);
                }
            }
        }).start();
    }

    private void showNotification(TaskEntity task) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle) 
                .setContentTitle("Sắp đến hạn: " + task.getTitle())
                .setContentText("Task này sẽ hết hạn trong vòng chưa đầy 1 tiếng!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(task.getId(), builder.build());
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
