package hcmute.edu.vn.ticktickandroid.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.ticktickandroid.Notification.NotificationEntity;
import hcmute.edu.vn.ticktickandroid.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationEntity> notifications;

    public NotificationAdapter(List<NotificationEntity> notifications) {
        this.notifications = notifications;
    }

    public void updateData(List<NotificationEntity> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationEntity notif = notifications.get(position);

        holder.tvCategory.setText(notif.getCategoryName() != null ? notif.getCategoryName() : "—");
        holder.tvTask.setText(notif.getTaskTitle());
        holder.tvTimeLeft.setText(notif.getMessage());

        holder.dotUnread.setVisibility(notif.isRead() ? View.GONE : View.VISIBLE);

        if (!notif.isRead()) {
            holder.ivIcon.setImageResource(R.drawable.ic_notification_on);
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_notification_off);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCategory, tvTask, tvTimeLeft;
        View dotUnread;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notif_icon);
            tvCategory = itemView.findViewById(R.id.tv_notif_category);
            tvTask = itemView.findViewById(R.id.tv_notif_task);
            tvTimeLeft = itemView.findViewById(R.id.tv_notif_time_left);
            dotUnread = itemView.findViewById(R.id.dot_unread);
        }
    }
}
