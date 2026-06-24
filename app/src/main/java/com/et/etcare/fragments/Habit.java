package com.et.etcare.fragments;

public class Habit {
    private int id;
    private String name;
    private String description;
    private String emoji;
    private boolean enabled;   // whether the habit is active for the user

    public Habit(int id, String name, String description, String emoji, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.enabled = enabled;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}