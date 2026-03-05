package hcmute.edu.vn.ticktickandroid.Task;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    List<TaskEntity> getAll();

    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    List<TaskEntity> getByCategoryId(int categoryId);

    @Insert
    long insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);
}
