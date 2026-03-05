package hcmute.edu.vn.ticktickandroid.Task;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import hcmute.edu.vn.ticktickandroid.Category.Category;

@Entity(
    tableName = "tasks",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "categoryId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("categoryId")
)
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private int categoryId;
    private boolean completed;
    private long createdAt;
    private long dueDate;

    public TaskEntity(String title, int categoryId) {
        this.title = title;
        this.categoryId = categoryId;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.dueDate = 0;
    }

    @Ignore
    public TaskEntity(String title, int categoryId, long dueDate) {
        this.title = title;
        this.categoryId = categoryId;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.dueDate = dueDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
}
