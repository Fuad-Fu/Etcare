package com.et.etcare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.et.etcare.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WellnessPlanFragment extends Fragment {

    private WellnessDbHelper dbHelper;
    private RecyclerView rvHabits;
    private TextView tvProgress, tvStreak, tvProgressPercent;
    private View emptyState; // Changed from TextView to View to avoid ClassCastException
    private LinearProgressIndicator progressIndicator;
    private FloatingActionButton fabAddHabit;
    private HabitAdapter adapter;
    private List<Habit> habits = new ArrayList<>();
    private List<Boolean> completions = new ArrayList<>();

    private String todayDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wellness_plan, container, false);

        // Header Back Button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isAdded()) requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Initialize Views
        dbHelper = new WellnessDbHelper(requireContext());
        rvHabits = view.findViewById(R.id.rvHabits);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvStreak = view.findViewById(R.id.tvStreakNumber);
        emptyState = view.findViewById(R.id.emptyState);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        fabAddHabit = view.findViewById(R.id.fabAddHabit);

        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Setup Floating Action Button
        fabAddHabit.setOnClickListener(v -> showAddHabitDialog());

        setupRecyclerView();
        loadData();

        return view;
    }

    private void setupRecyclerView() {
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        // Disable Nested Scrolling to let NestedScrollView handle it
        rvHabits.setNestedScrollingEnabled(false);
        
        adapter = new HabitAdapter(habits, completions, new HabitAdapter.OnHabitActionListener() {
            @Override
            public void onToggle(Habit habit, boolean completed) {
                dbHelper.markHabitCompleted(habit.getId(), todayDate, completed);
                refreshUI(); 
            }

            @Override
            public void onDelete(Habit habit) {
                showDeleteConfirmDialog(habit);
            }
        });
        rvHabits.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (!isAdded() || dbHelper == null) return;
        
        habits = dbHelper.getEnabledHabits();
        completions = dbHelper.getTodayCompletions(habits, todayDate);

        adapter.setData(habits, completions);
        updateStats();
    }

    private void refreshUI() {
        if (!isAdded()) return;
        // Only fetch completions to update stats without resetting the list scroll
        completions = dbHelper.getTodayCompletions(habits, todayDate);
        updateStats();
    }

    private void updateStats() {
        if (habits == null || completions == null) return;
        
        int doneCount = 0;
        for (boolean b : completions) if (b) doneCount++;
        
        int total = habits.size();
        tvProgress.setText(doneCount + "/" + total + " Habits Completed");
        
        if (total > 0) {
            int progress = (doneCount * 100) / total;
            progressIndicator.setProgress(progress, true);
            tvProgressPercent.setText(progress + "%");
            emptyState.setVisibility(View.GONE);
            rvHabits.setVisibility(View.VISIBLE);
        } else {
            progressIndicator.setProgress(0, true);
            tvProgressPercent.setText("0%");
            emptyState.setVisibility(View.VISIBLE);
            rvHabits.setVisibility(View.GONE);
        }
        
        // Re-calculate streak if habits changed
        tvStreak.setText(String.valueOf(dbHelper.getCurrentStreak(habits)));
    }

    private void showAddHabitDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null);
        TextInputEditText input = dialogView.findViewById(R.id.etHabitName);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("New Health Goal")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        dbHelper.addCustomHabit(name, "✨");
                        loadData(); 
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmDialog(Habit habit) {
        if (getContext() == null || habit == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove Goal")
                .setMessage("Are you sure you want to stop tracking '" + habit.getName() + "'?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    dbHelper.deleteHabit(habit.getId());
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}