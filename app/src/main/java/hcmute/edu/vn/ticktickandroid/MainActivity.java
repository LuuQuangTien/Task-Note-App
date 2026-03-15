package hcmute.edu.vn.ticktickandroid;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hcmute.edu.vn.ticktickandroid.Adapter.DrawerCategoryAdapter;
import hcmute.edu.vn.ticktickandroid.Adapter.DrawerTimerPresetAdapter;
import hcmute.edu.vn.ticktickandroid.Adapter.TaskExpandableListAdapter;
import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.Category.CategoryDao;
import hcmute.edu.vn.ticktickandroid.Database.AppDatabase;
import hcmute.edu.vn.ticktickandroid.Dialog.CategoryDialogHelper;
import hcmute.edu.vn.ticktickandroid.Dialog.TaskDialogHelper;
import hcmute.edu.vn.ticktickandroid.Fragment.NotificationFragment;
import hcmute.edu.vn.ticktickandroid.Fragment.TimerFragment;
import hcmute.edu.vn.ticktickandroid.Notification.NotificationDao;
import hcmute.edu.vn.ticktickandroid.Service.TaskReminderService;
import hcmute.edu.vn.ticktickandroid.Task.TaskDao;
import hcmute.edu.vn.ticktickandroid.Task.TaskEntity;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;
    private ExpandableListView expandableListView;
    private LinearLayout emptyState;

    private LinearLayout drawerCategorySection;
    private LinearLayout drawerTimerSection;

    private CategoryDao categoryDao;
    private TaskDao taskDao;

    private List<Category> categories = new ArrayList<>();
    private Category currentCategory = null;

    private List<String> groupList = new ArrayList<>();
    private Map<String, List<TaskEntity>> taskMap = new LinkedHashMap<>();

    private RecyclerView rvDrawerCategories;
    private RecyclerView rvDrawerTimerPresets;
    private DrawerCategoryAdapter drawerAdapter;
    private DrawerTimerPresetAdapter timerPresetAdapter;
    private TaskExpandableListAdapter taskAdapter;

    private TimerFragment timerFragment;
    private NotificationFragment notificationFragment;
    private boolean isNotificationPageVisible = false;

    private ImageView ivNotificationIcon;
    private TextView tvNotificationBadge;
    private NotificationDao notificationDao;

    private static final int[] DEFAULT_PRESETS_SECONDS = {5*60, 10*60, 15*60, 25*60, 30*60, 45*60, 60*60};
    private static final int DEFAULT_SELECTED_INDEX = 4;
    private static final String PREFS_NAME = "timer_presets";
    private static final String PREFS_KEY = "presets";
    private List<Integer> timerPresetList = new ArrayList<>();

    private TaskReminderService taskReminderService;
    private boolean isBound = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền thông báo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ứng dụng cần quyền thông báo để nhắc nhở deadline", Toast.LENGTH_LONG).show();
                }
            });

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TaskReminderService.LocalBinder binder = (TaskReminderService.LocalBinder) service;
            taskReminderService = binder.getService();
            isBound = true;
            taskReminderService.setNotificationListener(() -> updateNotificationBadge());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        checkNotificationPermission();
        initDatabase();
        bindViews();
        setupToolbar();
        setupBottomNav();
        setupDrawer();
        setupTimerPresets();
        setupNotificationIcon();

        fabAdd.setOnClickListener(v ->
                TaskDialogHelper.showAddDialog(this, taskDao, categoryDao, currentCategory, this::refreshAll));

        refreshTaskList();
        updateFabVisibility();
        updateNotificationBadge();

        Intent intent = new Intent(this, TaskReminderService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void initDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);
        categoryDao = db.categoryDao();
        taskDao = db.taskDao();
        notificationDao = db.notificationDao();
    }

    private void bindViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAdd = findViewById(R.id.fab_add);
        expandableListView = findViewById(R.id.expandable_task_list);
        emptyState = findViewById(R.id.empty_state);

        drawerCategorySection = findViewById(R.id.drawer_category_section);
        drawerTimerSection = findViewById(R.id.drawer_timer_section);

        ivNotificationIcon = findViewById(R.id.iv_notification_icon);
        tvNotificationBadge = findViewById(R.id.tv_notification_badge);
    }

    private void setupNotificationIcon() {
        FrameLayout btnNotification = findViewById(R.id.btn_notification);
        btnNotification.setOnClickListener(v -> toggleNotificationPage());
    }

    private void toggleNotificationPage() {
        if (isNotificationPageVisible) {
            hideNotificationPage();
        } else {
            showNotificationPage();
        }
    }

    private void showNotificationPage() {
        isNotificationPageVisible = true;

        emptyState.setVisibility(View.GONE);
        expandableListView.setVisibility(View.GONE);
        fabAdd.hide();
        if (timerFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(timerFragment).commit();
        }

        if (notificationFragment == null) {
            notificationFragment = new NotificationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, notificationFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(notificationFragment)
                    .commit();
        }

        toolbarTitle.setText("Thông báo");

        notificationDao.markAllAsRead();
        updateNotificationBadge();
    }

    private void hideNotificationPage() {
        isNotificationPageVisible = false;
        if (notificationFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(notificationFragment).commit();
        }

        int selectedId = bottomNavigationView.getSelectedItemId();
        if (selectedId == R.id.nav_tasks) {
            showTasksUi();
        } else if (selectedId == R.id.nav_timer) {
            showTimerUi();
        }
    }

    private void updateNotificationBadge() {
        int unreadCount = notificationDao.getUnreadCount();
        if (unreadCount > 0) {
            ivNotificationIcon.setImageResource(R.drawable.ic_notification_on);
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            ivNotificationIcon.setImageResource(R.drawable.ic_notification_off);
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                showTasksUi();
                return true;
            } else if (itemId == R.id.nav_calendar) {
                return true;
            } else if (itemId == R.id.nav_timer) {
                showTimerUi();
                return true;
            }
            return false;
        });
    }

    private void showTasksUi() {
        isNotificationPageVisible = false;
        boolean isEmpty = taskMap.isEmpty();
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        expandableListView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        updateFabVisibility();

        drawerCategorySection.setVisibility(View.VISIBLE);
        drawerTimerSection.setVisibility(View.GONE);
        toolbarTitle.setText(currentCategory != null ? currentCategory.getName() : getString(R.string.welcome));

        if (timerFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(timerFragment).commit();
        }
        if (notificationFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(notificationFragment).commit();
        }
    }

    private void showTimerUi() {
        isNotificationPageVisible = false;
        emptyState.setVisibility(View.GONE);
        expandableListView.setVisibility(View.GONE);
        fabAdd.hide();

        drawerCategorySection.setVisibility(View.GONE);
        drawerTimerSection.setVisibility(View.VISIBLE);
        toolbarTitle.setText(R.string.focus_timer);

        if (notificationFragment != null) {
            getSupportFragmentManager().beginTransaction().hide(notificationFragment).commit();
        }

        if (timerFragment == null) {
            timerFragment = new TimerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, timerFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .show(timerFragment)
                    .commit();
        }
    }

    private void setupDrawer() {
        rvDrawerCategories = findViewById(R.id.rv_drawer_categories);
        rvDrawerCategories.setLayoutManager(new LinearLayoutManager(this));

        drawerAdapter = new DrawerCategoryAdapter(new ArrayList<>(), new LinkedHashMap<>(),
                new DrawerCategoryAdapter.OnCategoryActionListener() {
                    @Override
                    public void onClick(Category category) {
                        currentCategory = category;
                        toolbarTitle.setText(category.getName());
                        drawerLayout.closeDrawer(GravityCompat.START);
                        refreshTaskList();
                        updateFabVisibility();
                    }

                    @Override
                    public void onEdit(Category category) {
                        CategoryDialogHelper.showEditDialog(MainActivity.this, category, categoryDao, () -> {
                            if (currentCategory != null && currentCategory.getId() == category.getId()) {
                                currentCategory = category;
                                toolbarTitle.setText(category.getName());
                            }
                            refreshAll();
                        });
                    }

                    @Override
                    public void onDelete(Category category) {
                        CategoryDialogHelper.confirmDelete(MainActivity.this, category, categoryDao, () -> {
                            if (currentCategory != null && currentCategory.getId() == category.getId()) {
                                currentCategory = null;
                                toolbarTitle.setText(R.string.welcome);
                            }
                            refreshAll();
                            updateFabVisibility();
                        });
                    }
                });
        rvDrawerCategories.setAdapter(drawerAdapter);

        LinearLayout btnAddCategory = findViewById(R.id.btn_add_category);
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v ->
                    CategoryDialogHelper.showAddDialog(this, categoryDao, this::refreshAll));
        }

        refreshDrawerCategories();
    }

    private void setupTimerPresets() {
        rvDrawerTimerPresets = findViewById(R.id.rv_drawer_timer_presets);
        rvDrawerTimerPresets.setLayoutManager(new LinearLayoutManager(this));

        timerPresetList = loadTimerPresets();

        timerPresetAdapter = new DrawerTimerPresetAdapter(timerPresetList, DEFAULT_SELECTED_INDEX, totalSeconds -> {
            if (timerFragment != null) {
                timerFragment.setTimerDuration(totalSeconds * 1000L);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        timerPresetAdapter.setOnPresetLongClickListener((position, totalSeconds) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa preset")
                    .setMessage("Bạn có muốn xóa preset này?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        timerPresetAdapter.removePreset(position);
                        saveTimerPresets(timerPresetList);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        rvDrawerTimerPresets.setAdapter(timerPresetAdapter);

        LinearLayout btnAddTimer = findViewById(R.id.btn_add_timer);
        if (btnAddTimer != null) {
            btnAddTimer.setOnClickListener(v -> showAddTimerPresetDialog());
        }
    }

    private void showAddTimerPresetDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_timer_preset, null);

        EditText etHour = dialogView.findViewById(R.id.et_hour);
        EditText etMinute = dialogView.findViewById(R.id.et_minute);
        EditText etSecond = dialogView.findViewById(R.id.et_second);

        ImageButton btnHourUp = dialogView.findViewById(R.id.btn_hour_up);
        ImageButton btnHourDown = dialogView.findViewById(R.id.btn_hour_down);
        ImageButton btnMinuteUp = dialogView.findViewById(R.id.btn_minute_up);
        ImageButton btnMinuteDown = dialogView.findViewById(R.id.btn_minute_down);
        ImageButton btnSecondUp = dialogView.findViewById(R.id.btn_second_up);
        ImageButton btnSecondDown = dialogView.findViewById(R.id.btn_second_down);

        btnHourUp.setOnClickListener(v -> {
            int val = parseIntSafe(etHour.getText().toString());
            etHour.setText(String.format(Locale.getDefault(), "%02d", Math.min(val + 1, 23)));
        });
        btnHourDown.setOnClickListener(v -> {
            int val = parseIntSafe(etHour.getText().toString());
            etHour.setText(String.format(Locale.getDefault(), "%02d", Math.max(val - 1, 0)));
        });
        btnMinuteUp.setOnClickListener(v -> {
            int val = parseIntSafe(etMinute.getText().toString());
            etMinute.setText(String.format(Locale.getDefault(), "%02d", Math.min(val + 1, 59)));
        });
        btnMinuteDown.setOnClickListener(v -> {
            int val = parseIntSafe(etMinute.getText().toString());
            etMinute.setText(String.format(Locale.getDefault(), "%02d", Math.max(val - 1, 0)));
        });
        btnSecondUp.setOnClickListener(v -> {
            int val = parseIntSafe(etSecond.getText().toString());
            etSecond.setText(String.format(Locale.getDefault(), "%02d", Math.min(val + 1, 59)));
        });
        btnSecondDown.setOnClickListener(v -> {
            int val = parseIntSafe(etSecond.getText().toString());
            etSecond.setText(String.format(Locale.getDefault(), "%02d", Math.max(val - 1, 0)));
        });

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    int hours = parseIntSafe(etHour.getText().toString());
                    int minutes = parseIntSafe(etMinute.getText().toString());
                    int seconds = parseIntSafe(etSecond.getText().toString());
                    int totalSeconds = hours * 3600 + minutes * 60 + seconds;

                    if (totalSeconds <= 0) {
                        Toast.makeText(this, "Vui lòng nhập thời gian lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (timerPresetList.contains(totalSeconds)) {
                        Toast.makeText(this, "Preset này đã tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    timerPresetAdapter.addPreset(totalSeconds);
                    saveTimerPresets(timerPresetList);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<Integer> loadTimerPresets() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(PREFS_KEY, null);
        List<Integer> list = new ArrayList<>();

        if (saved == null || saved.isEmpty()) {
            for (int s : DEFAULT_PRESETS_SECONDS) {
                list.add(s);
            }
        } else {
            for (String part : saved.split(",")) {
                try {
                    list.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }

    private void saveTimerPresets(List<Integer> presets) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < presets.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(presets.get(i));
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREFS_KEY, sb.toString())
                .apply();
    }

    private void refreshAll() {
        refreshDrawerCategories();
        refreshTaskList();
    }

    private void updateFabVisibility() {
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_tasks) {
            fabAdd.hide();
            return;
        }

        if (currentCategory == null) {
            fabAdd.hide();
        } else {
            fabAdd.show();
        }
    }

    private void refreshDrawerCategories() {
        categories = categoryDao.getAll();
        Map<Integer, Integer> taskCounts = new LinkedHashMap<>();
        for (Category cat : categories) {
            taskCounts.put(cat.getId(), taskDao.getByCategoryId(cat.getId()).size());
        }
        drawerAdapter.updateData(categories, taskCounts);
    }

    private void refreshTaskList() {
        groupList.clear();
        taskMap.clear();
        categories = categoryDao.getAll();
        Map<Integer, String> catNameMap = new LinkedHashMap<>();
        for (Category cat : categories) {
            catNameMap.put(cat.getId(), cat.getName());
        }
        List<TaskEntity> tasks = (currentCategory != null)
                ? taskDao.getByCategoryId(currentCategory.getId())
                : taskDao.getAll();

        for (TaskEntity task : tasks) {
            String catName = catNameMap.getOrDefault(task.getCategoryId(), "Unknown");
            if (!taskMap.containsKey(catName)) {
                taskMap.put(catName, new ArrayList<>());
                groupList.add(catName);
            }
            taskMap.get(catName).add(task);
        }

        if (bottomNavigationView.getSelectedItemId() == R.id.nav_tasks) {
            emptyState.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
            expandableListView.setVisibility(tasks.isEmpty() ? View.GONE : View.VISIBLE);
        }
        taskAdapter = new TaskExpandableListAdapter(this, groupList, taskMap,
                new TaskExpandableListAdapter.OnTaskActionListener() {
                    @Override
                    public void onTaskCheckedChanged(TaskEntity task, boolean isChecked) {
                        task.setCompleted(isChecked);
                        taskDao.update(task);

                        taskAdapter.sortAllTasks();
                    }

                    @Override
                    public void onTaskLongClick(TaskEntity task) {
                        TaskDialogHelper.showEditDialog(MainActivity.this, task, taskDao, categoryDao, MainActivity.this::refreshAll);
                    }
                });
        expandableListView.setAdapter(taskAdapter);
        taskAdapter.sortAllTasks();

        for (int i = 0; i < groupList.size(); i++) {
            expandableListView.expandGroup(i);
        }
    }
}
