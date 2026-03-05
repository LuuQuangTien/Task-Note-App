package hcmute.edu.vn.ticktickandroid.Dialog;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.Category.CategoryDao;
import hcmute.edu.vn.ticktickandroid.R;

public class CategoryDialogHelper {

    public static void showAddDialog(Activity activity, CategoryDao categoryDao, Runnable onSuccess) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.et_category_name);

        new AlertDialog.Builder(activity, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        categoryDao.insert(new Category(name));
                        onSuccess.run();
                        Toast.makeText(activity, "Đã thêm danh mục \"" + name + "\"", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public static void showEditDialog(Activity activity, Category category, CategoryDao categoryDao, Runnable onSuccess) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText etName = dialogView.findViewById(R.id.et_category_name);
        etName.setText(category.getName());
        etName.setSelection(etName.getText().length());

        new AlertDialog.Builder(activity, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setTitle("Sửa danh mục")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        category.setName(name);
                        categoryDao.update(category);
                        onSuccess.run();
                        Toast.makeText(activity, "Đã cập nhật danh mục", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public static void confirmDelete(Activity activity, Category category, CategoryDao categoryDao, Runnable onSuccess) {
        new AlertDialog.Builder(activity)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc muốn xóa \"" + category.getName() + "\"?\nTất cả nhiệm vụ trong danh mục này cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (d, w) -> {
                    categoryDao.delete(category);
                    onSuccess.run();
                    Toast.makeText(activity, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
