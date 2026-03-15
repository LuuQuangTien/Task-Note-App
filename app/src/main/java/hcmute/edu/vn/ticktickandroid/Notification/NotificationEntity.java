package hcmute.edu.vn.ticktickandroid.Notification;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int taskId;
    private String taskTitle;
    private String categoryName;
    private String message;
    private long createdAt;
    private boolean isRead;

    public NotificationEntity(int taskId, String taskTitle, String categoryName, String message) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.categoryName = categoryName;
        this.message = message;
        this.createdAt = System.currentTimeMillis();
        this.isRead = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
