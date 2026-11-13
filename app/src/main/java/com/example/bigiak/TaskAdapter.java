package com.example.bigiak;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onStatusChanged(Task task, boolean isCompleted);
        void onEditClick(Task task);
        void onDeleteClick(Task task);
    }

    private final Context context;
    private List<Task> taskList;
    private final OnTaskClickListener listener;

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskId.setText("#" + task.getId());
        holder.tvTitle.setText(task.getTitle());
        
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            holder.tvDesc.setText(task.getDescription());
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        holder.tvDueDate.setText(task.getDueDate() != null && !task.getDueDate().isEmpty() ? task.getDueDate() : "Không có ngày");

        // Priority styling
        String priority = task.getPriority() != null ? task.getPriority() : "Trung bình";
        holder.tvPriority.setText(priority);
        if ("Cao".equalsIgnoreCase(priority)) {
            holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
            holder.tvPriority.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_high));
        } else if ("Thấp".equalsIgnoreCase(priority)) {
            holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
            holder.tvPriority.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_low));
        } else { // Trung bình
            holder.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
            holder.tvPriority.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_badge_medium));
        }

        // Checkbox status & strikethrough logic
        holder.cbStatus.setOnCheckedChangeListener(null);
        holder.cbStatus.setChecked(task.isCompleted());
        updateStrikethrough(holder, task.isCompleted());

        holder.cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            updateStrikethrough(holder, isChecked);
            if (listener != null) {
                listener.onStatusChanged(task, isChecked);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(task);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(task);
            }
        });
    }

    private void updateStrikethrough(TaskViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbStatus;
        TextView tvTaskId, tvTitle, tvDesc, tvDueDate, tvPriority;
        ImageButton btnEdit, btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbStatus = itemView.findViewById(R.id.cb_status);
            tvTaskId = itemView.findViewById(R.id.tv_task_id);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}