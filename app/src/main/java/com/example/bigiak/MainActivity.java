package com.example.bigiak;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private TextView tvStatTotal, tvStatCompleted, tvStatPending;
    private EditText etSearch;
    private Spinner spFilterStatus, spFilterPriority;
    private RecyclerView rvTasks;
    private View layoutEmptyState;
    private FloatingActionButton fabAddTask;

    private DatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();

    private ActivityResultLauncher<Intent> addEditTaskLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupFilterSpinners();
        setupSearchInput();
        setupLauncher();

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            addEditTaskLauncher.launch(intent);
        });

        loadTasks();
    }

    private void initViews() {
        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatCompleted = findViewById(R.id.tv_stat_completed);
        tvStatPending = findViewById(R.id.tv_stat_pending);
        etSearch = findViewById(R.id.et_search);
        spFilterStatus = findViewById(R.id.sp_filter_status);
        spFilterPriority = findViewById(R.id.sp_filter_priority);
        rvTasks = findViewById(R.id.rv_tasks);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        fabAddTask = findViewById(R.id.fab_add_task);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this, taskList, this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupFilterSpinners() {
        String[] statusFilters = new String[]{
                getString(R.string.filter_status_all),
                getString(R.string.filter_status_pending),
                getString(R.string.filter_status_completed)
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusFilters);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterStatus.setAdapter(statusAdapter);

        String[] priorityFilters = new String[]{
                getString(R.string.filter_priority_all),
                getString(R.string.priority_high),
                getString(R.string.priority_medium),
                getString(R.string.priority_low)
        };
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorityFilters);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterPriority.setAdapter(priorityAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spFilterStatus.setOnItemSelectedListener(filterListener);
        spFilterPriority.setOnItemSelectedListener(filterListener);
    }

    private void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadTasks();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupLauncher() {
        addEditTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadTasks();
                    }
                }
        );
    }

    private void loadTasks() {
        String keyword = etSearch.getText().toString().trim();
        String statusFilter = spFilterStatus.getSelectedItem() != null ? spFilterStatus.getSelectedItem().toString() : "";
        String priorityFilter = spFilterPriority.getSelectedItem() != null ? spFilterPriority.getSelectedItem().toString() : "";

        taskList = dbHelper.getFilteredTasks(keyword, statusFilter, priorityFilter);
        taskAdapter.updateList(taskList);

        if (taskList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }

        updateStatistics();
    }

    private void updateStatistics() {
        int total = dbHelper.getTotalTasksCount();
        int completed = dbHelper.getCompletedTasksCount();
        int pending = dbHelper.getPendingTasksCount();

        tvStatTotal.setText(String.valueOf(total));
        tvStatCompleted.setText(String.valueOf(completed));
        tvStatPending.setText(String.valueOf(pending));
    }

    @Override
    public void onStatusChanged(Task task, boolean isCompleted) {
        dbHelper.updateTaskStatus(task.getId(), isCompleted);
        updateStatistics();

        // If status filter is currently active (not "Tất cả trạng thái"), reload to update list view
        String statusFilter = spFilterStatus.getSelectedItem() != null ? spFilterStatus.getSelectedItem().toString() : "";
        if (!statusFilter.equals(getString(R.string.filter_status_all))) {
            loadTasks();
        }
    }

    @Override
    public void onEditClick(Task task) {
        Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK, task);
        addEditTaskLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dbHelper.deleteTask(task.getId());
                    Toast.makeText(MainActivity.this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                    loadTasks();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}