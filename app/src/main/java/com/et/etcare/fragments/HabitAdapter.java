package com.et.etcare.fragments;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private List<Habit> habits;
    private List<Boolean> completions;
    private OnHabitActionListener listener;

    public interface OnHabitActionListener {
        void onToggle(Habit habit, boolean completed);
        void onDelete(Habit habit);
    }

    public HabitAdapter(List<Habit> habits, List<Boolean> completions, OnHabitActionListener listener) {
        this.habits = habits;
        this.completions = completions;
        this.listener = listener;
    }

    public void updateCompletions(List<Boolean> newCompletions) {
        this.completions = newCompletions;
        notifyDataSetChanged();
    }

    public void setData(List<Habit> habits, List<Boolean> completions) {
        this.habits = habits;
        this.completions = completions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvEmoji.setText(habit.getEmoji());
        holder.tvName.setText(habit.getName());
        holder.tvDesc.setText(habit.getDescription());

        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(completions.get(position));
        
        updateAppearance(holder, completions.get(position));

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateAppearance(holder, isChecked);
            if (listener != null) listener.onToggle(habit, isChecked);
        });
        
        holder.itemView.setOnClickListener(v -> {
            holder.cbCompleted.setChecked(!holder.cbCompleted.isChecked());
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(habit);
            return true;
        });
    }

    private void updateAppearance(ViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.tvName.setAlpha(0.4f);
            holder.tvDesc.setAlpha(0.4f);
            holder.tvEmoji.setAlpha(0.4f);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvName.setAlpha(1.0f);
            holder.tvDesc.setAlpha(1.0f);
            holder.tvEmoji.setAlpha(1.0f);
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return habits != null ? habits.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDesc;
        MaterialCheckBox cbCompleted;

        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvHabitEmoji);
            tvName = itemView.findViewById(R.id.tvHabitName);
            tvDesc = itemView.findViewById(R.id.tvHabitDesc);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
        }
    }
}