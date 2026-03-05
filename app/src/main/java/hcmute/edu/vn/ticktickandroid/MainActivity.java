package hcmute.edu.vn.ticktickandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import java.util.Map;

import hcmute.edu.vn.ticktickandroid.Adapter.DrawerCategoryAdapter;
import hcmute.edu.vn.ticktickandroid.Adapter.TaskExpandableListAdapter;
import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.Category.CategoryDao;
import hcmute.edu.vn.ticktickandroid.Database.AppDatabase;
import hcmute.edu.vn.ticktickandroid.Dialog.CategoryDialogHelper;
import hcmute.edu.vn.ticktickandroid.Dialog.TaskDialogHelper;
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

    private CategoryDao categoryDao;
    private TaskDao taskDao;

    private List<Category> categories = new ArrayList<>();
    private Category currentCategory = null;

    private List<String> groupList = new ArrayList<>();
    private Map<String, List<TaskEntity>> taskMap = new LinkedHashMap<>();

    private RecyclerView rvDrawerCategories;
    private DrawerCategoryAdapter drawerAdapter;
    private TaskExpandableListAdapter taskAdapter;

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

        initDatabase();
        bindViews();
        setupToolbar();
        setupBottomNav();
        setupDrawer();

        fabAdd.setOnClickListener(v ->
                TaskDialogHelper.showAddDialog(this, taskDao, categoryDao, currentCategory, this::refreshAll));

        refreshTaskList();
        updateFabVisibility();
    }

    private void initDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);
        categoryDao = db.categoryDao();
        taskDao = db.taskDao();
    }

    private void bindViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAdd = findViewById(R.id.fab_add);
        expandableListView = findViewById(R.id.expandable_task_list);
        emptyState = findViewById(R.id.empty_state);
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
            if (itemId == R.id.nav_tasks) return true;
            else if (itemId == R.id.nav_calendar) return true;
            else if (itemId == R.id.nav_focus) return true;
            return false;
        });
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

    private void refreshAll() {
        refreshDrawerCategories();
        refreshTaskList();
    }

    private void updateFabVisibility() {
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

        emptyState.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        expandableListView.setVisibility(tasks.isEmpty() ? View.GONE : View.VISIBLE);
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
