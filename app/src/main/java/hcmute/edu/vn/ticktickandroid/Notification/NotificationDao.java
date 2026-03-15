package hcmute.edu.vn.ticktickandroid.Notification;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    List<NotificationEntity> getAll();

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY createdAt DESC")
    List<NotificationEntity> getUnread();

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    int getUnreadCount();

    @Insert
    long insert(NotificationEntity notification);

    @Delete
    void delete(NotificationEntity notification);

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(int id);

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    void markAllAsRead();
}
