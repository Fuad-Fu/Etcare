package com.et.etcare.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WellnessDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wellness_v2.db"; // Incremented version via name to ensure fresh start
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_HABITS = "habits";
    private static final String TABLE_LOGS = "habit_logs";

    public WellnessDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HABITS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "description TEXT, " +
                "emoji TEXT, " +
                "enabled INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE " + TABLE_LOGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "habit_id INTEGER, " +
                "date TEXT, " +
                "completed INTEGER DEFAULT 0, " +
                "UNIQUE(habit_id, date) ON CONFLICT REPLACE, " +
                "FOREIGN KEY(habit_id) REFERENCES habits(id))");

        insertDefaultHabits(db);
    }

    private void insertDefaultHabits(SQLiteDatabase db) {
        String[][] defaults = {
                {"Drink Water", "8 glasses for hydration", "💧"},
                {"Daily Walk", "15 min light exercise", "🚶"},
                {"Stretching", "5 min morning stretch", "🧘"},
                {"Healthy Snack", "Eat a fruit serving", "🍎"}
        };
        for (String[] h : defaults) {
            ContentValues cv = new ContentValues();
            cv.put("name", h[0]);
            cv.put("description", h[1]);
            cv.put("emoji", h[2]);
            cv.put("enabled", 1);
            db.insert(TABLE_HABITS, null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        onCreate(db);
    }

    public List<Habit> getEnabledHabits() {
        List<Habit> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, null, "enabled = 1", null, null, null, "id DESC");
        while (cursor.moveToNext()) {
            list.add(new Habit(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("emoji")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) == 1
            ));
        }
        cursor.close();
        return list;
    }

    public void addCustomHabit(String name, String emoji) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", "Daily goal");
        cv.put("emoji", emoji);
        cv.put("enabled", 1);
        db.insert(TABLE_HABITS, null, cv);
    }

    public void deleteHabit(int habitId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOGS, "habit_id = ?", new String[]{String.valueOf(habitId)});
        db.delete(TABLE_HABITS, "id = ?", new String[]{String.valueOf(habitId)});
    }

    public void markHabitCompleted(int habitId, String date, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("habit_id", habitId);
        cv.put("date", date);
        cv.put("completed", completed ? 1 : 0);
        db.insertWithOnConflict(TABLE_LOGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Boolean> getTodayCompletions(List<Habit> habits, String date) {
        List<Boolean> completions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        for (Habit h : habits) {
            Cursor cursor = db.query(TABLE_LOGS, new String[]{"completed"}, 
                    "habit_id = ? AND date = ?", new String[]{String.valueOf(h.getId()), date}, null, null, null);
            boolean done = false;
            if (cursor.moveToFirst()) {
                done = cursor.getInt(0) == 1;
            }
            cursor.close();
            completions.add(done);
        }
        return completions;
    }

    public int getCurrentStreak(List<Habit> habits) {
        if (habits.isEmpty()) return 0;
        SQLiteDatabase db = getReadableDatabase();
        int streak = 0;
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        // A streak is the number of consecutive days (ending today or yesterday) 
        // where AT LEAST one habit was completed and NO habit was left incomplete 
        // (or simply count days where progress was 100%)
        while (true) {
            String date = sdf.format(cal.getTime());
            int totalHabits = habits.size();
            int completedCount = 0;
            
            for (Habit h : habits) {
                Cursor cursor = db.query(TABLE_LOGS, new String[]{"completed"}, 
                        "habit_id = ? AND date = ?", new String[]{String.valueOf(h.getId()), date}, null, null, null);
                if (cursor.moveToFirst() && cursor.getInt(0) == 1) {
                    completedCount++;
                }
                cursor.close();
            }

            if (completedCount == totalHabits && totalHabits > 0) {
                streak++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                // If today is not finished, check yesterday to continue streak
                if (streak == 0 && date.equals(sdf.format(Calendar.getInstance().getTime()))) {
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    continue;
                }
                break;
            }
        }
        return streak;
    }
}