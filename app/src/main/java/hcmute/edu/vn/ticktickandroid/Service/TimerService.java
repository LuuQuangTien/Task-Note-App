package hcmute.edu.vn.ticktickandroid.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import hcmute.edu.vn.ticktickandroid.R;

public class TimerService extends Service {

    private static final String CHANNEL_ID = "TimerChannel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new LocalBinder();
    private CountDownTimer countDownTimer;
    
    private long timeRemainingMillis = 30 * 60 * 1000;
    private long initialTimeMillis = 30 * 60 * 1000;
    private boolean isTimerRunning = false;
    
    private TimerTickListener listener;

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public interface TimerTickListener {
        void onTick(long millisUntilFinished);
        void onFinish();
    }

    public void setListener(TimerTickListener listener) {
        this.listener = listener;
        if (this.listener != null) {
            this.listener.onTick(timeRemainingMillis);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.listener = null;
        return super.onUnbind(intent);
    }

    public void startTimer() {
        if (isTimerRunning) return;

        countDownTimer = new CountDownTimer(timeRemainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMillis = millisUntilFinished;
                if (listener != null) {
                    listener.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeRemainingMillis = 0;
                sendFinishNotification();
                if (listener != null) {
                    listener.onFinish();
                }
            }
        }.start();
        
        isTimerRunning = true;
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
    }

    public void resetTimer() {
        pauseTimer();
        timeRemainingMillis = initialTimeMillis;
        if (listener != null) {
            listener.onTick(timeRemainingMillis);
        }
    }

    public void setInitialTime(long millis) {
        this.initialTimeMillis = millis;
        this.timeRemainingMillis = millis;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public long getTimeRemainingMillis() {
        return timeRemainingMillis;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void sendFinishNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Kết thúc thời gian")
                .setContentText("Timer đã kết thúc.")
                .setSmallIcon(R.drawable.ic_timer)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }
}
