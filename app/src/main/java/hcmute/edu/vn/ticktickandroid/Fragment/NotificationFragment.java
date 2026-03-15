package hcmute.edu.vn.ticktickandroid.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.ticktickandroid.Adapter.NotificationAdapter;
import hcmute.edu.vn.ticktickandroid.Database.AppDatabase;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationDao;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationEntity;
import hcmute.edu.vn.ticktickandroid.R;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private LinearLayout emptyState;
    private NotificationAdapter adapter;
    private NotificationDao notificationDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
        emptyState = view.findViewById(R.id.empty_notification_state);

        notificationDao = AppDatabase.getInstance(requireContext()).notificationDao();

        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
        notificationDao.markAllAsRead();
    }

    private void loadNotifications() {
        List<NotificationEntity> notifications = notificationDao.getAll();
        adapter.updateData(notifications);

        if (notifications.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}
