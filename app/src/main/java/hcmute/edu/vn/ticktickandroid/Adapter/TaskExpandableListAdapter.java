package hcmute.edu.vn.ticktickandroid.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hcmute.edu.vn.ticktickandroid.R;
import hcmute.edu.vn.ticktickandroid.Task.TaskEntity;

public class TaskExpandableListAdapter extends BaseExpandableListAdapter {

    public interface OnTaskActionListener {
        void onTaskCheckedChanged(TaskEntity task, boolean isChecked);
        void onTaskLongClick(TaskEntity task);
    }

    private static final SimpleDateFormat DUE_FORMAT =
            new SimpleDateFormat("dd/MM  HH:mm:ss", Locale.getDefault());

    private Context context;
    private List<String> groupList;
    private Map<String, List<TaskEntity>> taskMap;
    private OnTaskActionListener listener;

    public TaskExpandableListAdapter(Context context, List<String> groupList,
                                     Map<String, List<TaskEntity>> taskMap,
                                     OnTaskActionListener listener) {
        this.context = context;
        this.groupList = groupList;
        this.taskMap = taskMap;
        this.listener = listener;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String group = groupList.get(groupPosition);
        List<TaskEntity> tasks = taskMap.get(group);
        return tasks != null ? tasks.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String group = groupList.get(groupPosition);
        return taskMap.get(group).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task_group, parent, false);
        }
        String group = groupList.get(groupPosition);
        int count = getChildrenCount(groupPosition);

        TextView tvGroupName = convertView.findViewById(R.id.tv_group_name);
        TextView tvGroupCount = convertView.findViewById(R.id.tv_group_count);
        TextView tvArrow = convertView.findViewById(R.id.tv_arrow);

        tvGroupName.setText(group);
        tvGroupCount.setText(String.valueOf(count));
        tvArrow.setText(isExpanded ? "▾" : "▸");

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }
        TaskEntity task = (TaskEntity) getChild(groupPosition, childPosition);

        CheckBox checkBox = convertView.findViewById(R.id.cb_task);
        TextView tvTitle = convertView.findViewById(R.id.tv_task_title);
        TextView tvDue = convertView.findViewById(R.id.tv_task_due);

        tvTitle.setText(task.getTitle());
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvTitle.setAlpha(0.5f);
        } else {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            tvTitle.setAlpha(1.0f);
        }

        if (task.getDueDate() > 0) {
            tvDue.setVisibility(View.VISIBLE);
            tvDue.setText(DUE_FORMAT.format(new Date(task.getDueDate())));
        } else {
            tvDue.setVisibility(View.GONE);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskCheckedChanged(task, isChecked);
            }
        });

        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTaskLongClick(task);
            }
            return true;
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

