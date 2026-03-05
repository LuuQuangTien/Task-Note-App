package hcmute.edu.vn.ticktickandroid.Category;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<Category> getAll();

    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT COUNT(*) FROM categories")
    int getCount();
}
