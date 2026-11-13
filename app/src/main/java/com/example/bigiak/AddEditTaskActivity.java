package com.example.bigiak;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "EXTRA_TASK";

    private TextView tvHeaderTitle;
    private EditText etTaskTitle, etTaskDesc, etDueDate;
    private ImageButton btnBack, btnPickDate;
    private Spinner spPriority;
    private CheckBox cbTaskStatus;
    private Button btnSave, btnCancel;

    private DatabaseHelper dbHelper;
    private Task existingTask = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupPrioritySpinner();
        setupDatePicker();
        checkIntentData();

        btnSave.setOnClickListener(v -> saveTask());
        btnCancel.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        etTaskTitle = findViewById(R.id.et_task_title);
        etTaskDesc = findViewById(R.id.et_task_desc);
        etDueDate = findViewById(R.id.et_due_date);
        btnBack = findViewById(R.id.btn_back);
        btnPickDate = findViewById(R.id.btn_pick_date);
        spPriority = findViewById(R.id.sp_priority);
        cbTaskStatus = findViewById(R.id.cb_task_status);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupPrioritySpinner() {
        String[] priorities = new String[]{
                getString(R.string.priority_high),
                getString(R.string.priority_medium),
                getString(R.string.priority_low)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapter);
        spPriority.setSelection(1); // Default to "Trung bình"
    }

    private void setupDatePicker() {
        View.OnClickListener dateListener = v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                        etDueDate.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        };

        etDueDate.setOnClickListener(dateListener);
        btnPickDate.setOnClickListener(dateListener);
    }

    private void checkIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TASK)) {
            existingTask = (Task) intent.getSerializableExtra(EXTRA_TASK);
            if (existingTask != null) {
                isEditMode = true;
                tvHeaderTitle.setText(R.string.title_edit_task);
                etTaskTitle.setText(existingTask.getTitle());
                etTaskDesc.setText(existingTask.getDescription());
                etDueDate.setText(existingTask.getDueDate());
                cbTaskStatus.setChecked(existingTask.isCompleted());

                // Set spinner selection
                String priority = existingTask.getPriority();
                if ("Cao".equalsIgnoreCase(priority)) {
                    spPriority.setSelection(0);
                } else if ("Thấp".equalsIgnoreCase(priority)) {
                    spPriority.setSelection(2);
                } else {
                    spPriority.setSelection(1);
                }
            }
        }
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String desc = etTaskDesc.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String priority = spPriority.getSelectedItem().toString();
        boolean isCompleted = cbTaskStatus.isChecked();

        // Validation: Tên công việc không được bỏ trống
        if (title.isEmpty()) {
            etTaskTitle.setError(getString(R.string.err_empty_title));
            etTaskTitle.requestFocus();
            return;
        }

        if (isEditMode && existingTask != null) {
            existingTask.setTitle(title);
            existingTask.setDescription(desc);
            existingTask.setDueDate(dueDate);
            existingTask.setPriority(priority);
            existingTask.setCompleted(isCompleted);

            dbHelper.updateTask(existingTask);
            Toast.makeText(this, R.string.msg_task_saved, Toast.LENGTH_SHORT).show();
        } else {
            Task newTask = new Task(title, desc, dueDate, priority, isCompleted);
            dbHelper.insertTask(newTask);
            Toast.makeText(this, R.string.msg_task_saved, Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}