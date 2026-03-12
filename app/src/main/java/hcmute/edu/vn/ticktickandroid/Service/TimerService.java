package hcmute.edu.vn.ticktickandroid.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class TimerService extends Service {

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Keeps the service explicitly started and running in the background
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
}
