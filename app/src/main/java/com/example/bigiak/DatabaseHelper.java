package com.example.bigiak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskManager.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DUE_DATE = "due_date";
    private static final String COLUMN_PRIORITY = "priority";
    private static final String COLUMN_IS_COMPLETED = "is_completed";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DUE_DATE + " TEXT, " +
                COLUMN_PRIORITY + " TEXT, " +
                COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(createTableQuery);

        // Seed sample data for testing
        ContentValues cv1 = new ContentValues();
        cv1.put(COLUMN_TITLE, "Hoàn thành bài tập Java giữa kỳ");
        cv1.put(COLUMN_DESCRIPTION, "Viết ứng dụng quản lý công việc cá nhân đầy đủ chức năng.");
        cv1.put(COLUMN_DUE_DATE, "25/10/2024");
        cv1.put(COLUMN_PRIORITY, "Cao");
        cv1.put(COLUMN_IS_COMPLETED, 0);
        db.insert(TABLE_TASKS, null, cv1);

        ContentValues cv2 = new ContentValues();
        cv2.put(COLUMN_TITLE, "Họp nhóm thiết kế giao diện");
        cv2.put(COLUMN_DESCRIPTION, "Thảo luận layout và luồng trải nghiệm người dùng.");
        cv2.put(COLUMN_DUE_DATE, "28/10/2024");
        cv2.put(COLUMN_PRIORITY, "Trung bình");
        cv2.put(COLUMN_IS_COMPLETED, 1);
        db.insert(TABLE_TASKS, null, cv2);

        ContentValues cv3 = new ContentValues();
        cv3.put(COLUMN_TITLE, "Mua sách tham khảo Android");
        cv3.put(COLUMN_DESCRIPTION, "Tìm hiểu thêm về SQLite và Room Database.");
        cv3.put(COLUMN_DUE_DATE, "30/10/2024");
        cv3.put(COLUMN_PRIORITY, "Thấp");
        cv3.put(COLUMN_IS_COMPLETED, 0);
        db.insert(TABLE_TASKS, null, cv3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public long insertTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTitle());
        values.put(COLUMN_DESCRIPTION, task.getDescription());
        values.put(COLUMN_DUE_DATE, task.getDueDate());
        values.put(COLUMN_PRIORITY, task.getPriority());
        values.put(COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    public int updateTaskStatus(int id, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPLETED, isCompleted ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Task> getFilteredTasks(String keyword, String statusFilter, String priorityFilter) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_TASKS + " WHERE 1=1");
        List<String> selectionArgs = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND ").append(COLUMN_TITLE).append(" LIKE ?");
            selectionArgs.add("%" + keyword.trim() + "%");
        }

        if (statusFilter != null) {
            if (statusFilter.equalsIgnoreCase("Chưa thực hiện")) {
                query.append(" AND ").append(COLUMN_IS_COMPLETED).append(" = 0");
            } else if (statusFilter.equalsIgnoreCase("Đã hoàn thành")) {
                query.append(" AND ").append(COLUMN_IS_COMPLETED).append(" = 1");
            }
        }

        if (priorityFilter != null && !priorityFilter.equalsIgnoreCase("Tất cả mức ưu tiên") && !priorityFilter.equalsIgnoreCase("Tất cả")) {
            query.append(" AND ").append(COLUMN_PRIORITY).append(" = ?");
            selectionArgs.add(priorityFilter);
        }

        query.append(" ORDER BY ").append(COLUMN_ID).append(" DESC");

        Cursor cursor = db.rawQuery(query.toString(), selectionArgs.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY));
                boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1;

                taskList.add(new Task(id, title, description, dueDate, priority, isCompleted));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public int getTotalTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getCompletedTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_COMPLETED + " = 1", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getPendingTasksCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COLUMN_IS_COMPLETED + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}