package hcmute.edu.vn.ticktickandroid.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import hcmute.edu.vn.ticktickandroid.Category.Category;
import hcmute.edu.vn.ticktickandroid.R;


public class DrawerCategoryAdapter extends RecyclerView.Adapter<DrawerCategoryAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onClick(Category category);
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private List<Category> categories;
    private Map<Integer, Integer> taskCounts;
    private OnCategoryActionListener listener;

    public DrawerCategoryAdapter(List<Category> categories, Map<Integer, Integer> taskCounts, OnCategoryActionListener listener) {
        this.categories = categories;
        this.taskCounts = taskCounts;
        this.listener = listener;
    }

    public void updateData(List<Category> categories, Map<Integer, Integer> taskCounts) {
        this.categories = categories;
        this.taskCounts = taskCounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drawer_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());
        int count = taskCounts.getOrDefault(category.getId(), 0);
        holder.tvCount.setText(count > 0 ? String.valueOf(count) : "");

        holder.itemView.setOnClickListener(v -> listener.onClick(category));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        ImageView btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvCount = itemView.findViewById(R.id.tv_category_count);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
