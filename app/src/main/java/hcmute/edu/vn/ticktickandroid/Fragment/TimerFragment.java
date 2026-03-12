package hcmute.edu.vn.ticktickandroid.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import hcmute.edu.vn.ticktickandroid.R;
import hcmute.edu.vn.ticktickandroid.Service.TimerService;

public class TimerFragment extends Fragment {

    private TextView tvTimer;
    private MaterialButton btnStartPause;
    private MaterialButton btnReset;

    private TimerService timerService;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            isBound = true;
            timerService.setListener(new TimerService.TimerTickListener() {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimerText(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    updateTimerText(0);
                    updateButtons();
                }
            });

            updateTimerText(timerService.getTimeRemainingMillis());
            updateButtons();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvTimer = view.findViewById(R.id.tv_timer);
        btnStartPause = view.findViewById(R.id.btn_start_pause);
        btnReset = view.findViewById(R.id.btn_reset);

        btnStartPause.setOnClickListener(v -> {
            if (isBound && timerService != null) {
                if (timerService.isTimerRunning()) {
                    timerService.pauseTimer();
                } else {
                    timerService.startTimer();
                }
                updateButtons();
            }
        });

        btnReset.setOnClickListener(v -> {
            if (isBound && timerService != null) {
                timerService.resetTimer();
                updateButtons();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), TimerService.class);
        // Start the service so it keeps running even if we unbind
        requireActivity().startService(intent);
        // Bind to get the interface
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            if (timerService != null) {
                timerService.setListener(null);
            }
            requireActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void updateTimerText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (tvTimer != null) {
                    tvTimer.setText(timeLeftFormatted);
                }
            });
        }
    }

    private void updateButtons() {
        if (getActivity() != null && isBound && timerService != null) {
            getActivity().runOnUiThread(() -> {
                if (btnStartPause != null) {
                    if (timerService.isTimerRunning()) {
                        btnStartPause.setText("Pause");
                    } else {
                        btnStartPause.setText("Start");
                    }
                }
            });
        }
    }
}
