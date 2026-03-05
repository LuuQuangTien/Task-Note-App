package hcmute.edu.vn.ticktickandroid.Dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.Category.CategoryDao;
import hcmute.edu.vn.ticktickandroid.R;
import hcmute.edu.vn.ticktickandroid.Task.TaskDao;
import hcmute.edu.vn.ticktickandroid.Task.TaskEntity;

public class TaskDialogHelper {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public static void showAddDialog(Activity activity, TaskDao taskDao, CategoryDao categoryDao,
                                     Category currentFilter, Runnable onSuccess) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        EditText etTitle = dialogView.findViewById(R.id.et_task_title);
        Spinner spinnerGroup = dialogView.findViewById(R.id.spinner_group);
        TextView tvGroupLabel = dialogView.findViewById(R.id.tv_group_label);
        MaterialButton btnDate = dialogView.findViewById(R.id.btn_pick_date);
        TextView tvDatetime = dialogView.findViewById(R.id.tv_selected_datetime);

        EditText etHour = dialogView.findViewById(R.id.et_hour);
        EditText etMinute = dialogView.findViewById(R.id.et_minute);
        EditText etSecond = dialogView.findViewById(R.id.et_second);

        final Calendar selectedCal = Calendar.getInstance();
        final boolean[] dateSet = {false};

        List<Category> categories = categoryDao.getAll();

        if (currentFilter != null) {
            spinnerGroup.setVisibility(View.GONE);
            tvGroupLabel.setVisibility(View.GONE);
        } else {
            String[] categoryNames = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                categoryNames[i] = categories.get(i).getName();
            }
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    activity, android.R.layout.simple_spinner_item, categoryNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGroup.setAdapter(spinnerAdapter);
        }

        setupTimeSpinner(dialogView, etHour, etMinute, etSecond);

        btnDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                selectedCal.set(Calendar.YEAR, year);
                selectedCal.set(Calendar.MONTH, month);
                selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateSet[0] = true;
                btnDate.setText(DATE_FORMAT.format(selectedCal.getTime()));
                updateDatetimeLabel(tvDatetime, selectedCal, true,
                        etHour.getText().toString(), etMinute.getText().toString(), etSecond.getText().toString());
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(activity, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(activity, "Vui lòng nhập tên nhiệm vụ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int catId;
                    if (currentFilter != null) {
                        catId = currentFilter.getId();
                    } else {
                        int selectedIndex = spinnerGroup.getSelectedItemPosition();
                        if (selectedIndex < 0) {
                            Toast.makeText(activity, "Vui lòng chọn nhóm", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        catId = categories.get(selectedIndex).getId();
                    }

                    long dueDate = 0;
                    if (dateSet[0]) {
                        applyTimeToCalendar(selectedCal, etHour, etMinute, etSecond);
                        dueDate = selectedCal.getTimeInMillis();
                    }
                    taskDao.insert(new TaskEntity(title, catId, dueDate));
                    onSuccess.run();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    public static void showEditDialog(Activity activity, TaskEntity task, TaskDao taskDao,
                                      CategoryDao categoryDao, Runnable onSuccess) {
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        EditText etTitle = dialogView.findViewById(R.id.et_task_title);
        Spinner spinnerGroup = dialogView.findViewById(R.id.spinner_group);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        MaterialButton btnDate = dialogView.findViewById(R.id.btn_pick_date);
        TextView tvDatetime = dialogView.findViewById(R.id.tv_selected_datetime);

        EditText etHour = dialogView.findViewById(R.id.et_hour);
        EditText etMinute = dialogView.findViewById(R.id.et_minute);
        EditText etSecond = dialogView.findViewById(R.id.et_second);

        tvDialogTitle.setText("Sửa nhiệm vụ");
        etTitle.setText(task.getTitle());
        etTitle.setSelection(etTitle.getText().length());

        final Calendar selectedCal = Calendar.getInstance();
        final boolean[] dateSet = {task.getDueDate() > 0};

        if (task.getDueDate() > 0) {
            selectedCal.setTimeInMillis(task.getDueDate());
            btnDate.setText(DATE_FORMAT.format(selectedCal.getTime()));
            etHour.setText(String.format(Locale.getDefault(), "%02d", selectedCal.get(Calendar.HOUR_OF_DAY)));
            etMinute.setText(String.format(Locale.getDefault(), "%02d", selectedCal.get(Calendar.MINUTE)));
            etSecond.setText(String.format(Locale.getDefault(), "%02d", selectedCal.get(Calendar.SECOND)));
            updateDatetimeLabel(tvDatetime, selectedCal, true,
                    etHour.getText().toString(), etMinute.getText().toString(), etSecond.getText().toString());
        }

        setupTimeSpinner(dialogView, etHour, etMinute, etSecond);

        List<Category> categories = categoryDao.getAll();
        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                activity, android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(spinnerAdapter);

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == task.getCategoryId()) {
                spinnerGroup.setSelection(i);
                break;
            }
        }

        btnDate.setOnClickListener(v -> {
            int y = selectedCal.get(Calendar.YEAR);
            int m = selectedCal.get(Calendar.MONTH);
            int d = selectedCal.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
                selectedCal.set(Calendar.YEAR, year);
                selectedCal.set(Calendar.MONTH, month);
                selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateSet[0] = true;
                btnDate.setText(DATE_FORMAT.format(selectedCal.getTime()));
                updateDatetimeLabel(tvDatetime, selectedCal, true,
                        etHour.getText().toString(), etMinute.getText().toString(), etSecond.getText().toString());
            }, y, m, d).show();
        });

        new AlertDialog.Builder(activity, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    int selectedIndex = spinnerGroup.getSelectedItemPosition();
                    if (!title.isEmpty() && selectedIndex >= 0) {
                        task.setTitle(title);
                        task.setCategoryId(categories.get(selectedIndex).getId());
                        if (dateSet[0]) {
                            applyTimeToCalendar(selectedCal, etHour, etMinute, etSecond);
                            task.setDueDate(selectedCal.getTimeInMillis());
                        } else {
                            task.setDueDate(0);
                        }
                        taskDao.update(task);
                        onSuccess.run();
                    }
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa", (dialog, which) -> {
                    taskDao.delete(task);
                    onSuccess.run();
                    Toast.makeText(activity, "Đã xóa nhiệm vụ", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private static void setupTimeSpinner(View dialogView, EditText etHour, EditText etMinute, EditText etSecond) {
        ImageButton btnHourUp = dialogView.findViewById(R.id.btn_hour_up);
        ImageButton btnHourDown = dialogView.findViewById(R.id.btn_hour_down);
        ImageButton btnMinuteUp = dialogView.findViewById(R.id.btn_minute_up);
        ImageButton btnMinuteDown = dialogView.findViewById(R.id.btn_minute_down);
        ImageButton btnSecondUp = dialogView.findViewById(R.id.btn_second_up);
        ImageButton btnSecondDown = dialogView.findViewById(R.id.btn_second_down);

        btnHourUp.setOnClickListener(v -> adjustValue(etHour, 1, 0, 23));
        btnHourDown.setOnClickListener(v -> adjustValue(etHour, -1, 0, 23));
        btnMinuteUp.setOnClickListener(v -> adjustValue(etMinute, 1, 0, 59));
        btnMinuteDown.setOnClickListener(v -> adjustValue(etMinute, -1, 0, 59));
        btnSecondUp.setOnClickListener(v -> adjustValue(etSecond, 1, 0, 59));
        btnSecondDown.setOnClickListener(v -> adjustValue(etSecond, -1, 0, 59));

        addClampWatcher(etHour, 0, 23);
        addClampWatcher(etMinute, 0, 59);
        addClampWatcher(etSecond, 0, 59);
    }

    private static void adjustValue(EditText et, int delta, int min, int max) {
        int current = parseIntSafe(et.getText().toString());
        current += delta;
        if (current > max) current = min;
        if (current < min) current = max;
        et.setText(String.format(Locale.getDefault(), "%02d", current));
    }

    private static void addClampWatcher(EditText et, int min, int max) {
        et.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                int val = parseIntSafe(et.getText().toString());
                if (val < min) val = min;
                if (val > max) val = max;
                et.setText(String.format(Locale.getDefault(), "%02d", val));
            }
        });
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void applyTimeToCalendar(Calendar cal, EditText etH, EditText etM, EditText etS) {
        cal.set(Calendar.HOUR_OF_DAY, parseIntSafe(etH.getText().toString()));
        cal.set(Calendar.MINUTE, parseIntSafe(etM.getText().toString()));
        cal.set(Calendar.SECOND, parseIntSafe(etS.getText().toString()));
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static void updateDatetimeLabel(TextView tv, Calendar cal, boolean hasDate,
                                            String hour, String minute, String second) {
        if (!hasDate) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        String dateStr = DATE_FORMAT.format(cal.getTime());
        String timeStr = String.format(Locale.getDefault(), "%s:%s:%s", hour, minute, second);
        tv.setText("Thời Hạn: " + dateStr + "  " + timeStr);
    }
}
